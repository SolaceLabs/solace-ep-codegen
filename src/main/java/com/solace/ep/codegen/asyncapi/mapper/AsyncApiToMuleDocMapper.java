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

package com.solace.ep.codegen.asyncapi.mapper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.solace.ep.codegen.asyncapi.mapper.solace.AsyncApiSolaceBindingMapper;
import com.solace.ep.codegen.asyncapi.model.*;
import com.solace.ep.codegen.internal.model.MapFlow;
import com.solace.ep.codegen.internal.model.MapMuleDoc;
import com.solace.ep.codegen.internal.model.MapSubFlowEgress;
import com.solace.ep.codegen.internal.model.SchemaInstance;
import com.solace.ep.codegen.mule.mapper.MapUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to map from AsyncApi model to intermediate 'Mapper' structures
 * used to create Mule Flow Docs. This class is specifically created for
 * AsyncApi instances generated from Solace Event Portal application
 */
@Slf4j
public class AsyncApiToMuleDocMapper {

    public static MapMuleDoc mapMuleDocFromAsyncApi(AsyncApiAccessor asyncApiAccessor) throws Exception {

        log.info("BEGIN mapping AsyncApi to intermediate MapMuleDoc format");

        final MapMuleDoc mapMuleDoc = new MapMuleDoc();

//        final Map<String, Boolean> inputQueueMap = new HashMap<>();

        // Map EP App Version Id to Global Property

        if (asyncApiAccessor.getInfo() != null) {
            AsyncApiInfo info = asyncApiAccessor.getInfo();
            if (info.getEpApplicationVersionId() != null) {
                mapMuleDoc.getGlobalProperties().put(
                        MapUtils.GLOBAL_NAME_EP_APP_VERSION_ID,
                        asyncApiAccessor.getInfo().getEpApplicationVersionId()
                );
                log.debug("Added Global Property type: {} to Map Doc", MapUtils.GLOBAL_NAME_EP_APP_VERSION_ID);
            }

            if (info.getInfoDescription() != null) {
                mapMuleDoc.getGlobalProperties().put(
                        MapUtils.GLOBAL_NAME_EP_APP_VERSION_DESCRIPTION,
                        asyncApiAccessor.getInfo().getInfoDescription()
                );
                log.debug("Added Global Property type: {} to Map Doc", MapUtils.GLOBAL_NAME_EP_APP_VERSION_DESCRIPTION);
            }

            if (info.getInfoVersion() != null) {
                mapMuleDoc.getGlobalProperties().put(
                        MapUtils.GLOBAL_NAME_EP_APP_VERSION,
                        asyncApiAccessor.getInfo().getInfoVersion()
                );
                log.debug("Added Global Property type: {} to Map Doc", MapUtils.GLOBAL_NAME_EP_APP_VERSION);
            }

            if (info.getInfoTitle() != null) {
                mapMuleDoc.getGlobalProperties().put(
                        MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE,
                        asyncApiAccessor.getInfo().getInfoTitle()
                );
                log.debug("Added Global Property type: {} to Map Doc", MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE);
            }
        }

        // Get all unique schema entries and store in a map payload hash --> schema entry
        collectSchemas(mapMuleDoc, asyncApiAccessor);


        final Map<String, AsyncApiSolaceBindingMapper> consumers = new HashMap<>();

        {
//            int inputQueueCount = 0, inputDirectConsumerCount = 0;

            for (Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet()) {

                AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);

                if (!channel.hasPublishOperation()) {
                    continue;
                }
                if (channel.getPublishOpSolaceDestinations() == null || channel.getPublishOpSolaceDestinations().size() == 0) {
                    continue;
                }

                for (JsonElement destElement : channel.getPublishOpSolaceDestinations()) {
                    if (!destElement.isJsonObject()) {
                        // TODO - Error handling
                    }

                    Gson gson = new Gson();
                    String destAsString = gson.toJson(destElement.getAsJsonObject());

                    AsyncApiSolaceBindingMapper solaceConsumer = gson.fromJson(destAsString, AsyncApiSolaceBindingMapper.class);
                    String consumerName;

                    if (solaceConsumer.getTopic() != null) {
                        solaceConsumer.setDirectConsumer(true);
                        consumerName = solaceConsumer.getTopic().getName();
                    } else {
                        solaceConsumer.setDirectConsumer(false);
                        consumerName = solaceConsumer.getQueue().getName();
                    }

                    if (consumers.containsKey(consumerName)) {
                        solaceConsumer = consumers.get(consumerName);
                    } else {
                        consumers.put(consumerName, solaceConsumer);
                    }

                    for (AsyncApiMessage message : channel.getPublishOpMessages()) {
                        solaceConsumer.getMessages().add(message);
                    }
                }
            }
        }

        for (Map.Entry<String, AsyncApiSolaceBindingMapper> consumerEntry : consumers.entrySet()) {

            String consumerName = consumerEntry.getKey();
            AsyncApiSolaceBindingMapper solaceConsumer = consumerEntry.getValue();

            log.info(
                    "Mapping Solace Consumer of type {}='{}'",
                    solaceConsumer.isDirectConsumer() ? "DIRECT" : "QUEUE",
                    consumerName
            );

            MapFlow mapToFlow = new MapFlow();
            mapToFlow.setDirectConsumer(solaceConsumer.isDirectConsumer());
            mapToFlow.setFlowDesignation(consumerName.replaceAll(" ", "_"));

            if (solaceConsumer.isDirectConsumer()) {
                mapToFlow.setDirectListenerTopics(solaceConsumer.getTopic().getTopicSubscriptions());
            } else {
                mapToFlow.setFlowDesignation(solaceConsumer.getQueue().getName());

                mapToFlow.setQueueListenerAddress(solaceConsumer.getQueue().getName());
                mapToFlow.setQueueListenerAckMode(MapUtils.DEFAULT_ACKMODE);
            }

            boolean jsonContentType = false, sameSchema = false, hasSchema = false;
            String jsonPayload = null, jsonPayloadHash = null;
            if (solaceConsumer.getMessages() != null && !solaceConsumer.getMessages().isEmpty()) {

                mapToFlow.setMessageName( AsyncApiMessage.getMessageNameFromList( new ArrayList<>(solaceConsumer.getMessages() ) ) );
                mapToFlow.setMultipleMessageTypes( solaceConsumer.getMessages().size() == 1 ? false : true );

                Iterator<AsyncApiMessage> iMsg = solaceConsumer.getMessages().iterator();
                AsyncApiMessage firstMessage = iMsg.next();

                if ((firstMessage.getContentType() == null ? "" : firstMessage.getContentType()).contains("json")) {
                    jsonContentType = true;
                    jsonPayload = firstMessage.getPayloadAsString();
                    if (jsonPayload != null && jsonPayload.length() > 0) {
                        jsonPayloadHash = encodeHexString(MapUtils.getMd5Digest(jsonPayload));
                        hasSchema = true;
                        sameSchema = true;
                    }
                }

                while (jsonContentType == true && iMsg.hasNext()) {
                    AsyncApiMessage m = iMsg.next();
                    if (m.getContentType() == null || !m.getContentType().contains("json")) {
                        jsonContentType = false;
                        continue;
                    }
                    String mPayload = m.getPayloadAsString() == null ? "NOT A SCHEMA" : m.getPayloadAsString();
                    String mHash = encodeHexString(MapUtils.getMd5Digest(mPayload));
                    if (!mHash.contentEquals(jsonPayloadHash)) {
                        sameSchema = false;
                        continue;
                    }
                }

                mapToFlow.setMultipleMessageTypes( !sameSchema );

                // if (hasSchema && sameSchema && jsonContentType) {
                if (hasSchema && jsonContentType) {
                    mapToFlow.setJsonSchemaContent(jsonPayload);
                    mapToFlow.setJsonSchemaReference(jsonPayloadHash);
                    mapToFlow.setContentType(firstMessage.getContentType());
                    mapToFlow.setEncoding("utf-8");
                }
            }

            mapMuleDoc.getMapFlows().add(mapToFlow);
            log.info("Added Ingress flow '{}' to intermediate MapMuleDoc object", mapToFlow.getFlowDesignation());
        }

        // Create one egress sub-flow for each output channel
        // Output channel has subscribe operation
        log.info("Create Egress flow for each unique 'subscribe' operation in 'channels'");
        for (Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet()) {
            AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);
            String channelName = channelElement.getKey();
            if (!channel.hasSubscribeOperation()) {
                log.debug("Channel '{}' does not contain 'subscribe' operation, skipping for Egress", channelName);
                continue;
            }

            // Get Message Name
            List<AsyncApiMessage> asyncApiMessages = channel.getSubscribeOpMessages();
            String messageName = AsyncApiMessage.getMessageNameFromList(asyncApiMessages);

            MapSubFlowEgress mapToSubFlowEgress = new MapSubFlowEgress();
            mapToSubFlowEgress.setMessageName(messageName);

            for (String parameter : channel.getParameters()) {
                mapToSubFlowEgress.getSetVariables().put(parameter, "");
            }

            if (asyncApiMessages != null && asyncApiMessages.size() == 1) {
                AsyncApiMessage subscribeOpMessage = AsyncApiMessage.getMessageAsSingleton(asyncApiMessages);
                String jsonPayload = subscribeOpMessage != null ? subscribeOpMessage.getPayloadAsString() : null;

                if (jsonPayload != null) {
                    mapToSubFlowEgress.setJsonSchemaContent(jsonPayload);
                    mapToSubFlowEgress.setJsonSchemaReference(encodeHexString(MapUtils.getMd5Digest(jsonPayload)));
                    mapToSubFlowEgress.setContentType(subscribeOpMessage.getContentType());
                    mapToSubFlowEgress.setEncoding("utf-8");
                }
            }

            // Embedded parameters handled in Mule mapper
            mapToSubFlowEgress.setPublishAddress(channelName);

            mapMuleDoc.getMapEgressSubFlows().add(mapToSubFlowEgress);
            log.info("Added Egress flow to intermediate MapMuleDoc object");
        }

        log.info("DONE mapping AsyncApi to intermediate MapMuleDoc format");
        return mapMuleDoc;
    }

    public static MapMuleDoc mapMuleDocFromAsyncApi(String asyncApiAsString) throws Exception {
        log.info("Parsing AsyncApi input string");
        return mapMuleDocFromAsyncApi(new AsyncApiAccessor(
                AsyncApiAccessor.parseAsyncApi(asyncApiAsString)
        ));
    }

    private static void collectSchemas(MapMuleDoc mapMuleDoc, AsyncApiAccessor asyncApiAccessor) throws Exception {

        int uniqueIncrementer = 0;

        for (Map.Entry<String, JsonElement> msgEntry : asyncApiAccessor.getMessages().entrySet()) {

            String name = null, version = null, suffix = null, filename = null, payload = null;
            byte[] hash;

            AsyncApiMessage msg = new AsyncApiMessage(msgEntry.getValue().getAsJsonObject(), asyncApiAccessor);
            payload = msg.getPayloadAsString();
            if (payload == null || payload.isEmpty()) {
                continue;
            }
            hash = MapUtils.getMd5Digest(payload);

            if (mapMuleDoc.getSchemaMap().containsKey(encodeHexString(hash))) {
                continue;
            }

            JsonObject schemaObject = AsyncApiAccessor.parseAsyncApi(payload);
            if (schemaObject == null) {
                throw new Exception("Error parsing schema content: " + payload);
            }

            // Try to get schema name from metadata
            if (
                    schemaObject.has(EpFieldConstants.EP_SCHEMA_NAME) &&
                            schemaObject.get(EpFieldConstants.EP_SCHEMA_NAME).isJsonPrimitive()
            ) {
                name = schemaObject.get(EpFieldConstants.EP_SCHEMA_NAME).getAsString();
            } else if (
                    schemaObject.has(AsyncApiFieldConstants.API_SCHEMA_TITLE) &&
                            schemaObject.get(AsyncApiFieldConstants.API_SCHEMA_TITLE).isJsonPrimitive()
            ) {
                name = schemaObject.get(AsyncApiFieldConstants.API_SCHEMA_TITLE).getAsString();
            } else {
                String nameElt = null, namespaceElt = null;
                if (schemaObject.has("name") && schemaObject.get("name").isJsonPrimitive()) {
                    nameElt = schemaObject.get("name").getAsString();
                }
                if (
                        msg.getSchemaFormat() != null &&
                                msg.getSchemaFormat().contains("avro") &&
                                schemaObject.has("namespace") &&
                                schemaObject.get("namespace").isJsonPrimitive()
                ) {
                    namespaceElt = schemaObject.get("namespace").getAsString();
                }
                if (nameElt != null && !nameElt.isEmpty()) {
                    name = (namespaceElt != null && !namespaceElt.isBlank() ? (namespaceElt + ".") : "") + nameElt;
                }
            }

            // If name is blank, try to get schema name from reference to components.schemas
            if (name == null || name.isBlank()) {
                String payloadRef = msg.getPayloadRef();
                if (payloadRef != null) {
                    name = AsyncApiUtils.getLastElementFromRefString(payloadRef);
                }
            }

            // If name is still blank, use hash-code value
            if (name == null || name.isBlank()) {
                name = encodeHexString(hash);
            }

            // Get version from schema if present
            if (
                    schemaObject.has(EpFieldConstants.EP_SCHEMA_VERSION) &&
                            schemaObject.get(EpFieldConstants.EP_SCHEMA_VERSION).isJsonPrimitive()
            ) {
                version = schemaObject.get(EpFieldConstants.EP_SCHEMA_VERSION).getAsString();
                if (version.isBlank()) {
                    version = null;
                }
            }

            // TODO - remove assumption that schemas are json
            suffix = "json";

            // TODO - improve file name logic -- cannot contain spaces
            filename = (name + (version != null ? "_" + version : "") + "." + suffix).replaceAll("\\s", "_");

            for (Map.Entry<String, SchemaInstance> entry : mapMuleDoc.getSchemaMap().entrySet()) {
                SchemaInstance si = entry.getValue();
                if (si.getFileName().contentEquals(filename)) {
                    filename = "DUPNAME" + ++uniqueIncrementer + "_" + filename;
                    break;
                }
            }

            SchemaInstance schemaInstance = new SchemaInstance(name, version, suffix, filename, payload);
            mapMuleDoc.getSchemaMap().put(encodeHexString(hash), schemaInstance);
        }

    }

    /**
     * Adopted from: https://www.baeldung.com/java-byte-arrays-hex-strings
     *
     * @param byteArray
     * @return
     */
    private static String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }

    /**
     * Adopted from: https://www.baeldung.com/java-byte-arrays-hex-strings
     *
     * @param num
     * @return
     */
    private static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }
}
