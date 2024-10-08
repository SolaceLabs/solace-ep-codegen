package com.solace.ep.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.asyncapi.tests.TestAsyncApiParsing;
import com.solace.ep.codegen.asyncapi.mapper.AsyncApiToMuleDocMapper;
import com.solace.ep.codegen.internal.model.MapMuleDoc;
import com.solace.ep.codegen.mule.mapper.MuleDocMapper;
import com.solace.ep.codegen.mule.model.core.MuleDoc;
import com.solace.ep.codegen.mule.util.XmlMapperUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Test the mapping of AsyncApi --> MuleDocMapper intermediate format
 */
@Slf4j
public class TestMapper {
    
    @Test
    public void testCreateMapMuleDocFromAsyncApi() {

        final String asyncApi = TestAsyncApiParsing.getAsyncApiSample1();

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

        assertTrue(mapMuleDoc.getGlobalProperties().size() > 0);
    }

    /**
     * Test ability to map AsyncApi --> MuleDocMapper --> MuleDoc
     */
    @Test
    public void testCreateMuleDocFromAsyncApi() {

        final String asyncApi = TestAsyncApiParsing.getAsyncApiSample2();

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
        try {
            muleDoc = muleDocMapper.createMuleDoc();
        } catch ( Exception exc ) {
            log.error(exc.getMessage());
            fail( exc.getMessage() );
        }

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();

        try {
            xmlMapper.writeValue(new File("src/test/resources/test-output/mappedOutput19.xml"), muleDoc);
        } catch ( Exception exc ) {
            log.error( exc.getLocalizedMessage() );
            exc.printStackTrace();
            return;
        }
        log.info("Success - TestCreateMuleDocFromAsyncApi()");
    }
}
