package com.solace.ep.muleflow.mule.model.solace;

import java.util.ArrayList;
import java.util.List;

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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.KeyValuePair;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolaceConnection {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "brokerHost"
    )
    protected String brokerHost;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "msgVPN"
    )
    protected String msgVpn;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "clientUserName"
    )
    protected String clientUserName;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "password"
    )
    protected String password;

    @JacksonXmlElementWrapper(
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "jcsmp-properties",
        useWrapping = true
    )
    @JacksonXmlProperty(
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "jcsmp-property"
    )
    protected List<KeyValuePair> jcsmpProperties;

    public List<KeyValuePair> getJcsmpProperties() {
        if (jcsmpProperties == null) {
            jcsmpProperties = new ArrayList<KeyValuePair>();
        }
        return jcsmpProperties;
    }
}
