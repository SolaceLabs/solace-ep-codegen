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

package com.solace.ep.codegen.asyncapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class to access fields in AsyncApi 'info' element
 * parsed as Gson 'JsonObject'
 */
public class AsyncApiInfo {
    
    private JsonObject asyncApiInfo;

    /**
     * Public constructor - requires the asyncapi 'info' object as JsonObject
     * @param info
     */
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
