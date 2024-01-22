package com.solace.ep.muleflow.eclipse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.solace.ep.muleflow.MuleFlowGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEclipseProjectGenerator {
    
    private static final String INVENTORY_SERVICE = "src/test/resources/asyncapi/Inventory Service-1.0.3.json";
    // private static final String CATALOGUE_SERVICE = "src/test/resources/asyncapi/Catalogue Services-1.1.1.json";

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
        
        // log.info("Success - createStructure1");
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
            asyncApi = MuleFlowGenerator.getFileAsString(INVENTORY_SERVICE);
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
            generatedArchive = epg.createMuleProject(groupId, artifactId, version, xmlString);
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
            asyncApi = MuleFlowGenerator.getFileAsString(INVENTORY_SERVICE);
            EclipseProjectGenerator epg = new EclipseProjectGenerator();
            epg.generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, artifactId, version, asyncApi, generatedArchive);
        } catch ( Exception exc ) {
            log.error("Failed to read the input file {}", INVENTORY_SERVICE);
            fail( exc.getMessage() );
            return;
        }
        Path generatedArchivePath = Paths.get( generatedArchive );
        assertTrue( Files.exists( generatedArchivePath ) );

    }

}
