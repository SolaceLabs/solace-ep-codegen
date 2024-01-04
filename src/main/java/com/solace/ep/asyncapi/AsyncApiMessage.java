package com.solace.ep.asyncapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AsyncApiMessage {
    
    private JsonObject asyncApiMessage;

    private AsyncApiAccessor asyncApi;

    public AsyncApiMessage( JsonObject message, AsyncApiAccessor asyncApi ) {
        if (message == null) {
            throw new IllegalArgumentException( "[message] object cannot be null" );
        }
        this.asyncApiMessage = message;
        this.asyncApi = asyncApi;
    }

    public String getEpEventId() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_ID);
    }

    public String getEpVersionDisplayName() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_VERSION_DISPLAYNAME);
    }

    public String getDescription() {
        return getMessageFieldByName(AsyncApiFieldConstants.INFO_DESCRIPTION);
    }

    public String getEpApplicationDomainId() {
        return getMessageFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_ID);
    }

    public String getSchemaFormat() {
        return getMessageFieldByName(AsyncApiFieldConstants.API_SCHEMA_FORMAT);
    }

    public String getEpEventStateName() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_STATE_NAME);
    }

    public String getEpShared() {
        return getMessageFieldByName(EpFieldConstants.EP_SHARED);
    }

    public String getEpApplicationDomainName() {
        return getMessageFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_NAME);
    }

    public String getEpEventVersionId() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_VERSION_ID);
    }

    public String getEpEventVersion() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_VERSION);
    }

    public String getEpEventName() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_NAME);
    }

    public String getContentType() {
        return getMessageFieldByName(AsyncApiFieldConstants.API_CONTENT_TYPE);
    }

    public String getEpEventStateId() {
        return getMessageFieldByName(EpFieldConstants.EP_EVENT_STATE_ID);
    }

    public String getPayloadAsString() throws Exception {
        JsonObject payload = asyncApiMessage.getAsJsonObject( AsyncApiFieldConstants.API_PAYLOAD );
        if ( payload == null ) {
            return null;
        }
        if ( payload.has(AsyncApiFieldConstants.API_$REF) ) {
            // TODO Get Reference to schemas and return as string
            
            JsonElement refElement = payload.get( AsyncApiFieldConstants.API_$REF );
            if ( refElement.isJsonPrimitive() ) {
                return this.asyncApi.getSchemaAsReference( refElement.getAsString() );
            }
            throw new Exception( "$ref element for message is not property formatted" );
        }
        Gson gson = new Gson();
        return gson.toJson(payload);
    }

    public String getMessageFieldByName( String name ) {
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
}
