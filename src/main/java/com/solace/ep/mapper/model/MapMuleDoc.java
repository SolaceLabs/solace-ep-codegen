package com.solace.ep.mapper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapMuleDoc {
    
    MapConfig mapConfig = null;

    List<MapGlobalProperty> mapGlobalProperties = new ArrayList<MapGlobalProperty>();

    List<MapFlow> mapFlows = new ArrayList<MapFlow>();

    List<MapSubFlowEgress> mapEgressSubFlows = new ArrayList<MapSubFlowEgress>();

}
