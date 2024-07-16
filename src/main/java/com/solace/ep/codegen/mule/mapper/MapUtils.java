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

package com.solace.ep.codegen.mule.mapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import com.solace.ep.codegen.internal.model.MapConfig;

/**
 * Class to containing constants and convenience methods to support MuleDocMapper class
 */
public class MapUtils {

    public static final String
            DEFAULT_ACKMODE = "AUTOMATIC_ON_FLOW_COMPLETION",
            DEFAULT_CONFIG_REF = "Solace_PubSub__Connector_Config",
            DEFAULT_SOLACE_CONFIG_NAME = "Solace_PubSub__Connector_Config",
            DEFAULT_SOLACE_CONFIG_DOC_NAME = "Solace PubSub+ Connector Config",
            DEFAULT_TRANSFORM_MESSAGE_DOC_NAME = "Transform Message";

    // public static final String 
    //                 DEFAULT_CONNECT_BROKER_HOST = "tcps://mr-connection-service.messaging.solace.cloud:55443",
    //                 DEFAULT_CONNECT_MSGVPN = "defaultVpn",
    //                 DEFAULT_CONNECT_CLIENT_USERNAME = "defaultClientUser",
    //                 DEFAULT_CONNECT_PASSWORD = "defaultPassword1",
    //                 DEFAULT_EP_CLOUD_API_TOKEN = "eySetYourCloudApiTokenHere";
    //
    public static final String
            DEFAULT_CONNECT_BROKER_HOST = "${solace.connection.brokerHost}",
            DEFAULT_CONNECT_MSGVPN = "${solace.connection.messageVpn}",
            DEFAULT_CONNECT_CLIENT_USERNAME = "${solace.connection.clientUsername}",
            DEFAULT_CONNECT_PASSWORD = "${solace.connection.clientPassword}",
            DEFAULT_EP_CLOUD_API_TOKEN = "${solace.eventPortal.apiToken}";
    //
    public static final String
            FLOW_REF_DOC_NAME_TO_BIZ_LOGIC = "Business Logic",
            FLOW_REF_DOC_NAME_TO_EGRESS = "Connect this to the correct egress flow";

    public static final String
            GLOBAL_PROPERTY_DOC_NAME = "Global Property",
            GLOBAL_NAME_EP_APP_VERSION_ID = "epApplicationVersionId",
            GLOBAL_NAME_EP_APP_VERSION_DESCRIPTION = "epApplicationVersionDescription",
            GLOBAL_NAME_EP_APP_VERSION = "epApplicationVersion",
            GLOBAL_NAME_EP_APP_VERSION_TITLE = "epApplicationVersionTitle",
            GLOBAL_PROPERTY_DEFAULT_ENV_VAR_NAME = "env",
            GLOBAL_PROPERTY_DEFAULT_ENV = "dev";
    //
    public static final String
            CONFIG_PROPERTY_DOC_NAME = "Configuration Properties File";

    public static final String
            MSG_TYPE_TEXT_MESSAGE = "TEXT_MESSAGE",
            MSG_TYPE_BYTES_MESSAGE = "BYTES_MESSAGE";

    public static final String
            TRANSFORM_MESSAGE_BIZLOGIC_STUB =
            "%dw 2.0\n" +
                    "output application/java\n" +
                    "---\n" +
                    "{\n}";

    protected static final String
            PATTERN_MULE_FLOW_NAME = "Ingress.%s.%s",
            PATTERN_MULE_FLOW_DOC_NAME = "%s Listener",
            PATTERN_BIZLOGIC_SUBFLOW_NAME = "BizLogic.%s.%s",
            PATTERN_EGRESS_SUBFLOW_NAME = "Egress.%s.%s",
            PATTERN_EGRESS_SUBFLOW_DOC_NAME = "Publish %s Event",
            PATTERN_SETVARIABLE_DOC_NAME_FOR_TOPIC_PARAMETER = "Dynamic Topic Parameter: %s",
            PATTERN_CONFIG_PROPERTY_FILE = "${%s}.yaml";

    private static final String
            TOPIC = "topic",
            QUEUE = "queue";
    //
    public static final String
            REGEX_SOLACE_TOPIC_CHAR_CLASS = "[" +
            "A-Za-z0-9" +
            "\\!\\#\\$\\&\\(\\)\\*\\+\\," +
            "\\-\\.\\:\\;\\=\\[\\]\\_\\~" +
            "]",
            REGEX_VAR_NODE = "^\\{(" + REGEX_SOLACE_TOPIC_CHAR_CLASS + "+)\\}$";
    //
    public static final Pattern
            PATTERN_VAR_NODE = Pattern.compile(REGEX_VAR_NODE);

    /**
     * Get Ingress flow name attribute from Designation (queueName or 'Direct Topic') and isDirectConsumer flag
     *
     * @param designation
     * @param isDirectConsumer
     * @return
     */
    public static String getFlowNameFromDesignation(String designation, boolean isDirectConsumer) {
        if (designation == null || designation.length() == 0) {
            designation = "UNKNOWN";
        }
        return String.format(PATTERN_MULE_FLOW_NAME, designation, (isDirectConsumer ? TOPIC : QUEUE));
    }

    /**
     * Get Ingress flow doc:name attribute from Designation (queueName or 'Direct Topic')
     *
     * @param designation
     * @return
     */
    public static String getFlowDocNameFromDesignation(String designation) {
        if (designation == null || designation.length() == 0) {
            designation = "UNKNOWN";
        }
        return String.format(PATTERN_MULE_FLOW_DOC_NAME, designation);
    }

    /**
     * Get BizLogic sub-flow name attribute from Designation (queueName or 'Direct Topic') and isDirectConsumer flag
     *
     * @param designation
     * @param isDirectConsumer
     * @return
     */
    public static String getBizLogicSubFlowNameFromDesignation(String designation, boolean isDirectConsumer) {
        if (designation == null || designation.length() == 0) {
            designation = "UNKNOWN";
        }
        return String.format(PATTERN_BIZLOGIC_SUBFLOW_NAME, designation, (isDirectConsumer ? TOPIC : QUEUE));
    }

    /**
     * Return static string defining stub transformation business logic
     *
     * @return
     */
    public static String getTransformMessageStub() {
        return TRANSFORM_MESSAGE_BIZLOGIC_STUB;
    }

    /**
     * Get Egress sub-flow name for a messageName
     * Assumes that the messageName is unique in the constructed MuleDoc
     */
    public static String getEgressSubFlowNameFromMessageName(String messageName) {
        if (messageName == null || messageName.length() == 0) {
            messageName = "OutputMessage";
        }
        return String.format(PATTERN_EGRESS_SUBFLOW_NAME, messageName.replace(" ", ""), TOPIC);
    }

    /**
     * Get Egress sub-flow doc:name for a message
     *
     * @param messageName
     * @return
     */
    public static String getEgressSubFlowDocNameFromMessageName(String messageName) {
        if (messageName == null || messageName.length() == 0) {
            messageName = "OutputMessage";
        }
        return String.format(PATTERN_EGRESS_SUBFLOW_DOC_NAME, messageName);
    }

    /**
     * Get set-variable doc:name attribute value for a topic parameter
     *
     * @param parameter
     * @return
     */
    public static String getSetVariableDocNameForTopicParameter(String parameter) {
        return String.format(PATTERN_SETVARIABLE_DOC_NAME_FOR_TOPIC_PARAMETER, parameter);
    }

    /**
     * Build and return default MapConfig object for Solace broker
     *
     * @return
     */
    public static MapConfig getDefaultSolaceConfiguration() {
        return MapConfig.builder()
                .configName(DEFAULT_CONFIG_REF)
                .connectBrokerHost(DEFAULT_CONNECT_BROKER_HOST)
                .connectMsgVpn(DEFAULT_CONNECT_MSGVPN)
                .connectClientUserName(DEFAULT_CONNECT_CLIENT_USERNAME)
                .connectPassword(DEFAULT_CONNECT_PASSWORD)
                .epCloudApiToken(DEFAULT_EP_CLOUD_API_TOKEN)
                .build();
    }

    /**
     * Get the 'file name' to use for Configuration Properties
     * file name contains environment variable token
     *
     * @param environmentString
     * @return
     */
    public static String getConfigPropertiesFileWithEnvToken(String environmentVariable) {
        return String.format(PATTERN_CONFIG_PROPERTY_FILE, environmentVariable);
    }

    /**
     * Get MD5 Digest of input string
     * Use to compare/sort Schema content
     *
     * @param value
     * @return
     */
    public static byte[] getMd5Digest(String value) {

        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            digest = md.digest(value.getBytes());
        } catch (NoSuchAlgorithmException nsaExc) {
        }

        return digest;
    }

    /**
     * Compare byte arrays
     * Use to compare MD5 hashes associated with schema content
     *
     * @param v1
     * @param v2
     * @return
     */
    public static boolean md5DigestMatches(byte[] v1, byte[] v2) {

        if (v1.length != v2.length) {
            return false;
        }
        for (int i = 0; i < v1.length; i++) {
            if (v1[i] != v2[i]) {
                return false;
            }
        }
        return true;
    }
}
