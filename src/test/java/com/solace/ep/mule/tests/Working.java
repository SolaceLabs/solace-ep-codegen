package com.solace.ep.mule.tests;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.mule.model.base.KeyValuePair;
import com.solace.ep.mule.model.core.GlobalProperty;
import com.solace.ep.mule.model.core.MuleDoc;
import com.solace.ep.mule.model.core.MuleFlow;
import com.solace.ep.mule.model.core.MuleFlowRef;
import com.solace.ep.mule.model.core.SetVariable;
import com.solace.ep.mule.model.ee.TransformOperation;
import com.solace.ep.mule.model.ee.TransformOperation.TransformMessage;
import com.solace.ep.mule.model.json.ValidateJsonSchema;
import com.solace.ep.mule.model.solace.SolaceConfiguration;
import com.solace.ep.mule.model.solace.SolaceConfiguration.EventPortalConfiguration;
import com.solace.ep.mule.model.solace.SolaceConnection;
import com.solace.ep.mule.model.solace.SolaceMessage;
import com.solace.ep.mule.model.solace.SolacePublish;
import com.solace.ep.mule.model.solace.SolaceQueueListener;
import com.solace.ep.mule.model.util.XmlMapperUtils;
import com.solace.ep.mule.model.xml_module.ValidateXmlSchema;

public class Working {
    
    @Test
    public void working() {
        SolaceConfiguration solaceConfiguration = new SolaceConfiguration();
        solaceConfiguration.setName("Solace_PubSub__Connector_Config");
        solaceConfiguration.setDocName("Solace PubSub+ Connector Config");
        solaceConfiguration.generateDocId();

        solaceConfiguration.setEventPortalConfiguration( new EventPortalConfiguration() );
        solaceConfiguration.getEventPortalConfiguration().setCloudApiToken("eySecretCloudApiTokenhiouqhfeuohwaeoiufhwoefhaefiohw83hh984357348tqyuheakfj");

        solaceConfiguration.setSolaceConnection( new SolaceConnection() );
        solaceConfiguration.getSolaceConnection().setBrokerHost("tcps://mr-connection-bpeyke548a8.messaging.solace.cloud:55443");
        solaceConfiguration.getSolaceConnection().setMsgVpn("testVpn");
        solaceConfiguration.getSolaceConnection().setClientUserName("solace-cloud-client");
        solaceConfiguration.getSolaceConnection().setPassword("football1");

        solaceConfiguration.getSolaceConnection().getJcsmpProperties().add(new KeyValuePair("key1", "value1"));
        solaceConfiguration.getSolaceConnection().getJcsmpProperties().add(new KeyValuePair("key2", "value2"));

        MuleDoc mule = new MuleDoc();
        mule.setSolaceConfiguration(solaceConfiguration);

        GlobalProperty gp1 = new GlobalProperty();
        GlobalProperty gp2 = new GlobalProperty();
        gp1.setDocNameAndGenerateDocId("Global Property 1");
        gp1.setPropertyNameValue("epApplicationVersionId", "tt794pd354t");
        gp2.setDocNameAndGenerateDocId("Global Property 2");
        gp2.setPropertyNameValue("AnotherProperty", "AnotherValue");

        mule.getGlobalProperty().add( gp1 );
        mule.getGlobalProperty().add( gp2 );

        MuleFlow flow1 = new MuleFlow();
        flow1.generateDocId();
        flow1.setName("Ingress.SHIPPING.CATALOGUE.queue");
        SolaceQueueListener q1 = new SolaceQueueListener();
        q1.setDocNameAndGenerateDocId("SHIPPING.CATALOGUE Listener");
        q1.setConfigRef("Solace_PubSub__Connector_Config");
        q1.setAddress("SHIPPING.CATALOGUE");
        q1.setAckMode("AUTOMATIC_ON_FLOW_COMPLETION");

        flow1.setQueueListener(q1);

        ValidateJsonSchema val1 = new ValidateJsonSchema();
        val1.setDocNameAndGenerateDocId("Validate JSON schema");
        val1.setSchemaLocation("schemas/my-schema.json");
        val1.setSchemaContents("This is some crazy, crazy content!!!");

        flow1.setValidateJsonSchema(val1);
        flow1.setFlowRef(new MuleFlowRef("BizLogic.SHIPPING.CATALOG.queue", "Business Logic"));

        ValidateXmlSchema vXml = new ValidateXmlSchema();
        vXml.setDocNameAndGenerateDocId("Validate Xml");
        vXml.setSchemaLocation("schemas/myschema.xsd");
        vXml.setSchemaContents("Super content!!!!");

        flow1.setValidateXmlSchema(vXml);

        MuleFlow subFlow1 = new MuleFlow();
        subFlow1.generateDocId();
        subFlow1.setName("BizLogic.SHIPPING.CATALOG.queue");

        TransformOperation transform1 = new TransformOperation();
        transform1.setDocNameAndGenerateDocId("Transform Message");
        transform1.setTransformMessage(new TransformMessage());
        transform1.getTransformMessage().setSetPayload("%dw 2.0\noutput application/java\n---\n{\n}");

        MuleFlowRef flowRef2 = new MuleFlowRef("BizLogic.SHIPPING.CATALOG.queue", "Connect this to the correct egress flow");
        subFlow1.setTransform(transform1);
        subFlow1.setFlowRef(flowRef2);

        MuleFlow subFlow2 = new MuleFlow();
        subFlow2.generateDocId();
        subFlow2.setName("Egress.ShipmentCreated.topic");
        subFlow2.getSetVariable().add(new SetVariable("someVariable", "value123", "Generate Dynamic Topic"));
        subFlow2.getSetVariable().add(new SetVariable("anotherVariable", "anotherValue456", "Multiple Variables are possible"));
        ValidateXmlSchema vXml2 = new ValidateXmlSchema();
        vXml2.setDocNameAndGenerateDocId("Validate schema XML");
        ValidateJsonSchema vJson2 = new ValidateJsonSchema();
        vJson2.setDocNameAndGenerateDocId("Validate schema Json");
        vJson2.setSchemaContents(TestData.getSampleJson1());
        SolacePublish solacePublish2 = new SolacePublish();
        solacePublish2.setDocNameAndGenerateDocId("Publish Shipment Updated Event");
        solacePublish2.setAddress("acmeretail/shipping/shipment/created/v1/regionId/statusId/shipmentId");
        solacePublish2.setConfigRef("Solace_PubSub__Connector_Config");
        solacePublish2.setDestinationType("QUEUE");
        solacePublish2.setMessage(new SolaceMessage("TEXT_MESSAGE"));

        subFlow2.setValidateXmlSchema(vXml2);
        subFlow2.setValidateJsonSchema(vJson2);
        subFlow2.setPublish(solacePublish2);

        mule.getFlow().add(flow1);
        mule.getSubFlow().add(subFlow1);
        mule.getSubFlow().add(subFlow2);
        
        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();

        try {
            xmlMapper.writeValue(new File("src/test/resources/test-output/working.xml"), mule);
            String outXml = xmlMapper.writeValueAsString(mule);
            System.out.println(outXml);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
        }

    }
}
