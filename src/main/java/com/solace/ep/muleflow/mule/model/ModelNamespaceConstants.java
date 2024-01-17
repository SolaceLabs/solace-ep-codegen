package com.solace.ep.muleflow.mule.model;

public class ModelNamespaceConstants {
    
    public static final String
                PRE_JSON = "json",
                PRE_XML_MODULE = "xml-module",
                PRE_EE_CORE = "ee",
                PRE_SOLACE = "solace",
                PRE_HTTP = "http",
                PRE_DOC = "doc";

    public static final String
                NS_JSON = "http://www.mulesoft.org/schema/mule/json",
                NS_XML_MODULE = "http://www.mulesoft.org/schema/mule/xml-module",
                NS_EE_CORE = "http://www.mulesoft.org/schema/mule/ee/core",
                NS_MULE_CORE = "http://www.mulesoft.org/schema/mule/core",
                NS_SOLACE = "http://www.mulesoft.org/schema/mule/solace",
                NS_HTTP = "http://www.mulesoft.org/schema/mule/http",
                NS_DOC = "http://www.mulesoft.org/schema/mule/documentation";

    public static final String
                LOC_JSON = "http://www.mulesoft.org/schema/mule/json/current/mule-json.xsd",
                LOC_XML_MODULE = "http://www.mulesoft.org/schema/mule/xml-module/current/mule-xml-module.xsd",
                LOC_EE_CORE = "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd",
                LOC_MULE_CORE = "http://www.mulesoft.org/schema/mule/core/current/mule.xsd",
                LOC_SOLACE = "http://www.mulesoft.org/schema/mule/solace/current/mule-solace.xsd",
                LOC_HTTP = "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd";

    public static final String
                SCHEMA_LOCATION = getSchemaLocation();
    
    private static final String getSchemaLocation() {
        StringBuilder s = new StringBuilder();
        s.append(NS_MULE_CORE);
        s.append(" ");
        s.append(LOC_MULE_CORE);
        s.append(" ");
        s.append(NS_SOLACE);
        s.append(" ");
        s.append(LOC_SOLACE);
        s.append(" ");
        s.append(NS_EE_CORE);
        s.append(" ");
        s.append(LOC_EE_CORE);
        s.append(" ");
        s.append(NS_XML_MODULE);
        s.append(" ");
        s.append(LOC_XML_MODULE);
        s.append(" ");
        s.append(NS_JSON);
        s.append(" ");
        s.append(LOC_JSON);
        s.append(" ");
        s.append(NS_HTTP);
        s.append(" ");
        s.append(LOC_HTTP);
        return s.toString();
    }

}
