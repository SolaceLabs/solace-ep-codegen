package com.solace.ep.codegen.eclipse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.solace.ep.asyncapi.tests.TestAsyncApiParsing;
import com.solace.ep.codegen.mule.MuleFlowGenerator;
import com.solace.ep.codegen.util.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEclipseProjectGenerator {
    
    private static final String INVENTORY_SERVICE = "src/test/resources/asyncapi/Inventory Service-1.0.3.json";
    // private static final String CATALOGUE_SERVICE = "src/test/resources/asyncapi/Catalogue Services-1.1.1.json";

    @Test
    public void testCreateAcmeRetailAsapioYaml_0_1_2() {

        EclipseProjectGenerator epg = new EclipseProjectGenerator();
        final String
            groupId = "com.solace.ep",
            artifactId = "acme-retail",
            version = "0.1.2",
            inputPath = "src/test/resources/asyncapi/AcmeRetailAsapio-0.1.2.yaml",
            outputPath = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";
        
        try {
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, FileUtils.getFileAsString(inputPath), outputPath);
        } catch ( Exception exc ) {
            log.error(exc.getMessage());
            exc.printStackTrace();
            fail(exc.getMessage());
            return;
        }
        System.out.println("Project Created");
    }

    /**
     * Test ability to generate a new project using EclipseProjectGenerator
     */
    @Test
    public void testCreateStructure1() {

        EclipseProjectGenerator epg = new EclipseProjectGenerator();
        String newProject = "";

        try {
            newProject = epg.createProjectStructure("Test-new-project");
        } catch (Exception e) {
            log.error(e.getMessage());
            fail(e.getMessage());
            return;
        }

        Path newProjectPath = Paths.get( newProject );
        assertTrue( Files.exists( newProjectPath ) );
    }

    /**
     * Test createMuleProject method - Creates project structure in tmp directory
     */
    @Test 
    public void testCreateMuleProject() {

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "sample-mule-project",
            version = "1.0.1";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(INVENTORY_SERVICE);
        } catch ( Exception exc ) {
            log.error("Failed to read the input file {}", INVENTORY_SERVICE);
            log.error("Test {} failed", "createMuleProject");
            fail( exc.getMessage() );
            return;
        }

        String xmlString;
        try {
            xmlString = MuleFlowGenerator.getMuleDocXmlFromAsyncApiString(asyncApi);
        } catch ( Exception exc ) {
            log.error("Test {} failed", "createMuleProject");
            fail(exc.getMessage());
            return;
        }

        String generatedArchive = "NADA";
        EclipseProjectGenerator epg = new EclipseProjectGenerator();
        try {
            epg.createMuleProject(groupId, artifactId, version, xmlString, null, true);
            generatedArchive = epg.createMuleArchive(artifactId);
        } catch (Exception e) {
            log.error(e.getMessage());
            fail(e.getMessage());
            return;
        }

        // If we're here, we succeeded
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );
    }

    @Test
    public void testGenerateEclipseArchiveForMuleFlowFromAsyncApi() {

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "sample-mule-project",
            version = "1.0.1";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(INVENTORY_SERVICE);
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", INVENTORY_SERVICE);
            fail( exc.getMessage() );
            return;
        }
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

    @Test
    public void testGenerateShippingService0_1_2() {

        final String SHIPPING_SERVICE_ASYNCAPI = "src/test/resources/asyncapi/Shipping Service-0.1.2.json";

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "shipping-service",
            version = "0.1.2";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId  + ".jar";

        String asyncApi;
        try {
            // Read asyncapi into string
            asyncApi = FileUtils.getFileAsString(SHIPPING_SERVICE_ASYNCAPI);
            // Instantiate Generator
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            // Generate Mule Flow Project Archive
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", SHIPPING_SERVICE_ASYNCAPI);
            fail( exc.getMessage() );
            return;
        }

        // Test for existance of output
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

    @Test
    public void testGenerateShippingService0_1_3() {

        final String SHIPPING_SERVICE_ASYNCAPI = "src/test/resources/asyncapi/Shipping Service-0.1.3 (1).json";

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "shipping-service",
            version = "0.1.3";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";

        String asyncApi;
        try {
            // Read asyncapi into string
            asyncApi = FileUtils.getFileAsString(SHIPPING_SERVICE_ASYNCAPI);
            // Instantiate Generator
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            // Generate Mule Flow Project Archive
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", SHIPPING_SERVICE_ASYNCAPI);
            fail( exc.getMessage() );
            return;
        }

        // Test for existance of output
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

    @Test
    public void testGenerateEclipseArchiveForMuleFlowFromAsyncApi_() {

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "another-sample",
            version = "1.0.2";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";

        String asyncApi;
        try {
            asyncApi = TestAsyncApiParsing.getSampleAsyncApi();
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", INVENTORY_SERVICE);
            fail( exc.getMessage() );
            return;
        }
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

    @Test
    public void testCoffeeShopOrderManagement0_1_1() {

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "coffee-shop-order-mgt",
            version = "0.1.1";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";
        final String ASYNC_API_FILE = "src/test/resources/ep-demo/asyncapi/Order Management-0.1.1.json";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(ASYNC_API_FILE);
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", ASYNC_API_FILE);
            fail( exc.getMessage() );
            return;
        }
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

    @Test
    public void testGenerateEclipseArchiveCustomerFacingMobileApp016() {

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "customer-facing-mobile-app",
            version = "1.0.2";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";
        final String ASYNC_API_FILE = "src/test/resources/asyncapi/Customer Facing Mobile Application-0.1.6.json";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(ASYNC_API_FILE);
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", ASYNC_API_FILE);
            fail( exc.getMessage() );
            return;
        }
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

    @Test
    public void testCoffeeShopOrderManagement0_1_2() {

        final String 
            groupId = "com.solace.ep.awesome",
            artifactId = "coffee-shop-order-mgt",
            version = "0.1.2";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";
        final String ASYNC_API_FILE = "src/test/resources/asyncapi/Order Management-0.1.2.json";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(ASYNC_API_FILE);
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", ASYNC_API_FILE);
            fail( exc.getMessage() );
            return;
        }
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

    @Test
    public void testLms_0_1_1() {

        final String 
            groupId = "com.solace.ep.lms",
            artifactId = "lms-app",
            version = "0.1.1";
        final String
            generatedArchive = "src/test/resources/test-output/generated-archive/" + artifactId + ".jar";
        final String ASYNC_API_FILE = "src/test/resources/asyncapi/lms_asyncapi-0.1.1.json";

        String asyncApi;
        try {
            asyncApi = FileUtils.getFileAsString(ASYNC_API_FILE);
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to create the archive file for {}", ASYNC_API_FILE);
            fail( exc.getMessage() );
            return;
        }
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }
}
