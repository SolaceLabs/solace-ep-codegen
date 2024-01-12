package com.solace.ep.mule.model.core;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SetVariable extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "variableName"
    )
    protected String variableName;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "value"
    )
    protected String value;

    public SetVariable( String variableName, String value ) {
        this.variableName = variableName;
        this.value = value;
    }

    public SetVariable( String variableName, String value, String docName ) {
        this( variableName, value );
        this.setDocName(docName);
//        super.setDocNameAndGenerateDocId(docName);
    }
}
