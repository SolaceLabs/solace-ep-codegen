package com.solace.ep.asyncapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AsyncApiChannel {
    
    private JsonObject asyncApiChannel;

    private List<String> parameters;

    private JsonObject publishOpFirstDestination;

    private AsyncApiAccessor asyncApi;

    public AsyncApiChannel( JsonObject channel, AsyncApiAccessor asyncApi ) {
        if (channel == null) {
            throw new IllegalArgumentException( "[channel] object cannot be null" );
        }
        this.asyncApiChannel = channel;
        this.asyncApi = asyncApi;
    }

    public List<String> getParameters() {
        if ( parameters == null ) {
            parameters = new ArrayList<String>();
            if ( asyncApiChannel.has( AsyncApiFieldConstants.CHANNEL_PARAMETERS ) ) {
                Map<String, JsonElement> parms = 
                    asyncApiChannel
                    .getAsJsonObject(AsyncApiFieldConstants.CHANNEL_PARAMETERS)
                    .asMap();

                for ( String parmName : parms.keySet() ) {
                    JsonElement parmElement = parms.get(parmName);
                    String parameterName = null;
                    if ( parmElement.getClass().isAssignableFrom( JsonObject.class ) ) {
                        JsonObject parmObject = ( JsonObject )parmElement;
                        if ( parmObject.has(EpFieldConstants.EP_PARAMETER_NAME) ) {
                            parameterName = parmObject.get( EpFieldConstants.EP_PARAMETER_NAME ).getAsString();
                                                // .getAsJsonObject( EpFieldConstants.EP_PARAMETER_NAME )
                                                // .getAsString();
                        }
                    }
                    if ( parameterName == null ) {
                        parameterName = parmName;
                    }
                    parameters.add(parameterName);
                }
            }
        }
        return parameters;
    }

    public boolean hasPublishOperation() {
        if ( asyncApiChannel.has(AsyncApiFieldConstants.OP_PUBLISH) ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasSubscribeOperation() {
        if ( asyncApiChannel.has( AsyncApiFieldConstants.OP_SUBSCRIBE)) {
            return true;
        } else {
            return false;
        }
    }

    public AsyncApiMessage getOpMessage( String operationType ) throws Exception {
        if ( operationType != AsyncApiFieldConstants.OP_PUBLISH && operationType != AsyncApiFieldConstants.OP_SUBSCRIBE ) {
            return null;
        }
        if ( ! asyncApiChannel.has( operationType ) ) {
            return null;
        }
        JsonObject operation = asyncApiChannel.getAsJsonObject(operationType);
        if ( ! operation.has(AsyncApiFieldConstants.OP_MESSAGE) ) {
            return null;
        }
        JsonObject message = operation.getAsJsonObject(AsyncApiFieldConstants.OP_MESSAGE);
        if ( message.has(AsyncApiFieldConstants.API_$REF) ) {
            JsonElement refElement = message.get( AsyncApiFieldConstants.API_$REF );
            if ( refElement.isJsonPrimitive() ) {
                return this.asyncApi.getMessageAsReference( refElement.getAsString() );
            }
            throw new Exception( "$ref element for message is not property formatted" );
        }
        return new AsyncApiMessage(message, asyncApi);
    }

    public AsyncApiMessage getSubscribeOpMessage() throws Exception {
        return getOpMessage( AsyncApiFieldConstants.OP_SUBSCRIBE );
    }

    public AsyncApiMessage getPublishOpMessage() throws Exception {
        return getOpMessage( AsyncApiFieldConstants.OP_PUBLISH );
    }

    public JsonArray getPublishOpSolaceDestinations() {
        if ( ! hasPublishOperation() ) return null;
        try {
            JsonArray destinations = asyncApiChannel
                                        .getAsJsonObject( AsyncApiFieldConstants.OP_PUBLISH )
                                        .getAsJsonObject( "bindings" )
                                        .getAsJsonObject( "solace" )
                                        .getAsJsonArray( "destinations" );
            return destinations;
        } catch ( Exception exc ) {
            return null;
        }
    }

    public String getPublishQueueName() {

        JsonObject queueObject = getPublishQueueJsonObject();

        if (queueObject == null) return null;

        if (queueObject.has("name") ) {
            return queueObject.get("name").getAsString();
        }
        return null;
    }

    public List<String> getPublishQueueSubscriptions() {

        JsonObject queueObject = getPublishQueueJsonObject();

        if (queueObject == null) return null;

        if (queueObject.has("topicSubscriptions") ) {
            JsonArray array = queueObject.get("topicSubscriptions").getAsJsonArray();
            List<String> subscriptions = new ArrayList<String>();
            for ( JsonElement element : array ) {
                subscriptions.add( element.getAsString() );
            }
            return subscriptions;
        }
        return null;
    }

    public List<String> getPublishTopicSubscriptions() {

        JsonObject topicObject = getPublishTopicJsonObject();

        if (topicObject == null) return null;

        if (topicObject.has("topicSubscriptions") ) {
            JsonArray array = topicObject.get("topicSubscriptions").getAsJsonArray();
            List<String> subscriptions = new ArrayList<String>();
            for ( JsonElement element : array ) {
                subscriptions.add( element.getAsString() );
            }
            return subscriptions;
        }
        return null;
    }

    private JsonObject getPublishQueueJsonObject() {
        
        if ( this.publishOpFirstDestination == null ) {
            JsonArray array = getPublishOpSolaceDestinations();
            if ( array == null || array.size() < 1 ) return null;

            for ( JsonElement je : array.asList() ) {
                if ( ! je.isJsonObject() ) return null;
                this.publishOpFirstDestination = je.getAsJsonObject();
                break;
            }
        }

        if ( publishOpFirstDestination.has("destinationType") && 
                publishOpFirstDestination.get("destinationType").getAsString().contentEquals( "queue" ) ) {
            if ( publishOpFirstDestination.has( "queue" ) ) {
                JsonElement queueElement = publishOpFirstDestination.get("queue");
                if ( queueElement.isJsonObject() ) {
                    return ( JsonObject )queueElement;
                }
            }
        }
        return null;
    }

    private JsonObject getPublishTopicJsonObject() {
        
        if ( this.publishOpFirstDestination == null ) {
            JsonArray array = getPublishOpSolaceDestinations();
            if ( array == null || array.size() < 1 ) return null;

            for ( JsonElement je : array.asList() ) {
                if ( ! je.isJsonObject() ) return null;
                this.publishOpFirstDestination = je.getAsJsonObject();
                break;
            }
        }

        if ( publishOpFirstDestination.has("destinationType") && 
                publishOpFirstDestination.get("destinationType").getAsString().contentEquals( "queue" ) ) {
            if ( publishOpFirstDestination.has( "queue" ) ) {
                JsonElement queueElement = publishOpFirstDestination.get("queue");
                if ( queueElement.isJsonObject() ) {
                    return ( JsonObject )queueElement;
                }
            }
        }
        return null;
    }
}
