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

/**
 * Static content with tokens to find/replace to create project-specific files
 */
public class MuleProjectContent {

    // TODO - Replace POM and LOG4J static content with a content builder
    
    public static final String
                FILE_MULE_ARTIFACT__JSON = "mule-artifact.json",
                FILE_LOG4J2_TEST__XML = "log4j2-test.xml",
                FILE_LOG4J2__XML = "log4j2.xml",
                FILE_HOME__MD = "home.md",
                FILE_POM__XML = "pom.xml",
                FILE_GLOBAL_CONFIGS = "Global-Configs";

    public static final String
                TOKEN_FLOW_NAME = "___FLOW_NAME___",
                TOKEN_ARTIFACT_ID = "___ARTIFACT_ID___",
                TOKEN_GROUP_ID = "___GROUP_ID___",
                TOKEN_VERSION = "___VERSION___",
                TOKEN_CLOUD_HUB_DEPLOYMENT = "___CLOUD_HUB_DEPLOYMENT___";
    //
    public static final String
        CONTENT_CONFIG_PROPERTIES =
        "solace:\n" + //
        "  connection:\n" + //
        "    brokerHost: tcps://mr-your-service.messaging.solace.cloud:55443\n" + //
        "    messageVpn: defaultVpn\n" + //
        "    clientUsername: defaultUser\n" + //
        "    clientPassword: defaultPassword\n" +
        "  eventPortal:\n" +
        "    apiToken: eySetYourCloudApiTokenHere\n";

    public static final String
        CONTENT_MULE_ARTIFACT__JSON = 
        "{\n" + //
        "  \"minMuleVersion\": \"4.4.0\"\n" + //
        "}";

    /**
     * Get the actual name of the properties configuration file given the environment string
     * @param environmentString
     * @return
     */
    public static String getConfigurationPropertiesFileName( String environmentString ) {
        return ( environmentString != null ? environmentString : "UNKNOWN" ) + ".yaml";
    }

    public static final String
        CONTENT_LOG4J2_TEST__XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
        "<Configuration>\n" + //
        "\n" + //
        "    <Appenders>\n" + //
        "        <Console name=\"Console\" target=\"SYSTEM_OUT\">\n" + //
        "            <PatternLayout pattern=\"%-5p %d [%t] %c: %m%n\"/>\n" + //
        "        </Console>\n" + //
        "    </Appenders>\n" + //
        "\n" + //
        "    <Loggers>\n" + //
        "        <!-- Http Logger shows wire traffic on DEBUG -->\n" + //
        "        <!--AsyncLogger name=\"org.mule.service.http.impl.service.HttpMessageLogger\" level=\"DEBUG\"/-->\n" + //
        "        <AsyncLogger name=\"org.mule.service.http\" level=\"WARN\"/>\n" + //
        "        <AsyncLogger name=\"org.mule.extension.http\" level=\"WARN\"/>\n" + //
        "\n" + //
        "        <!-- Reduce startup noise -->\n" + //
        "        <AsyncLogger name=\"com.mulesoft.mule.runtime.plugin\" level=\"WARN\"/>\n" + //
        "        <AsyncLogger name=\"org.mule.maven.client\" level=\"WARN\"/>\n" + //
        "        <AsyncLogger name=\"org.mule.runtime.core.internal.util\" level=\"WARN\"/>\n" + //
        "        <AsyncLogger name=\"org.quartz\" level=\"WARN\"/>\n" + //
        "        <AsyncLogger name=\"org.mule.munit.plugins.coverage.server\" level=\"WARN\"/>\n" + //
        "\n" + //
        "        <!-- Mule logger -->\n" + //
        "        <AsyncLogger name=\"org.mule.runtime.core.internal.processor.LoggerMessageProcessor\" level=\"INFO\"/>\n" + //
        "\n" + //
        "        <AsyncRoot level=\"INFO\">\n" + //
        "            <AppenderRef ref=\"Console\"/>\n" + //
        "        </AsyncRoot>\n" + //
        "    </Loggers>\n" + //
        "\n" + //
        "</Configuration>\n";

    //
    public static final String
        CONTENT_LOG4J2__XML = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + //
        "<Configuration>\n" + //
        "\n" + //
        "    <!--These are some of the loggers you can enable. \n" + //
        "        There are several more you can find in the documentation. \n" + //
        "        Besides this log4j configuration, you can also use Java VM environment variables\n" + //
        "        to enable other logs like network (-Djavax.net.debug=ssl or all) and \n" + //
        "        Garbage Collector (-XX:+PrintGC). These will be append to the console, so you will \n" + //
        "        see them in the mule_ee.log file. -->\n" + //
        "\n" + //
        "    <Appenders>\n" + //
        "        <RollingFile name=\"file\" fileName=\"${sys:mule.home}${sys:file.separator}logs${sys:file.separator}___FLOW_NAME___.log\"\n" + //
        "                 filePattern=\"${sys:mule.home}${sys:file.separator}logs${sys:file.separator}___FLOW_NAME___-%i.log\">\n" + //
        "            <PatternLayout pattern=\"%-5p %d [%t] [processor: %X{processorPath}; event: %X{correlationId}] %c: %m%n\"/>\n" + //
        "            <SizeBasedTriggeringPolicy size=\"10 MB\"/>\n" + //
        "            <DefaultRolloverStrategy max=\"10\"/>\n" + //
        "        </RollingFile>\n" + //
        "    </Appenders>\n" + //
        "\n" + //
        "    <Loggers>\n" + //
        "        <!-- Http Logger shows wire traffic on DEBUG -->\n" + //
        "        <!--AsyncLogger name=\"org.mule.service.http.impl.service.HttpMessageLogger\" level=\"DEBUG\"/-->\n" + //
        "        <AsyncLogger name=\"org.mule.service.http\" level=\"WARN\"/>\n" + //
        "        <AsyncLogger name=\"org.mule.extension.http\" level=\"WARN\"/>\n" + //
        "\n" + //
        "        <!-- Mule logger -->\n" + //
        "        <AsyncLogger name=\"org.mule.runtime.core.internal.processor.LoggerMessageProcessor\" level=\"INFO\"/>\n" + //
        "\n" + //
        "        <AsyncRoot level=\"INFO\">\n" + //
        "            <AppenderRef ref=\"file\"/>\n" + //
        "        </AsyncRoot>\n" + //
        "    </Loggers>\n" + //
        "\n" + //
        "</Configuration>";

    //
    public static final String
        CONTENT_HOME__MD =
                "# Home.md\n" + //
                        "\n" + //
                        "Auto-generated\n";
    //

    public static final String
        CONTENT_CLOUDHUB_DEPLOYMENT =
                "\t\t\t\t\t<cloudHubDeployment>\n" + //
                        "\t\t\t\t\t\t<uri>https://anypoint.mulesoft.com</uri>\n" + //
                        "\t\t\t\t\t\t<muleVersion>${app.runtime}</muleVersion>\n" + //
                        "\t\t\t\t\t\t<username>${anypoint.username}</username>\n" + //
                        "\t\t\t\t\t\t<password>${anypoint.password}</password>\n" + //
                        "\t\t\t\t\t\t<applicationName>${app.name}</applicationName>\n" + //
                        "\t\t\t\t\t\t<environment>${env}</environment>\n" + //
                        "\t\t\t\t\t\t<workerType>${app.workerType:MICRO}</workerType>\n" + //
                        "\t\t\t\t\t\t<region>${app.region:us-east-2}</region>\n" + //
                        "\t\t\t\t\t\t<workers>${app.workers:1}</workers>\n" + //
                        "\t\t\t\t\t\t<objectStoreV2>${app.objectstorev2:true}</objectStoreV2>\n" + //
                        "\t\t\t\t\t</cloudHubDeployment>\n";

    //
    public static final String
        CONTENT_POM__XML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + //
                        "\t<modelVersion>4.0.0</modelVersion>\n" + //
                        "\n" + //
                        "\t<groupId>___GROUP_ID___</groupId>\n" + //
                        "\t<artifactId>___ARTIFACT_ID___</artifactId>\n" + //
                        "\t<version>___VERSION___</version>\n" + //
                        "\t<packaging>mule-application</packaging>\n" + //
                        "\n" + //
                        "\t<name>___ARTIFACT_ID___</name>\n" + //
                        "\n" + //
                        "\t<properties>\n" + //
                        "\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" + //
                        "\t\t<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>\n" + //
                        "\n" + //
                        "\t\t<app.runtime>4.4.0-20231110</app.runtime>\n" + //
                        "\t\t<mule.maven.plugin.version>4.0.0</mule.maven.plugin.version>\n" + //
                        "\t</properties>\n" + //
                        "\n" + //
                        "\t<build>\n" + //
                        "\t\t<plugins>\n" + //
                        "\t\t\t<plugin>\n" + //
                        "\t\t\t\t<groupId>org.apache.maven.plugins</groupId>\n" + //
                        "\t\t\t\t<artifactId>maven-clean-plugin</artifactId>\n" + //
                        "\t\t\t\t<version>3.2.0</version>\n" + //
                        "\t\t\t</plugin>\n" + //
                        "\t\t\t<plugin>\n" + //
                        "\t\t\t\t<groupId>org.mule.tools.maven</groupId>\n" + //
                        "\t\t\t\t<artifactId>mule-maven-plugin</artifactId>\n" + //
                        "\t\t\t\t<version>${mule.maven.plugin.version}</version>\n" + //
                        "\t\t\t\t<extensions>true</extensions>\n" + //
                        "\t\t\t\t<configuration>\n" + //
                        "___CLOUD_HUB_DEPLOYMENT___" + //
                        "\t\t\t\t\t<classifier>mule-application</classifier>\n" + //
                        "\t\t\t\t</configuration>\n" + //
                        "\t\t\t</plugin>\n" + //
                        "\t\t</plugins>\n" + //
                        "\t</build>\n" + //
                        "\n" + //
                        "\t<dependencies>\n" + //
                        "\t\t<dependency>\n" + //
                        "\t\t\t<groupId>org.mule.connectors</groupId>\n" + //
                        "\t\t\t<artifactId>mule-http-connector</artifactId>\n" + //
                        "\t\t\t<version>1.8.0</version>\n" + //
                        "\t\t\t<classifier>mule-plugin</classifier>\n" + //
                        "\t\t</dependency>\n" + //
                        "\t\t<dependency>\n" + //
                        "\t\t\t<groupId>org.mule.connectors</groupId>\n" + //
                        "\t\t\t<artifactId>mule-sockets-connector</artifactId>\n" + //
                        "\t\t\t<version>1.2.3</version>\n" + //
                        "\t\t\t<classifier>mule-plugin</classifier>\n" + //
                        "\t\t</dependency>\n" + //
                        "\t\t<dependency>\n" + //
                        "\t\t\t<groupId>com.solace.connector</groupId>\n" + //
                        "\t\t\t<artifactId>solace-mulesoft-connector</artifactId>\n" + //
                        "\t\t\t<version>1.3.1</version>\n" + //
                        "\t\t\t<classifier>mule-plugin</classifier>\n" + //
                        "\t\t</dependency>\n" + //
                        "\t\t<dependency>\n" + //
                        "\t\t\t<groupId>org.mule.modules</groupId>\n" + //
                        "\t\t\t<artifactId>mule-xml-module</artifactId>\n" + //
                        "\t\t\t<version>1.4.2</version>\n" + //
                        "\t\t\t<classifier>mule-plugin</classifier>\n" + //
                        "\t\t</dependency>\n" + //
                        "\t\t<dependency>\n" + //
                        "\t\t\t<groupId>org.mule.modules</groupId>\n" + //
                        "\t\t\t<artifactId>mule-json-module</artifactId>\n" + //
                        "\t\t\t<version>2.4.2</version>\n" + //
                        "\t\t\t<classifier>mule-plugin</classifier>\n" + //
                        "\t\t</dependency>\n" + //
                        "\t</dependencies>\n" + //
                        "\n" + //
                        "\t<repositories>\n" + //
                        "\t\t<repository>\n" + //
                        "\t\t\t<id>anypoint-exchange-v3</id>\n" + //
                        "\t\t\t<name>Anypoint Exchange</name>\n" + //
                        "\t\t\t<url>https://maven.anypoint.mulesoft.com/api/v3/maven</url>\n" + //
                        "\t\t\t<layout>default</layout>\n" + //
                        "\t\t</repository>\n" + //
                        "\t\t<repository>\n" + //
                        "\t\t\t<id>mulesoft-releases</id>\n" + //
                        "\t\t\t<name>MuleSoft Releases Repository</name>\n" + //
                        "\t\t\t<url>https://repository.mulesoft.org/releases/</url>\n" + //
                        "\t\t\t<layout>default</layout>\n" + //
                        "\t\t</repository>\n" + //
                        "\t</repositories>\n" + //
                        "\n" + //
                        "\t<pluginRepositories>\n" + //
                        "\t\t<pluginRepository>\n" + //
                        "\t\t\t<id>mulesoft-releases</id>\n" + //
                        "\t\t\t<name>MuleSoft Releases Repository</name>\n" + //
                        "\t\t\t<layout>default</layout>\n" + //
                        "\t\t\t<url>https://repository.mulesoft.org/releases/</url>\n" + //
                        "\t\t\t<snapshots>\n" + //
                        "\t\t\t\t<enabled>false</enabled>\n" + //
                        "\t\t\t</snapshots>\n" + //
                        "\t\t</pluginRepository>\n" + //
                        "\t</pluginRepositories>\n" + //
                        "\n" + //
                        "</project>\n" + //
                        "";
}
