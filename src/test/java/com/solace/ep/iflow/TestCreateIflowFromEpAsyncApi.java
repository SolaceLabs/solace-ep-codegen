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


import org.junit.jupiter.api.Test;

import com.solace.ep.codegen.asyncapi.mapper.AsyncApiToMuleDocMapper;
import com.solace.ep.codegen.internal.model.MapMuleDoc;
import com.solace.ep.codegen.sap.iflow.SapIFlowGenerator;
import com.solace.ep.codegen.sap.iflow.SapIflowUtils;
import com.solace.ep.codegen.sap.iflow.model.config.SapIflowExtensionConfig;
import com.solace.ep.codegen.util.FileUtils;


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

            assertTrue( configs.getCallActivity() != null );

        } catch ( Exception exc ) {
            System.out.println(exc.getMessage());
            fail( exc.getMessage() );
        }
    }

    @Test
    public void testCreateIflow_02() {

        String inputFile = "src/test/resources/asyncapi/Order Management-0.1.2.json";
        String outputFile = "src/test/resources/test-output/iflow/OrderMgt-0.1.2.xml";

        try {
            SapIFlowGenerator.writeSapIflowFileFromAsyncApiFile(inputFile, outputFile);
        } catch (Exception exc) {
            exc.printStackTrace();
            fail( exc.getMessage() );
        }
    }

    @Test
    void testCreateIflow_03() {

        String inputFile = "src/test/resources/asyncapi/Shipping Service-0.1.2.json";
        String outputFile = "src/test/resources/test-output/iflow/Shipping Service-0.1.2.xml";

        try {
            MapMuleDoc input = createMapMuleDocFromAsyncApiFile(inputFile);
            String iflowXml = SapIFlowGenerator.getSapIflowFromMapDoc(input);
            System.out.println( iflowXml );
            FileUtils.writeStringToFile(iflowXml, outputFile);
        } catch ( Exception exc ) {
            exc.printStackTrace();
            fail( exc.getMessage() );
        }
    }

    @Test void testCreateIflow_NoPublishers_04() {

        String inputFile = "src/test/resources/asyncapi/Shipping Service-NoPublishers.json";
        String outputFile = "src/test/resources/test-output/iflow/Shipping Service-NoPublishers.xml";

        try {
            MapMuleDoc input = createMapMuleDocFromAsyncApiFile(inputFile);
            String iflowXml = SapIFlowGenerator.getSapIflowFromMapDoc(input);
            System.out.println( iflowXml );
            FileUtils.writeStringToFile(iflowXml, outputFile);
        } catch ( Exception exc ) {
            exc.printStackTrace();
            fail( exc.getMessage() );
        }
    }

    @Test void testCreateIflow_NoConsumers_05() {

        String inputFile = "src/test/resources/asyncapi/Shipping Service-NoConsumers.json";
        String outputFile = "src/test/resources/test-output/iflow/Shipping Service-NoConsumers.xml";

        try {
            MapMuleDoc input = createMapMuleDocFromAsyncApiFile(inputFile);
            String iflowXml = SapIFlowGenerator.getSapIflowFromMapDoc(input);
            System.out.println( iflowXml );
            FileUtils.writeStringToFile(iflowXml, outputFile);
        } catch ( Exception exc ) {
            exc.printStackTrace();
            fail( exc.getMessage() );
        }
    }

    @Test void testCreateIflow_ShippingOneToOne_06() {

        final String inputFile = "src/test/resources/sap/iflow/stack-tests/Shipping Service-0.1.2.json";
        final String outputFile = "src/test/resources/test-output/iflow/stack-tests/Shipping Service-0.1.2.xml";

        try {
            final MapMuleDoc input = createMapMuleDocFromAsyncApiFile(inputFile);
            final String iflowXml = SapIFlowGenerator.getSapIflowFromMapDoc(input);
            System.out.println( iflowXml );
            FileUtils.writeStringToFile(iflowXml, outputFile);
        } catch ( Exception exc ) {
            exc.printStackTrace();
            fail( exc.getMessage() );
        }
    }

    @Test void testCreateIflow_ERPManyPublishers_07() {

        final String inputFile = "src/test/resources/sap/iflow/stack-tests/ERP System-1.0.0.json";
        final String outputFile = "src/test/resources/test-output/iflow/stack-tests/ERP System-1.0.0.json.xml";

        try {
            final MapMuleDoc input = createMapMuleDocFromAsyncApiFile(inputFile);
            final String iflowXml = SapIFlowGenerator.getSapIflowFromMapDoc(input);
            System.out.println( iflowXml );
            FileUtils.writeStringToFile(iflowXml, outputFile);
        } catch ( Exception exc ) {
            exc.printStackTrace();
            fail( exc.getMessage() );
        }
    }

    @Test void testCreateIflow_UpdateInventory_08() {

        final String inputFile = "src/test/resources/asyncapi/UpdateInventory-0.1.0 (1).yaml";
        final String outputFile = "src/test/resources/test-output/iflow/stack-tests/UpdateInventory-0.1.0 (1).json.xml";

        try {
            final MapMuleDoc input = createMapMuleDocFromAsyncApiFile(inputFile);
            final String iflowXml = SapIFlowGenerator.getSapIflowFromMapDoc(input);
            System.out.println( iflowXml );
            FileUtils.writeStringToFile(iflowXml, outputFile);
        } catch ( Exception exc ) {
            exc.printStackTrace();
            fail( exc.getMessage() );
        }
    }

    private static MapMuleDoc createMapMuleDocFromAsyncApiFile( String inputFile ) throws Exception {
        final String asyncApi = FileUtils.getFileAsString(inputFile);
        return AsyncApiToMuleDocMapper.mapMuleDocFromAsyncApi(asyncApi);
    }
}
