package com.solace.ep.mapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MapSubFlowBizLogic {
    
    protected String bizLogicFlowName;

    protected String bizLogicTransformPayload;

    protected MapFlowRef bizLogicFlowRef;

}
