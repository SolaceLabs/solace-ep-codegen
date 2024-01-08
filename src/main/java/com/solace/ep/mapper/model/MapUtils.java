package com.solace.ep.mapper.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MapUtils {
    
    public static String getDefaultSolaceConfigurationName() {
        return "Solace_PubSub__Connector_Config";
    }

    public static String getDefaultSolaceConfigurationDocName() {
        return "Solace PubSub+ Connector Config";
    }

    public static String getFlowNameFromQueueName( String queueName ) {
        if ( queueName == null || queueName.length() == 0 ) {
            queueName = "INPUT.QUEUE";
        }
        return String.format("Ingress.%s.queue", queueName);
    }

    public static String getBizLogicSubFlowNameFromQueueName( String queueName ) {
        if ( queueName == null || queueName.length() == 0 ) {
            queueName = "INPUT.QUEUE";
        }
        return String.format("BizLogic.%s.queue", queueName);
    }

    public static String getTransformMessageStub() {
        return 
            "%dw 2.0\n" +
            "output application/java\n" +
            "---\n" +
            "{\n}";
    }

    public static String getTransformMessageDefaultDocName() {
        return "Transform Message";
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
