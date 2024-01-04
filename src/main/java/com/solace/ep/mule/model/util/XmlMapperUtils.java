package com.solace.ep.mule.model.util;

import com.ctc.wstx.api.WstxOutputProperties;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class XmlMapperUtils {
    
    public static XmlMapper createXmlMapperForMuleDoc() {
        XmlMapper xmlMapper = new XmlMapper( new NamespaceXmlFactory() );
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        xmlMapper
                .getFactory()
                .getXMLOutputFactory()
                .setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true);
        return xmlMapper;
    }
}
