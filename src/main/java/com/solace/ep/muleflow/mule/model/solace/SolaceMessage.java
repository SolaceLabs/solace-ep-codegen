package com.solace.ep.muleflow.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SolaceMessage {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "messageType"
    )
    protected String messageType;

    public SolaceMessage( String messageType ) {
        this.messageType = messageType;
    }
}
