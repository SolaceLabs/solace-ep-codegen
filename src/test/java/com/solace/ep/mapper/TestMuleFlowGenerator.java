package com.solace.ep.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.solace.ep.muleflow.mule.MuleFlowGenerator;
import com.solace.ep.muleflow.util.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMuleFlowGenerator {
    
    private static final String INVENTORY_SERVICE = "src/test/resources/asyncapi/Inventory Service-1.0.3.json";
    private static final String CATALOGUE_SERVICE = "src/test/resources/asyncapi/Catalogue Services-1.1.1.json";
    private static final String OUTPUT_DIR = "src/test/resources/test-output/builder/";

    @Test
    public void testCreateXmlFileFromAsyncApiFile() {

        final String test = "testCreateXmlFileFromAsyncApiFile";
        final String outputFile = OUTPUT_DIR + test + ".xml";
        try {
            MuleFlowGenerator.writeMuleXmlFileFromAsyncApiFile(INVENTORY_SERVICE, outputFile);
        } catch ( Exception exc ) {
            log.error("Test {} failed", test);
            fail(exc.getMessage());
            return;
        }

        assertTrue( true);
        log.info("Test {} passed", test);

    }

    @Test
    public void testCreateXmlFileFromAsyncApiString() {

        final String test = "testCreateXmlFileFromAsyncApiString";
        final String outputFile = OUTPUT_DIR + test + ".xml";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(CATALOGUE_SERVICE);
        } catch ( Exception exc ) {
            log.error("Failed to read the input file {}", CATALOGUE_SERVICE);
            log.error("Test {} failed", test);
            fail( exc.getMessage() );
            return;
        }

        try {
            MuleFlowGenerator.writeMuleXmlFileFromAsyncApiString(asyncApi, outputFile);
        } catch ( Exception exc ) {
            log.error("Test {} failed", test);
            fail(exc.getMessage());
            return;
        }

        assertTrue( true );
        log.info("Test {} passed", test);
    }

    @Test
    public void testGetXmlStringFromAsyncApiFile() {

        final String test = "testGetXmlStringFromAsyncApiFile";

        String xmlString;
        try {
            xmlString = MuleFlowGenerator.getMuleDocXmlFromAsyncApiFile(INVENTORY_SERVICE);
        } catch ( Exception exc ) {
            log.error("Test {} failed", test);
            fail(exc.getMessage());
            return;
        }

        assertTrue( xmlString.contains("<set-variable") );
    }

    @Test
    public void testGetXmlStringFromAsyncApiString() {

        final String test = "testGetXmlStringFromAsyncApiString";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(CATALOGUE_SERVICE);
        } catch ( Exception exc ) {
            log.error("Failed to read the input file {}", CATALOGUE_SERVICE);
            log.error("Test {} failed", test);
            fail( exc.getMessage() );
            return;
        }

        String xmlString;
        try {
            xmlString = MuleFlowGenerator.getMuleDocXmlFromAsyncApiString(asyncApi);
        } catch ( Exception exc ) {
            log.error("Test {} failed", test);
            fail(exc.getMessage());
            return;
        }

        assertTrue( xmlString.contains("<set-variable") );

    }
}
