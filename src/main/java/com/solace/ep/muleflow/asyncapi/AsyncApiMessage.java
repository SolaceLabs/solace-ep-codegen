/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.solace.ep.muleflow.asyncapi;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class to provide access to fields in AsyncApi 'message' objects
 * parsed as Gson 'JsonObject'
 */
public class AsyncApiMessage {
    
    private JsonObject asyncApiMessage;

    private AsyncApiAccessor asyncApi;

    /**
     * Public constructor - requires the message object as JsonObject and 
     * AsyncApiAccessor of the root document to extract references
     * @param message
     * @param asyncApi
     */
    public AsyncApiMessage( JsonObject message, AsyncApiAccessor asyncApi ) {
        if (message == null) {
            throw new IllegalArgumentException( "[message] object cannot be null" );
        }
        this.asyncApiMessage = message;
        this.asyncApi = asyncApi;
    }

    /**
     * Get the Event Portal Event Id for this message
     * @return
     */
    public String getEpEventId() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_ID);
    }

    /**
     * Get the Event Portal Display name for this message
     */
    public String getEpVersionDisplayName() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_VERSION_DISPLAYNAME);
    }

    /**
     * Get the Event Portal description for this message
     * @return
     */
    public String getDescription() {
        return getMessageFieldByName(AsyncApiFieldConstants.INFO_DESCRIPTION);
    }

    /**
     * Get Event Portal Application Domain Id for this message
     * @return
     */
    public String getEpApplicationDomainId() {
        return getMessageFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_ID);
    }

    /**
     * Get Event Portal schema format for this message
     * @return
     */
    public String getSchemaFormat() {
        return getMessageFieldByName(AsyncApiFieldConstants.API_SCHEMA_FORMAT);
    }

    /**
     * Get Event Portal State Name for this message
     * @return
     */
    public String getEpEventStateName() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_STATE_NAME);
    }

    /**
     * Get Event Portal Shared setting for this message
     * @return
     */
    public String getEpShared() {
        return getMessageFieldByName(EpFieldConstants.EP_SHARED);
    }

    /**
     * Get Event Portal Application Domain Name for this message
     * @return
     */
    public String getEpApplicationDomainName() {
        return getMessageFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_NAME);
    }

    /**
     * Get Event Portal Version Id for this message
     * @return
     */
    public String getEpEventVersionId() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_VERSION_ID);
    }

    /**
     * Get Event Portal Event Version for this message
     * @return
     */
    public String getEpEventVersion() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_VERSION);
    }

    /**
     * Gent Event Portal Event Name for this message
     * @return
     */
    public String getEpEventName() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_NAME);
    }

    /**
     * Get message content type for this message
     * @return
     */
    public String getContentType() {
        return getMessageFieldByName(AsyncApiFieldConstants.API_CONTENT_TYPE);
    }

    /**
     * Get Event Portal State Id for this message
     * @return
     */
    public String getEpEventStateId() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_STATE_ID);
    }

    /**
     * Get the contents of the message payload schema as a String
     * This method will resolve message payloads defined as a reference '$ref' element
     * @return
     * @throws Exception
     */
    public String getPayloadAsString() throws Exception {
        JsonObject payload = asyncApiMessage.getAsJsonObject( AsyncApiFieldConstants.API_PAYLOAD );
        if ( payload == null ) {
            return null;
        }
        if ( payload.has(AsyncApiFieldConstants.API_$REF) ) {
            JsonElement refElement = payload.get( AsyncApiFieldConstants.API_$REF );
            if ( refElement.isJsonPrimitive() ) {
                return this.asyncApi.getSchemaAsReference( refElement.getAsString() );
            }
            throw new Exception( "$ref element for message is not property formatted" );
        }
        Gson gson = new Gson();
        return gson.toJson(payload);
    }

    public String getPayloadRef() {
        JsonObject payload = asyncApiMessage.getAsJsonObject( AsyncApiFieldConstants.API_PAYLOAD );
        if ( payload == null ) {
            return null;
        }
        if ( payload.has(AsyncApiFieldConstants.API_$REF) ) {
            JsonElement refElement = payload.get( AsyncApiFieldConstants.API_$REF );
            if ( refElement.isJsonPrimitive() ) {
                try {
                    return refElement.getAsString();
                } catch ( Exception exc ) { }
            }
        }
        return null;
    }

    /**
     * Get the contents of a string field for this message
     * Returns null if content not found
     * @param name
     * @return
     */
    protected String getMessageFieldByName( String name ) {
        JsonElement element = asyncApiMessage.get(name);

        if (element == null) {
            return null;
        }

        if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else {
            return null;
        }
    }

    public static String getMessageNameFromList( List<AsyncApiMessage> messageList ) {
        
        if ( messageList.size() == 0 ) {
            return "NOT_FOUND";
        }
        return messageList.get( 0 ).getEpEventName();
    }

    public static AsyncApiMessage getMessageAsSingleton( List<AsyncApiMessage> messageList ) {
        if ( messageList.size() != 1 ) {
            return null;
        }
        return messageList.get(0);
    }

}
