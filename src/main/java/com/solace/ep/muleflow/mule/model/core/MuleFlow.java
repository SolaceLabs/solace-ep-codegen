package com.solace.ep.muleflow.mule.model.core;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;
import com.solace.ep.muleflow.mule.model.ee.TransformOperation;
import com.solace.ep.muleflow.mule.model.json.ValidateJsonSchema;
import com.solace.ep.muleflow.mule.model.solace.*;
import com.solace.ep.muleflow.mule.model.xml_module.ValidateXmlSchema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class MuleFlow extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "set-variable"
    )
    @JacksonXmlElementWrapper( useWrapping = false )
    protected List<SetVariable> setVariable;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "queue-listener"
    )
    protected SolaceQueueListener queueListener;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "topic-listener"
    )
    protected SolaceTopicListener topicListener;

    @JacksonXmlProperty(
        isAttribute = false,
//        namespace = "http://www.mulesoft.org/schema/mule/xml-module",
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "xml-module:validate-schema"
    )
    protected ValidateXmlSchema validateXmlSchema;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/json",
        localName = "validate-schema"
    )
    protected ValidateJsonSchema validateJsonSchema;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/ee/core",
        localName = "transform"
    )
    protected TransformOperation transform;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "publish"
    )
    protected SolacePublish publish;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "flow-ref"
    )
    protected MuleFlowRef flowRef;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "name"
    )
    protected String name;

    public List<SetVariable> getSetVariable() {
        if (this.setVariable == null) {
            this.setVariable = new ArrayList<SetVariable>();
        }
        return this.setVariable;
    }
}
