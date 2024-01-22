package com.solace.ep.muleflow.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtls {
    
    public static void copyFile( String source, String target ) throws Exception {

        Path sourceFile = Paths.get( source );
        Path targetFile = Paths.get( target );
        
        Files.copy(
            sourceFile, 
            targetFile, 
            StandardCopyOption.REPLACE_EXISTING );

    }
}
