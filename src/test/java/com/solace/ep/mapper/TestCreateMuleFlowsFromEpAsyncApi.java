package com.solace.ep.mapper;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.asyncapi.tests.TestAsyncApiParsing;
import com.solace.ep.codegen.mapper.MuleDocMapper;
import com.solace.ep.codegen.mapper.asyncapi.AsyncApiToMuleDocMapper;
import com.solace.ep.codegen.mapper.model.MapMuleDoc;
import com.solace.ep.codegen.mule.model.core.MuleDoc;
import com.solace.ep.codegen.mule.util.XmlMapperUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestCreateMuleFlowsFromEpAsyncApi {
    
    /**
     * Create Mule Flow XML for Inventory Service
     */
    @Test
    public void testInventoryService() {

        createMuleDocFromAsyncApi(
            "src/test/resources/asyncapi/Inventory Service-1.0.3.json", 
            "src/test/resources/test-output/inventory-service-02.xml");
    }

    /**
     * Create Mule Flow XML for Catalogue Service
     */
    @Test
    public void testCatalogueService() {

        createMuleDocFromAsyncApi(
            "src/test/resources/asyncapi/Catalogue Services-1.1.1.json", 
            "src/test/resources/test-output/catalogue-service-01.xml");
    }

    private static void createMuleDocFromAsyncApi( String inputFile, String outputFile ) {

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

        MuleDocMapper muleDocMapper = new MuleDocMapper( mapMuleDoc );

        MuleDoc muleDoc = null;
        MuleDoc globalConfigsDoc = null;
        try {
            muleDoc = muleDocMapper.createMuleDoc();
            globalConfigsDoc = muleDocMapper.createGlobalConfigsDoc();
        } catch ( Exception exc ) {
            log.error(exc.getMessage());
            fail( exc.getMessage() );
        }

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();

        try {
            xmlMapper.writeValue(new File(outputFile), muleDoc);
            xmlMapper.writeValue(new File("src/test/resources/test-output/Global.xml"), globalConfigsDoc );
        } catch ( Exception exc ) {
            log.error( exc.getLocalizedMessage() );
            exc.printStackTrace();
            return;
        }
        log.info("End 'createOutput1'");

    }
}
