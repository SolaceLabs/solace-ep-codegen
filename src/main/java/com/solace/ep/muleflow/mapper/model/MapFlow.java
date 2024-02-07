package com.solace.ep.muleflow.mapper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Intermediate mapping object equivalent to a Mule Flow object in a Mule Flow Document
 * One object of this type will be created for each Ingress (mule 'flow') in a Mule Doc
 */
@Data
@NoArgsConstructor
public class MapFlow {
    
    protected String flowDesignation;

    protected boolean isDirectConsumer = false;

    // Queue Listener fields
    protected String queueListenerAddress;

    protected String queueListenerAckMode;
    // ---
    
    // Topic Listener Fields
    protected List<String> directListenerTopics = new ArrayList<>();

    protected String directListenerContentType;
    // ---

    protected String directListenerEncoding;
    
    protected String jsonSchemaContent = null;

    protected byte[] jsonSchemaReference = null;

    protected String xmlSchemaContent = null;

    protected byte[] xmlSchemaReference = null;

}
