package com.solace.ep.muleflow.mule.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import com.solace.ep.muleflow.mule.model.ModelNamespaceConstants;

public class NamespaceXmlFactory extends XmlFactory {

    private final String defaultNamespace;
    private Map<String, String> prefix2Namespace;

    public final Map<String, String> getMuleNamespaces() {

        if ( prefix2Namespace == null ) {
            prefix2Namespace = new HashMap<>();
            prefix2Namespace.put("", ModelNamespaceConstants.NS_MULE_CORE);
            prefix2Namespace.put(ModelNamespaceConstants.PRE_DOC,        ModelNamespaceConstants.NS_DOC);
            prefix2Namespace.put(ModelNamespaceConstants.PRE_EE_CORE,    ModelNamespaceConstants.NS_EE_CORE);
            prefix2Namespace.put(ModelNamespaceConstants.PRE_XML_MODULE, ModelNamespaceConstants.NS_XML_MODULE);
            prefix2Namespace.put(ModelNamespaceConstants.PRE_JSON,       ModelNamespaceConstants.NS_JSON);
            prefix2Namespace.put(ModelNamespaceConstants.PRE_SOLACE,     ModelNamespaceConstants.NS_SOLACE);
            prefix2Namespace.put(ModelNamespaceConstants.PRE_HTTP,       ModelNamespaceConstants.NS_HTTP);
        }
        return prefix2Namespace;
    }

    public static final String getMuleDefaultNamespace() {
        return ModelNamespaceConstants.NS_MULE_CORE;
    }

    public NamespaceXmlFactory(String defaultNamespace, Map<String, String> prefix2Namespace) {
        this.defaultNamespace = defaultNamespace;
        this.prefix2Namespace = prefix2Namespace;
    }

    public NamespaceXmlFactory() {
        this.defaultNamespace = getMuleDefaultNamespace();
        this.prefix2Namespace = getMuleNamespaces();
    }

    @Override
    protected XMLStreamWriter _createXmlWriter(IOContext ctxt, OutputStream out) throws IOException {
        XMLStreamWriter writer = super._createXmlWriter(ctxt, out);
        try {
            writer.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    return getMuleNamespaces().get(prefix);
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    Iterator<Map.Entry<String, String>> iterator = getMuleNamespaces().entrySet().iterator();
                    while (iterator.hasNext() ) {
                        Map.Entry<String, String> entry = iterator.next();
                        if (entry.getValue().contentEquals(namespaceURI)) {
                            return entry.getKey();
                        }
                    }
                    return "";
//                    throw new Exception("Could not find prefix for namespace: " + namespaceURI);
                }

                @Override
                public Iterator<String> getPrefixes(String namespaceURI) {
                    return getMuleNamespaces().keySet().iterator();
                }
            });
        } catch (Exception exc) {

        }

        try {
            writer.setDefaultNamespace(defaultNamespace);
            for (Map.Entry<String, String> e : getMuleNamespaces().entrySet()) {
                writer.setPrefix(e.getKey(), e.getValue());
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, null);
        }
        return writer;
    }

    @Override
    protected XMLStreamWriter _createXmlWriter(IOContext ctxt, Writer w) throws IOException {
        XMLStreamWriter writer = super._createXmlWriter(ctxt, w);
        try {
            writer.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    return getMuleNamespaces().get(prefix);
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    Iterator<Map.Entry<String, String>> iterator = getMuleNamespaces().entrySet().iterator();
                    while (iterator.hasNext() ) {
                        Map.Entry<String, String> entry = iterator.next();
                        if (entry.getValue().contentEquals(namespaceURI)) {
                            return entry.getKey();
                        }
                    }
                    return "";
//                    throw new Exception("Could not find prefix for namespace: " + namespaceURI);
                }

                @Override
                public Iterator<String> getPrefixes(String namespaceURI) {
                    return getMuleNamespaces().keySet().iterator();
                }
            });
        } catch (Exception exc) {

        }

        try {
            writer.setDefaultNamespace(defaultNamespace);
            for (Map.Entry<String, String> e : getMuleNamespaces().entrySet()) {
                writer.setPrefix(e.getKey(), e.getValue());
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, null);
        }
        return writer;
    }
}
