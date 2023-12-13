package com.solace.ep.mule.model.base;

import java.util.UUID;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
    protected String docId = null;

    public void generateDocId() {
        this.docId = UUID.randomUUID().toString();
    }

    public void setDocNameAndGenerateDocId( String docName ) {
        this.docName = docName;
        this.generateDocId();
    }
}
