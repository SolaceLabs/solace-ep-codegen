package com.solace.ep.muleflow.mapper.asyncapi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.solace.ep.muleflow.asyncapi.*;
import com.solace.ep.muleflow.mapper.MapUtils;
import com.solace.ep.muleflow.mapper.model.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to map from AsyncApi model to intermediate 'Mapper' structures
 * used to create Mule Flow Docs. This class is specifically created for
 * AsyncApi instances generated from Solace Event Portal application
 */
@Slf4j
public class AsyncApiToMuleDocMapper {
    
    public static MapMuleDoc mapMuleDocFromAsyncApi( AsyncApiAccessor asyncApiAccessor ) throws Exception {

        log.info( "BEGIN mapping AsyncApi to intermediate MapMuleDoc format" );

        final MapMuleDoc mapMuleDoc = new MapMuleDoc();
        
        final Map<String, Boolean> inputQueueMap = new HashMap<>();

        // Map EP App Version Id to Global Property
        if ( asyncApiAccessor.getInfo().getEpApplicationVersionId() != null ) {
            mapMuleDoc.getGlobalProperties().put(
                MapUtils.GLOBAL_NAME_EP_APP_VERSION_ID, 
                asyncApiAccessor.getInfo().getEpApplicationVersionId()
            );
            log.debug( "Added Global Property type: {} to Map Doc", MapUtils.GLOBAL_NAME_EP_APP_VERSION_ID );
        }

        // Get all unique schema entries and store in a map payload hash --> schema entry
        collectSchemas(mapMuleDoc, asyncApiAccessor);

        {
            log.debug("Check input [publish] channels for duplicate queue names");

            int inputQueueCount = 0, inputQueueDuplicateCount = 0, inputDirectTopicSubscriptionCount = 0;
            // Identify unique queues --> which ones have duplicates
            for ( Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet() ) {
                AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);
                
                if ( ! channel.hasPublishOperation() ) {
                    continue;
                }
                if ( channel.getPublishQueueName() == null ) {
                    inputDirectTopicSubscriptionCount++;
                    continue;
                }
                inputQueueCount++;

                if ( inputQueueMap.containsKey(channel.getPublishQueueName() ) ) {
                    inputQueueMap.put( channel.getPublishQueueName(), true );
                    inputQueueDuplicateCount++;
                    log.info("Found DUPLICATE Ingress Queue in Channels: {}", channel.getPublishQueueName());
                } else {
                    inputQueueMap.put( channel.getPublishQueueName(), false );
                    log.info("Found Ingress Queue in Channels: {}", channel.getPublishQueueName());
                }
            }

            log.info( 
                "Found Ingress Queues in AsyncApi 'channels' - TOTAL: {}; UNIQUE: {}; DUPLICATE: {}", 
                inputQueueCount, 
                ( inputQueueCount - inputQueueDuplicateCount ),
                inputQueueDuplicateCount
            );
            log.info( 
                "Found {} Direct Topic Subscriptions in AsyncApi 'channels'", 
                inputDirectTopicSubscriptionCount );
        }

        // Create one flow + one business logic sub-flow for each UNIQUE input channel
        // Input channel has publish operation
        log.info("Create Ingress flow for each unique 'publish' operation in 'channels'");
        for ( Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet() ) {
            AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);
            String channelName = channelElement.getKey();
            log.debug( "Handling Channel '{}'", channelElement.getKey() );
            if ( ! channel.hasPublishOperation() ) {
                log.debug("Channel '{}' does not contain 'publish' operation, skipping for Ingress", channelName);
                continue;
            }

            log.info("Found Channel '{}' with publish operation; mapping Ingress flow to MapFlow", channelName);
            MapFlow mapToFlow = new MapFlow();

            if ( channel.getPublishQueueName() == null ) {
                // Handle as DIRECT
                log.debug("No queueName found, handle as DIRECT Ingress flow");

                mapToFlow.setDirectConsumer(true);
                mapToFlow.setFlowDesignation("DirectSubscriber");

                if ( channel.getPublishTopicSubscriptions() != null && ! channel.getPublishTopicSubscriptions().isEmpty() )  {
                    mapToFlow.setDirectListenerTopics(channel.getPublishTopicSubscriptions());
                } else {
                    String singleTopic = channelName.replaceAll("\\{" + AsyncApiUtils.REGEX_SOLACE_TOPIC_CHAR_CLASS + "*\\}","*");
                    mapToFlow.setDirectListenerTopics(
                        Arrays.asList( singleTopic )
                    );
                }
                mapToFlow.setDirectListenerContentType(channel.getPublishOpMessage().getContentType());

                if ( channel.getPublishOpMessage().getContentType().toLowerCase().contains( "json" ) ) {
                    mapToFlow.setJsonSchemaContent(channel.getPublishOpMessage().getPayloadAsString());
                }

            } else {
                // Handle as QUEUE
                log.debug("queueName found, handle as Queue Consumer");

                // Check if this is a duplicate queue
                // If yes, check to see has already been handled; if yes, skip to the next channel
                boolean duplicateQueue = false;
                if ( inputQueueMap.get( channel.getPublishQueueName() ).booleanValue() == true ) {
                    // This is a duplicate queue in the spec, check to see if it has already been handled
                    for ( MapFlow checkDup : mapMuleDoc.getMapFlows() ) {
                        if ( checkDup.getQueueListenerAddress().contentEquals(channel.getPublishQueueName()) ) {
                            duplicateQueue = true;
                            break;
                        }
                    }
                }
                if ( duplicateQueue ) {
                    continue;
                }

                final String queueName = channel.getPublishQueueName();
                mapToFlow.setFlowDesignation(queueName);
                mapToFlow.setDirectConsumer(false);

                mapToFlow.setQueueListenerAddress(queueName);
                mapToFlow.setQueueListenerAckMode( MapUtils.DEFAULT_ACKMODE );

                if ( 
                    inputQueueMap.get( channel.getPublishQueueName() ).booleanValue() == false &&
                    channel.getPublishOpMessage().getContentType().toLowerCase().contains( "json" ) )
                {
                    mapToFlow.setJsonSchemaContent(channel.getPublishOpMessage().getPayloadAsString());
                    mapToFlow.setJsonSchemaReference( MapUtils.getMd5Digest(mapToFlow.getJsonSchemaContent()) );
                } else {
                    mapToFlow.setXmlSchemaContent("");
                }
            }
            mapMuleDoc.getMapFlows().add(mapToFlow);
            log.info("Added Ingress flow to intermediate MapMuleDoc object");
        }

        // Create one egress sub-flow for each output channel
        // Output channel has subscribe operation
        log.info("Create Egress flow for each unique 'subscribe' operation in 'channels'");
        for ( Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet() ) {
            AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);
            String channelName = channelElement.getKey();
            if ( ! channel.hasSubscribeOperation() ) {
                log.debug("Channel '{}' does not contain 'subscribe' operation, skipping for Egress", channelName);
                continue;
            }

            // Get Message Name
            AsyncApiMessage asyncApiMessage = channel.getSubscribeOpMessage();
            String messageName = asyncApiMessage.getEpEventName();

            MapSubFlowEgress mapToSubFlowEgress = new MapSubFlowEgress();
            mapToSubFlowEgress.setMessageName( messageName );

            for ( String parameter : channel.getParameters() ) {
                mapToSubFlowEgress.getSetVariables().put( parameter, "" );
            }

            String jsonPayload = channel.getSubscribeOpMessage().getPayloadAsString();
            if ( jsonPayload != null ) {
                mapToSubFlowEgress.setJsonSchemaContent(jsonPayload);
                mapToSubFlowEgress.setJsonSchemaReference( MapUtils.getMd5Digest(jsonPayload));
            }

            mapToSubFlowEgress.setPublishAddress(
                channelName.
                    replace("/{", "/").
                    replace("}/", "/").
                    replaceAll("\\}$", "")
            );

            mapMuleDoc.getMapEgressSubFlows().add(mapToSubFlowEgress);
            log.info("Added Egress flow to intermediate MapMuleDoc object");
        }

        log.info("DONE mapping AsyncApi to intermediate MapMuleDoc format");
        return mapMuleDoc;
    }

    public static MapMuleDoc mapMuleDocFromAsyncApi( String asyncApiAsString ) throws Exception {
        log.info("Parsing AsyncApi input string");
        return mapMuleDocFromAsyncApi( new AsyncApiAccessor(
            AsyncApiAccessor.parseAsyncApi(asyncApiAsString)
        ) );
    }

    private static void collectSchemas( MapMuleDoc mapMuleDoc, AsyncApiAccessor asyncApiAccessor ) throws Exception {

        int uniqueIncrementer = 0;

        for ( Map.Entry<String, JsonElement> msgEntry : asyncApiAccessor.getMessages().entrySet() ) {
            
            String name = null, version = null, suffix = null, filename = null, payload = null;
            byte[] hash;

            AsyncApiMessage msg = new AsyncApiMessage( msgEntry.getValue().getAsJsonObject(), asyncApiAccessor);
            payload = msg.getPayloadAsString();
            if ( payload == null || payload.isEmpty() ) {
                continue;
            }
            hash = MapUtils.getMd5Digest(payload);
            if ( mapMuleDoc.getSchemaMap().containsKey( hash ) ) {
                continue;
            }

            JsonObject schemaObject = AsyncApiAccessor.parseAsyncApi(payload);
            if ( schemaObject == null ) {
                throw new Exception( "Error parsing schema content: " + payload );
            }

            // Try to get schema name from metadata
            if (
                schemaObject.has( EpFieldConstants.EP_SCHEMA_NAME ) && 
                schemaObject.get( EpFieldConstants.EP_SCHEMA_NAME ).isJsonPrimitive()
            ) {
                name = schemaObject.get( EpFieldConstants.EP_SCHEMA_NAME ).getAsString();
            } else if (
                schemaObject.has( AsyncApiFieldConstants.API_SCHEMA_TITLE ) &&
                schemaObject.get( AsyncApiFieldConstants.API_SCHEMA_TITLE ).isJsonPrimitive()
            ) {
                name = schemaObject.get( AsyncApiFieldConstants.API_SCHEMA_TITLE ).getAsString();
            } else {
                String nameElt = null, namespaceElt = null;
                if ( schemaObject.has( "name" ) && schemaObject.get("name").isJsonPrimitive() ) {
                    nameElt = schemaObject.get("name").getAsString();
                }
                if ( schemaObject.has( "namespace" ) && schemaObject.get("namespace").isJsonPrimitive() ) {
                    namespaceElt = schemaObject.get("namespace").getAsString();
                }
                if ( nameElt != null && !nameElt.isEmpty() ) {
                    name = ( namespaceElt != null && namespaceElt.isBlank() ? ( namespaceElt + "." ) : "" ) + nameElt;
                }
            }

            // If name is blank, try to get schema name from reference to components.schemas
            if ( name == null || name.isBlank() ) {
                String payloadRef = msg.getPayloadRef();
                if ( payloadRef != null ) {
                    name = AsyncApiUtils.getLastElementFromRefString(payloadRef);
                }
            }

            // If name is still blank, use hash-code value
            if ( name == null || name.isBlank() ) {
                name = encodeHexString( hash );
            }

            // Get version from schema if present
            if (
                schemaObject.has( EpFieldConstants.EP_SCHEMA_VERSION ) && 
                schemaObject.get( EpFieldConstants.EP_SCHEMA_VERSION ).isJsonPrimitive()
            ) {
                version = schemaObject.get( EpFieldConstants.EP_SCHEMA_VERSION ).getAsString();
                if (version.isBlank()) {
                    version = null;
                }
            }

            // TODO - remove assumption that schemas are json
            suffix = "json";

            filename = name + ( version != null ? "_" + version : "" ) + "." + suffix;

            for ( Map.Entry<byte[], SchemaInstance> entry : mapMuleDoc.getSchemaMap().entrySet() ) {
                SchemaInstance si = entry.getValue();
                if ( si.getFileName().contentEquals( filename )) {
                    filename = "DUPNAME" + ++uniqueIncrementer + "_" + filename;
                    break;
                }
            }

            SchemaInstance schemaInstance = new SchemaInstance(name, version, suffix, filename, payload);
            mapMuleDoc.getSchemaMap().put(hash, schemaInstance);
        }

    }

    /**
     * Adopted from: https://www.baeldung.com/java-byte-arrays-hex-strings
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
