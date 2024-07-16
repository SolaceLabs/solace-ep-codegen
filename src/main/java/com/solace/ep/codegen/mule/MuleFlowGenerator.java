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

package com.solace.ep.codegen.mule;

import java.io.File;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.codegen.mapper.MuleDocMapper;
import com.solace.ep.codegen.mapper.asyncapi.AsyncApiToMuleDocMapper;
import com.solace.ep.codegen.mapper.model.MapMuleDoc;
import com.solace.ep.codegen.mule.model.core.MuleDoc;
import com.solace.ep.codegen.mule.util.XmlMapperUtils;
import com.solace.ep.codegen.util.FileUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to convert AsyncApi for Solace Applications generated from
 * Event Portal to Mule Flow XML for import into AnyPoint Studio
 */
@Slf4j
public class MuleFlowGenerator {
    
    /**
     * Reads AsyncApi from storage and writes Mule Flow XML to storage
     * Parameters are paths to input/output files.
     * @param inputAsyncApiFile
     * @param outputMuleXmlFile
     * @throws Exception
     */
    public static void writeMuleXmlFileFromAsyncApiFile( String inputAsyncApiFile, String outputMuleXmlFile ) 
                    throws Exception {

        try {
            final String asyncApiString = FileUtils.getFileAsString(inputAsyncApiFile);
            final MuleDoc muleDoc = createMuleDocFromAsyncApi( asyncApiString );
            writeMuleDocToXmlFile(muleDoc, outputMuleXmlFile);
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input file" );
            throw exc;
        }
    }

    /**
     * Accepts AsyncApi as String parameter and writes Mule Flow XML to storage
     * Parameter for AsyncApi is the content as String. Parameter for Mule XML
     * is path to output file.
     * @param inputAsyncApiString
     * @param outputMuleXmlFile
     * @throws Exception
     */
    public static void writeMuleXmlFileFromAsyncApiString( String inputAsyncApiString, String outputMuleXmlFile )
                    throws Exception {

        try {
            final MuleDoc muleDoc = createMuleDocFromAsyncApi( inputAsyncApiString );
            writeMuleDocToXmlFile(muleDoc, outputMuleXmlFile);
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input" );
            throw exc;
        }
    }

    /**
     * Reads AsyncApi from Storage and returns serialized Mule Flow XML content as String.
     * Parameter for AsyncApi is file path.
     * Returns null if the operation fails.
     * @param inputAsyncApiFile
     * @return
     */
    public static String getMuleDocXmlFromAsyncApiFile( String inputAsyncApiFile ) 
        throws Exception {

        try {
            final String asyncApiString = FileUtils.getFileAsString(inputAsyncApiFile);
            final MuleDoc muleDoc = createMuleDocFromAsyncApi( asyncApiString );
            return writeMuleDocToXmlString( muleDoc );
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input file" );
            throw exc;
        }
    }

    /**
     * Accepts AsyncApi as String parameter and returns serialized Mule Flow XML
     * content as String. Parameter for AsyncApi is the content as String.
     * Returns null if the operation fails.
     * @param inputAsyncApiString
     * @return
     */
    public static String getMuleDocXmlFromAsyncApiString( String inputAsyncApiString ) 
        throws Exception {

        try {
            final MuleDoc muleDoc = createMuleDocFromAsyncApi( inputAsyncApiString );
            return writeMuleDocToXmlString( muleDoc );
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input file" );
            throw exc;
        }
    }

    private static MuleDoc createMuleDocFromAsyncApi(
                            String inputAsyncApiString ) throws Exception {

        MapMuleDoc mapMuleDoc = AsyncApiToMuleDocMapper.mapMuleDocFromAsyncApi(inputAsyncApiString);
        MuleDocMapper muleDocMapper = new MuleDocMapper( mapMuleDoc );
        return muleDocMapper.createMuleDoc();
    }

    public static void writeMuleDocToXmlFile( MuleDoc muleDoc, String outputMuleXmlFile ) throws Exception {

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();
        xmlMapper.writeValue(new File(outputMuleXmlFile), muleDoc);
        log.info("Wrote Mule Flow XML to file: {}", outputMuleXmlFile);
    }

    public static String writeMuleDocToXmlString( MuleDoc muleDoc ) throws Exception {

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();
        return xmlMapper.writeValueAsString( muleDoc );
    }
}
