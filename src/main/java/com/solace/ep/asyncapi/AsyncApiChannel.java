package com.solace.ep.asyncapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AsyncApiChannel {
    
    private JsonObject asyncApiChannel;

    private List<String> parameters;

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
                            parameterName = parmObject
                                                .getAsJsonObject( EpFieldConstants.EP_PARAMETER_NAME )
                                                .getAsString();
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

}
