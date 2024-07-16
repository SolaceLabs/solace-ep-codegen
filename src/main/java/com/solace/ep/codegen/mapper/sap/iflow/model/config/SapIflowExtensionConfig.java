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
package com.solace.ep.codegen.mapper.sap.iflow.model.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class SapIflowExtensionConfig {
    
    @JsonProperty
    List<ExtProperty> collaboration;

    @JsonProperty
    ParticipantExt participant;

    @JsonProperty
    MessageFlowExt messageFlow;

    @JsonProperty
    ProcessExt inboundProcess;

    @JsonProperty
    ProcessExt outboundProcess;

    @JsonProperty
    ProcessExt calledProcess;

    @JsonProperty
    CallActivityExt callActivity;

    @Data
    @NoArgsConstructor
    public static class MessageFlowExt {

        @JsonProperty
        List<ExtProperty> allMessageFlows;

        @JsonProperty
        List<ExtProperty> publication;

        @JsonProperty
        List<ExtProperty> subscription;

    }

    @Data
    @NoArgsConstructor
    public static class ParticipantExt {

        @JsonProperty
        List<ExtProperty> eventMeshSender;

        @JsonProperty
        List<ExtProperty> eventMeshReceiver;

        @JsonProperty
        List<ExtProperty> sourceSystem;

        @JsonProperty
        List<ExtProperty> destinationSystemReceiver;
    }

    @Data
    @NoArgsConstructor
    public static class ProcessExt {

        @JsonProperty
        List<ExtProperty> processExtensions;

        @JsonProperty
        List<ExtProperty> startEvent;

        @JsonProperty
        List<ExtProperty> endEvent;
    }

    @Data
    @NoArgsConstructor
    public static class CallActivityExt {

        @JsonProperty
        List<ExtProperty> mapping;

        @JsonProperty
        List<ExtProperty> callProcess;

        // TODO - Remove field, also remove topicAssembly element from extension-elements.yaml
        @JsonProperty
        List<ExtProperty> topicAssembly;

        @JsonProperty
        List<ExtProperty> defineTopic;

        @JsonProperty
        List<ExtProperty> groovyScript;
    }

    @Data
    @NoArgsConstructor
    public static class ExtProperty {

        @JsonProperty
        @NonNull
        protected String key;

        @JsonProperty
        protected String value;

    }
}
