package com.solace.ep.muleflow.mapper.sap.iflow.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "iflProperty", propOrder = {
    "key",
    "value"
})
public class TSapIflowProperty {
    
    @XmlElement(required = true)
    protected String key;
    @XmlElement( required = true )
    protected String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
