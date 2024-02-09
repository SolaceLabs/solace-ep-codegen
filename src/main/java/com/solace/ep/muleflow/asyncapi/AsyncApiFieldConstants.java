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

/**
 * Class containing static field names in standard AsyncApi
 */
public class AsyncApiFieldConstants {
    
    public static final String
                    INFO_DESCRIPTION = "description",
                    INFO_TITLE = "title",
                    INFO_VERSION = "version";

    public static final String
                    API_ASYNCAPI = "asyncapi";

    public static final String
                    API_SCHEMAS = "#/components/schemas",
                    API_MESSAGES = "#/components/messages",
                    API_CHANNELS = "#/channels",
                    API_INFO = "#/info";

    public static final String 
                    API_SCHEMA_FORMAT = "schemaFormat",
                    API_PAYLOAD = "payload",
                    API_CONTENT_TYPE = "contentType",
                    API_SCHEMA_TITLE = "title";

    public static final String
                    API_$REF = "$ref";

    public static final String
                    OP_PUBLISH = "publish",
                    OP_SUBSCRIBE = "subscribe",
                    CHANNEL_PARAMETERS = "parameters",
                    OP_MESSAGE = "message";
}
