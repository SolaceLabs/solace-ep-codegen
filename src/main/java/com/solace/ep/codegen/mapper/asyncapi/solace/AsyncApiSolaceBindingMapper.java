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

package com.solace.ep.codegen.mapper.asyncapi.solace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.solace.ep.codegen.asyncapi.AsyncApiMessage;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AsyncApiSolaceBindingMapper {
    
    @JsonProperty
    protected String destinationType;

    @JsonProperty
    protected TopicOrQueue topic;

    @JsonProperty
    protected TopicOrQueue queue;

    @JsonIgnore
    protected Set<AsyncApiMessage> messages = new HashSet<>();

    @JsonIgnore
    protected boolean directConsumer = false;

    @Data
    @NoArgsConstructor
    public static class TopicOrQueue {

        @JsonProperty
        protected String name;

        @JsonProperty
        protected List<String> topicSubscriptions = new ArrayList<>();
        
    }
}
