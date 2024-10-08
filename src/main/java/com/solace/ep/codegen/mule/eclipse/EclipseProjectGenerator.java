/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.solace.ep.codegen.mule.eclipse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.solace.ep.codegen.asyncapi.mapper.AsyncApiToMuleDocMapper;
import com.solace.ep.codegen.internal.model.MapMuleDoc;
import com.solace.ep.codegen.internal.model.SchemaInstance;
import com.solace.ep.codegen.mule.MuleFlowGenerator;
import com.solace.ep.codegen.mule.mapper.MuleDocMapper;
import com.solace.ep.codegen.mule.model.core.MuleDoc;
import com.solace.ep.codegen.util.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to generate Mule Flow Project and project archive as jar file
 */
@Slf4j
public class EclipseProjectGenerator {
    
    protected static final String
                        PATH_TMP                    = "$$_TEMP_$$",
                        PATH_META_INF               = "META-INF",
                        PATH_PROJECT_ROOT           = "META-INF/mule-src/%s",
                        PATH_EXCHANGE_DOCS          = "exchange-docs",
                        PATH_SRC_MAIN_JAVA          = "src/main/java",
                        PATH_SRC_MAIN_MULE          = "src/main/mule",
                        PATH_SRC_MAIN_RESOURCES     = "src/main/resources",
                        PATH_SCHEMAS                = "src/main/resources/schemas",
                        PATH_SRC_MAIN_RESOURCES_API = "src/main/resources/api",
                        PATH_SRC_TEST_JAVA          = "src/test/java",
                        PATH_SRC_TEST_RESOURCES     = "src/test/resources",
                        PATH_SRC_TEST_MUNIT         = "src/test/munit";
    //
    private static final String
                        ARCHIVE_EXT_STRING          = ".jar",
                        FILE_SEPARATOR              = File.separator,
                        DEFAULT_ENV_STRING          = "dev";

    //
    private final List<String> projectPathList = new ArrayList<>();
    //
    private final Map<String, File> projectPaths = new HashMap<>();
    //
    private File tempDirectory;
    private File metaInfDirectory;
    private File newProjectRoot;

    /**
     * Default constructor
     */
    public EclipseProjectGenerator() {
        // Defining list of paths to create in for new Mule Project under
        // project root
        projectPathList.add( PATH_EXCHANGE_DOCS         );
        projectPathList.add( PATH_SRC_MAIN_JAVA         );
        projectPathList.add( PATH_SRC_MAIN_MULE         );
        projectPathList.add( PATH_SRC_MAIN_RESOURCES    );
        projectPathList.add( PATH_SCHEMAS               );
        projectPathList.add( PATH_SRC_MAIN_RESOURCES_API);
        projectPathList.add( PATH_SRC_TEST_JAVA         );
        projectPathList.add( PATH_SRC_TEST_RESOURCES    );
        projectPathList.add( PATH_SRC_TEST_MUNIT        );
    }

    /**
     * Generate a new Mule Project Archive (jar) from AsyncApi file
     * @param groupId
     * @param flowNameArtifactId
     * @param version
     * @param asyncApiAsString
     * @param projectOutputPathAsString
     * @param cloudHubProject - Set to True to add Cloud Hub Deployment to the generated pom.xml
     * @throws Exception
     */
    public void generateEclipseArchiveForMuleFlowFromAsyncApi( 
        String groupId,
        String flowNameArtifactId,
        String version,
        String asyncApiAsString,
        String projectOutputPathAsString,
        boolean cloudHubProject
     ) throws Exception {

        final MapMuleDoc mapMuleDoc = AsyncApiToMuleDocMapper.mapMuleDocFromAsyncApi(asyncApiAsString);
        final MuleDocMapper muleDocMapper = new MuleDocMapper( mapMuleDoc );

        final MuleDoc muleFlowDoc = muleDocMapper.createMuleDoc();
        final MuleDoc muleGlobalConfigsDoc = muleDocMapper.createGlobalConfigsDoc();

        final String muleFlowXml = MuleFlowGenerator.writeMuleDocToXmlString(muleFlowDoc);
        final String muleGlobalConfigsXml = MuleFlowGenerator.writeMuleDocToXmlString(muleGlobalConfigsDoc);

        createMuleProject(groupId, flowNameArtifactId, version, muleFlowXml, muleGlobalConfigsXml, cloudHubProject);
        createSchemaFiles(mapMuleDoc);

        final String newMuleArchivePath = createMuleArchive(flowNameArtifactId);

        FileUtils.copyFile(newMuleArchivePath, projectOutputPathAsString);

        log.info("Done Creating Mule Project for Group:Artifact/Flow:Version {}:{}:{}",
                        groupId, flowNameArtifactId, version);
        log.info("Project structure written to: {}", projectOutputPathAsString);
    }

        /**
     * Generate a new Mule Project Archive (jar) from AsyncApi file
     * Cloud Hub Deployment configuration is automatically added to the project pom.xml
     * @param groupId
     * @param flowNameArtifactId
     * @param version
     * @param asyncApiAsString
     * @param projectOutputPathAsString
     * @throws Exception
     */
    public void generateEclipseArchiveForMuleFlowFromAsyncApi( 
        String groupId,
        String flowNameArtifactId,
        String version,
        String asyncApiAsString,
        String projectOutputPathAsString
     ) throws Exception {
        generateEclipseArchiveForMuleFlowFromAsyncApi(groupId, flowNameArtifactId, version, asyncApiAsString, projectOutputPathAsString, true);
     }

    /**
     * Create a new Mule Project for a specified Group:Artifact/Flow:Version
     * and Mule Flow as Serialized XML
     * The Mule project will be created in a tmp directory and compressed
     * to a 'jar' archive.
     * Returns the path of the compressed archive.
     * @param groupId
     * @param flowNameArtifactId
     * @param version
     * @param muleFlowXmlData
     * @return The absolute path of the new Mule Project archive
     * @throws Exception
     */
    public void createMuleProject(
        String groupId,
        String flowNameArtifactId,
        String version,
        String muleFlowXmlData,
        String muleFlowGlobalConfigsXmlData,
        boolean cloudHubProject
    ) throws Exception {
        log.info("Creating Mule Project for Group:Artifact/Flow:Version {}:{}:{}",
                        groupId, flowNameArtifactId, version);
        createProjectStructure( flowNameArtifactId );
        createCreateStaticFiles();
        createConfigPropertiesFile( DEFAULT_ENV_STRING );
        createLog4jFile(flowNameArtifactId);
        createPomFile(groupId, flowNameArtifactId, version, cloudHubProject);
        createMuleFlow(muleFlowXmlData, flowNameArtifactId);
        if ( muleFlowGlobalConfigsXmlData != null && ! muleFlowGlobalConfigsXmlData.isEmpty() ) {
            String globalConfigsBaseFilename = MuleProjectContent.FILE_GLOBAL_CONFIGS;
            if ( flowNameArtifactId.contentEquals( MuleProjectContent.FILE_GLOBAL_CONFIGS ) ) {
                globalConfigsBaseFilename += "_2";
            }
            createMuleFlow(muleFlowGlobalConfigsXmlData, globalConfigsBaseFilename);
        }

        // String compressedFilePath = 
        //             tempDirectory.getAbsolutePath() + 
        //             FILE_SEPARATOR + 
        //             flowNameArtifactId + 
        //             ARCHIVE_EXT_STRING; 

        // Zipper.zipDirectoryToArchive(metaInfDirectory, compressedFilePath);

        // log.info("Done Creating Mule Project for Group:Artifact/Flow:Version {}:{}:{}",
        //                 groupId, flowNameArtifactId, version);
        // log.info("Project structure written to temp directory at: {}", compressedFilePath);

        // return compressedFilePath;
    }

    public String createMuleArchive( 
            String flowNameArtifactId ) throws Exception {

        String compressedFilePath = 
                tempDirectory.getAbsolutePath() + 
                FILE_SEPARATOR + 
                flowNameArtifactId + 
                ARCHIVE_EXT_STRING; 

        Zipper.zipDirectoryToArchive(metaInfDirectory, compressedFilePath);

        return compressedFilePath;
    }

    /**
     * Create static files (same for every Mule project)
     * in the new Mule project
     * @throws IOException
     */
    protected void createCreateStaticFiles() throws IOException {

        log.debug("Start creating static Mule project files");
        File projectRoot = projectPaths.get( PATH_PROJECT_ROOT );
        File exchangeDocs = projectPaths.get( PATH_EXCHANGE_DOCS );
        File srcTestResources = projectPaths.get( PATH_SRC_TEST_RESOURCES );

        FileUtils.writeStringToFile( 
            MuleProjectContent.CONTENT_MULE_ARTIFACT__JSON, 
            projectRoot, 
            MuleProjectContent.FILE_MULE_ARTIFACT__JSON);
        
        FileUtils.writeStringToFile( 
            MuleProjectContent.CONTENT_HOME__MD,
            exchangeDocs,
            MuleProjectContent.FILE_HOME__MD);

        FileUtils.writeStringToFile(
            MuleProjectContent.CONTENT_LOG4J2_TEST__XML, 
            srcTestResources, 
            MuleProjectContent.FILE_LOG4J2_TEST__XML);

        log.debug("Completed creating static Mule project files");
    }

    protected void createSchemaFiles( MapMuleDoc mapMuleDoc ) throws IOException {
        log.debug("Start creating mule schema files");
        File schemaDir = projectPaths.get( PATH_SCHEMAS );

        for (Map.Entry<String, SchemaInstance> entry : mapMuleDoc.getSchemaMap().entrySet()) {
            SchemaInstance si = entry.getValue();
            FileUtils.writeStringToFile(
                si.getPayload(), schemaDir, si.getFileName());
        }

        log.debug("Completed creating mule schema files");
    }

    /**
     * Create the src/main/resources/log4j.xml configuration file in the 
     * new Mule project
     * @param flowName
     * @throws Exception
     */
    protected void createLog4jFile( String flowName ) throws Exception {

        log.debug("Start creating log4j2.xml config file for Mule Project");
        File srcMainResources = projectPaths.get( PATH_SRC_MAIN_RESOURCES );
        String log4jXml = MuleProjectContent.CONTENT_LOG4J2__XML.replaceAll(
                                MuleProjectContent.TOKEN_FLOW_NAME, flowName );

        FileUtils.writeStringToFile(
            log4jXml, 
            srcMainResources, 
            MuleProjectContent.FILE_LOG4J2__XML);

        log.debug("Completed creating log4j2.xml config file for Mule Project");
    }

    /**
     * Create default configuration properties file for this project
     * @param environmentString
     * @throws IOException
     */
    protected void createConfigPropertiesFile(
        String environmentString
    ) throws IOException
    {
        log.debug( "Start creating yaml properties file" );
        String propertiesFileName = MuleProjectContent.getConfigurationPropertiesFileName(environmentString);
        File resourcesDir = projectPaths.get( PATH_SRC_MAIN_RESOURCES );
        
        FileUtils.writeStringToFile( 
            MuleProjectContent.CONTENT_CONFIG_PROPERTIES , 
            resourcesDir, 
            propertiesFileName
        );
        log.debug( "Completed creating config properties file: {}", propertiesFileName );
    }

    /**
     * Create pom.xml file in the new Mule Project
     * @param groupId
     * @param artifactId
     * @param version
     * @throws Exception
     */
    protected void createPomFile(
        String groupId, 
        String artifactId, 
        String version,
        boolean cloudHubDeployment
    ) throws Exception {

        log.debug("Start creating pom.xml for Mule Project");
        File projectRoot = projectPaths.get( PATH_PROJECT_ROOT );
        String pomXml = MuleProjectContent.CONTENT_POM__XML
                            .replaceAll(MuleProjectContent.TOKEN_GROUP_ID, groupId)
                            .replaceAll(MuleProjectContent.TOKEN_ARTIFACT_ID, artifactId)
                            .replaceAll(MuleProjectContent.TOKEN_VERSION, version );

        if ( cloudHubDeployment ) {
            pomXml = pomXml.replace(MuleProjectContent.TOKEN_CLOUD_HUB_DEPLOYMENT, MuleProjectContent.CONTENT_CLOUDHUB_DEPLOYMENT);
        } else {
            pomXml = pomXml.replace(MuleProjectContent.TOKEN_CLOUD_HUB_DEPLOYMENT, "");
        }
        
        FileUtils.writeStringToFile(
            pomXml,
            projectRoot,
            MuleProjectContent.FILE_POM__XML);

        log.debug("Completed creating pom.xml for Mule Project");
    }

    /**
     * Create Mule Flow XML file in the new Mule project
     * @param xmlData
     * @param flowName
     * @throws Exception
     */
    protected void createMuleFlow( String xmlData, String flowName ) throws Exception {

        log.debug("Start creating Mule Flow {}.xml for Mule Project", flowName);
        File srcMainMule = projectPaths.get( PATH_SRC_MAIN_MULE );

        FileUtils.writeStringToFile( 
            xmlData, 
            srcMainMule, 
            flowName + ".xml" );

        log.debug("Completed creating Mule Flow {}.xml for Mule Project", flowName);
    }

    /**
     * Creates Mule Flow project with sub-directories in system temporary directory
     * @param newProjectName
     * @return
     * @throws Exception
     */
    protected String createProjectStructure( String newProjectName ) throws Exception {

        log.debug("Start creating Mule Project folder structure in temp directory");
        try {
            // Create Temp Directory
            tempDirectory = Files.createTempDirectory( newProjectName + "_" ).toFile();
            projectPaths.put( PATH_TMP, tempDirectory );
            // Create META-INFO Directory
            metaInfDirectory = FileUtils.createDirectory(tempDirectory, PATH_META_INF);
            projectPaths.put( PATH_META_INF, metaInfDirectory );
            // Create Project Root Directory
            newProjectRoot = FileUtils.createDirectory(
                                tempDirectory, 
                                String.format(PATH_PROJECT_ROOT, newProjectName) );
            projectPaths.put( PATH_PROJECT_ROOT, newProjectRoot );

            for ( String subPath : projectPathList ) {
                File subDir = FileUtils.createDirectory(newProjectRoot, subPath);
                projectPaths.put( subPath, subDir );
            }
        } catch ( IOException ioExc ) {
            log.error("Error creating project directory structure: {}", ioExc.getMessage());
            throw ioExc;
        }
        log.debug("Completed creating Mule Project folder structure in temp directory: {}", 
            projectPaths.get( PATH_TMP ));
        return newProjectRoot.getAbsolutePath();
    }

}
