package com.solace.ep.mapper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapFlow {
    
    protected String flowDesignation;

    protected boolean isDirectConsumer = false;

    protected MapSolaceQueueListener flowQueueListener;

    protected MapSolaceTopicListener flowTopicListener;

    protected MapValidateSchemaJson flowMapValidateSchemaJson;

    protected MapValidateSchemaXml flowMapValidateSchemaXml;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapSolaceQueueListener {

        protected String listenerAddress;

        protected String listenerAckMode;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapSolaceTopicListener {

        protected List<String> listenerTopics = new ArrayList<>();

        protected String listenerContentType;

        protected String listenerEncoding;

    }
}
