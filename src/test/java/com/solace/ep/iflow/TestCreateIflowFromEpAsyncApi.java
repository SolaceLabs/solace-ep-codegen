package com.solace.ep.iflow;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;
import org.omg.spec.bpmn._20100524.model.TCallActivity;
import org.omg.spec.bpmn._20100524.model.TDefinitions;
import org.omg.spec.bpmn._20100524.model.TEndEvent;
import org.omg.spec.bpmn._20100524.model.TParticipant;
import org.omg.spec.bpmn._20100524.model.TProcess;
import org.omg.spec.bpmn._20100524.model.TSequenceFlow;
import org.omg.spec.bpmn._20100524.model.TStartEvent;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.asyncapi.tests.TestAsyncApiParsing;
import com.solace.ep.muleflow.mapper.MuleDocMapper;
import com.solace.ep.muleflow.mapper.asyncapi.AsyncApiToMuleDocMapper;
import com.solace.ep.muleflow.mapper.model.MapMuleDoc;
import com.solace.ep.muleflow.mapper.sap.iflow.SapIflowMapper;
import com.solace.ep.muleflow.mapper.sap.iflow.utils.Bpmn2NamespaceMapper;
import com.solace.ep.muleflow.mule.model.core.MuleDoc;
import com.solace.ep.muleflow.mule.util.XmlMapperUtils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestCreateIflowFromEpAsyncApi {
    
    @Test
    public void testCreateIflow_01() {

        String inputFile = "src/test/resources/asyncapi/Order Management-0.1.2.json";
        String outputFile = "src/test/resources/test-output/iflow/OrderMgt-0.1.2.xml";

        MapMuleDoc mapMuleDoc =
        createIflowFromAsyncApi( inputFile, outputFile );

        SapIflowMapper iflowMapper = new SapIflowMapper();

        iflowMapper.createSapIflow(mapMuleDoc);


        TDefinitions td = iflowMapper.getOut();

        try {
            // JAXBContext context = JAXBContext.newInstance( 
            //     TDefinitions.class,
            //     TStartEvent.class,
            //     TEndEvent.class,
            //     TCallActivity.class,
            //     TSequenceFlow.class,
            //     TProcess.class,
            //     TParticipant.class );
            JAXBContext context = JAXBContext.newInstance( 
                    TDefinitions.class
                 );
                Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper", new Bpmn2NamespaceMapper() );
//                                         com.sun.xml.internal.bind.namespacePrefixMapper
//            marshaller.setProperty(, marshaller);

            marshaller.marshal( iflowMapper.getJaxbOut(), new FileOutputStream( outputFile ) );
        } catch ( JAXBException jaxbExc ) {
            log.error( jaxbExc.getMessage() );
            System.out.println( jaxbExc.getMessage() );
            jaxbExc.printStackTrace();
            fail( jaxbExc.getMessage() );
        } catch ( FileNotFoundException fnfExc ) {
            log.error( fnfExc.getMessage() );
            System.out.println( fnfExc.getMessage() );
            fnfExc.printStackTrace();
            fail( fnfExc.getMessage() );
        }

        assertTrue( true );

    }

    private static MapMuleDoc createIflowFromAsyncApi( String inputFile, String outputFile ) {

        final String asyncApi = TestAsyncApiParsing.getAsyncApi(inputFile);

        MapMuleDoc mapMuleDoc = null;

        try {
            mapMuleDoc = AsyncApiToMuleDocMapper.mapMuleDocFromAsyncApi(asyncApi);
        }
        catch ( Exception exc ) {
            log.error(exc.getMessage());
            System.out.println( exc.getCause().getMessage() );
        }

        if ( mapMuleDoc == null ) {
            fail( "Failed to create map doc" );
        }

        return mapMuleDoc;

        // MuleDocMapper muleDocMapper = new MuleDocMapper( mapMuleDoc );

        // MuleDoc muleDoc = null;
        // MuleDoc globalConfigsDoc = null;
        // try {
        //     muleDoc = muleDocMapper.createMuleDoc();
        //     globalConfigsDoc = muleDocMapper.createGlobalConfigsDoc();
        // } catch ( Exception exc ) {
        //     log.error(exc.getMessage());
        //     fail( exc.getMessage() );
        // }

        // XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();

        // try {
        //     xmlMapper.writeValue(new File(outputFile), muleDoc);
        //     xmlMapper.writeValue(new File("src/test/resources/test-output/Global.xml"), globalConfigsDoc );
        // } catch ( Exception exc ) {
        //     log.error( exc.getLocalizedMessage() );
        //     exc.printStackTrace();
        //     return;
        // }
        // log.info("End 'createOutput1'");

    }
}
