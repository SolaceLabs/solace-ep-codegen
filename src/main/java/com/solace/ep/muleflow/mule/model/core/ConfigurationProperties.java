package com.solace.ep.muleflow.mule.model.core;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ConfigurationProperties extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "file"
    )
    protected String file;

    public ConfigurationProperties( String fileName, String docName ) {
        this.file = fileName;
        this.setDocName(docName);
    }

}
