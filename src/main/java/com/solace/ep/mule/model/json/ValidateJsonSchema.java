package com.solace.ep.mule.model.json;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ValidateJsonSchema extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "schema"
    )
    protected String schemaLocation;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/json",
        localName = "contents"
    )
    @JacksonXmlCData
    protected String schemaContents;

}
