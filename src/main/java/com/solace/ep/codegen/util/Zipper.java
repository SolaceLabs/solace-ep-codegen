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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to create compressed archive of source directory
 * Adapted from example found at:
 *     https://www.baeldung.com/java-compress-and-uncompress
 */
@Slf4j
public class Zipper {

    private static int BYTE_SZ = 1024;
    
    /**
     * Create new zipped archive from directory
     * @param dirToZip - java.io.File of input directory
     * @param compressedFilePath
     * @throws Exception
     */
    public static void zipDirectoryToArchive( File dirToZip, String compressedFilePath ) throws Exception {
        FileOutputStream fos = new FileOutputStream(compressedFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        log.info("Creating archive of directory: {} --> {}", dirToZip.getAbsolutePath(), compressedFilePath);
        Exception exc = null;
        try {
            zipFile(dirToZip, dirToZip.getName(), zipOut);
        } catch ( Exception e ) {
            log.error("Caught exception while creating archive: {}", compressedFilePath);
            exc = e;
        } finally {
            zipOut.close();
            fos.close();
        }
        if ( exc != null ) {
            throw exc;
        }
        log.info("Finished creating archive: {}", compressedFilePath);
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) 
        throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[ BYTE_SZ ];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

}
