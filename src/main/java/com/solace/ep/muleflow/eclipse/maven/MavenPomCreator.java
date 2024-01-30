package com.solace.ep.muleflow.eclipse.maven;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import com.solace.ep.muleflow.eclipse.maven.MavenPomConfigSettings.DependencyConfig;
import com.solace.ep.muleflow.eclipse.maven.MavenPomConfigSettings.RepositoryConfig;
import com.solace.ep.muleflow.util.FileUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class MavenPomCreator {

    private static final String
                PROP_SOURCE_ENCODING        = "project.build.sourceEncoding",
                PROP_OUTPUT_ENCODING        = "project.reporting.outputEncoding",
                PROP_APP_RUNTIME            = "app.runtime",
                PROP_MULE_PLUGIN_VERSION    = "mule.maven.plugin.version";
    //
    private static final String
                MAVEN_MODEL_VERSION         = "4.0.0";
    //
    private static final String 
                ENCODING_UTF_8 = "UTF-8",
                MULE_APPLICATION_PACKAGING  = "mule-application";

    //
    private static final String
                DEFAULT_GAV_GROUP_ID        = "com.mycompany",
                DEFAULT_GAV_ARTIFACT_ID     = "new-mule-project",
                DEFAULT_GAV_VERSION         = "1.0.0-SNAPSHOT",
                DEFAULT_GAV_NAME            = "New Mule Project";
    //
    private static final String
                APP_RUNTIME_VERSION         = "4.4.0-20230320",
                MULE_MAVEN_PLUGIN_VERSION   = "4.0.0",
                JAVA_RUNTIME_VERSION_TARGET = "1.8";
    //
    private static final String
                MAVEN_CLEAN_PLUGIN_VERSION      = "3.2.0",
                MAVEN_COMPILER_PLUGIN_VERSION   = "3.10.1";
    //

    protected String groupId                = DEFAULT_GAV_GROUP_ID;

    protected String artifactId             = DEFAULT_GAV_ARTIFACT_ID;

    protected String version                = DEFAULT_GAV_VERSION;

    protected String name                   = DEFAULT_GAV_NAME;

    protected String packaging              = MULE_APPLICATION_PACKAGING;

    protected String sourceEncoding         = ENCODING_UTF_8;

    protected String outputEncoding         = ENCODING_UTF_8;

    protected String mavenCleanPluginVersion    = MAVEN_CLEAN_PLUGIN_VERSION;

    protected String muleMavenPluginVersion     = MULE_MAVEN_PLUGIN_VERSION;

    protected String appRuntimeVersion          = APP_RUNTIME_VERSION;

    protected String mavenCompilerPluginVersion = MAVEN_COMPILER_PLUGIN_VERSION;

    protected String javaRuntimeVersionTarget   = JAVA_RUNTIME_VERSION_TARGET;

    protected MavenPomConfigSettings mavenPomConfigSettings;

    protected Set<ModuleProtocolType> moduleProtocolTypes;

    protected Model mulePomModel;

    public MavenPomCreator( 
        MavenPomConfigSettings mavenPomConfigSettings, 
        Set<ModuleProtocolType> moduleProtocolTypes ) 
        throws Exception
    {
        if ( mavenPomConfigSettings == null || moduleProtocolTypes == null ) {
            throw new Exception( "Maven POM config settings and Module/Protocol List must not be null" );
        }
        this.mavenPomConfigSettings = mavenPomConfigSettings;
        this.moduleProtocolTypes = moduleProtocolTypes;
    }

    public MavenPomCreator(
        MavenPomConfigSettings mavenPomConfigSettings,
        Set<ModuleProtocolType> moduleProtocolTypes,
        String groupId,
        String artifactId,
        String version
    )   throws Exception 
    {
        this( mavenPomConfigSettings, moduleProtocolTypes );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public void addModuleProtocolType( ModuleProtocolType moduleProtocolType ) {
        if ( moduleProtocolType == null ) {
            return;
        }
        moduleProtocolTypes.add( moduleProtocolType );
    }

    public boolean removeModuleProtocolType( ModuleProtocolType moduleProtocolType ) {
        if ( moduleProtocolType == null ) {
            return false;
        }
        return moduleProtocolTypes.remove( moduleProtocolType );
    }

    @Data
    @NoArgsConstructor
    private static class MulePluginConfiguration {
        public String classifier = "mule-application";
    }

    public Model createMavenPomModel() {

        log.debug("Start - Creating Maven POM Model");

        Model pomModel = new Model();

        pomModel.setModelVersion(MAVEN_MODEL_VERSION);
        pomModel.setGroupId(groupId);
        pomModel.setArtifactId(artifactId);
        pomModel.setVersion(version);
        pomModel.setPackaging(packaging);
        pomModel.setName(name);

        pomModel.setProperties( new Properties() );
        pomModel.getProperties().setProperty(PROP_SOURCE_ENCODING, sourceEncoding);
        pomModel.getProperties().setProperty(PROP_OUTPUT_ENCODING, outputEncoding);
        pomModel.getProperties().setProperty(PROP_APP_RUNTIME, appRuntimeVersion);
        pomModel.getProperties().setProperty(PROP_MULE_PLUGIN_VERSION, muleMavenPluginVersion);

        List<Plugin> buildPlugins = new ArrayList<Plugin>();
        {
            Plugin clean = new Plugin(), muleMaven = new Plugin(), compiler = new Plugin();
            clean.setGroupId("org.apache.maven.plugins");
            clean.setArtifactId("maven-clean-plugi");
            clean.setVersion(mavenCleanPluginVersion);

            muleMaven.setGroupId("org.mule.tools.maven");
            muleMaven.setArtifactId("mule-maven-plugin");
            muleMaven.setVersion("${mule.maven.plugin.version}");
            muleMaven.setExtensions(true);
            muleMaven.setConfiguration( new MulePluginConfiguration() );

            compiler.setGroupId("org.apache.maven.plugins");
            compiler.setArtifactId("maven-compiler-plugin");
            compiler.setVersion(mavenCompilerPluginVersion);

            buildPlugins.add( clean );
            buildPlugins.add( muleMaven );
            buildPlugins.add( compiler );
        }
        pomModel.setBuild( new Build() );
        pomModel.getBuild().setPlugins( buildPlugins );

        pomModel.setDependencies( new ArrayList<Dependency>() );
        for ( DependencyConfig dc : mavenPomConfigSettings.getDependencies() ) {
            if ( dc.getAppliesTo() == ModuleProtocolType.ALL || moduleProtocolTypes.contains( dc.getAppliesTo() ) ) {
                Dependency dependency = new Dependency();
                dependency.setGroupId(dc.getGroupId());
                dependency.setArtifactId(dc.getArtifactId());
                dependency.setVersion(dc.version);
                dependency.setClassifier("mule-plugin");
                pomModel.getDependencies().add( dependency );
            }
        }

        pomModel.setRepositories( new ArrayList<Repository>() );
        pomModel.setPluginRepositories( new ArrayList<Repository>() );
        for ( RepositoryConfig rc : mavenPomConfigSettings.getRepositories() ) {
            Repository repository = new Repository();
            repository.setId( rc.getId() );
            repository.setName( rc.getName() );
            repository.setUrl( rc.getUrl() );
            repository.setLayout("default");
            if ( rc.getPluginRepository() == null || rc.getPluginRepository() == false ) {
                pomModel.getRepositories().add( repository );
            }
            if ( rc.getPluginRepository() != null && rc.getPluginRepository() == true ) {
                RepositoryPolicy snapshots = new RepositoryPolicy();
                snapshots.setEnabled(false);
                repository.setSnapshots( snapshots );
                pomModel.getPluginRepositories().add( repository );
            }
        }
        this.mulePomModel = pomModel;
        log.info("Created Maven POM Model");
        return this.mulePomModel;
    }

    public Model getMulePomModel() {
        if ( this.mulePomModel == null ) {
            return this.createMavenPomModel();
        }
        return this.mulePomModel;
    }

    public void writeMulePomToFile( File dir, String fileName ) throws IOException {
        MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
        mavenXpp3Writer.setFileComment(
            "Mule Application POM generated from AsyncAPI"
        );
        Writer stringWriter = new StringWriter();
        mavenXpp3Writer.write( stringWriter, this.getMulePomModel() );
        FileUtils.writeStringToFile( stringWriter.toString(), dir, fileName);
        stringWriter.close();
        log.info( "Wrote Maven pom.xml to: {}", dir.getAbsolutePath() );
    }

    public static MavenPomConfigSettings getMavenPomConfigSettingsFromFile() {
        // TODO - Get the MavenPomConfigSettings from file
        MavenPomConfigSettings mavenPomConfigSettings = null;

        return mavenPomConfigSettings;
    }
}
