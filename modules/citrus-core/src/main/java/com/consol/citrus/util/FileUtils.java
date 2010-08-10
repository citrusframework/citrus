/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.util;

import java.io.*;
import java.util.*;

import org.springframework.core.io.Resource;

/**
 * Class to provide general file utilities, such as listing all XML files in a directory, 
 * or finding certain tests in a directory.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public abstract class FileUtils {

    /**
     * Read file resource to string value.
     * @param resource
     * @return
     * @throws IOException
     */
    public static String readToString(Resource resource) throws IOException {
        return readToString(resource.getInputStream());
    }
    
    /**
     * Read file input stream to string value.
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readToString(InputStream inputStream) throws IOException {
        BufferedInputStream reader = new BufferedInputStream(inputStream);
        StringBuilder builder = new StringBuilder();
        
        byte[] contents = new byte[1024];
        int bytesRead=0;
        while( (bytesRead = reader.read(contents)) != -1){
            builder.append(new String(contents, 0, bytesRead));
        }
        
        return builder.toString();
    }
    
    /**
     * Method to retrieve all test defining XML files in given directory.
     * Hierarchy of folders is supported.
     *
     * @param startDir the directory to hold the files
     * @return list of test files as filename paths
     */
    public static List<File> getTestFiles(final String startDir) {
        /* file names to be returned */
        final List<File> files = new ArrayList<File>();

        /* Stack to hold potential sub directories */
        final Stack<File> dirs = new Stack<File>();
        /* start directory */
        final File startdir = new File(startDir);
        
        if (startdir.isDirectory()) {
            dirs.push(startdir);
        }

        /* walk through the directories */
        while (dirs.size() > 0) {
            File file = dirs.pop();
            File[] found = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File tmp = new File(dir.getPath() + "/" + name);

                    /* Only allowing XML files as spring configuration files */
                    if ((name.endsWith(".xml") || tmp.isDirectory()) && !name.startsWith("CVS")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            for (int i = 0; i < found.length; i++) {
                /* Subfolder support */
                if (found[i].isDirectory()) {
                    dirs.push(found[i]);
                } else {
                    files.add(found[i]);
                }
            }
        }

        return files;
    }
}
