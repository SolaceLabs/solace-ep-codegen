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

import java.util.Map;
import java.util.StringTokenizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to read content of asyncapi document parsed using Gson
 * to a JsonObject
 */
@Slf4j
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

    public static JsonObject parseAsyncApi( String asyncApi ) throws Exception {

        try {
            JsonElement jsonElement = JsonParser.parseString(asyncApi);
            if ( jsonElement != null && jsonElement.isJsonObject() ) {
                return jsonElement.getAsJsonObject();
            }
        } catch ( JsonSyntaxException jsexc ) {
            log.debug( "Failed to parse AsyncApi as JSON; re-trying as YAML" );
        } catch ( Exception exc ) {
            log.warn( "Caught exception parsing AsyncApi: {}", exc.getMessage() );
        }

        try {
            ObjectMapper yamlReader = new ObjectMapper( new YAMLFactory() );
            Object parsedYamlObj = yamlReader.readValue(asyncApi, Object.class);

            ObjectMapper jsonWriter = new ObjectMapper();
            String jsonAsyncApi = jsonWriter.writeValueAsString(parsedYamlObj);

            JsonElement jsonElement = JsonParser.parseString(jsonAsyncApi);
            if ( jsonElement != null && jsonElement.isJsonObject() ) {
                return jsonElement.getAsJsonObject();
            }
        } catch ( JsonProcessingException jpexc ) {
            log.error( "Failed to parse AsyncApi as YAML: {}", jpexc.getMessage() );
            throw jpexc;
        } catch ( JsonSyntaxException jsexc ) {
            log.error( "Caught exception parsing AsyncApi: {}", jsexc.getMessage() );
            throw jsexc;
        } catch ( Exception exc ) {
            log.error( "Caught Exception parsing AsyncApi: {}", exc.getMessage() );
            throw exc;
        }

        return null;

        // JsonElement jsonElement = JsonParser.parseString(asyncApi);
        // if (jsonElement == null) {
        //     return null;
        // }
        // return jsonElement.getAsJsonObject();
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

    public String getSchemaByReference( String referencePath ) throws Exception {
        JsonObject schemaObject = getFieldAsReference(referencePath);

        Gson gson = new Gson();
        return gson.toJson(schemaObject);
    }

    /**
     * Retrives the schema as object form the asyncapi
     * A references to definitions within the schema will be relative to
     * the asyncapi and not the stand-alone schema
     * @param referencePath
     * @return
     * @throws Exception
     */
    public JsonObject getSchemaAsObjectByReference( String referencePath ) throws Exception {
        JsonObject schemaObject = getFieldAsReference(referencePath);
        return schemaObject;
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

    public Map<String, JsonElement> getSchemas() throws Exception {
        if (schemas == null) {
            JsonObject schemasObject = getFieldAsReference(AsyncApiFieldConstants.API_SCHEMAS);
            schemas = schemasObject.asMap();
        }
        return schemas;
    }

    public Map<String, JsonElement> getChannels() throws Exception {
        if (channels == null) {
            JsonObject channelsObject = getFieldAsReference(AsyncApiFieldConstants.API_CHANNELS);
            channels = channelsObject.asMap();
        }
        return channels;
    }

    public Map<String, JsonElement> getMessages() throws Exception {
        if (messages == null) {
            JsonObject messagesObject = getFieldAsReference(AsyncApiFieldConstants.API_MESSAGES);
            messages = messagesObject.asMap();
        }
        return messages;
    }

}
