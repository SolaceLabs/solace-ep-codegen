package com.solace.ep.mule.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.codegen.mule.model.core.GlobalProperty;
import com.solace.ep.codegen.mule.model.core.MuleDoc;
import com.solace.ep.codegen.mule.model.core.MuleFlow;
import com.solace.ep.codegen.mule.model.core.MuleFlowRef;
import com.solace.ep.codegen.mule.model.core.SetVariable;
import com.solace.ep.codegen.mule.model.ee.TransformOperation;
import com.solace.ep.codegen.mule.model.ee.TransformOperation.TransformMessage;
import com.solace.ep.codegen.mule.model.json.ValidateJsonSchema;
import com.solace.ep.codegen.mule.model.solace.SolaceConfiguration;
import com.solace.ep.codegen.mule.model.solace.SolaceConnection;
import com.solace.ep.codegen.mule.model.solace.SolaceMessage;
import com.solace.ep.codegen.mule.model.solace.SolacePublish;
import com.solace.ep.codegen.mule.model.solace.SolaceQueueListener;
import com.solace.ep.codegen.mule.model.solace.SolaceConfiguration.EventPortalConfiguration;
import com.solace.ep.codegen.mule.model.xml_module.ValidateXmlSchema;
import com.solace.ep.codegen.mule.util.XmlMapperUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Rough tests for Mule Flow XML with Solace artifacts
 */
@Slf4j
public class TestCreateOutputForStudio {
    
    @Test
    public void testCreateOutput1() {

        // Solace Configuration
        SolaceConfiguration solaceConfiguration = new SolaceConfiguration();
        solaceConfiguration.setName("Solace_PubSub__Connector_Config");
        solaceConfiguration.setDocName("Solace PubSub+ Connector Config");

        // Event Portal Configuration
        solaceConfiguration.setEventPortalConfiguration( new EventPortalConfiguration() );
        solaceConfiguration.getEventPortalConfiguration().setCloudApiToken("eySecretCloudApiTokenTHISISNOTREALalskdjfaskldfjhw83hh984357348tqyuheakfj");

        // Solace Connection
        solaceConfiguration.setSolaceConnection( new SolaceConnection() );
        solaceConfiguration.getSolaceConnection().setBrokerHost("tcps://mr-connection-SAMPLEHOST.messaging.solace.cloud:55443");
        solaceConfiguration.getSolaceConnection().setMsgVpn("testVpn");
        solaceConfiguration.getSolaceConnection().setClientUserName("solace-cloud-client");
        solaceConfiguration.getSolaceConnection().setPassword("football1");

        MuleDoc mule = new MuleDoc();
        mule.setSolaceConfiguration(solaceConfiguration);

        // Global Property
        mule.getGlobalProperty().add( 
                    new GlobalProperty(
                            "Global Property 1", 
                            "epApplicationVersionId", 
                            "tt794pd354t") );
        mule.getGlobalProperty().add( 
                    new GlobalProperty(
                            "Global Property 2", 
                            "AnotherProperty", 
                            "AnotherValue") );


        // Flow 1
        MuleFlow flow1 = new MuleFlow();
        flow1.setName("Ingress.SHIPPING.CATALOGUE.queue");

        // Flow 1 Listener
        SolaceQueueListener q1 = new SolaceQueueListener();
        q1.setAddress("SHIPPING.CATALOGUE");
        q1.setDocName(q1.getAddress() + " Listener");
        q1.setConfigRef(solaceConfiguration.getName());
        q1.setAckMode("AUTOMATIC_ON_FLOW_COMPLETION");
        flow1.setQueueListener(q1);

        // Flow 1 - Validate Json
        ValidateJsonSchema val1 = new ValidateJsonSchema();
        val1.setDocName("Validate JSON schema");
        val1.setSchemaLocation("schemas\\shipping.avro.1.0.1.json");
        flow1.setValidateJsonSchema(val1);

        flow1.setFlowRef(new MuleFlowRef("BizLogic.SHIPPING.CATALOG.queue", "Business Logic"));

        MuleFlow subFlow1 = new MuleFlow();
        subFlow1.setName(flow1.getFlowRef().getName());

        TransformOperation transform1 = new TransformOperation();
        transform1.setDocName("Transform Message");
        transform1.setTransformMessage(new TransformMessage());
        transform1.getTransformMessage().setSetPayload("%dw 2.0\noutput application/java\n---\n{\n}");
        subFlow1.setTransform(transform1);

        subFlow1.setFlowRef(new MuleFlowRef("Egress.ShipmentCreated.topic", "Connect this to the correct egress flow"));


        MuleFlow subFlow2 = new MuleFlow();
        subFlow2.setName(subFlow1.getFlowRef().getName());
        subFlow2.getSetVariable().add(new SetVariable("someVariable", "value123", "Generate Dynamic Topic"));
        subFlow2.getSetVariable().add(new SetVariable("anotherVariable", "anotherValue456", "Multiple Variables are possible"));
        ValidateXmlSchema vXml2 = new ValidateXmlSchema();
        vXml2.setDocName("Validate schema XML");
        ValidateJsonSchema vJson2 = new ValidateJsonSchema();
        vJson2.setDocName("Validate schema Json");
        vJson2.setSchemaContents(TestData.getSampleJson1());
        SolacePublish solacePublish2 = new SolacePublish();
        solacePublish2.setDocName("Publish Shipment Created Event");
        solacePublish2.setAddress("acmeretail/shipping/shipment/created/v1/regionId/statusId/shipmentId");
        solacePublish2.setConfigRef(mule.getSolaceConfiguration().getName());
        solacePublish2.setDestinationType("QUEUE");
        solacePublish2.setMessage(new SolaceMessage("TEXT_MESSAGE"));

        subFlow2.setValidateXmlSchema(vXml2);
        subFlow2.setValidateJsonSchema(vJson2);
        subFlow2.setPublish(solacePublish2);

        mule.getFlow().add(flow1);
        mule.getSubFlow().add(subFlow1);
        mule.getSubFlow().add(subFlow2);

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();
        String outXml = "";

        try {
            outXml = xmlMapper.writeValueAsString(mule);
            // System.out.println(outXml);
            log.info("Serialized Xml output");
        } catch ( Exception exc ) {
            log.error( exc.getLocalizedMessage() );
            exc.printStackTrace();
            fail(exc.getMessage());
            return;
        }
        assertTrue(outXml.contains("variableName=\"someVariable"));
        assertTrue(outXml.contains("Validate schema Json"));
        assertTrue(outXml.contains("Egress.ShipmentCreated.topic"));
    }

}
