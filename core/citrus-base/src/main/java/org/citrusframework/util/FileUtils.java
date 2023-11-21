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

package org.citrusframework.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.citrusframework.CitrusSettings;
import org.citrusframework.TestSource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to provide general file utilities, such as listing all XML files in a directory,
 * or finding certain tests in a directory.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public abstract class FileUtils {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final String FILE_EXTENSION_JAVA = ".java";
    public static final String FILE_EXTENSION_XML = ".xml";
    public static final String FILE_EXTENSION_GROOVY = ".groovy";
    public static final String FILE_EXTENSION_YAML = ".yaml";
    public static final String FILE_PATH_CHARSET_PARAMETER = CitrusSettings.getFilePathCharsetParameter();

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
            resource.getLocation();
        }

        if (!resource.exists()) {
            throw new CitrusRuntimeException("Failed to read resource %s - does not exist".formatted(resource.getLocation()));
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Reading file resource: '%s' (encoding is '%s')", resource.getLocation(), charset.displayName()));
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
        if (inputStream == null) {
            throw new CitrusRuntimeException("Failed to read resource %s - input stream is empty");
        }

        return new String(inputStream.readAllBytes(), charset);
    }

    /**
     * Writes inputStream content to file. Uses default charset encoding.
     * @param inputStream
     * @param file
     */
    public static void writeToFile(InputStream inputStream, File file) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Writing file resource: '%s'", file.getName()));
        }

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new CitrusRuntimeException("Unable to create folder structure for file: " + file.getPath());
            }
        }

        try (inputStream) {
            Files.copy(inputStream, file.toPath());
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
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Writing file resource: '%s' (encoding is '%s')", file.getName(), charset.displayName()));
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
        final List<File> files = new ArrayList<>();

        /* Stack to hold potential subdirectories */
        final Stack<File> dirs = new Stack<>();
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
                    if (name.matches(fileNamePattern)) {
                        accepted = true;
                        break;
                    }
                }

                /* Only allowing XML files as spring configuration files */
                return accepted && !name.startsWith("CVS") && !name.startsWith(".svn") && !name.startsWith(".git");
            });

            for (File found : Optional.ofNullable(foundFiles).orElseGet(() -> new File[] {})) {
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
        return getFileResource(context.replaceDynamicContentInString(filePath));
    }

    /**
     * Reads file resource from path with variable replacement support.
     * @param filePath
     * @return
     */
    public static Resource getFileResource(String filePath) {
        String path;
        if (filePath.contains(FileUtils.FILE_PATH_CHARSET_PARAMETER)) {
            path = filePath.substring(0, filePath.indexOf(FileUtils.FILE_PATH_CHARSET_PARAMETER));
        } else {
            path = filePath;
        }

        return Resources.create(path);
    }

    /**
     * Gets the default charset. If set by Citrus system property (citrus.file.encoding) use
     * this one otherwise use system default.
     * @return
     */
    public static Charset getDefaultCharset() {
        return Charset.forName(CitrusSettings.CITRUS_FILE_ENCODING);
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

    /**
     * Extract file extension from given path.
     * @param path
     * @return
     */
    public static String getFileExtension(String path) {
        if (path.indexOf(".") > 0) {
            return path.substring(path.lastIndexOf(".") + 1);
        }

        return "";
    }

    /**
     * Load properties from file. Supports XML and key-value format.
     * @param resource
     * @return
     */
    public static Properties loadAsProperties(Resource resource) {
        Properties properties = new Properties();
        try (InputStream is = resource.getInputStream()) {
            String filename = getFileName(resource.getLocation());
            if (filename != null && filename.endsWith(FILE_EXTENSION_XML)) {
                properties.loadFromXML(is);
            } else {
                properties.load(is);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load properties from file", e);
        }

        return properties;
    }

    /**
     * Gets the file name from given file path.
     * @param path
     * @return
     */
    public static String getFileName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        int separatorIndex = path.replace("\\", "/").lastIndexOf("/");
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
    }

    /**
     * Remove file extension from file name.
     * @param fileName
     * @return
     */
    public static String getBaseName(String fileName) {
        if (fileName == null) {
            return null;
        }

        if (fileName.indexOf('.') > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }

        return fileName;
    }

    /**
     * Gets the base path of given file path removing any file name if present.
     * @param filePath
     * @return
     */
    public static String getBasePath(String filePath) {
        if (filePath == null) {
            return null;
        }

        String fileSeparator;
        if (filePath.contains("/")) {
            fileSeparator = "/";
        } else if (filePath.contains("\\")) {
            fileSeparator = "\\";
        } else {
            fileSeparator = File.separator;
        }

        if (filePath.contains(fileSeparator)) {
            return filePath.substring(0, filePath.lastIndexOf(fileSeparator));
        }

        return filePath;
    }

    public static byte[] copyToByteArray(File file) {
        if (file == null) {
            return new byte[0];
        }

        try (InputStream in = Files.newInputStream(file.toPath())) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read file content", e);
        }
    }

    public static byte[] copyToByteArray(Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            if (in == null) {
                throw new CitrusRuntimeException(String.format("Unable to access input stream of resource %s", resource.getLocation()));
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read resource", e);
        }
    }

    public static byte[] copyToByteArray(InputStream inputStream) {
        try (inputStream) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read input stream", e);
        }
    }

    /**
     * Construct content type information with given charset parameter.
     * @param contentType
     * @param charset
     * @return
     */
    public static String constructContentType(String contentType, Charset charset) {
        return contentType + FILE_PATH_CHARSET_PARAMETER + charset;
    }

    /**
     * Read String representation and construct proper test source instance.
     * Extract source type from give file extension and try to set proper test source name from given file name.
     *
     * @param sourceFile
     * @return
     */
    public static TestSource getTestSource(String sourceFile) {
        String ext = getFileExtension(sourceFile);
        String name = getFileName(sourceFile);
        return new TestSource(ext, name, sourceFile);
    }
}
