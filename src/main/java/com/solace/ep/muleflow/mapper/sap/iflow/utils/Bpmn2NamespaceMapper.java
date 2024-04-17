package com.solace.ep.muleflow.mapper.sap.iflow.utils;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

public class Bpmn2NamespaceMapper extends NamespacePrefixMapper {
    
    private Map<String, String> namespaceMap = new HashMap<>();
    
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
