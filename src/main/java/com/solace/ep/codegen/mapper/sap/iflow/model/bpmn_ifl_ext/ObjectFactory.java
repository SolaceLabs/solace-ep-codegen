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
package com.solace.ep.codegen.mapper.sap.iflow.model.bpmn_ifl_ext;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;

public class ObjectFactory {
    
    private final static QName _SapIflowProperty_QNAME = new QName("http://com.sap.ifl.model/Ifl.xsd", "property");
    private final static QName _SapIflowType_QNAME = new QName("http://com.sap.ifl.model/Ifl.xsd", "type");

    public TSapIflowProperty createTSapIflowProperty() {
        return new TSapIflowProperty();
    }

    @XmlElementDecl(namespace = "http://com.sap.ifl.model/Ifl.xsd", name = "iflProperty", substitutionHeadNamespace = "http://com.sap.ifl.model/Ifl.xsd", substitutionHeadName = "property")
    public JAXBElement<TSapIflowProperty> createProperty(TSapIflowProperty value) {
        return new JAXBElement<TSapIflowProperty>(_SapIflowProperty_QNAME, TSapIflowProperty.class, null, value);
    }

    public QName getSapIflowType_QName() {
        return _SapIflowType_QNAME;
    }

}
