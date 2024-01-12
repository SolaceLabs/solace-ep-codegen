package com.solace.ep.mapper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

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

    protected String xmlSchemaContent = null;

}
