package com.solace.ep.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SolaceTopicListener extends BaseElement {
    
    /* 
        <solace:topic-listener 
            doc:name="Direct Topic Subscriber" 
            doc:id="6a68b16f-7d08-45cb-b595-abd6ec10c26a" 
            config-ref="Solace_PubSub__Connector_Config" 
            topics="this/is/a/topic, and/another/&gt;" 
            contentType="application/json" 
            encoding="utf-8"/>
    */

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "config-ref"
    )
    protected String configRef;
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "topics"
    )
    protected String topics;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "contentType"
    )
    protected String contentType;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "encoding"
    )
    protected String encoding;

}
