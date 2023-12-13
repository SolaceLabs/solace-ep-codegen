package com.solace.ep.mule.model.solace;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.solace.ep.mule.model.base.KeyValuePair;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SolaceConnection {
    
    @JacksonXmlProperty(
        isAttribute = true,
        localName = "brokerHost"
    )
    protected String brokerHost;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "msgVPN"
    )
    protected String msgVpn;

    @JacksonXmlProperty(
        isAttribute = true,
        localName = "clientUserName"
    )
    protected String clientUserName;

        @JacksonXmlProperty(
        isAttribute = true,
        localName = "password"
    )
    protected String password;

    @JacksonXmlElementWrapper(
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "jcsmp-properties",
        useWrapping = true
    )
    @JacksonXmlProperty(
        namespace = "http://www.mulesoft.org/schema/mule/solace",
        localName = "jcsmp-property"
    )
    protected List<KeyValuePair> jcsmpProperties;

    public List<KeyValuePair> getJcsmpProperties() {
        if (jcsmpProperties == null) {
            jcsmpProperties = new ArrayList<KeyValuePair>();
        }
        return jcsmpProperties;
    }
}
