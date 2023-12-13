package com.solace.ep.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SolacePublish extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "address"
    )
    protected String address;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "destinationType"
    )
    protected String destinationType;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "config-ref"
    )
    protected String configRef;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "message"
    )
    protected SolaceMessage message;

}
