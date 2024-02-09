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

package com.solace.ep.muleflow.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SolaceTopicListener extends BaseElement {
    
    /* 
        <solace:topic-listener 
            doc:name="Direct Topic Subscriber" 
            doc:id="6a68b16f-7d08-45cb-b595-abd6ec10c26a" 
            config-ref="Solace_PubSub__Connector_Config" 
            topics="this/is/a/topic, and/another/&gt;" 
            contentType="application/json" 
            encoding="utf-8"/>
    */

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "config-ref"
    )
    protected String configRef;
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "topics"
    )
    protected String topics;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "contentType"
    )
    protected String contentType;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "encoding"
    )
    protected String encoding;

}
