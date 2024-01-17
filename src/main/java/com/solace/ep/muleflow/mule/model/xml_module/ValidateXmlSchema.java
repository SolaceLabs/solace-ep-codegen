package com.solace.ep.muleflow.mule.model.xml_module;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ValidateXmlSchema extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "schema"
    )
    protected String schemaLocation;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/xml-module",
        localName = "contents"
    )
    @JacksonXmlCData
    protected String schemaContents;

}
