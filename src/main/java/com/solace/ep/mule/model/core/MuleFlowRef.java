package com.solace.ep.mule.model.core;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

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
        super.setDocNameAndGenerateDocId(docName);
    }
}
