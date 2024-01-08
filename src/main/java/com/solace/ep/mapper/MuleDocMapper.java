package com.solace.ep.mapper;

import com.solace.ep.mapper.model.MapConfig;
import com.solace.ep.mapper.model.MapFlow;
import com.solace.ep.mapper.model.MapFlowRef;
import com.solace.ep.mapper.model.MapGlobalProperty;
import com.solace.ep.mapper.model.MapSubFlowBizLogic;
import com.solace.ep.mapper.model.MapSubFlowEgress;
import com.solace.ep.mapper.model.MapUtils;
import com.solace.ep.mapper.model.MapValidateSchemaJson;
import com.solace.ep.mapper.model.MapValidateSchemaXml;
import com.solace.ep.mapper.model.PseudoMuleDoc;
import com.solace.ep.mule.model.core.GlobalProperty;
import com.solace.ep.mule.model.core.MuleDoc;
import com.solace.ep.mule.model.core.MuleFlow;
import com.solace.ep.mule.model.core.MuleFlowRef;
import com.solace.ep.mule.model.ee.TransformOperation;
import com.solace.ep.mule.model.ee.TransformOperation.TransformMessage;
import com.solace.ep.mule.model.json.ValidateJsonSchema;
import com.solace.ep.mule.model.solace.SolaceConfiguration;
import com.solace.ep.mule.model.solace.SolaceQueueListener;
import com.solace.ep.mule.model.xml_module.ValidateXmlSchema;

public class MuleDocMapper {
    
    MuleDoc createMuleDoc( PseudoMuleDoc pseudoMuleDoc ) {

        MuleDoc muleDoc = new MuleDoc();

        String configRef;

        muleDoc.setSolaceConfiguration( mapSolaceConfiguration( pseudoMuleDoc.getMapConfig() ) );
        configRef = muleDoc.getSolaceConfiguration().getName();

        for ( MapGlobalProperty mgp : pseudoMuleDoc.getMapGlobalProperties() ) {
            addGlobalProperty(muleDoc, mgp);
        }

        for ( MapFlow mapFromFlow : pseudoMuleDoc.getMapFlows() ) {
            addMuleFlow(muleDoc, mapFromFlow, configRef);
        }

        for ( MapSubFlowBizLogic mapFromSubFlowBizLogic : pseudoMuleDoc.getMapBizLogicSubFlows() ) {

        }

        for ( MapSubFlowEgress mapFromSubFlowEgress : pseudoMuleDoc.getMapEgressSubFlows() ) {

        }

        return muleDoc;
    }

    public void addMuleFlow( MuleDoc muleDoc, MapFlow mapFromFlow, String configRef ) {

        MuleFlow muleFlow = new MuleFlow();
        muleFlow.setName( mapFromFlow.getFlowName() );
        muleFlow.generateDocId();

        if ( mapFromFlow.getFlowListener() != null ) {
            SolaceQueueListener solaceQueueListener = new SolaceQueueListener();
            solaceQueueListener.setAddress( ( 
                mapFromFlow.getFlowListener().getListenerAddress() != null ? 
                mapFromFlow.getFlowListener().getListenerAddress() :
                "DEFAULT.ADDRESS"
                )
            );
            solaceQueueListener.setAckMode( mapFromFlow.getFlowListener().getListenerAckMode() );
            solaceQueueListener.setDocNameAndGenerateDocId(
                mapFromFlow.getFlowListener().getListenerDocName()
            );
            solaceQueueListener.setConfigRef( configRef );
        }

        addValidateJsonSchema(muleFlow, mapFromFlow.getFlowMapValidateSchemaJson());

        addValidateXmlSchema(muleFlow, mapFromFlow.getFlowMapValidateSchemaXml());

        addFlowRef(muleFlow, mapFromFlow.getFlowRef() );
    }

    public void addMuleSubFlowBizLogic( MuleDoc muleDoc, MapSubFlowBizLogic mapFromSubFlow ) {

        MuleFlow subFlowBizLogic = new MuleFlow();

        subFlowBizLogic.setName( mapFromSubFlow.getBizLogicFlowName() );
        subFlowBizLogic.generateDocId();
        
        subFlowBizLogic.setTransform( createDefaultTransformMessageOperation() );

        addFlowRef(subFlowBizLogic, mapFromSubFlow.getBizLogicFlowRef());
    }

    public void addMuleSubflowEgress( MuleDoc muleDoc, MapSubFlowEgress mapFromSubFlow ) {

        MuleFlow subFlowEgress = new MuleFlow();

        subFlowEgress.setName( mapFromSubFlow.getEgressFlowName() );
        subFlowEgress.generateDocId();

        
    }

    public TransformOperation createDefaultTransformMessageOperation() {

        TransformMessage transformMessage = new TransformMessage();
        transformMessage.setSetPayload( MapUtils.getTransformMessageStub() );

        TransformOperation transformOperation = new TransformOperation();
        transformOperation.setDocNameAndGenerateDocId(MapUtils.getTransformMessageDefaultDocName());
        transformOperation.setTransformMessage(transformMessage);

        return transformOperation;
    }

    public void addValidateJsonSchema( MuleFlow muleFlow, MapValidateSchemaJson mapValidateSchemaJson ) {

        if (mapValidateSchemaJson == null) {
            return;
        }

        ValidateJsonSchema validateJsonSchema = new ValidateJsonSchema();
        String docName = (
            mapValidateSchemaJson.getValidateSchemaDocName() != null && mapValidateSchemaJson.getValidateSchemaDocName().length() != 0 ?
            mapValidateSchemaJson.getValidateSchemaDocName() :
            "Validate JSON schema"
        );
        validateJsonSchema.setDocNameAndGenerateDocId(docName);
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
        String docName = (
            mapValidateSchemaXml.getValidateSchemaDocName() != null && mapValidateSchemaXml.getValidateSchemaDocName().length() != 0 ?
            mapValidateSchemaXml.getValidateSchemaDocName() :
            "Validate XML schema"
        );
        validateXmlSchema.setDocNameAndGenerateDocId(docName);
        if ( mapValidateSchemaXml.getValidateSchemaContents() != null && mapValidateSchemaXml.getValidateSchemaContents().length() > 0 ) {
            validateXmlSchema.setSchemaContents( mapValidateSchemaXml.getValidateSchemaContents() );
        }
        muleFlow.setValidateXmlSchema(validateXmlSchema);
    }

    public void addFlowRef( MuleFlow muleFlow, MapFlowRef mapFromFlowRef ) {
        if ( mapFromFlowRef == null ) {
            return;
        }
        MuleFlowRef muleFlowRef = new MuleFlowRef( mapFromFlowRef.getRefName() );
        muleFlowRef.setDocNameAndGenerateDocId( mapFromFlowRef.getRefDocName() );
        muleFlow.setFlowRef( muleFlowRef );
    }

    public void addGlobalProperty( MuleDoc muleDoc, MapGlobalProperty mapFromGlobalProperty ) {
        GlobalProperty globalProperty = new GlobalProperty();
        globalProperty.setName(mapFromGlobalProperty.getGlobalName());
        globalProperty.setValue(mapFromGlobalProperty.getGlobalValue());
        globalProperty.setDocNameAndGenerateDocId(mapFromGlobalProperty.getGlobalDocName());
        muleDoc.getGlobalProperty().add( globalProperty );
    }

    public MapConfig defaultSolaceConfiguration() {
        
        MapConfig mapConfig = new MapConfig();

        mapConfig.setConfigName( MapUtils.getDefaultSolaceConfigurationName() );
        mapConfig.setConfigDocName( MapUtils.getDefaultSolaceConfigurationDocName() );
        mapConfig.setEpCloudApiToken( "eySetYourCloudApiTokenHere" );
        mapConfig.setConnectBrokerHost("http://mr-connection-service.messaging.solace.cloud:55443");
        mapConfig.setConnectMsgVpn("defaultVpn");
        mapConfig.setConnectClientUserName("defaultClientUser");
        mapConfig.setConnectPassword("defaultPassword1");

        return mapConfig;
    }

    public SolaceConfiguration mapSolaceConfiguration( MapConfig mapFromConfig ) {

        if (mapFromConfig == null) {
            mapFromConfig = defaultSolaceConfiguration();
        }

        SolaceConfiguration solaceConfiguration = new SolaceConfiguration();
        solaceConfiguration.setName(mapFromConfig.getConfigName());
        solaceConfiguration.setDocNameAndGenerateDocId(mapFromConfig.getConfigDocName());
        solaceConfiguration.getEventPortalConfiguration().setCloudApiToken(mapFromConfig.getEpCloudApiToken());
        solaceConfiguration.getSolaceConnection().setBrokerHost(mapFromConfig.getConnectBrokerHost());
        solaceConfiguration.getSolaceConnection().setMsgVpn(mapFromConfig.getConnectMsgVpn());
        solaceConfiguration.getSolaceConnection().setClientUserName(mapFromConfig.getConnectClientUserName());
        solaceConfiguration.getSolaceConnection().setPassword(mapFromConfig.getConnectPassword());

        return solaceConfiguration;
    }
}
