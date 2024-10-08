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

package com.solace.ep.codegen.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;

/**
 * File I/O utilities for MuleDoc Mapper
 */
@Slf4j
public class FileUtils {
    
    // The actual path separator doesn't matter; it's used as a String tokenizer
    private static final String
        STATIC_PATH_SEPARATOR = "/";

    /**
     * Copy file from source to target, parameters as Strings
     * Over-writes existing file
     * @param source
     * @param target
     * @throws Exception
     */
    public static void copyFile( String source, String target ) throws Exception {

        Path sourceFile = Paths.get( source );
        Path targetFile = Paths.get( target );
        
        Files.copy(
            sourceFile, 
            targetFile, 
            StandardCopyOption.REPLACE_EXISTING );

        log.info("Copied file: '{}' to '{}'", source, target);
    }

    /**
     * Write contents of String to a file, over-writing file contents if it exists
     * @param dataToWrite
     * @param dir - java.io.File object of target directory
     * @param fileName
     * @throws IOException
     */
    public static void writeStringToFile( String dataToWrite, File dir, String fileName )  
    throws IOException {
        writeStringToFile(dataToWrite, dir, fileName, false);
    }

    /**
     * Write contents of String to a file; option to append or over-write existing file
     * @param dataToWrite
     * @param dir - java.io.File object of target directory
     * @param fileName
     * @throws IOException
     */
    public static void writeStringToFile( String dataToWrite, File dir, String fileName, boolean append )  
    throws IOException {

        BufferedWriter writer = new BufferedWriter(
            new FileWriter( 
                new File( dir, fileName ), 
                append ) );
        writer.write(dataToWrite);
        writer.close();
    }

    /**
     * Write contents of String to a file; option to append or over-write existing file
     * @param dataToWrite
     * @param fileName
     * @param append
     * @throws IOException
     */
    public static void writeStringToFile( String dataToWrite, String fileName, boolean append )  
    throws IOException {

        BufferedWriter writer = new BufferedWriter(
            new FileWriter( 
                new File( fileName ), 
                append ) );
        writer.write(dataToWrite);
        writer.close();
    }

    /**
     * Write contents of String to a file, over-writing file contents if it exists
     * @param dataToWrite
     * @param fileName
     * @throws IOException
     */
    public static void writeStringToFile( String dataToWrite, String fileName )  
    throws IOException {

        writeStringToFile(dataToWrite, fileName, false);
    }

    /**
     * Create subdirectory in the given root
     * @param rootPath - java.io.File Object representing folder in which to create new directory
     * @param subPath
     * @return
     * @throws Exception
     */
    public static File createDirectory( File rootPath, String subPath ) throws Exception {

        StringTokenizer pathTokenizer = new StringTokenizer(subPath, STATIC_PATH_SEPARATOR);
        File newDirectory = rootPath;

        while ( pathTokenizer.hasMoreTokens() ) {
            String s = pathTokenizer.nextToken();
            File workingDir = new File(newDirectory, s);
            if ( ! workingDir.exists() ) {
                // Use mkdir() instead of mkdirs() to avoid file separator problems
                workingDir.mkdir();
            }
            // TODO - Error handling
            newDirectory = workingDir;
        }

        log.info("Created directory: {}", newDirectory);
        return newDirectory;
    }

    /**
     * Self-Explanatory
     * @param filePath
     * @return
     * @throws Exception
     */
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
