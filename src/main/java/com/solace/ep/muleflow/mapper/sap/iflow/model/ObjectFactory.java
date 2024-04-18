package com.solace.ep.muleflow.mapper.sap.iflow.model;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;

public class ObjectFactory {
    
    private final static QName _SapIflowProperty_QNAME = new QName("http:///com.sap.ifl.model/Ifl.xsd", "property");
    private final static QName _SapIflowType_QNAME = new QName("http:///com.sap.ifl.model/Ifl.xsd", "type");

    public TSapIflowProperty createTSapIflowProperty() {
        return new TSapIflowProperty();
    }

    @XmlElementDecl(namespace = "http:///com.sap.ifl.model/Ifl.xsd", name = "process", substitutionHeadNamespace = "http:///com.sap.ifl.model/Ifl.xsd", substitutionHeadName = "property")
    public JAXBElement<TSapIflowProperty> createProperty(TSapIflowProperty value) {
        return new JAXBElement<TSapIflowProperty>(_SapIflowProperty_QNAME, TSapIflowProperty.class, null, value);
    }

    public QName getSapIflowType_QName() {
        return _SapIflowType_QNAME;
    }

}
