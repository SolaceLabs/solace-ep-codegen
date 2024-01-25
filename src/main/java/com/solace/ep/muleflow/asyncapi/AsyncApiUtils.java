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

}
