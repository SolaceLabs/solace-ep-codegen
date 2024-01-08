package com.solace.ep.mapper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MapSubFlowEgress {
    
    String egressFlowName;

    @Builder.Default
    List<MapSetVariable> setVariables = new ArrayList<MapSetVariable>();

    @Builder.Default
    MapValidateSchemaXml validateSchemaXml = null;

    @Builder.Default
    MapValidateSchemaJson validateSchemaJson = null;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MapSolacePublish {

        protected String publishAddress;

        protected String publishDocName;

        protected String publishConfigRef;

        protected String publishMessageType;
    }
}
