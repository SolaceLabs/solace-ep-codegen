package com.solace.ep.mapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.solace.ep.mapper.model.MapConfig;

public class MapUtils {
    
    public static final String
                    DEFAULT_ACKMODE = "AUTOMATIC_ON_FLOW_COMPLETION",
                    DEFAULT_CONFIG_REF = "Solace_PubSub__Connector_Config",
                    DEFAULT_SOLACE_CONFIG_NAME = "Solace_PubSub__Connector_Config",
                    DEFAULT_SOLACE_CONFIG_DOC_NAME = "Solace PubSub+ Connector Config",
                    DEFAULT_TRANSFORM_MESSAGE_DOC_NAME = "Transform Message";

    public static final String
                    FLOW_REF_DOC_NAME_TO_BIZ_LOGIC = "Business Logic",
                    FLOW_REF_DOC_NAME_TO_EGRESS = "Connect this to the correct egress flow";

    public static final String
                    GLOBAL_PROPERTY_DOC_NAME = "Global Property",
                    GLOBAL_NAME_EP_APP_VERSION_ID = "epApplicationVersionId";

    public static final String
                    MSG_TYPE_TEXT_MESSAGE = "TEXT_MESSAGE",
                    MSG_TYPE_BYTES_MESSAGE = "BYTES_MESSAGE";

    private static final String
                    TOPIC = "topic",
                    QUEUE = "queue";

    public static String getFlowNameFromDesignation( String designation, boolean isDirectConsumer ) {
        if ( designation == null || designation.length() == 0 ) {
            designation = "UNKNOWN";
        }
        return String.format("Ingress.%s.%s", designation, ( isDirectConsumer ? TOPIC : QUEUE ) );
    }

    public static String getFlowDocNameFromDesignation( String designation ) {
        if ( designation == null || designation.length() == 0 ) {
            designation = "UNKNOWN";
        }
        return String.format("%s Listener", designation);
    }

    public static String getBizLogicSubFlowNameFromDesignation( String designation, boolean isDirectConsumer ) {
        if ( designation == null || designation.length() == 0 ) {
            designation = "UNKNOWN";
        }
        return String.format("BizLogic.%s.%s", designation, ( isDirectConsumer ? TOPIC : QUEUE ) );
    }

    public static String getTransformMessageStub() {
        return 
            "%dw 2.0\n" +
            "output application/java\n" +
            "---\n" +
            "{\n}";
    }

    public static String getEgressSubFlowNameFromMessageName( String messageName ) {
        if ( messageName == null || messageName.length() == 0 ) {
            messageName = "OutputMessage";
        }
        return String.format("Egress.%s.topic", messageName.replace(" ", ""));
    }

    public static String getEgressSubFlowDocNameFromMessageName( String messageName ) {
        if ( messageName == null || messageName.length() == 0 ) {
            messageName = "OutputMessage";
        }
        return String.format("Publish %s Event", messageName );
    }

    public static String getSetVariableDocNameForTopicParameter( String parameter ) {
        return "Dynamic Topic Parameter: " + parameter;
    }

    public static MapConfig getDefaultSolaceConfiguration() {
        
        MapConfig mapConfig = new MapConfig();

        mapConfig.setConfigName( MapUtils.DEFAULT_SOLACE_CONFIG_NAME );
        mapConfig.setEpCloudApiToken( "eySetYourCloudApiTokenHere" );
        mapConfig.setConnectBrokerHost("http://mr-connection-service.messaging.solace.cloud:55443");
        mapConfig.setConnectMsgVpn("defaultVpn");
        mapConfig.setConnectClientUserName("defaultClientUser");
        mapConfig.setConnectPassword("defaultPassword1");

        return mapConfig;
    }
    
    public static byte[] getMd5Digest( String value ) {

        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            digest = md.digest();
        } catch ( NoSuchAlgorithmException nsaExc ) {
        }

        return digest;
    }

    public static boolean md5DigestMatches( byte[] v1, byte[] v2 ) {

        if ( v1.length != v2.length ) {
            return false;
        }
        for ( int i = 0; i < v1.length; i++ ) {
            if ( v1[ i ] != v2[ i ] ) {
                return false;
            }
        }
        return true;
    }
}
