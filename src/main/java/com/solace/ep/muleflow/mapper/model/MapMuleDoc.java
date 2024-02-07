package com.solace.ep.muleflow.mapper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Intermediate structure representing a Mule Flow for mapping.
 * This object equates to a Mule Flow XML document
 */
@Data
@NoArgsConstructor
public class MapMuleDoc {
    
    Map<byte[], SchemaInstance> schemaMap = new HashMap<>();

    MapConfig mapConfig = null;

    // Name-Value pairs
    Map<String, String> globalProperties = new HashMap<>();
    
    // Create one MapFlow for each Ingress (queue consumer and topic subscription) in the application
    List<MapFlow> mapFlows = new ArrayList<MapFlow>();

    // Create one MapSubFlowEgress for each Egress (publication) in the application
    List<MapSubFlowEgress> mapEgressSubFlows = new ArrayList<MapSubFlowEgress>();

}
