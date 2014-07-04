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

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Class to provide general file utilities, such as listing all XML files in a directory, 
 * or finding certain tests in a directory.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public abstract class FileUtils {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Prevent instantiation.
     */
    private FileUtils() {
    }

    /**
     * Read file resource to string value with default charset settings.
     * @param resource
     * @return
     * @throws IOException
     */
    public static String readToString(Resource resource) throws IOException {
        return readToString(resource.getInputStream(), getDefaultCharset());
    }

    /**
     * Read file input stream to string value with default charset settings.
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readToString(InputStream inputStream) throws IOException {
         return readToString(inputStream, getDefaultCharset());
    }
    
    /**
     * Read file resource to string value.
     * @param resource
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readToString(Resource resource, Charset charset) throws IOException {
        return readToString(resource.getInputStream(), charset);
    }
    
    /**
     * Read file input stream to string value.
     * @param inputStream
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readToString(InputStream inputStream, Charset charset) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Reading file resource (encoding is '" + charset.displayName() + "')");
        }

        return new String(FileCopyUtils.copyToByteArray(inputStream), charset);
    }

    /**
     * Writes String content to file. Uses default charset encoding.
     * @param content
     * @param file
     */
    public static void writeToFile(String content, File file) {
        writeToFile(content, file, getDefaultCharset());
    }

    /**
     * Writes String content to file with given charset encoding. Automatically closes file output streams when done.
     * @param content
     * @param file
     */
    public static void writeToFile(String content, File file, Charset charset) {
        if (log.isDebugEnabled()) {
            log.debug("Writing file resource (encoding is '" + charset.displayName() + "')");
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes(charset));
            fos.flush();
        } catch (FileNotFoundException e) {
            throw new CitrusRuntimeException("Failed to write file", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write file", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.warn("Unable to close file output stream", e);
                }
            }
        }
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
        
        if (!startdir.exists()) {
            throw new CitrusRuntimeException("Test directory " + startdir.getAbsolutePath() + " does not exist");
        }
        
        if (startdir.isDirectory()) {
            dirs.push(startdir);
        }

        /* walk through the directories */
        while (dirs.size() > 0) {
            File file = dirs.pop();
            File[] found = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File tmp = new File(dir.getPath() + File.separator + name);

                    /* Only allowing XML files as spring configuration files */
                    return (name.endsWith(".xml") || tmp.isDirectory()) && !name.startsWith("CVS") && !name.startsWith(".svn");
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

    /**
     * Reads file resource from path with variable replacement support.
     * @param filePath
     * @param context
     * @return
     */
    public static Resource getFileResource(String filePath, TestContext context) {
        return new PathMatchingResourcePatternResolver().getResource(
                context.replaceDynamicContentInString(filePath));
    }

    /**
     * Gets the default charset. If set by Citrus system property (citrus.file.encoding) use
     * this one otherwise use system default.
     * @return
     */
    private static Charset getDefaultCharset() {
        return Charset.forName(System.getProperty(CitrusConstants.CITRUS_FILE_ENCODING,
                    Charset.defaultCharset().displayName()));
    }
}
