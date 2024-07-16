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

package com.solace.ep.codegen.sap.iflow.utils;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

/**
 * Output SAP IFlow Doc with expected namespaces
 */
public class Bpmn2NamespaceMapper extends NamespacePrefixMapper {
    
    private final Map<String, String> namespaceMap = new HashMap<>();
    
    public Bpmn2NamespaceMapper() {
        namespaceMap.put("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2");
        namespaceMap.put("http://www.omg.org/spec/BPMN/20100524/DI", "bpmndi");
        namespaceMap.put("http://www.omg.org/spec/DD/20100524/DC", "dc");
        namespaceMap.put("http://www.omg.org/spec/DD/20100524/DI", "di");
        namespaceMap.put("http:///com.sap.ifl.model/Ifl.xsd", "ifl");
        namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        namespaceMap.put("", "");
        namespaceMap.put(null, null);
    }

    @Override
    public String getPreferredPrefix( String namespaceUri, String suggestion, boolean required ) {
        return namespaceMap.getOrDefault(namespaceUri, suggestion);
    }
}
