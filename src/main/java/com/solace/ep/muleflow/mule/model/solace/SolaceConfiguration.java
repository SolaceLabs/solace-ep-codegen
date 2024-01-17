package com.solace.ep.muleflow.mule.model.solace;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.muleflow.mule.model.base.BaseElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Builder
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

    public SolaceConfiguration() {
        this.solaceConnection = new SolaceConnection();
        this.eventPortalConfiguration = new EventPortalConfiguration();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
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

        // public EventPortalConfiguration( String cloudApiToken ) {
        //     this.cloudApiToken = cloudApiToken;
        // }
    }
}
