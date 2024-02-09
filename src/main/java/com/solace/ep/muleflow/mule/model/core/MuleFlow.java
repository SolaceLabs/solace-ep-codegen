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

package com.solace.ep.muleflow.mule.model.core;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;
import com.solace.ep.muleflow.mule.model.ee.TransformOperation;
import com.solace.ep.muleflow.mule.model.json.ValidateJsonSchema;
import com.solace.ep.muleflow.mule.model.solace.*;
import com.solace.ep.muleflow.mule.model.xml_module.ValidateXmlSchema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class MuleFlow extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "set-variable"
    )
    @JacksonXmlElementWrapper( useWrapping = false )
    protected List<SetVariable> setVariable;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "queue-listener"
    )
    protected SolaceQueueListener queueListener;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "topic-listener"
    )
    protected SolaceTopicListener topicListener;

    @JacksonXmlProperty(
        isAttribute = false,
//        namespace = "http://www.mulesoft.org/schema/mule/xml-module",
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "xml-module:validate-schema"
    )
    protected ValidateXmlSchema validateXmlSchema;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/json",
        localName = "validate-schema"
    )
    protected ValidateJsonSchema validateJsonSchema;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/ee/core",
        localName = "transform"
    )
    protected TransformOperation transform;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "publish"
    )
    protected SolacePublish publish;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "flow-ref"
    )
    protected MuleFlowRef flowRef;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "name"
    )
    protected String name;

    public List<SetVariable> getSetVariable() {
        if (this.setVariable == null) {
            this.setVariable = new ArrayList<SetVariable>();
        }
        return this.setVariable;
    }
}
