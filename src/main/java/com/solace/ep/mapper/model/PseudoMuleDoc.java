package com.solace.ep.mapper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PseudoMuleDoc {
    
    MapConfig mapConfig;

    List<MapGlobalProperty> mapGlobalProperties = new ArrayList<MapGlobalProperty>();

    List<MapFlow> mapFlows = new ArrayList<MapFlow>();

    List<MapSubFlowBizLogic> mapBizLogicSubFlows = new ArrayList<MapSubFlowBizLogic>();

    List<MapSubFlowEgress> mapEgressSubFlows = new ArrayList<MapSubFlowEgress>();

}
