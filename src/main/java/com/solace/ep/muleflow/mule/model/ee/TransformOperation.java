package com.solace.ep.muleflow.mule.model.ee;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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

import com.solace.ep.muleflow.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransformOperation extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/ee/core",
        localName = "message"
    )
    protected TransformMessage transformMessage;

    public TransformOperation( String transformDocName, String transformMessagePayload ) {
        this.setDocName( docName );
        this.transformMessage = new TransformMessage();
        this.transformMessage.setSetPayload( transformMessagePayload );
    }

    @Data
    @NoArgsConstructor
    public static class TransformMessage {

        @JacksonXmlProperty(
            isAttribute = false,
            namespace = "http://www.mulesoft.org/schema/mule/ee/core",
            localName = "set-payload"
        )
        @JacksonXmlCData
        protected String setPayload;

    }
}
