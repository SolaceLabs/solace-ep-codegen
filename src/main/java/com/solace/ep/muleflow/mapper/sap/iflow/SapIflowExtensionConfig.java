package com.solace.ep.muleflow.mapper.sap.iflow;

import java.util.ArrayList;
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

        @JsonProperty
        List<ExtProperty> topicAssembly;
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
