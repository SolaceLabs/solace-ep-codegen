package com.solace.ep.muleflow.util;

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
