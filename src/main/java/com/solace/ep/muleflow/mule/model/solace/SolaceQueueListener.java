package com.solace.ep.muleflow.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SolaceQueueListener extends BaseElement {

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "selector"
    )
    protected String selector;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "ackMode"
    )
    protected String ackMode;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "address"
    )
    protected String address;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "provisionQueueWithOptionalTopicSubscription"
    )
    protected String provisionQueueWithOptionalTopicSubscription;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "addTopicSubscription"
    )
    protected String addTopicSubscription;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "config-ref"
    )
    protected String configRef;

}
