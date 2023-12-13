package com.solace.ep.mule.model.core;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class GlobalProperty extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "name"
    )
    protected String name;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "value"
    )
    protected String value;

    public GlobalProperty( String name, String value, String docName ) {
        this.setPropertyNameValue(name, value);
        super.setDocNameAndGenerateDocId(docName);
    }

    public void setPropertyNameValue( String name, String value ) {
        this.name = name;
        this.value = value;
    }
}
