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
package com.solace.ep.iflow;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;
import org.omg.spec.bpmn._20100524.model.TDefinitions;

import com.solace.ep.asyncapi.tests.TestAsyncApiParsing;
import com.solace.ep.muleflow.mapper.asyncapi.AsyncApiToMuleDocMapper;
import com.solace.ep.muleflow.mapper.model.MapMuleDoc;
import com.solace.ep.muleflow.mapper.sap.iflow.SapIflowExtensionConfig;
import com.solace.ep.muleflow.mapper.sap.iflow.SapIflowMapper;
import com.solace.ep.muleflow.mapper.sap.iflow.SapIflowUtils;
import com.solace.ep.muleflow.mapper.sap.iflow.model.TSapIflowProperty;
import com.solace.ep.muleflow.mapper.sap.iflow.utils.Bpmn2NamespaceMapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestCreateIflowFromEpAsyncApi {
    
    @Test
    public void testExtConfigs() {

        try {
            SapIflowExtensionConfig configs = SapIflowUtils.parseExtensionConfig("src/main/resources/sap/iflow/extension-elements.yaml");

            for ( SapIflowExtensionConfig.ExtProperty prop : configs.getCollaboration() ) {
                System.out.println( "key: " + prop.getKey() );
                System.out.println( "val: " + ( prop.getValue() == null ? "NULL" : prop.getValue() ) );
            }

            for ( SapIflowExtensionConfig.ExtProperty prop : configs.getMessageFlow().getAllMessageFlows() ) {
                System.out.println( "key: " + prop.getKey() );
                System.out.println( "val: " + ( prop.getValue() == null ? "NULL" : prop.getValue() ) );
            }

        } catch ( Exception exc ) {
            System.out.println(exc.getMessage());
            fail( exc.getMessage() );
        }
    }

    @Test
    public void testCreateIflow_01() throws Exception {

        String inputFile = "src/test/resources/asyncapi/Order Management-0.1.2.json";
        String outputFile = "src/test/resources/test-output/iflow/OrderMgt-0.1.2.xml";

        MapMuleDoc mapMuleDoc =
        createIflowFromAsyncApi( inputFile, outputFile );

        SapIflowMapper iflowMapper = new SapIflowMapper();

        iflowMapper.createSapIflow(mapMuleDoc);

        // TDefinitions td = iflowMapper.getOut();

        try {
            JAXBContext context = JAXBContext.newInstance( 
                    TDefinitions.class,
                    TSapIflowProperty.class,
                    com.solace.ep.muleflow.mapper.sap.iflow.model.ObjectFactory.class
                 );
            
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper", new Bpmn2NamespaceMapper() );

            marshaller.marshal( iflowMapper.getJaxbOut(), new FileOutputStream( outputFile ) );
        } catch ( JAXBException jaxbExc ) {
            log.error( jaxbExc.getMessage() );
            System.out.println( jaxbExc.getMessage() );
            jaxbExc.printStackTrace();
            fail( jaxbExc.getMessage() );
        } catch ( FileNotFoundException fnfExc ) {
            log.error( fnfExc.getMessage() );
            System.out.println( fnfExc.getMessage() );
            fnfExc.printStackTrace();
            fail( fnfExc.getMessage() );
        }

        assertTrue( true );

    }

    private static MapMuleDoc createIflowFromAsyncApi( String inputFile, String outputFile ) {

        final String asyncApi = TestAsyncApiParsing.getAsyncApi(inputFile);

        MapMuleDoc mapMuleDoc = null;

        try {
            mapMuleDoc = AsyncApiToMuleDocMapper.mapMuleDocFromAsyncApi(asyncApi);
        }
        catch ( Exception exc ) {
            log.error(exc.getMessage());
            System.out.println( exc.getCause().getMessage() );
        }

        if ( mapMuleDoc == null ) {
            fail( "Failed to create map doc" );
        }

        return mapMuleDoc;
    }
}
