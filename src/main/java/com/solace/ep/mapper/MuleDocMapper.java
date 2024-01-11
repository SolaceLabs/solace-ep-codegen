package com.solace.ep.mapper;

import java.util.HashMap;
import java.util.Map;

import com.solace.ep.mapper.model.MapConfig;
import com.solace.ep.mapper.model.MapFlow;
import com.solace.ep.mapper.model.MapGlobalProperty;
import com.solace.ep.mapper.model.MapSetVariable;
import com.solace.ep.mapper.model.MapSubFlowEgress;
import com.solace.ep.mapper.model.MapValidateSchemaJson;
import com.solace.ep.mapper.model.MapValidateSchemaXml;
import com.solace.ep.mapper.model.MapMuleDoc;
import com.solace.ep.mapper.model.MapSubFlowEgress.MapSolacePublish;
import com.solace.ep.mule.model.core.GlobalProperty;
import com.solace.ep.mule.model.core.MuleDoc;
import com.solace.ep.mule.model.core.MuleFlow;
import com.solace.ep.mule.model.core.MuleFlowRef;
import com.solace.ep.mule.model.core.SetVariable;
import com.solace.ep.mule.model.ee.TransformOperation;
import com.solace.ep.mule.model.ee.TransformOperation.TransformMessage;
import com.solace.ep.mule.model.json.ValidateJsonSchema;
import com.solace.ep.mule.model.solace.SolaceConfiguration;
import com.solace.ep.mule.model.solace.SolaceMessage;
import com.solace.ep.mule.model.solace.SolacePublish;
import com.solace.ep.mule.model.solace.SolaceQueueListener;
import com.solace.ep.mule.model.solace.SolaceTopicListener;
import com.solace.ep.mule.model.solace.SolaceConfiguration.EventPortalConfiguration;
import com.solace.ep.mule.model.xml_module.ValidateXmlSchema;

public class MuleDocMapper {
    
    private int directConsumerCount = 0;

    // Construct to keep track of message names published
    // because message names are used for egress flow names and cannot be duplicated
    private Map<String, Integer> countByMessageName = new HashMap<>();

    // Construct to keep track of Ingress flow designations
    // These should be unique for queues but can be duplicates for direct topic subscribers
    private Map<String, Integer> countByFlowDesignation = new HashMap<>();

    MuleDoc createMuleDoc( MapMuleDoc mapMuleDoc ) {

        MuleDoc muleDoc = new MuleDoc();

        muleDoc.setSolaceConfiguration( mapSolaceConfiguration( 
            mapMuleDoc.getMapConfig() != null ?
            mapMuleDoc.getMapConfig() : 
            MapUtils.getDefaultSolaceConfiguration()
            ) );

        for ( MapGlobalProperty mgp : mapMuleDoc.getMapGlobalProperties() ) {
            addGlobalProperty(muleDoc, mgp);
        }

        for ( MapFlow mapFromFlow : mapMuleDoc.getMapFlows() ) {
            addMuleFlow(muleDoc, mapFromFlow);
        }

        for ( MapSubFlowEgress mapFromSubFlowEgress : mapMuleDoc.getMapEgressSubFlows() ) {
            addMuleSubflowEgress(muleDoc, mapFromSubFlowEgress);
        }

        return muleDoc;
    }

    public void addMuleFlow( MuleDoc muleDoc, MapFlow mapFromFlow ) {

        MuleFlow muleFlow = new MuleFlow();
        muleFlow.generateDocId();

        if ( ! mapFromFlow.isDirectConsumer() ) {
            muleFlow.setName( MapUtils.getFlowNameFromDesignation(mapFromFlow.getFlowDesignation(), false) );

            SolaceQueueListener solaceQueueListener = new SolaceQueueListener();
            solaceQueueListener.setAddress( ( 
                mapFromFlow.getFlowQueueListener().getListenerAddress() != null ? 
                mapFromFlow.getFlowQueueListener().getListenerAddress() :
                "DEFAULT.ADDRESS"
                )
            );
            solaceQueueListener.setAckMode( mapFromFlow.getFlowQueueListener().getListenerAckMode() );
            solaceQueueListener.setDocNameAndGenerateDocId(
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
            for ( String t : mapFromFlow.getFlowTopicListener().getListenerTopics() ) {
                if ( isFirstTopic ) {
                    isFirstTopic = false;
                } else {
                    topicString.append(", ");
                }
                topicString.append(t);
            }
            solaceTopicListener.setTopics(topicString.toString());
            if ( mapFromFlow.getFlowMapValidateSchemaJson().getValidateSchemaContents() != null ) {
                solaceTopicListener.setContentType( mapFromFlow.getFlowTopicListener().getListenerContentType() );
                solaceTopicListener.setEncoding(mapFromFlow.getFlowTopicListener().getListenerEncoding());
            }
            solaceTopicListener.setDocNameAndGenerateDocId( "Direct Topic Subscriber" );
            solaceTopicListener.setConfigRef( MapUtils.DEFAULT_CONFIG_REF );
            muleFlow.setTopicListener( solaceTopicListener );
        }

        addValidateXmlSchema(muleFlow, mapFromFlow.getFlowMapValidateSchemaXml());
        addValidateJsonSchema(muleFlow, mapFromFlow.getFlowMapValidateSchemaJson());

        String bizLogicFlowDesignation = mapFromFlow.getFlowDesignation();
        if ( countByFlowDesignation.get( bizLogicFlowDesignation ) == null ) {
            countByFlowDesignation.put( bizLogicFlowDesignation, 0 );
        } else {
            int i = countByFlowDesignation.get( bizLogicFlowDesignation ) + 1;
            countByFlowDesignation.put( bizLogicFlowDesignation, i );
            bizLogicFlowDesignation += "_" + Integer.toString(i);
        }

        String bizLogicFlowName = 
            MapUtils.getBizLogicSubFlowNameFromDesignation(
                bizLogicFlowDesignation, 
                mapFromFlow.isDirectConsumer()
            );
        addFlowRef(muleFlow, bizLogicFlowName, MapUtils.FLOW_REF_DOC_NAME_TO_BIZ_LOGIC);
        muleDoc.getFlow().add(muleFlow);

        addMuleSubFlowBizLogic(muleDoc, bizLogicFlowName);
    }

    public void addMuleSubFlowBizLogic( MuleDoc muleDoc, String bizLogicFlowName ) {

        MuleFlow subFlowBizLogic = new MuleFlow();

        subFlowBizLogic.setName( bizLogicFlowName );
        subFlowBizLogic.generateDocId();
        subFlowBizLogic.setTransform( createDefaultTransformMessageOperation() );
        addFlowRef(subFlowBizLogic, bizLogicFlowName, MapUtils.FLOW_REF_DOC_NAME_TO_EGRESS);
        muleDoc.getSubFlow().add(subFlowBizLogic);
    }

    public void addMuleSubflowEgress( MuleDoc muleDoc, MapSubFlowEgress mapFromSubFlow ) {

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
        subFlowEgress.generateDocId();
        for ( MapSetVariable v : mapFromSubFlow.getSetVariables()) {
            addSetVariable(subFlowEgress, v);
        }
        addValidateXmlSchema(subFlowEgress, mapFromSubFlow.getValidateSchemaXml());
        addValidateJsonSchema(subFlowEgress, mapFromSubFlow.getValidateSchemaJson());
        addSolacePublish(subFlowEgress, mapFromSubFlow );

        muleDoc.getSubFlow().add(subFlowEgress);
    }

    public void addSolacePublish( MuleFlow subFlow, MapSubFlowEgress mapFromSubFlow ) {
        if ( mapFromSubFlow.getMapSolacePublish() == null) {
            return;
        }
        MapSolacePublish mapFromSolacePublish = mapFromSubFlow.getMapSolacePublish();
        SolacePublish solacePublish = new SolacePublish();
        solacePublish.setAddress( mapFromSolacePublish.getPublishAddress() );
//        solacePublish.setDocNameAndGenerateDocId( mapFromSolacePublish.getPublishDocName() );
        solacePublish.setDocNameAndGenerateDocId( MapUtils.getEgressSubFlowDocNameFromMessageName( mapFromSubFlow.getMessageName() ) );
//        solacePublish.setConfigRef(mapFromSolacePublish.getPublishConfigRef());
        solacePublish.setConfigRef(MapUtils.DEFAULT_CONFIG_REF);
        // if (
        //     mapFromSolacePublish.getPublishMessageType() != null && 
        //     mapFromSolacePublish.getPublishMessageType().length() > 0
        // ) {
        //     solacePublish.setMessage( new SolaceMessage( mapFromSolacePublish.getPublishMessageType() ) );
        // }

        String messageType;
        if ( mapFromSubFlow.getValidateSchemaJson().getValidateSchemaContents() == null ) {
            messageType = MapUtils.MSG_TYPE_BYTES_MESSAGE;
        } else {
            messageType = MapUtils.MSG_TYPE_TEXT_MESSAGE;
        }
        solacePublish.setMessage( new SolaceMessage( messageType ) );

        subFlow.setPublish(solacePublish);
    }

    public void addSetVariable( MuleFlow muleFlow, MapSetVariable mapFromSetVariable ) {

        if ( mapFromSetVariable == null ) {
            return;
        }
        SetVariable setVariable = 
            new SetVariable(
                mapFromSetVariable.getVariableName(),
                ( 
                    mapFromSetVariable.getVariableValue() != null ?
                    mapFromSetVariable.getVariableValue() :
                    "" 
                ),
                MapUtils.getSetVariableDocNameForTopicParameter( mapFromSetVariable.getVariableName() )
            );
        muleFlow.getSetVariable().add( setVariable );
    }

    public TransformOperation createDefaultTransformMessageOperation() {

        TransformMessage transformMessage = new TransformMessage();
        transformMessage.setSetPayload( MapUtils.getTransformMessageStub() );

        TransformOperation transformOperation = new TransformOperation();
        transformOperation.setDocNameAndGenerateDocId(MapUtils.DEFAULT_TRANSFORM_MESSAGE_DOC_NAME);
        transformOperation.setTransformMessage(transformMessage);

        return transformOperation;
    }

    public void addValidateJsonSchema( MuleFlow muleFlow, MapValidateSchemaJson mapValidateSchemaJson ) {

        if (mapValidateSchemaJson == null) {
            return;
        }

        ValidateJsonSchema validateJsonSchema = new ValidateJsonSchema();
        validateJsonSchema.setDocNameAndGenerateDocId("Validate JSON schema");
        if ( mapValidateSchemaJson.getValidateSchemaContents() != null && mapValidateSchemaJson.getValidateSchemaContents().length() > 0 ) {
            validateJsonSchema.setSchemaContents( mapValidateSchemaJson.getValidateSchemaContents() );
        }
        muleFlow.setValidateJsonSchema(validateJsonSchema);
    }

    public void addValidateXmlSchema( MuleFlow muleFlow, MapValidateSchemaXml mapValidateSchemaXml ) {

        if (mapValidateSchemaXml == null) {
            return;
        }

        ValidateXmlSchema validateXmlSchema = new ValidateXmlSchema();
        validateXmlSchema.setDocNameAndGenerateDocId("Validate XML schema");
        if ( mapValidateSchemaXml.getValidateSchemaContents() != null && mapValidateSchemaXml.getValidateSchemaContents().length() > 0 ) {
            validateXmlSchema.setSchemaContents( mapValidateSchemaXml.getValidateSchemaContents() );
        }
        muleFlow.setValidateXmlSchema(validateXmlSchema);
    }

    public void addFlowRef( MuleFlow muleFlow, String refName, String refDocName ) {
        muleFlow.setFlowRef( new MuleFlowRef( refName, refDocName ) );
    }

    public void addGlobalProperty( MuleDoc muleDoc, MapGlobalProperty mapFromGlobalProperty ) {
        if ( mapFromGlobalProperty == null ) {
            return;
        }
        muleDoc.getGlobalProperty().add( 
            new GlobalProperty(
                mapFromGlobalProperty.getGlobalName(), 
                mapFromGlobalProperty.getGlobalValue(), 
                MapUtils.GLOBAL_PROPERTY_DOC_NAME
            )
        );
    }

    public SolaceConfiguration mapSolaceConfiguration( MapConfig mapFromConfig ) {

        if (mapFromConfig == null) {
            mapFromConfig = MapUtils.getDefaultSolaceConfiguration();
        }

        SolaceConfiguration solaceConfiguration = 
            new SolaceConfiguration(
                mapFromConfig.getConfigName(),
                MapUtils.DEFAULT_SOLACE_CONFIG_DOC_NAME
            );
        solaceConfiguration.getSolaceConnection().setBrokerHost(mapFromConfig.getConnectBrokerHost());
        solaceConfiguration.getSolaceConnection().setMsgVpn(mapFromConfig.getConnectMsgVpn());
        solaceConfiguration.getSolaceConnection().setClientUserName(mapFromConfig.getConnectClientUserName());
        solaceConfiguration.getSolaceConnection().setPassword(mapFromConfig.getConnectPassword());
        solaceConfiguration.getEventPortalConfiguration().setCloudApiToken(mapFromConfig.getEpCloudApiToken());
        return solaceConfiguration;
    }
}
