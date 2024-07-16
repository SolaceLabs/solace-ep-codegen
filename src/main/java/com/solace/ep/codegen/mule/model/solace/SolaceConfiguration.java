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

package com.solace.ep.codegen.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.codegen.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Builder
public class SolaceConfiguration extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "name"
    )
    protected String name;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "connection"
    )
    protected SolaceConnection solaceConnection;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "event-portal-config"
    )
    protected EventPortalConfiguration eventPortalConfiguration;

    public SolaceConfiguration() {
        this.solaceConnection = new SolaceConnection();
        this.eventPortalConfiguration = new EventPortalConfiguration();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventPortalConfiguration {

        @JacksonXmlProperty(
            isAttribute = true,
            namespace = "",
            localName = "cloudApiToken"
        )
        protected String cloudApiToken;

        @JacksonXmlProperty(
            isAttribute = true,
            namespace = "",
            localName = "cloudOrgPrefix"
        )
        protected String cloudOrgPrefix;

        public EventPortalConfiguration( String cloudApiToken ) {
            this.cloudApiToken = cloudApiToken;
        }
    }
}
