package com.solace.ep.muleflow.mule.model.base;

import java.util.UUID;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "doc:name"
        // namespace = "http://www.mulesoft.org/schema/mule/documentation",
        // localName = "name"
    )
    protected String docName = null;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "doc:id"
        // namespace = "http://www.mulesoft.org/schema/mule/documentation",
        // localName = "id"
    )
    protected String docId = UUID.randomUUID().toString();

}
