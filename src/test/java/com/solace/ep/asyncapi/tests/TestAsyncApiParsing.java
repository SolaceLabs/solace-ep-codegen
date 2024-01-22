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
    public void parseTopLevel() {

        final String asyncApi = getAsyncApiSample1();

        JsonObject parsedAsyncApi = AsyncApiAccessor.parseAsyncApi(asyncApi);

        assertTrue( parsedAsyncApi.has( "components" ) );
        assertTrue( parsedAsyncApi.has( "asyncapi" ) );
        assertTrue( parsedAsyncApi.has( "info" ) );
        assertTrue( parsedAsyncApi.has( "channels" ) );

    }

    @Test
    public void getTopLevel() {
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
    public void validateInfo() {

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
    public void validateMessage() {

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
    public void validateSchemasAccessors() {
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
    
    @Test void validateSchemaAccessFromMessage() {
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

    @Test void validateAsyncApiPublishChannels() {
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
    void validateAsyncApiSubscribeChannels() {
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
}
