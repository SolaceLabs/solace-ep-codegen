/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.solace.ep.codegen.mapper.model;

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
    
    Map<String, SchemaInstance> schemaMap = new HashMap<>();

    MapConfig mapConfig = null;

    // Name-Value pairs
    Map<String, String> globalProperties = new HashMap<>();
    
    // Create one MapFlow for each Ingress (queue consumer and topic subscription) in the application
    List<MapFlow> mapFlows = new ArrayList<MapFlow>();

    // Create one MapSubFlowEgress for each Egress (publication) in the application
    List<MapSubFlowEgress> mapEgressSubFlows = new ArrayList<MapSubFlowEgress>();

}
