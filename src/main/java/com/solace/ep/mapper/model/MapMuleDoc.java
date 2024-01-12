package com.solace.ep.mapper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapMuleDoc {
    
    MapConfig mapConfig = null;

    Map<String, String> globalProperties = new HashMap<>();
    
    List<MapFlow> mapFlows = new ArrayList<MapFlow>();

    List<MapSubFlowEgress> mapEgressSubFlows = new ArrayList<MapSubFlowEgress>();

}
