package com.solace.ep.asyncapi;

import java.util.Map;
import java.util.StringTokenizer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AsyncApiAccessor {
    
    protected JsonObject root;

    protected AsyncApiInfo info;

    protected Map<String, JsonElement> schemas;

    protected Map<String, JsonElement> messages;

    protected Map<String, JsonElement> channels;

    public AsyncApiAccessor( JsonObject asyncApiRoot ) throws IllegalArgumentException {
        if (asyncApiRoot == null) {
            throw new IllegalArgumentException( "AsyncApi input cannot be null" );
        }
        this.root = asyncApiRoot;
    }

    public static JsonObject parseAsyncApi( String asyncApi ) {
        JsonElement jsonElement = JsonParser.parseString(asyncApi);
        if (jsonElement == null) {
            return null;
        }
        return jsonElement.getAsJsonObject();
    }

    public AsyncApiInfo getInfo() throws Exception {
        if (info == null) {
            info = new AsyncApiInfo( getFieldAsReference(AsyncApiFieldConstants.API_INFO) );
        }
        return info;
    }

    public String getAsyncapiVersion() throws Exception {
        JsonElement element = root.get(AsyncApiFieldConstants.API_ASYNCAPI);
        if (element == null) {
            throw new Exception("Field [asyncapi] (version) cannot be null");
        }
        if (!element.isJsonPrimitive()) {
            throw new Exception("Field [asyncapi] (version) is invalid");
        }
        return element.getAsString();
    }

    private JsonObject getFieldAsReference( String referencePath ) throws Exception {

        JsonObject node = root;
        StringTokenizer t = new StringTokenizer(referencePath, "/");

        if (t.countTokens() < 2) {
            // TODO Exception?
        }

        while ( t.hasMoreTokens() ) {
            String s = t.nextToken();
            if ( s.contentEquals("#")) {
                continue;
            }
            if ( !node.has(s) ) {
                throw new Exception(String.format(
                                    "Could not find element [%s] in reference path: %s",
                                    s,
                                    referencePath));
            }
            node = node.getAsJsonObject( s );
        }
        return node;
    }

    public AsyncApiMessage getMessageAsReference( String referencePath ) throws Exception {
        JsonObject messageObject = getFieldAsReference(referencePath);

        return new AsyncApiMessage(messageObject, new AsyncApiAccessor(root.deepCopy()));
    }

    public AsyncApiMessage getMessageByName( String messageName ) throws Exception {
        JsonElement messageElement = getMessages().get(messageName);
        if ( !messageElement.isJsonObject() ) {
            throw new Exception( String.format("Element components.messages.[%s] is invalid", messageName) );
        }
        JsonObject messageObject = messageElement.getAsJsonObject();
        return new AsyncApiMessage(messageObject, new AsyncApiAccessor(root.deepCopy()));
    }

    public String getSchemaAsReference( String referencePath ) throws Exception {
        JsonObject schemaObject = getFieldAsReference(referencePath);

        Gson gson = new Gson();
        return gson.toJson(schemaObject);
    }

    public String getSchemaByName( String schemaName ) throws Exception {
        JsonElement schemaElement = getSchemas().get(schemaName);
        if ( !schemaElement.isJsonObject() ) {
            throw new Exception( String.format("Element components.schemas.[%s] is invalid", schemaName) );
        }
        JsonObject schemaObject = schemaElement.getAsJsonObject();

        Gson gson = new Gson();
        return gson.toJson(schemaObject);
    }

    private Map<String, JsonElement> getSchemas() throws Exception {
        if (schemas == null) {
            JsonObject schemasObject = getFieldAsReference(AsyncApiFieldConstants.API_SCHEMAS);
            schemas = schemasObject.asMap();
        }
        return schemas;
    }

    private Map<String, JsonElement> getChannels() throws Exception {
        if (channels == null) {
            JsonObject channelsObject = getFieldAsReference(AsyncApiFieldConstants.API_CHANNELS);
            channels = channelsObject.asMap();
        }
        return channels;
    }

    private Map<String, JsonElement> getMessages() throws Exception {
        if (messages == null) {
            JsonObject messagesObject = getFieldAsReference(AsyncApiFieldConstants.API_MESSAGES);
            messages = messagesObject.asMap();
        }
        return messages;
    }

}
