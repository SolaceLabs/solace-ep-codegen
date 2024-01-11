package com.solace.ep.mapper;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.asyncapi.AsyncApiAccessor;
import com.solace.ep.asyncapi.tests.TestAsyncApiParsing;
import com.solace.ep.mapper.asyncapi.AsyncApiToMuleDocMapper;
import com.solace.ep.mapper.model.MapMuleDoc;
import com.solace.ep.mule.model.core.MuleDoc;
import com.solace.ep.mule.model.util.XmlMapperUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMapper {
    
    @Test
    public void TestCreateMapMuleDocFromAsyncApi() {

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
    }

    @Test
    public void TestCreateMuleDocFromAsyncApi() {

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

        MuleDocMapper muleDocMapper = new MuleDocMapper();

        MuleDoc muleDoc = null;
        try {
            muleDoc = muleDocMapper.createMuleDoc(mapMuleDoc);
        } catch ( Exception exc ) {
            log.error(exc.getMessage());
            fail( exc.getMessage() );
        }

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();

        try {
            xmlMapper.writeValue(new File("src/test/resources/test-output/mappedOutput13.xml"), muleDoc);
        } catch ( Exception exc ) {
            log.error( exc.getLocalizedMessage() );
            exc.printStackTrace();
            return;
        }
        log.info("End 'createOutput1'");
    }
}
