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
package com.solace.ep.codegen.sap.iflow;

import java.io.InputStream;
import java.io.FileInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.solace.ep.codegen.sap.iflow.model.config.SapIflowExtensionConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Contants and utility functions for SapIFlowMapper
 */
@Slf4j
public class SapIflowUtils {
    

    public static final String
                DEFINITIONS_PREFIX              = "Definitions_",
                COLLAB_ID_PREFIX                = "Collaboration_",
                COLLAB_NAME_DEFAULT             = "Default Collaboration";

    public static final String
                PARTICIPANT_ID_PREFIX           = "Participant_",
                PARTICIPANT_NAME_SEND           = "EventMeshSender",
                PARTICIPANT_NAME_RECV           = "EventMeshReceiver",
                PARTICIPANT_NAME_SRC            = "SourceSystem",
                PARTICIPANT_NAME_DEST           = "DestinationSystemReceiver",
                PARTICIPANT_IFL_SEND            = "EndpointSender",
                PARTICIPANT_IFL_RECV            = "EndpointRecevier",
                PARTICIPANT_IFL_INT             = "IntegrationProcess",
                MSGFLOW_ID_PREFIX               = "MessageFlow_";
                
    public static final String
                PROCESS_ID_PREFIX               = "Process_",
                PROCESS_INB_NAME_TEMPLATE       = "Process inbound event from %s",
                PROCESS_INB_BL_NAME_TEMPLATE    = "Business Logic for %s event",
                PROCESS_OUT_GEN_NAME_TEMPLATE   = "Generate %s event",
                PROCESS_OUT_SEND_NAME_TEMPLATE  = "Process source data for %s event, send to Event Mesh",
                ACT_START_EVENT_PREFIX          = "StartEvent_",
                ACT_END_EVENT_PREFIX            = "EndEvent_",
                ACT_CALL_PREFIX                 = "CallActivity_",
                ACT_INB_END_NAME                = "Send to Destination Receiver",
                ACT_BL_TEMPLATE_NAME            = PROCESS_INB_BL_NAME_TEMPLATE,
                ACT_VALIDATE_SCHEMA_TEMPLATE    = "Validate %s schema",
                ACT_BL_START_NAME_TEMPLATE      = "From inbound %s process",
                ACT_BL_END_NAME_TEMPLATE        = "Return to %s process",
                ACT_BL_STUBMAP_TEMPLATE         = "Stub map for transforming source data to %s",

                ACT_SEND_START_NAME             = "This is where source connectivity and any enrichment would occur",
                ACT_SEND_END_NAME_TEMPLATE      = "Send %s to Event Mesh",
                ACT_SEND_GENERATE_NAME_TEMPLATE = "Generate %s event",
                ACT_SEND_STUBMAP_TEMPLATE       = ACT_BL_STUBMAP_TEMPLATE,

                ACT_GEN_START_NAME_TEMPLATE     = "From %s process",
                ACT_GEN_END_NAME_TEMPLATE       = "Return to %s process",
                ACT_GEN_EXTRACT_TOPIC_PREFIX    = "Extract ",
                ACT_GEN_COMPOSED_TOPIC_NAME     = "Generate composedTopic using runtime variables",
                ACT_GEN_VALIDATE_SCHEMA_TEMPLATE   = ACT_VALIDATE_SCHEMA_TEMPLATE;

    public static final String
                MAPPING_URI_TEMPLATE            = "dir://mmap/src/main/resources/mapping/%s.mmap",
                MAPPING_PATH_TEMPLATE           = "src/main/resources/mapping/";

    public static final String
                BPMN_EDGE_PREFIX                = "BPMNEdge_",
                BPMN_SHAPE_PREFIX               = "BPMNShape_";

    //
    public static final String
                SEQ_FLOW_PREFIX                 = "SequenceFlow_";

    /**
     * Parse extension-elements.yaml file containing the definitions for static SAP iFlow BPMN2 extensions
     * @param configFile
     * @return
     * @throws Exception
     */
    public static com.solace.ep.codegen.sap.iflow.model.config.SapIflowExtensionConfig parseExtensionConfig( String configFile ) throws Exception {

    	final ObjectMapper mapper = new ObjectMapper( new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES) );

        try (InputStream is = SapIflowUtils.class.getResourceAsStream( configFile.replace("src/main/resources", "") )) {
            return mapper.readValue( is, SapIflowExtensionConfig.class);
        } catch (Exception exc) {
            log.debug( "Failed to load config from the classpath, trying resources directory" );
            try (InputStream is = new FileInputStream( configFile )) {
                return mapper.readValue( is, SapIflowExtensionConfig.class);
            } catch ( Exception exc0 ) {
                log.error( "Could not retrieve SAP IFL Extension configuration from either the classpath or resources directory" );
                log.error( exc0.getMessage());
                throw exc0;
            }
        }
    }
}
