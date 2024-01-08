package com.solace.ep.mapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapFlow {
    
    protected String flowName;

    protected MapSolaceQueueListener flowListener;

    protected MapValidateSchemaJson flowMapValidateSchemaJson;

    protected MapValidateSchemaXml flowMapValidateSchemaXml;

    protected MapFlowRef flowRef;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapSolaceQueueListener {

        protected String listenerAddress;

        protected String listenerAckMode;

        protected String listenerDocName;

        protected String listenerConfigRef;
    }
}
