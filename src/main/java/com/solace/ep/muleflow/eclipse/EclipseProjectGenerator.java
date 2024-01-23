package com.solace.ep.muleflow.eclipse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.solace.ep.muleflow.MuleFlowGenerator;
import com.solace.ep.muleflow.util.FileUtils;
import com.solace.ep.muleflow.util.Zipper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EclipseProjectGenerator {
    
    // The actual path separator doesn't matter, it's used as a String tokenizer
    protected static final String
                        STATIC_PATH_SEPARATOR = "/";

    protected static final String
                        PATH_TMP                    = "$$_TEMP_$$",
                        PATH_META_INF               = "META-INF",
                        PATH_PROJECT_ROOT           = "META-INF/mule-src/%s",
                        PATH_EXCHANGE_DOCS          = "exchange-docs",
                        PATH_SRC_MAIN_JAVA          = "src/main/java",
                        PATH_SRC_MAIN_MULE          = "src/main/mule",
                        PATH_SRC_MAIN_RESOURCES     = "src/main/resources",
                        PATH_SRC_MAIN_RESOURCES_API = "src/main/resources/api",
                        PATH_SRC_TEST_JAVA          = "src/test/java",
                        PATH_SRC_TEST_RESOURCES     = "src/test/resources",
                        PATH_SRC_TEST_MUNIT         = "src/test/munit";

    private static final String
                        ARCHIVE_EXT_STRING          = ".jar",
                        FILE_SEPARATOR              = File.separator;

    //
    private static final List<String> projectPathList = new ArrayList<>();
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
        // Defining list of paths to create in for new Mule Project        
        projectPathList.add( PATH_EXCHANGE_DOCS         );
        projectPathList.add( PATH_SRC_MAIN_JAVA         );
        projectPathList.add( PATH_SRC_MAIN_MULE         );
        projectPathList.add( PATH_SRC_MAIN_RESOURCES    );
        projectPathList.add( PATH_SRC_MAIN_RESOURCES_API);
        projectPathList.add( PATH_SRC_TEST_JAVA         );
        projectPathList.add( PATH_SRC_TEST_RESOURCES    );
        projectPathList.add( PATH_SRC_TEST_MUNIT        );
    }

    /**
     * 
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

        String muleFlowXml = MuleFlowGenerator.getMuleDocXmlFromAsyncApiString( asyncApiAsString );

        String newMuleArchivePath = createMuleProject(groupId, flowNameArtifactId, version, muleFlowXml);

        FileUtils.copyFile(newMuleArchivePath, projectOutputPathAsString);
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
    public String createMuleProject(
        String groupId,
        String flowNameArtifactId,
        String version,
        String muleFlowXmlData
    ) throws Exception {
        log.info("Creating Mule Project for Group:Artifact/Flow:Version {}:{}:{}",
                        groupId, flowNameArtifactId, version);
        createProjectStructure( flowNameArtifactId );
        createCreateStaticFiles();
        createLog4jFile(flowNameArtifactId);
        createPomFile(groupId, flowNameArtifactId, version);
        createMuleFlow(muleFlowXmlData, flowNameArtifactId);

        String compressedFilePath = 
                    tempDirectory.getAbsolutePath() + 
                    FILE_SEPARATOR + 
                    flowNameArtifactId + 
                    ARCHIVE_EXT_STRING; 

        Zipper.zipDirectoryToArchive(metaInfDirectory, compressedFilePath);

        log.info("Done Creating Mule Project for Group:Artifact/Flow:Version {}:{}:{}",
                        groupId, flowNameArtifactId, version);
        log.info("Output written to temp directory at: {}", compressedFilePath);

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

        writeStringToFile( 
            MuleProjectContent.CONTENT_MULE_ARTIFACT__JSON, 
            projectRoot, 
            MuleProjectContent.FILE_MULE_ARTIFACT__JSON);
        
        writeStringToFile( 
            MuleProjectContent.CONTENT_HOME__MD,
            exchangeDocs,
            MuleProjectContent.FILE_HOME__MD);

        writeStringToFile(
            MuleProjectContent.CONTENT_LOG4J2_TEST__XML, 
            srcTestResources, 
            MuleProjectContent.FILE_LOG4J2_TEST__XML);

        log.debug("Completed creating static Mule project files");
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

        writeStringToFile(
            log4jXml, 
            srcMainResources, 
            MuleProjectContent.FILE_LOG4J2__XML);

        log.debug("Completed creating log4j2.xml config file for Mule Project");
    }

    /**
     * Create pom.xml file in the new Mule Project
     * @param groupId
     * @param artifactId
     * @param version
     * @throws Exception
     */
    protected void createPomFile( String groupId, String artifactId, String version ) throws Exception {

        log.debug("Start creating pom.xml for Mule Project");
        File projectRoot = projectPaths.get( PATH_PROJECT_ROOT );
        String pomXml = MuleProjectContent.CONTENT_POM__XML
                            .replaceAll(MuleProjectContent.TOKEN_GROUP_ID, groupId)
                            .replaceAll(MuleProjectContent.TOKEN_ARTIFACT_ID, artifactId)
                            .replaceAll(MuleProjectContent.TOKEN_VERSION, version );
        
        writeStringToFile(
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

        writeStringToFile( 
            xmlData, 
            srcMainMule, 
            flowName + ".xml" );

        log.debug("Completed creating Mule Flow {}.xml for Mule Project", flowName);
    }

    protected String createProjectStructure( String newProjectName ) throws Exception {

        log.debug("Start creating Mule Project folder structure in temp directory");
        try {
            // Create Temp Directory
            tempDirectory = Files.createTempDirectory( newProjectName + "_" ).toFile();
            projectPaths.put( PATH_TMP, tempDirectory );
            // Create META-INFO Directory
            metaInfDirectory = createDirectory(tempDirectory, PATH_META_INF);
            projectPaths.put( PATH_META_INF, metaInfDirectory );
            // Create Project Root Directory
            newProjectRoot = createDirectory(
                                tempDirectory, 
                                String.format(PATH_PROJECT_ROOT, newProjectName) );
            projectPaths.put( PATH_PROJECT_ROOT, newProjectRoot );

            for ( String subPath : projectPathList ) {
                File subDir = createDirectory(newProjectRoot, subPath);
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

    private void writeStringToFile( String dataToWrite, File dir, String fileName )  
    throws IOException {

        BufferedWriter writer = new BufferedWriter(
            new FileWriter( 
                new File( dir, fileName ), 
                false ) );
        writer.write(dataToWrite);
        writer.close();
    }

    private File createDirectory( File rootPath, String subPath ) throws Exception {

        StringTokenizer pathTokenizer = new StringTokenizer(subPath, STATIC_PATH_SEPARATOR);
        File newDirectory = rootPath;

        while ( pathTokenizer.hasMoreTokens() ) {
            String s = pathTokenizer.nextToken();
            File workingDir = new File(newDirectory, s);
            if ( ! workingDir.exists() ) {
                // Use mkdir() instead of mkdirs() to avoid file separator problems
                workingDir.mkdir();
            }
            // TODO - Error handling
            newDirectory = workingDir;
        }

        return newDirectory;
    }
}
