package com.solace.ep.muleflow.mapper.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapSubFlowEgress {
    
    protected String messageName;

    protected Map<String, String> setVariables = new HashMap<>();

    protected String jsonSchemaContent = null;

    protected String xmlSchemaContent = null;

    protected String publishAddress;

    protected boolean publishToQueue = false;

}
