package com.solace.ep.muleflow.asyncapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AsyncApiInfo {
    
    private JsonObject asyncApiInfo;

    public AsyncApiInfo( JsonObject info ) {
        if (info == null) {
            throw new IllegalArgumentException( "AsyncApi [info] block cannot be null" );
        }
        this.asyncApiInfo = info;
    }

    public String getEpApplicationVersion() {
        return getInfoFieldByName(EpFieldConstants.EP_APPLICATION_VERSION);
    }

    public String getEpApplicationVersionId() {
        return getInfoFieldByName(EpFieldConstants.EP_APPLICATION_VERSION_ID);
    }

    public String getEpApplicationId() {
        return getInfoFieldByName(EpFieldConstants.EP_APPLICATION_ID);
    }

    public String getEpStateName() {
        return getInfoFieldByName(EpFieldConstants.EP_STATE_NAME);
    }

    public String getEpStateId() {
        return getInfoFieldByName(EpFieldConstants.EP_STATE_ID);
    }

    public String getEpApplicationDomainId() {
        return getInfoFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_ID);
    }

    public String getEpApplicationDomainName() {
        return getInfoFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_NAME);
    }
    
    public String getInfoDescription() {
        return getInfoFieldByName(AsyncApiFieldConstants.INFO_DESCRIPTION);
    }

    public String getInfoTitle() {
        return getInfoFieldByName(AsyncApiFieldConstants.INFO_TITLE);
    }

    public String getInfoVersion() {
        return getInfoFieldByName(AsyncApiFieldConstants.INFO_VERSION);
    }

    public String getInfoFieldByName( String name ) {

        JsonElement element = asyncApiInfo.get(name);

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
