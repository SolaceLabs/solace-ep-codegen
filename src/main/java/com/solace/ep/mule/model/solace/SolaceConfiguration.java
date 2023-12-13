package com.solace.ep.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SolaceConfiguration extends BaseElement {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "name"
    )
    protected String name;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "connection"
    )
    protected SolaceConnection solaceConnection;

    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "http://www.mulesoft.org/schema/mule/solace"
    )
    protected EventPortalConfiguration eventPortalConfiguration;

    @Data
    @NoArgsConstructor
    public static class EventPortalConfiguration {

        @JacksonXmlProperty(
            isAttribute = true,
            namespace = "",
            localName = "cloudApiToken"
        )
        protected String cloudApiToken;

        @JacksonXmlProperty(
            isAttribute = true,
            namespace = "",
            localName = "cloudOrgPrefix"
        )
        protected String cloudOrgPrefix;
    }
}
