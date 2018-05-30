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

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

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

    public final static String FILE_PATH_CHARSET_PARAMETER = ";charset=";

    /** Simulation mode required for Citrus administration UI when loading test cases from Java DSL */
    private static boolean simulationMode = false;

    /**
     * Prevent instantiation.
     */
    private FileUtils() {
        super();
    }

    /**
     * Sets the simulation mode.
     */
    public static void setSimulationMode(boolean mode) {
        simulationMode = mode;
    }

    /**
     * Read file resource to string value with default charset settings.
     * @param resource
     * @return
     * @throws IOException
     */
    public static String readToString(Resource resource) throws IOException {
        return readToString(resource, getDefaultCharset());
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
     * Read file content to string value with default charset settings.
     * @param file
     * @return
     * @throws IOException
     */
    public static String readToString(File file) throws IOException {
         return readToString(new FileInputStream(file), getDefaultCharset());
    }

    /**
     * Read file resource to string value.
     * @param resource
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readToString(Resource resource, Charset charset) throws IOException {
        if (simulationMode) {
            if (resource instanceof ClassPathResource) {
                return ((ClassPathResource) resource).getPath();
            } else if (resource instanceof FileSystemResource) {
                return ((FileSystemResource) resource).getPath();
            } else {
                return resource.getFilename();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Reading file resource: '%s' (encoding is '%s')", resource.getFilename(), charset.displayName()));
        }
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
        return new String(FileCopyUtils.copyToByteArray(inputStream), charset);
    }

    /**
     * Writes inputStream content to file. Uses default charset encoding.
     * @param inputStream
     * @param file
     */
    public static void writeToFile(InputStream inputStream, File file) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
            writeToFile(FileCopyUtils.copyToString(inputStreamReader), file, getDefaultCharset());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write file", e);
        }
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
            log.debug(String.format("Writing file resource: '%s' (encoding is '%s')", file.getName(), charset.displayName()));
        }

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new CitrusRuntimeException("Unable to create folder structure for file: " + file.getPath());
            }
        }

        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
            fos.write(content.getBytes(charset));
            fos.flush();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write file", e);
        }
    }

    /**
     * Method to retrieve all files with given file name pattern in given directory.
     * Hierarchy of folders is supported.
     *
     * @param startDir the directory to hold the files
     * @param fileNamePatterns the file names to include
     * @return list of test files as filename paths
     */
    public static List<File> findFiles(final String startDir, final Set<String> fileNamePatterns) {
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
            final File file = dirs.pop();
            File[] foundFiles = file.listFiles((dir, name) -> {
                File tmp = new File(dir.getPath() + File.separator + name);

                boolean accepted = tmp.isDirectory();

                for (String fileNamePattern : fileNamePatterns) {
                    if (fileNamePattern.contains("/")) {
                        fileNamePattern = fileNamePattern.substring(fileNamePattern.lastIndexOf('/') + 1);
                    }

                    fileNamePattern = fileNamePattern.replace(".", "\\.").replace("*", ".*");

                    if (name.matches(fileNamePattern)) {
                        accepted = true;
                    }
                }

                /* Only allowing XML files as spring configuration files */
                return accepted && !name.startsWith("CVS") && !name.startsWith(".svn") && !name.startsWith(".git");
            });

            for (File found : Optional.ofNullable(foundFiles).orElse(new File[] {})) {
                /* Subfolder support */
                if (found.isDirectory()) {
                    dirs.push(found);
                } else {
                    files.add(found);
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
        if (filePath.contains(FILE_PATH_CHARSET_PARAMETER)) {
            return new PathMatchingResourcePatternResolver().getResource(
                    context.replaceDynamicContentInString(filePath.substring(0, filePath.indexOf(FileUtils.FILE_PATH_CHARSET_PARAMETER))));
        } else {
            return new PathMatchingResourcePatternResolver().getResource(
                    context.replaceDynamicContentInString(filePath));
        }
    }

    /**
     * Reads file resource from path with variable replacement support.
     * @param filePath
     * @return
     */
    public static Resource getFileResource(String filePath) {
        String path;

        if (filePath.contains(FILE_PATH_CHARSET_PARAMETER)) {
            path = filePath.substring(0, filePath.indexOf(FileUtils.FILE_PATH_CHARSET_PARAMETER));
        } else {
            path = filePath;
        }

        if (path.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            return new FileSystemResource(path.substring(ResourceUtils.FILE_URL_PREFIX.length() - 1));
        } else if (path.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            return new PathMatchingResourcePatternResolver().getResource(path);
        }

        Resource file = new FileSystemResource(path);
        if (!file.exists()) {
            return  new PathMatchingResourcePatternResolver().getResource(path);
        }

        return file;
    }

    /**
     * Gets the default charset. If set by Citrus system property (citrus.file.encoding) use
     * this one otherwise use system default.
     * @return
     */
    public static Charset getDefaultCharset() {
        return Charset.forName(Citrus.CITRUS_FILE_ENCODING);
    }

    /**
     * Extract charset information from file path. If not set return default charset. Charset
     * is read as path parameter at the end of the file path {@see FileUtils.FILE_PATH_CHARSET_PARAMETER}
     * @param path
     * @return
     */
    public static Charset getCharset(String path) {
        if (path.contains(FileUtils.FILE_PATH_CHARSET_PARAMETER)) {
            return Charset.forName(path.substring(path.indexOf(FileUtils.FILE_PATH_CHARSET_PARAMETER) + FileUtils.FILE_PATH_CHARSET_PARAMETER.length()));
        } else {
            return FileUtils.getDefaultCharset();
        }
    }
}
