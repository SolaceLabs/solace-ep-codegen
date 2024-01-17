package com.solace.ep.muleflow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.solace.ep.mapper.MuleDocMapper;
import com.solace.ep.mapper.asyncapi.AsyncApiToMuleDocMapper;
import com.solace.ep.mapper.model.MapMuleDoc;
import com.solace.ep.mule.model.core.MuleDoc;
import com.solace.ep.mule.model.util.XmlMapperUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MuleFlowGenerator {
    
    public static void writeMuleXmlFileFromAsyncApiFile( String inputAsyncApiFile, String outputMuleXmlFile ) 
                    throws Exception {

        try {
            final String asyncApiString = getFileAsString(inputAsyncApiFile);
            final MuleDoc muleDoc = createMuleDocFromAsyncApi( asyncApiString );
            writeMuleDocToXmlFile(muleDoc, outputMuleXmlFile);
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input file" );
            throw exc;
        }
    }

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

    public static String getMuleDocXmlFromAsyncApiFile( String inputAsyncApiFile ) {

        try {
            final String asyncApiString = getFileAsString(inputAsyncApiFile);
            final MuleDoc muleDoc = createMuleDocFromAsyncApi( asyncApiString );
            return writeMuleDocToXmlString( muleDoc );
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input file" );
            return null;
        }
    }

    public static String getMuleDocXmlFromAsyncApiString( String inputAsyncApiString ) {

        try {
            final MuleDoc muleDoc = createMuleDocFromAsyncApi( inputAsyncApiString );
            return writeMuleDocToXmlString( muleDoc );
        } catch ( Exception exc ) {
            log.error( "Failed to create Mule Doc XML output file from the AsyncApi input file" );
            return null;
        }
    }

    public static MuleDoc createMuleDocFromAsyncApi(
                            String inputAsyncApiString ) throws Exception {

        MapMuleDoc mapMuleDoc = AsyncApiToMuleDocMapper.mapMuleDocFromAsyncApi(inputAsyncApiString);
        MuleDocMapper muleDocMapper = new MuleDocMapper();
        return muleDocMapper.createMuleDoc(mapMuleDoc);
    }

    private static void writeMuleDocToXmlFile( MuleDoc muleDoc, String outputMuleXmlFile ) throws Exception {

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();
        xmlMapper.writeValue(new File(outputMuleXmlFile), muleDoc);
        log.info("Wrote Mule Flow XML to file: {}", outputMuleXmlFile);
    }

    private static String writeMuleDocToXmlString( MuleDoc muleDoc ) throws Exception {

        XmlMapper xmlMapper = XmlMapperUtils.createXmlMapperForMuleDoc();
        return xmlMapper.writeValueAsString( muleDoc );
    }

    public static String getFileAsString( String filePath ) throws Exception {

        StringBuilder data = new StringBuilder();
        try {
            Path path = Paths.get(filePath);
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);

            for ( String s : allLines ) {
                data.append(s);
                data.append('\n');
            }
        } catch (InvalidPathException ipExc) {
            log.error("The file path '{}' is invalid", filePath);
            log.error( ipExc.getMessage() );
            throw ipExc;
        } catch (IOException ioExc) {
            log.error("Error reading file '{}' -- Does the file exist?", filePath);
            log.error( ioExc.getMessage() );
            throw ioExc;
        } catch (Exception exc) {
            log.error( "Failed to read file {}", filePath );
            log.error( exc.getLocalizedMessage() );
            throw exc;
        }
        log.info("Read contents of file '{}'", filePath);
        return data.toString();
    }
}
