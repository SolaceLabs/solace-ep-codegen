package com.solace.ep.muleflow.asyncapi;

import java.util.StringTokenizer;

public class AsyncApiUtils {

    public static final String
                    DEFAULT_MESSAGE_NAME = "MessageType";

    public static final String
                    REGEX_SOLACE_TOPIC_CHAR_CLASS = "[" + 
                    "A-Za-z0-9" +
                    "\\!\\#\\$\\&\\(\\)\\*\\+\\," +
                    "\\-\\.\\:\\;\\=\\[\\]\\_\\~" +
                    "]";

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
