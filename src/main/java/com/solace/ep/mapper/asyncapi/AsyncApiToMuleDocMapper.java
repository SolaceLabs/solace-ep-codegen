package com.solace.ep.mapper.asyncapi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.solace.ep.asyncapi.AsyncApiAccessor;
import com.solace.ep.asyncapi.AsyncApiChannel;
import com.solace.ep.asyncapi.AsyncApiMessage;
import com.solace.ep.asyncapi.AsyncApiUtils;
import com.solace.ep.mapper.MapUtils;
import com.solace.ep.mapper.model.MapFlow;
import com.solace.ep.mapper.model.MapGlobalProperty;
import com.solace.ep.mapper.model.MapMuleDoc;
import com.solace.ep.mapper.model.MapSetVariable;
import com.solace.ep.mapper.model.MapSubFlowEgress;
import com.solace.ep.mapper.model.MapValidateSchemaJson;
import com.solace.ep.mapper.model.MapValidateSchemaXml;
import com.solace.ep.mapper.model.MapFlow.MapSolaceQueueListener;
import com.solace.ep.mapper.model.MapFlow.MapSolaceTopicListener;

public class AsyncApiToMuleDocMapper {
    
    public static MapMuleDoc mapMuleDocFromAsyncApi( AsyncApiAccessor asyncApiAccessor ) throws Exception {

        final MapMuleDoc mapMuleDoc = new MapMuleDoc();
        
        final Map<String, Boolean> inputQueueMap = new HashMap<>();

        // Map EP App Version Id to Global Property
        if ( asyncApiAccessor.getInfo().getEpApplicationVersionId() != null ) {
            mapMuleDoc.getMapGlobalProperties().add( 
                new MapGlobalProperty(
                    MapUtils.GLOBAL_NAME_EP_APP_VERSION_ID, 
                    asyncApiAccessor.getInfo().getEpApplicationVersionId()
                )
            );
        }

        // Identify unique queues --> which ones have duplicates
        for ( Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet() ) {
            AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);
            
            if (
                ! channel.hasPublishOperation() ||
                channel.getPublishQueueName() == null
            ) {
                continue;
            }

            if ( inputQueueMap.containsKey(channel.getPublishQueueName() ) ) {
                inputQueueMap.put( channel.getPublishQueueName(), true );
            } else {
                inputQueueMap.put( channel.getPublishQueueName(), false );
            }
        }

        // Create one flow + one business logic sub-flow for each input channel
        // Input channel has publish operation
        for ( Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet() ) {
            AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);
            String channelName = channelElement.getKey();
            if ( ! channel.hasPublishOperation() ) {
                continue;
            }

            MapFlow mapToFlow = new MapFlow();

            if ( channel.getPublishQueueName() == null ) {
                // Handle as DIRECT
                
                final MapSolaceTopicListener mapToTopicListener = new MapSolaceTopicListener();
                if ( channel.getPublishTopicSubscriptions() != null && ! channel.getPublishTopicSubscriptions().isEmpty() )  {
                    mapToTopicListener.setListenerTopics( channel.getPublishTopicSubscriptions() );
                } else {
                    String singleTopic = channelName.replaceAll("\\{" + AsyncApiUtils.REGEX_SOLACE_TOPIC_CHAR_CLASS + "*\\}","*");
                    mapToTopicListener.setListenerTopics(
                        Arrays.asList( singleTopic )
                    );
                }
                mapToTopicListener.setListenerContentType(channel.getPublishOpMessage().getContentType());

                mapToFlow.setFlowTopicListener(mapToTopicListener);
                mapToFlow.setDirectConsumer(true);
                mapToFlow.setFlowDesignation("DirectSubscriber");
                if ( channel.getPublishOpMessage().getContentType().toLowerCase().contains( "json" ) ) {
                    MapValidateSchemaJson validateJson = new MapValidateSchemaJson( channel.getPublishOpMessage().getPayloadAsString() );
                    mapToFlow.setFlowMapValidateSchemaJson(validateJson);
                }

            } else {
                // Handle as QUEUE

                // Check if this is a duplicate queue
                // If yes, check to see has already been handled; if yes, skip to the next channel
                boolean duplicateQueue = false;
                if ( inputQueueMap.get( channel.getPublishQueueName() ).booleanValue() == true ) {
                    // This is a duplicate queue in the spec, check to see if it has already been handled
                    for ( MapFlow checkDup : mapMuleDoc.getMapFlows() ) {
                        if ( checkDup.getFlowQueueListener().getListenerAddress().contentEquals(channel.getPublishQueueName()) ) {
                            duplicateQueue = true;
                            break;
                        }
                    }
                }
                if ( duplicateQueue ) {
                    continue;
                }

                final String queueName = channel.getPublishQueueName();
                final MapSolaceQueueListener mapToQueueListener = new MapSolaceQueueListener();
                mapToQueueListener.setListenerAddress(queueName);
                mapToQueueListener.setListenerAckMode( MapUtils.DEFAULT_ACKMODE );

                mapToFlow.setFlowQueueListener(mapToQueueListener);
                mapToFlow.setFlowDesignation(queueName);
                mapToFlow.setDirectConsumer(false);
                if ( 
                    inputQueueMap.get( channel.getPublishQueueName() ).booleanValue() == false &&
                    channel.getPublishOpMessage().getContentType().toLowerCase().contains( "json" ) 
                ) 
                {
                    MapValidateSchemaJson validateJson = new MapValidateSchemaJson( channel.getPublishOpMessage().getPayloadAsString() );
                    mapToFlow.setFlowMapValidateSchemaJson(validateJson);
                } else {
                    MapValidateSchemaXml validateXml = new MapValidateSchemaXml();
                    mapToFlow.setFlowMapValidateSchemaXml(validateXml);
                }
            }
            mapMuleDoc.getMapFlows().add(mapToFlow);
        }

        // Create one egress sub-flow for each output channel
        // Output channel has subscribe operation

        for ( Map.Entry<String, JsonElement> channelElement : asyncApiAccessor.getChannels().entrySet() ) {
            AsyncApiChannel channel = new AsyncApiChannel(channelElement.getValue().getAsJsonObject(), asyncApiAccessor);
            String channelName = channelElement.getKey();
            if ( ! channel.hasSubscribeOperation() ) {
                continue;
            }

            // Get Message Name
            AsyncApiMessage asyncApiMessage = channel.getSubscribeOpMessage();
            String messageName = asyncApiMessage.getEpEventName();

            MapSubFlowEgress mapToSubFlowEgress = new MapSubFlowEgress();
            mapToSubFlowEgress.setMessageName( messageName );

            for ( String parameter : channel.getParameters() ) {
                mapToSubFlowEgress.getSetVariables().add(
                    new MapSetVariable( 
                        parameter, 
                        "" )
                );
            }

            String jsonPayload = channel.getSubscribeOpMessage().getPayloadAsString();
            if ( jsonPayload != null ) {
                mapToSubFlowEgress.getValidateSchemaJson().setValidateSchemaContents(jsonPayload);
            }

            mapToSubFlowEgress.getMapSolacePublish().setPublishAddress(
                channelName.
                    replace("/{", "/").
                    replace("}/", "/").
                    replaceAll("\\}$", "")
            );

            mapMuleDoc.getMapEgressSubFlows().add(mapToSubFlowEgress);
        }

        return mapMuleDoc;
    }

    public static MapMuleDoc mapMuleDocFromAsyncApi( String asyncApiAsString ) throws Exception {
        return mapMuleDocFromAsyncApi( new AsyncApiAccessor(
            AsyncApiAccessor.parseAsyncApi(asyncApiAsString)
        ) );
    }

}
