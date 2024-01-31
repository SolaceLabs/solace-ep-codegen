package com.solace.ep.asyncapi.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.solace.ep.muleflow.asyncapi.AsyncApiAccessor;
import com.solace.ep.muleflow.asyncapi.AsyncApiChannel;
import com.solace.ep.muleflow.asyncapi.AsyncApiMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestAsyncApiParsing {

    @Test
    public void testParseTopLevel() {

        final String asyncApi = getAsyncApiSample1();

        JsonObject parsedAsyncApi = AsyncApiAccessor.parseAsyncApi(asyncApi);

        assertTrue( parsedAsyncApi.has( "components" ) );
        assertTrue( parsedAsyncApi.has( "asyncapi" ) );
        assertTrue( parsedAsyncApi.has( "info" ) );
        assertTrue( parsedAsyncApi.has( "channels" ) );

    }

    @Test
    public void testGetTopLevel() {
        final String asyncApi = getAsyncApiSample1();

        AsyncApiAccessor accessor = new AsyncApiAccessor( AsyncApiAccessor.parseAsyncApi(asyncApi) );

        try {
            assertTrue( accessor.getChannels() != null);
            assertTrue( accessor.getInfo() != null);
            assertTrue( accessor.getMessages() !=null );
            assertTrue( accessor.getSchemas() !=null);
            String version = accessor.getAsyncapiVersion();
            assertTrue(version.contentEquals("2.5.0"));
        } catch ( Exception e ) {
            fail(e.getLocalizedMessage());
        }

    }

    @Test
    public void testValidateInfo() {

        final String asyncApi = getAsyncApiSample1();

        AsyncApiAccessor accessor = new AsyncApiAccessor( AsyncApiAccessor.parseAsyncApi(asyncApi) );

        try {
            
            assertTrue( accessor.getInfo().getEpApplicationVersion().contentEquals("0.1.2"));
            assertTrue( accessor.getInfo().getEpApplicationVersionId().contentEquals("angoiawro24"));
            assertTrue( accessor.getInfo().getEpApplicationId().contentEquals("eampiojgi4"));
            assertTrue( accessor.getInfo().getInfoDescription().contentEquals("A streaming service leveraging the solace Streams API. This service reacts to orders as they are created, updating the Shipping topic as notifications are received from the delivery company.\n\n[GitHub Source](https://github.com/confluentinc/solace-streams-examples)"));
            assertTrue( accessor.getInfo().getEpStateName().contentEquals("RELEASED"));
            assertTrue( accessor.getInfo().getInfoTitle().contentEquals("Shipping Service"));
            assertTrue( accessor.getInfo().getEpApplicationDomainId().contentEquals("aregpij409"));
            assertTrue( accessor.getInfo().getInfoVersion().contentEquals("0.1.2"));
            assertTrue( accessor.getInfo().getEpStateId().contentEquals("2"));
            assertTrue( accessor.getInfo().getEpApplicationDomainName().contentEquals("Shipping"));

        } catch ( Exception exc ) {
            log.error( exc.getMessage() );
            fail( exc.getMessage() );
        }
    }

    @Test
    public void testValidateMessage() {

        final String asyncApi = getAsyncApiSample1();

        AsyncApiAccessor accessor = new AsyncApiAccessor(AsyncApiAccessor.parseAsyncApi(asyncApi));

        try {
            AsyncApiMessage message1 = accessor.getMessageByName("Catalogue Updated");

            assertTrue( message1.getEpEventId().contentEquals("dummyeventid"));
            assertTrue( message1.getEpVersionDisplayName().contentEquals(""));
            assertTrue( message1.getDescription().contentEquals("Changes in items available for purchase from the store and their descriptions."));
            assertTrue( message1.getEpApplicationDomainId().contentEquals("dummyappdomain10"));
            assertTrue( message1.getSchemaFormat().contentEquals("application/vnd.apache.avro+json;version=1.9.0"));
            assertTrue( message1.getEpEventStateName().contentEquals("RELEASED"));
            assertTrue( message1.getEpShared().contentEquals("true"));
            assertTrue( message1.getEpApplicationDomainName().contentEquals("Merchandising"));
            assertTrue( message1.getEpEventVersionId().contentEquals("eventversionfake100"));
            assertTrue( message1.getEpEventVersion().contentEquals("1.0.2"));
            assertTrue( message1.getEpEventName().contentEquals("Catalogue Updated"));
            assertTrue( message1.getContentType().contentEquals("application/json"));
            assertTrue( message1.getEpEventStateId().contentEquals("2"));

            assertTrue( message1.getPayloadAsString().contains("UNDERPANTS") );
            System.out.println( message1.getPayloadAsString() );
        } catch ( Exception e ) {
            fail(e.getLocalizedMessage());
        }
     
    }

    @Test
    public void testValidateSchemasAccessors() {
        final String asyncApi = getAsyncApiSample1();

        AsyncApiAccessor accessor = new AsyncApiAccessor(AsyncApiAccessor.parseAsyncApi(asyncApi));

        try {
            String schema1 = accessor.getSchemaByName("Category");
            String schema2 = accessor.getSchemaAsReference("#/components/schemas/Tag");

            System.out.println(schema1);
            System.out.println(schema2);

            assertTrue( schema1.contains("properties"));
            assertTrue(schema2.contains("properties"));
        } catch (Exception exc) {

            fail(exc.getLocalizedMessage());
        }
    }
    
    @Test void testValidateSchemaAccessFromMessage() {
        final String asyncApi = getAsyncApiSample1();

        AsyncApiAccessor accessor = new AsyncApiAccessor(AsyncApiAccessor.parseAsyncApi(asyncApi));

        try {
            AsyncApiMessage m = accessor.getMessageByName("Test Message");
            String schema1 = m.getPayloadAsString();

            System.out.println(schema1);

            assertTrue(schema1.contains("integer"));
        } catch( Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test void testValidateAsyncApiPublishChannels() {
        final String asyncApi = getAsyncApiSample1();

        AsyncApiAccessor accessor = new AsyncApiAccessor(AsyncApiAccessor.parseAsyncApi(asyncApi));

        try {

            for ( String channelId : accessor.getChannels().keySet() ) {

                AsyncApiChannel channel = new AsyncApiChannel( accessor.getChannels().get(channelId).getAsJsonObject(), accessor );

                if ( channel.hasPublishOperation() ) {
                    System.out.println( channel.getPublishOpMessage().getPayloadAsString() );

                    assertTrue( 
                        channel.getPublishOpMessage().getPayloadAsString().contains("namespace") ||
                        channel.getPublishOpMessage().getPayloadAsString().contains("properties"));

                    for (String p : channel.getParameters() ) {
                        System.out.println( "Parameter: " + p );
                    }

                    String queueName = channel.getPublishQueueName();

                    if (queueName != null) {
                        System.out.println("QueueName: " + queueName);
                        for ( String s : channel.getPublishQueueSubscriptions() ) {
                            
                            assertTrue( s.startsWith("acmeretail") );
                            
                            System.out.println("Subscription: " + s);
                        }
                    }

                    System.out.println();
                }
            }

        } catch( Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test 
    void testValidateAsyncApiSubscribeChannels() {
        final String asyncApi = getAsyncApiSample1();

        AsyncApiAccessor accessor = new AsyncApiAccessor(AsyncApiAccessor.parseAsyncApi(asyncApi));

        try {

            for ( String channelId : accessor.getChannels().keySet() ) {

                AsyncApiChannel channel = new AsyncApiChannel( accessor.getChannels().get(channelId).getAsJsonObject(), accessor );

                if ( channel.hasSubscribeOperation() ) {
                    System.out.println( channel.getSubscribeOpMessage().getPayloadAsString() );

                    assertTrue( 
                        channel.getSubscribeOpMessage().getPayloadAsString().contains("namespace") ||
                        channel.getSubscribeOpMessage().getPayloadAsString().contains( "properties" ));

                    for (String p : channel.getParameters() ) {
                        System.out.println( "Parameter: " + p );
                    }

                    System.out.println();
                }
            }

        } catch( Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    public static String getAsyncApiSample1() {

        String fileName = "src/test/resources/asyncapi/sample1.json";
        Path path = Paths.get(fileName);
        StringBuilder data = new StringBuilder();
        try {
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for ( String s : allLines ) {
                data.append(s);
                data.append('\n');
            }
        } catch (Exception exc) {
            log.error( exc.getLocalizedMessage() );
            return null;
        }
        return data.toString();
    }

    public static String getAsyncApiSample2() {

        String fileName = "src/test/resources/asyncapi/sample2.json";
        Path path = Paths.get(fileName);
        StringBuilder data = new StringBuilder();
        try {
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for ( String s : allLines ) {
                data.append(s);
                data.append('\n');
            }
        } catch (Exception exc) {
            log.error( exc.getLocalizedMessage() );
            return null;
        }
        return data.toString();
    }

    public static String getAsyncApi( String filePath ) {

        Path path = Paths.get(filePath);
        StringBuilder data = new StringBuilder();
        try {
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for ( String s : allLines ) {
                data.append(s);
                data.append('\n');
            }
        } catch (Exception exc) {
            log.error( exc.getLocalizedMessage() );
            return null;
        }
        return data.toString();
    }

    public static String getSampleAsyncApi() {
        return "{\"components\":{\"schemas\":{\"Invoice_Received\":{\"x-ep-schema-version\":\"0.1.0\",\"x-ep-schema-version-id\":\"i5j8l2ga066\",\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"x-ep-schema-state-name\":\"DRAFT\",\"x-ep-schema-name\":\"Invoice Received\",\"title\":\"Invoice Received\",\"type\":\"object\",\"x-ep-application-domain-id\":\"bex7ddi7ke5\",\"required\":[\"event_type\",\"event_id\",\"timestamp\",\"invoice\"],\"x-ep-schema-version-displayname\":\"0.1.0\",\"x-ep-shared\":\"true\",\"x-ep-application-domain-name\":\"Acme Retail - Supply Chain Optimisation\",\"x-ep-schema-state-id\":\"1\",\"x-ep-schema-id\":\"jj5la1u8rj8\",\"properties\":{\"event_type\":{\"type\":\"string\",\"enum\":[\"Invoice Received\"]},\"event_id\":{\"type\":\"string\"},\"invoice\":{\"type\":\"object\",\"properties\":{\"total_price\":{\"type\":\"number\"},\"due_date\":{\"format\":\"date\",\"type\":\"string\"},\"supplier_name\":{\"type\":\"string\"},\"invoice_number\":{\"type\":\"string\"},\"supplier_id\":{\"type\":\"string\"},\"items\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"quantity\":{\"type\":\"integer\"},\"total_price\":{\"type\":\"number\"},\"product_id\":{\"type\":\"string\"},\"unit_price\":{\"type\":\"number\"},\"product_name\":{\"type\":\"string\"}},\"required\":[\"product_id\",\"product_name\",\"quantity\",\"unit_price\",\"total_price\"]}},\"invoice_date\":{\"format\":\"date\",\"type\":\"string\"},\"status\":{\"type\":\"string\",\"enum\":[\"Pending\",\"Paid\",\"Partially Paid\",\"Cancelled\"]}},\"required\":[\"invoice_number\",\"supplier_name\",\"supplier_id\",\"invoice_date\",\"due_date\",\"items\",\"total_price\",\"status\"]},\"timestamp\":{\"format\":\"date-time\",\"type\":\"string\"}}}},\"messages\":{\"Invoice_Received\":{\"x-ep-event-id\":\"dvmxhwp5t38\",\"x-ep-event-version-displayname\":\"0.1.0\",\"description\":\"\",\"x-ep-application-domain-id\":\"bex7ddi7ke5\",\"schemaFormat\":\"application/vnd.aai.asyncapi+json;version=2.0.0\",\"x-ep-event-state-name\":\"DRAFT\",\"x-ep-shared\":\"false\",\"x-ep-application-domain-name\":\"Acme Retail - Supply Chain Optimisation\",\"x-ep-event-version-id\":\"w15nstc9joz\",\"payload\":{\"$ref\":\"#/components/schemas/Invoice_Received\"},\"x-ep-event-version\":\"0.1.0\",\"x-ep-event-name\":\"Invoice Received\",\"contentType\":\"application/json\",\"x-ep-event-state-id\":\"1\"}}},\"channels\":{\"acmeRetail/sc/procurement/invoicing/invoice/received/v1/{supplierID}/{invoiceID}/{invoiceAmount}\":{\"subscribe\":{\"message\":{\"$ref\":\"#/components/messages/Invoice_Received\"}},\"parameters\":{\"supplierID\":{\"schema\":{\"type\":\"string\"},\"x-ep-parameter-name\":\"supplierID\"},\"invoiceID\":{\"schema\":{\"type\":\"string\"},\"x-ep-parameter-name\":\"invoiceID\"},\"invoiceAmount\":{\"schema\":{\"type\":\"string\"},\"x-ep-parameter-name\":\"invoiceAmount\"}}}},\"asyncapi\":\"2.5.0\",\"info\":{\"x-ep-application-version\":\"0.1.0\",\"x-ep-application-version-id\":\"oawbyn6fili\",\"x-ep-application-id\":\"1i9tjh7t65x\",\"description\":\"An accounts payable system is a financial management system that helps businesses track and manage their outstanding bills and invoices from suppliers and vendors. Its main functions include processing and recording vendor invoices, managing payments and cash flow, and ensuring timely and accurate payment of bills to vendors. The system helps businesses streamline their payment processes, reduce errors and fraud, and improve their financial visibility and control. Accounts payable systems typically integrate with other financial and procurement systems to provide end-to-end automation of the procure-to-pay process.\",\"x-ep-displayname\":\"0.1.0\",\"x-ep-state-name\":\"DRAFT\",\"title\":\"Accounts Payable\",\"x-ep-application-domain-id\":\"bex7ddi7ke5\",\"version\":\"0.1.0\",\"x-ep-state-id\":\"1\",\"x-ep-application-domain-name\":\"Acme Retail - Supply Chain Optimisation\"}}";
    }

    @Test 
    void testValidateAsyncApiSubscribeChannels_() {
        final String asyncApi = getSampleAsyncApi();

        AsyncApiAccessor accessor = new AsyncApiAccessor(AsyncApiAccessor.parseAsyncApi(asyncApi));

        try {

            for ( String channelId : accessor.getChannels().keySet() ) {

                AsyncApiChannel channel = new AsyncApiChannel( accessor.getChannels().get(channelId).getAsJsonObject(), accessor );

                if ( channel.hasSubscribeOperation() ) {
                    System.out.println( channel.getSubscribeOpMessage().getPayloadAsString() );

                    assertTrue( 
                        channel.getSubscribeOpMessage().getPayloadAsString().contains("namespace") ||
                        channel.getSubscribeOpMessage().getPayloadAsString().contains( "properties" ));

                    for (String p : channel.getParameters() ) {
                        System.out.println( "Parameter: " + p );
                    }

                    System.out.println();
                }
            }

        } catch( Exception e) {
            fail(e.getLocalizedMessage());
        }
    }



}
