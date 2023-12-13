package com.solace.ep.mule.tests;

public class TestData {
    
    public static String getSampleJson1() {
        String s = "{\"namespace\": \"io.confluent.examples.streams.avro.microservices\",\n" + //
                        " \"type\": \"record\",\n" + //
                        " \"name\": \"Shipping\",\n" + //
                        " \"fields\": [\n" + //
                        "     {\"name\": \"id\", \"type\": \"string\"},\n" + //
                        "     {\"name\": \"customerId\", \"type\": \"long\"},\n" + //
                        "     {\"name\": \"trackingNumber\", \"type\": \"int\"}\n" + //
                        " ]\n" + //
                        "}";
        return s;
    }
}
