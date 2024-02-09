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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MuleFlowRef extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "name"
    )
    protected String name;

    public MuleFlowRef( String name ) {
        this.name = name;
    }

    public MuleFlowRef( String name, String docName ) {
        this.name = name;
        this.setDocName(docName);
//        super.setDocNameAndGenerateDocId(docName);
    }
}
