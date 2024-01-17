package com.solace.ep.muleflow.mule.model.base;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "key",
    "value"
})
public class KeyValuePair {
    
    @JacksonXmlProperty(
        isAttribute = true,
        namespace = "",
        localName = "key"
    )
    protected String key;

    @JacksonXmlProperty(
        isAttribute = true,
        namespace = "",
        localName = "value"
    )
    protected String value;

}
