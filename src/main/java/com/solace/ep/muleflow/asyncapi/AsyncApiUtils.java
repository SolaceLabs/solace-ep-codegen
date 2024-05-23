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

import java.util.StringTokenizer;

public class AsyncApiUtils {

    public static final String
                    DEFAULT_MESSAGE_NAME = "MessageType";

    /**
     * RegEx expression to parse Solace topic elements
     * Purpose is to identify variable topic elements
     */
    public static final String
                    REGEX_SOLACE_TOPIC_CHAR_CLASS = "[" + 
                    "A-Za-z0-9" +
                    "\\!\\#\\$\\&\\(\\)\\*\\+\\," +
                    "\\-\\.\\:\\;\\=\\[\\]\\_\\~" +
                    "]";

    /**
     * Gen the last element from $ref element in AsyncApi field reference
     * Example:
     *   Value of "$ref": "#/components/messages/Order Created"
     *   Returns: 'Order Created'
     * @param ref
     * @return
     */
    public static String getLastElementFromRefString( String ref ) {
        StringTokenizer t = new StringTokenizer(ref, "/");
        if ( t.countTokens() < 2 ) {
            return null;
        }
        String lastElement = null;
        while ( t.hasMoreTokens() ) {
            lastElement = t.nextToken();
        }
        if ( lastElement == null || lastElement.length() == 0 ) {
            return null;
        } else {
            return lastElement;
        }
    }

    /**
     * Refactors Schema String to make definitions local to the schema instance
     * instead of relative to the asyncapi
     * @param schemaString
     * @param schemaRef
     * @return
     */
    public static String refactorJsonSchemaDefinitions( String schemaString, String schemaRef ) {
        return schemaString.replace( schemaRef + "/definitions", "#/definitions");
    }

}
