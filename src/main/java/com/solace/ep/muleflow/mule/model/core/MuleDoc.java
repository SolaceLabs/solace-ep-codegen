package com.solace.ep.muleflow.mule.model.core;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.solace.ep.muleflow.mule.model.solace.SolaceConfiguration;
import com.solace.ep.muleflow.mule.util.ModelNamespaceConstants;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JacksonXmlRootElement(
    namespace = "http://www.mulesoft.org/schema/mule/core",
    localName = "mule"
)
public class MuleDoc {
    
    @JacksonXmlProperty(
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "config"
    )
    protected SolaceConfiguration solaceConfiguration;

    @JacksonXmlProperty(
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "global-property"
    )
    @JacksonXmlElementWrapper( useWrapping = false )
    protected List<GlobalProperty> globalProperty;

    @JacksonXmlProperty(
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "configuration-properties"
    )
    protected ConfigurationProperties configurationProperties;

    @JacksonXmlProperty(
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "flow"
    )
    @JacksonXmlElementWrapper( useWrapping = false )
    protected List<MuleFlow> flow;

    @JacksonXmlProperty(
        namespace = "http://www.mulesoft.org/schema/mule/core",
        localName = "sub-flow"
    )
    @JacksonXmlElementWrapper( useWrapping = false )
    protected List<MuleFlow> subFlow;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns"
    )
    protected String ns0 = ModelNamespaceConstants.NS_MULE_CORE;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns:doc"
    )
    protected String ns1 = ModelNamespaceConstants.NS_DOC;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns:solace"
    )
    protected String ns2 = ModelNamespaceConstants.NS_SOLACE;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns:xml-module"
    )
    protected String ns3 = ModelNamespaceConstants.NS_XML_MODULE;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns:json"
    )
    protected String ns4 = ModelNamespaceConstants.NS_JSON;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns:ee"
    )
    protected String ns5 = ModelNamespaceConstants.NS_EE_CORE;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns:http"
    )
    protected String ns6 = ModelNamespaceConstants.NS_HTTP;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xmlns:xsi"
    )
    protected String nsXsi = "http://www.w3.org/2001/XMLSchema-instance";

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "xsi:schemaLocation"
    )
    protected String schemaLocation = ModelNamespaceConstants.SCHEMA_LOCATION;

    public List<GlobalProperty> getGlobalProperty() {
        if ( this.globalProperty == null ) {
            globalProperty = new ArrayList<GlobalProperty>();
        }
        return this.globalProperty;
    }

    public List<MuleFlow> getFlow() {
        if ( this.flow == null ) {
            flow = new ArrayList<MuleFlow>();
        }
        return this.flow;
    }

    public List<MuleFlow> getSubFlow() {
        if ( this.subFlow == null ) {
            subFlow = new ArrayList<MuleFlow>();
        }
        return this.subFlow;
    }
}
