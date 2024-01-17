package com.solace.ep.mapper;

import java.util.HashMap;
import java.util.Map;

import com.solace.ep.mapper.model.MapConfig;
import com.solace.ep.mapper.model.MapFlow;
import com.solace.ep.mapper.model.MapSubFlowEgress;
import com.solace.ep.mapper.model.MapMuleDoc;
import com.solace.ep.mule.model.core.GlobalProperty;
import com.solace.ep.mule.model.core.MuleDoc;
import com.solace.ep.mule.model.core.MuleFlow;
import com.solace.ep.mule.model.core.MuleFlowRef;
import com.solace.ep.mule.model.core.SetVariable;
import com.solace.ep.mule.model.ee.TransformOperation;
import com.solace.ep.mule.model.json.ValidateJsonSchema;
import com.solace.ep.mule.model.solace.SolaceConfiguration;
import com.solace.ep.mule.model.solace.SolaceConnection;
import com.solace.ep.mule.model.solace.SolaceMessage;
import com.solace.ep.mule.model.solace.SolacePublish;
import com.solace.ep.mule.model.solace.SolaceQueueListener;
import com.solace.ep.mule.model.solace.SolaceTopicListener;
import com.solace.ep.mule.model.solace.SolaceConfiguration.EventPortalConfiguration;
import com.solace.ep.mule.model.xml_module.ValidateXmlSchema;

import lombok.extern.slf4j.Slf4j;

/** 
 * 'MuleDocMapper' class 
 */
@Slf4j
public class MuleDocMapper {
    
    /**
     * The MuleDoc model
     */
    private MuleDoc muleDoc;

    // Keep track of direct consumer count for unique naming
    private int directConsumerCount = 0;

    // Construct to keep track of message names published
    // because message names are used for egress flow names and cannot be duplicated
    private Map<String, Integer> countByMessageName = new HashMap<>();

    // Construct to keep track of Ingress flow designations
    // These should be unique for queues but can be duplicates for direct topic subscribers
    private Map<String, Integer> countByFlowDesignation = new HashMap<>();

    /**
     * Default Constructor
     */
    public MuleDocMapper() {
        initialize();
    }

    private void initialize() {
        muleDoc = new MuleDoc();
        countByMessageName.clear();
        countByFlowDesignation.clear();
        directConsumerCount = 0;
    }

    /**
     * Create MuleDoc model from 'MapMuleDoc' intermediate format
     * Call this method to generate the MuleDoc output in full
     * @param mapMuleDoc
     * @return
     */
    public MuleDoc createMuleDoc( MapMuleDoc mapMuleDoc ) {

        // In case of instance re-use
        initialize();
        log.info("BEGIN Mapping from MapMuleDoc --> Mule Flow");

        // Create solace:config block; null config is handled
        muleDoc.setSolaceConfiguration( createSolaceConfiguration( mapMuleDoc.getMapConfig() ) );

        // Add global-property(ies)
        // for ( MapGlobalProperty mgp : mapMuleDoc.getMapGlobalProperties() ) {
        //     addGlobalProperty( mgp );
        // }
        addGlobalProperties( mapMuleDoc.getGlobalProperties() );

        // Add Mule Flow to Doc, one per MapMuleDoc instance
        // Each flow equates to an ingress: queue or direct subscription
        // Will also create one BizLogic sub-flow per ingress flow
        for ( MapFlow mapFromFlow : mapMuleDoc.getMapFlows() ) {
            addMuleFlow( mapFromFlow );
        }

        // Create one Egress sub-flow per MapEgressSubFlow instance
        for ( MapSubFlowEgress mapFromSubFlowEgress : mapMuleDoc.getMapEgressSubFlows() ) {
            addMuleSubflowEgress( mapFromSubFlowEgress) ;
        }

        log.info("DONE Mapping from MapMuleDoc --> Mule Flow");
        return muleDoc;
    }

    /**
     * Add MuleFlow for MapFromFlow instance -- 
     * Including solace:queue-listener or solace:topic-listener, 
     * + json:validate-schema or xml:validate-schema
     * + flow-ref to BizLogic sub-flow
     * Add BizLogic sub-flow per MuleFlow
     * @param mapFromFlow
     */
    public void addMuleFlow( MapFlow mapFromFlow ) {

        MuleFlow muleFlow = new MuleFlow();

        if ( ! mapFromFlow.isDirectConsumer() ) {
            muleFlow.setName( MapUtils.getFlowNameFromDesignation(mapFromFlow.getFlowDesignation(), false) );

            SolaceQueueListener solaceQueueListener = new SolaceQueueListener();
            solaceQueueListener.setAddress( ( 
                mapFromFlow.getQueueListenerAddress() != null ? 
                mapFromFlow.getQueueListenerAddress() :
                "DEFAULT.ADDRESS"
                )
            );
            solaceQueueListener.setAckMode( mapFromFlow.getQueueListenerAckMode() );
            solaceQueueListener.setDocName(
                MapUtils.getFlowDocNameFromDesignation( mapFromFlow.getFlowDesignation() )
            );
            solaceQueueListener.setConfigRef( MapUtils.DEFAULT_CONFIG_REF );
            muleFlow.setQueueListener(solaceQueueListener);
        }

        if ( mapFromFlow.isDirectConsumer() ) {

            muleFlow.setName( 
                MapUtils.getFlowNameFromDesignation(mapFromFlow.getFlowDesignation(), true) + 
                ( directConsumerCount > 0 ? Integer.toString( directConsumerCount ) : "" )
                );
            directConsumerCount++;

            SolaceTopicListener solaceTopicListener = new SolaceTopicListener();
            StringBuilder topicString = new StringBuilder();
            boolean isFirstTopic = true;
            for ( String t : mapFromFlow.getDirectListenerTopics() ) {
                if ( isFirstTopic ) {
                    isFirstTopic = false;
                } else {
                    topicString.append(", ");
                }
                topicString.append(t);
            }
            solaceTopicListener.setTopics(topicString.toString());
            if ( mapFromFlow.getJsonSchemaContent() != null ) {
                solaceTopicListener.setContentType( mapFromFlow.getDirectListenerContentType() );
                solaceTopicListener.setEncoding(mapFromFlow.getDirectListenerEncoding());
            }
            solaceTopicListener.setDocName( "Direct Topic Subscriber" );
            solaceTopicListener.setConfigRef( MapUtils.DEFAULT_CONFIG_REF );
            muleFlow.setTopicListener( solaceTopicListener );
        }

        // Add validate blocks
        addValidateXmlSchema(muleFlow, mapFromFlow.getXmlSchemaContent());
        addValidateJsonSchema(muleFlow, mapFromFlow.getJsonSchemaContent());

        // Use the assigned designation (queue name or static topic subscriber) and append
        // integer to ensure uniqueness
        String bizLogicFlowDesignation = mapFromFlow.getFlowDesignation();
        if ( countByFlowDesignation.get( bizLogicFlowDesignation ) == null ) {
            countByFlowDesignation.put( bizLogicFlowDesignation, 0 );
        } else {
            int i = countByFlowDesignation.get( bizLogicFlowDesignation ) + 1;
            countByFlowDesignation.put( bizLogicFlowDesignation, i );
            bizLogicFlowDesignation += "_" + Integer.toString(i);
        }

        // Derive name of corresponding BizLogic sub-flow to go with the Ingress flow
        String bizLogicFlowName = 
            MapUtils.getBizLogicSubFlowNameFromDesignation(
                bizLogicFlowDesignation, 
                mapFromFlow.isDirectConsumer()
            );
        // Create Flow-ref from Ingress flow to BizLogic stub flow
        addFlowRef(muleFlow, bizLogicFlowName, MapUtils.FLOW_REF_DOC_NAME_TO_BIZ_LOGIC);

        // Add the completed ingress flow to the MuleDoc
        muleDoc.getFlow().add( muleFlow );

        // Add a corresponding BizLogic sub-flow to the MuleDoc
        addMuleSubFlowBizLogic( bizLogicFlowName );

        log.info("Added ingress flow '{}' to Mule Doc", muleFlow.getName());
    }

    public void addMuleSubFlowBizLogic( String bizLogicFlowName ) {

        MuleFlow subFlowBizLogic = new MuleFlow();

        subFlowBizLogic.setName( bizLogicFlowName );
        subFlowBizLogic.setTransform( createDefaultTransformMessageOperation() );
        addFlowRef(subFlowBizLogic, bizLogicFlowName, MapUtils.FLOW_REF_DOC_NAME_TO_EGRESS);
        muleDoc.getSubFlow().add(subFlowBizLogic);

        log.info("Added BizLogic sub-flow '{}' to Mule Doc", subFlowBizLogic.getName());
    }

    public void addMuleSubflowEgress( MapSubFlowEgress mapFromSubFlow ) {

        MuleFlow subFlowEgress = new MuleFlow();

        String messageName = mapFromSubFlow.getMessageName();
        if ( countByMessageName.get( messageName ) == null ) {
            countByMessageName.put( messageName, 0 );
        } else {
            int i = countByMessageName.get( messageName ) + 1;
            countByMessageName.put( messageName, i );
            messageName += "_" + Integer.toString(i);
        }

        subFlowEgress.setName( MapUtils.getEgressSubFlowNameFromMessageName( messageName ) );
        addSetVariables(subFlowEgress, mapFromSubFlow.getSetVariables());

        addValidateXmlSchema(subFlowEgress, mapFromSubFlow.getXmlSchemaContent());
        addValidateJsonSchema(subFlowEgress, mapFromSubFlow.getJsonSchemaContent());
        addSolacePublish(subFlowEgress, mapFromSubFlow );

        muleDoc.getSubFlow().add(subFlowEgress);
        log.info("Added egress sub-flow '{}' to MuleDoc", subFlowEgress.getName());
    }

    public void addSolacePublish( MuleFlow subFlow, MapSubFlowEgress mapFromSubFlow ) {
        if ( mapFromSubFlow.getPublishAddress() == null || mapFromSubFlow.getPublishAddress().length() == 0 ) {
            log.warn("No publish addresses found for egress sub-flow '{}'", subFlow.getName());
            return;
        }

        SolacePublish solacePublish = new SolacePublish();
        solacePublish.setAddress( mapFromSubFlow.getPublishAddress() );
        solacePublish.setDocName( MapUtils.getEgressSubFlowDocNameFromMessageName( mapFromSubFlow.getMessageName() ) );
        solacePublish.setConfigRef(MapUtils.DEFAULT_CONFIG_REF);

        String messageType;
        if ( mapFromSubFlow.getJsonSchemaContent() == null ) {
            messageType = MapUtils.MSG_TYPE_BYTES_MESSAGE;
        } else {
            messageType = MapUtils.MSG_TYPE_TEXT_MESSAGE;
        }
        solacePublish.setMessage( new SolaceMessage( messageType ) );

        subFlow.setPublish(solacePublish);
        log.debug("Added solace:publish to egress sub-flow '{}'", subFlow.getName());
    }

    /**
     * Add set-variable element to MuleFlow from MapSetVariable instance
     * or do nothing if null
     * @param muleFlow
     * @param mapFromSetVariable
     */
    public static void addSetVariables( MuleFlow muleFlow, Map<String, String> mapFromSetVariables ) {
        if ( mapFromSetVariables == null ) {
            log.debug("No Set Variable entries found");
            return;
        }
        int setVariableCount = 0;
        for (Map.Entry<String, String> v : mapFromSetVariables.entrySet() ) {
            muleFlow.getSetVariable().add(
                new SetVariable(
                    v.getKey(),
                    v.getValue(),
                    MapUtils.getSetVariableDocNameForTopicParameter( v.getKey() )
                )
            );
            setVariableCount++;
        }
        log.info("Mapped {} Set Variable entries to Mule Flow '{}'", setVariableCount, muleFlow.getName());
    }

    /**
     * Create ee:transform (Transform Operation) block and it's sub-elements
     * @return
     */
    public static TransformOperation createDefaultTransformMessageOperation() {
        return new TransformOperation(
            MapUtils.DEFAULT_TRANSFORM_MESSAGE_DOC_NAME, 
            MapUtils.TRANSFORM_MESSAGE_BIZLOGIC_STUB
        );
    }

    /**
     * Add json:validate-schema element to MuleFlow from MapValidateSchemaJson instance
     * or do nothing if jsonSchemaContents is null
     * @param muleFlow
     * @param jsonSchemaContents
     */
    public static void addValidateJsonSchema( MuleFlow muleFlow, String jsonSchemaContents ) {
        if (jsonSchemaContents == null) {
            log.debug("jsonSchemaContents is empty for Mule Flow '{}' -- skipping", muleFlow.getName());
            return;
        }

        ValidateJsonSchema validateJsonSchema = new ValidateJsonSchema();
        validateJsonSchema.setDocName("Validate JSON schema");
        if ( jsonSchemaContents.length() > 0 ) {
            validateJsonSchema.setSchemaContents( jsonSchemaContents );
        }
        muleFlow.setValidateJsonSchema(validateJsonSchema);
        log.debug("Added xml:validate-schema for Mule Flow '{}'", muleFlow.getName());
    }

    /**
     * Add xml:validate-schema element to MuleFlow from MapValidateSchemaXml instance
     * or do nothing if xmlSchemaContents is null
     * @param muleFlow
     * @param xmlSchemaContents
     */
    public static void addValidateXmlSchema( MuleFlow muleFlow, String xmlSchemaContents ) {
        if (xmlSchemaContents == null) {
            log.debug("xmlSchemaContents is empty for Mule Flow '{}' -- skipping", muleFlow.getName());
            return;
        }

        ValidateXmlSchema validateXmlSchema = new ValidateXmlSchema();
        validateXmlSchema.setDocName("Validate XML schema");
        if ( xmlSchemaContents.length() > 0 ) {
            validateXmlSchema.setSchemaContents( xmlSchemaContents );
        }
        muleFlow.setValidateXmlSchema(validateXmlSchema);
        log.debug("Added xml:validate-schema for Mule Flow '{}'", muleFlow.getName());
    }

    /**
     * Add flow-ref to a Mule Flow
     * @param muleFlow
     * @param refName
     * @param refDocName
     */
    public static void addFlowRef( MuleFlow muleFlow, String refName, String refDocName ) {
        muleFlow.setFlowRef( new MuleFlowRef( refName, refDocName ) );
        log.debug("Added MuleFlowRef: {} to MuleFlow '{}'", refName, muleFlow.getName());
    }

    /**
     * Add global-property(ies) to this Mule Doc
     * or do nothing if input Map is null
     * @param mapFromGlobalProperty
     */
    public void addGlobalProperties( Map<String, String> mapFromGlobalProperties ) {
        if ( mapFromGlobalProperties == null ) {
            log.debug("No Global Properties found");
            return;
        }
        int globalPropertyCount = 0;
        for ( Map.Entry<String, String> gp : mapFromGlobalProperties.entrySet() ) {
            muleDoc.getGlobalProperty().add( 
                new GlobalProperty(
                    gp.getKey(),
                    gp.getValue(), 
                    MapUtils.GLOBAL_PROPERTY_DOC_NAME
                )
            );
            globalPropertyCount++;
        }
        log.info("Mapped {} Global Properties to MuleDoc", globalPropertyCount);
    }

    /**
     * Create solace:config for Mule Doc
     * @param mapFromConfig
     * @return
     */
    public static SolaceConfiguration createSolaceConfiguration( MapConfig mapFromConfig ) {
        if (mapFromConfig == null) {
            mapFromConfig = MapUtils.getDefaultSolaceConfiguration();
            log.info("Solace Configuration not found in input - using Default");
        }

        SolaceConfiguration solaceConfiguration = 
            SolaceConfiguration.builder().
                solaceConnection(
                    SolaceConnection.builder().
                        brokerHost( mapFromConfig.getConnectBrokerHost() ).
                        msgVpn(mapFromConfig.getConnectMsgVpn()).
                        clientUserName(mapFromConfig.getConnectClientUserName()).
                        password(mapFromConfig.getConnectPassword()).
                        build()
                ).
                eventPortalConfiguration(
                    EventPortalConfiguration.builder().
                        cloudApiToken( 
                            mapFromConfig.getEpCloudApiToken() != null && mapFromConfig.getEpCloudApiToken().length() > 0 ?
                            mapFromConfig.getEpCloudApiToken() :
                            null
                        ).build()
                )
                .build();
        solaceConfiguration.setName( MapUtils.DEFAULT_SOLACE_CONFIG_NAME );
        solaceConfiguration.setDocName( MapUtils.DEFAULT_SOLACE_CONFIG_DOC_NAME );

        log.debug("Mapped MapConfig --> SolaceConfiguration");
        return solaceConfiguration;
    }

    /**
     * Create default solace:config for Mule Doc
     * @return
     */
    public static SolaceConfiguration createDefaultSolaceConfiguration() {
        return createSolaceConfiguration(null);
    }

}
