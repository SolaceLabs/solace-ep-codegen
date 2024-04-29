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
package com.solace.ep.muleflow.mapper.sap.iflow;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;

import org.omg.spec.bpmn._20100524.model.TDefinitions;

import com.solace.ep.muleflow.mapper.asyncapi.AsyncApiToMuleDocMapper;
import com.solace.ep.muleflow.mapper.model.MapMuleDoc;
import com.solace.ep.muleflow.mapper.sap.iflow.model.bpmn_ifl_ext.TSapIflowProperty;
import com.solace.ep.muleflow.mapper.sap.iflow.utils.Bpmn2NamespaceMapper;
import com.solace.ep.muleflow.util.FileUtils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SapIFlowGenerator {
    
        /**
     * Reads AsyncApi from storage and writes SAP IFlow to storage
     * Parameters are paths to input/output files.
     * @param inputAsyncApiFile
     * @param outputSapIFlowFile
     * @throws Exception
     */
    public static void writeSapIflowFileFromAsyncApiFile( String inputAsyncApiFile, String outputSapIFlowFile ) 
                    throws Exception {

        try {
            final String asyncApiString = FileUtils.getFileAsString(inputAsyncApiFile);
            final JAXBElement<TDefinitions> bpmnDoc = createIFlowFromAsyncApiString( asyncApiString );
            writeSapIflowToXmlFile(bpmnDoc, outputSapIFlowFile);
        } catch ( Exception exc ) {
            log.error( "Failed to create SAP IFlow output file from the AsyncApi input file" );
            throw exc;
        }
    }

    /**
     * Accepts AsyncApi as String parameter and writes SAP IFlow to storage
     * Parameter for AsyncApi is the content as String. 
     * @param inputAsyncApiString
     * @param outputSapIFlowFile
     * @throws Exception
     */
    public static void writeSapIflowFileFromAsyncApiString( String inputAsyncApiString, String outputSapIFlowFile )
                    throws Exception {

        try {
            final JAXBElement<TDefinitions> bpmnDoc = createIFlowFromAsyncApiString( inputAsyncApiString );
            writeSapIflowToXmlFile(bpmnDoc, outputSapIFlowFile);
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input" );
            throw exc;
        }
    }

    /**
     * Accepts MapDoc as input paramter and writes SAP IFlow to storage
     * @param input
     * @param outputSapIFlowFile
     * @throws Exception
     */
    public static void writeSapIflowFileFromMapDoc( MapMuleDoc input, String outputSapIFlowFile )
                    throws Exception {

        try {
            final JAXBElement<TDefinitions> bpmnDoc = createIFlowFromMapDoc( input );
            writeSapIflowToXmlFile(bpmnDoc, outputSapIFlowFile);
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the input" );
            throw exc;
        }
    }

    /**
     * Accepts AsyncApi input String and returns SAP IFlow as XML String
     * @param inputAsyncApiString
     * @return
     * @throws Exception
     */
    public static String getSapIflowFromAsyncApiString( String inputAsyncApiString ) 
    throws Exception {

        try {
            final JAXBElement<TDefinitions> bpmnDoc = createIFlowFromAsyncApiString( inputAsyncApiString );
            return getSapIflowToXmlString( bpmnDoc );
        } catch ( Exception exc ) {
            log.error( "Failed to create SAP BPMN2 Iflow XML output file from the AsyncApi input file" );
            throw exc;
        }
    }

    /**
     * Accepts AsyncApi input file and returns SAP IFlow as XML String
     * @param inputAsyncApiFile
     * @return
     * @throws Exception
     */
    public static String getSapIflowFromAsyncApiFile( String inputAsyncApiFile ) 
    throws Exception {

        try {
            final String asyncApiString = FileUtils.getFileAsString(inputAsyncApiFile);
            final JAXBElement<TDefinitions> bpmnDoc = createIFlowFromAsyncApiString( asyncApiString );
            return getSapIflowToXmlString( bpmnDoc );
        } catch ( Exception exc ) {
            log.error( "Failed to create SAP BPMN2 Iflow XML output file from the AsyncApi input file" );
            throw exc;
        }
    }

    /**
     * Return SAP Iflow as serialized XML
     * @param input
     * @return
     * @throws Exception
     */
    public static String getSapIflowFromMapDoc( MapMuleDoc input ) 
    throws Exception {

        try {
            final JAXBElement<TDefinitions> bpmnDoc = createIFlowFromMapDoc(input);
            return getSapIflowToXmlString( bpmnDoc );
        } catch ( Exception exc ) {
            log.error( "Failed to create SAP BPMN2 Iflow XML output file from the AsyncApi input file" );
            throw exc;
        }
    }

    /**
     * Creates JAXBElement<TDefinitions> representing SAP IFlow BPMN2 data model
     * @param inputAsyncApiString
     * @return
     * @throws Exception
     */
    public static JAXBElement<TDefinitions> createIFlowFromAsyncApiString(
                            String inputAsyncApiString ) throws Exception {

        MapMuleDoc mapMuleDoc = AsyncApiToMuleDocMapper.mapMuleDocFromAsyncApi(inputAsyncApiString);
        SapIflowMapper sapIflowMapper = new SapIflowMapper( mapMuleDoc );
        return sapIflowMapper.createSapIflowAsJAXBElement();
    }

    /**
     * Accepts MapMuleDoc input to create SAP Iflow output
     * @param input
     * @return
     * @throws Exception
     */
    public static JAXBElement<TDefinitions> createIFlowFromMapDoc( 
                            MapMuleDoc input ) throws Exception {

        SapIflowMapper sapIflowMapper = new SapIflowMapper( input );
        return sapIflowMapper.createSapIflowAsJAXBElement();
    }

    /**
     * Accepts JAXBElement<TDefinitions> representing SAP IFlow BPMN2 data model and writes
     * serialized XML output to a file
     * @param sapJaxbIflow
     * @param outputSapIflowXmlFile
     * @throws Exception
     */
    public static void writeSapIflowToXmlFile( JAXBElement<TDefinitions> sapJaxbIflow, String outputSapIflowXmlFile ) throws Exception {

        try (FileOutputStream fileOutputStream = new FileOutputStream( outputSapIflowXmlFile )) {
            fileOutputStream.write( getSapIflowToXmlString(sapJaxbIflow).getBytes() );
        } catch ( FileNotFoundException fnfexc ) {
            log.error( fnfexc.getMessage() );
            throw fnfexc;
        }
        log.info("Wrote SAP BPMN2 IFlow XML to file: {}", outputSapIflowXmlFile);
    }

    /**
     * Accepts JAXBElement<TDefintions> representing SAP IFlow BPMN2 data model and returns
     * serialized XML output as a string
     * @param iFlowDoc
     * @return
     * @throws Exception
     */
    public static String getSapIflowToXmlString( JAXBElement<TDefinitions> iFlowDoc ) throws Exception {

        try ( StringWriter stringWriter = new StringWriter() ) {
            JAXBContext context = JAXBContext.newInstance( 
                    TDefinitions.class,
                    TSapIflowProperty.class //,
            );
            
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper", new Bpmn2NamespaceMapper() );
            marshaller.marshal( iFlowDoc, stringWriter );

            return stringWriter.toString();
            
        } catch ( JAXBException jaxbExc ) {
            log.error( jaxbExc.getMessage() );
            throw jaxbExc;
        }
    }
}
