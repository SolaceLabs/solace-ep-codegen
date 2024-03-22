package com.solace.ep.muleflow.mapper.asyncapi.solace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.solace.ep.muleflow.asyncapi.AsyncApiMessage;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AsyncApiSolaceBindingMapper {
    
    @JsonProperty
    protected String destinationType;

    @JsonProperty
    protected TopicOrQueue topic;

    @JsonProperty
    protected TopicOrQueue queue;

    @JsonIgnore
    protected Set<AsyncApiMessage> messages = new HashSet<>();

    @JsonIgnore
    protected boolean directConsumer = false;

    @Data
    @NoArgsConstructor
    public static class TopicOrQueue {

        @JsonProperty
        protected String name;

        @JsonProperty
        protected List<String> topicSubscriptions = new ArrayList<>();
        
    }
}
