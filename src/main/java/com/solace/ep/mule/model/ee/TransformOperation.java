package com.solace.ep.mule.model.ee;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransformOperation extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/ee/core",
        localName = "message"
    )
    protected TransformMessage transformMessage;

    public TransformOperation( String transformDocName, String transformMessagePayload ) {
        this.setDocName( docName );
        this.transformMessage = new TransformMessage();
        this.transformMessage.setSetPayload( transformMessagePayload );
    }

    @Data
    @NoArgsConstructor
    public static class TransformMessage {

        @JacksonXmlProperty(
            isAttribute = false,
            namespace = "http://www.mulesoft.org/schema/mule/ee/core",
            localName = "set-payload"
        )
        @JacksonXmlCData
        protected String setPayload;

    }
}
