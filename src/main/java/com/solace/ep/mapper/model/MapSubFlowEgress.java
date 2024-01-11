package com.solace.ep.mapper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapSubFlowEgress {
    
    String messageName;

    List<MapSetVariable> setVariables = new ArrayList<MapSetVariable>();

    MapValidateSchemaXml validateSchemaXml = new MapValidateSchemaXml();

    MapValidateSchemaJson validateSchemaJson = new MapValidateSchemaJson();

    MapSolacePublish mapSolacePublish = new MapSolacePublish();

    @Data
    @NoArgsConstructor
    public static class MapSolacePublish {

        protected String publishAddress;

        protected String publishMessageType;

        //  protected String publishConfigRef;
    }
}
