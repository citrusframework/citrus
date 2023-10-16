/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.docs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public abstract class AbstractTestDocsGenerator implements TestDocsGenerator {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String OVERVIEW_PLACEHOLDER = "+++++ OVERVIEW +++++";
    private static final String BODY_PLACEHOLDER = "+++++ BODY +++++";

    private static final String OUTPUT_DIRECTORY = "target" + File.separator + "docs";

    String srcDirectory = CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY;
    private String testDocTemplate;
    String outputFile;

    private List<File> testFiles = null;

    /**
     * Default constructor using template name.
     */
    AbstractTestDocsGenerator(final String outputFile, final String testDocTemplate) {
        this.outputFile = outputFile;
        this.testDocTemplate = testDocTemplate;
    }

    /**
     * Generates the test documentation.
     */
    public void generateDoc() {
        BufferedReader reader = null;
        FileOutputStream fos = null;
        BufferedOutputStream buffered = null;

        try {
            final Properties props = getTestDocProperties();

            fos = getFileOutputStream(outputFile);
            buffered = new BufferedOutputStream(fos);

            reader = new BufferedReader(new InputStreamReader(ExcelTestDocsGenerator.class.getResourceAsStream(testDocTemplate)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(OVERVIEW_PLACEHOLDER)) {
                    doHeader(buffered);
                } else if (line.trim().equalsIgnoreCase(BODY_PLACEHOLDER)) {
                    doBody(buffered);
                } else {
                    buffered.write((PropertyUtils.replacePropertiesInString(line, props) + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (final TransformerException | IOException | SAXException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    logger.error("Failed to close reader", e);
                }
            }

            if (buffered != null) {
                try {
                    buffered.flush();
                } catch (final IOException e) {
                    logger.error("Failed to close output stream", e);
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (final IOException e) {
                    logger.error("Failed to close file", e);
                }
            }
        }
    }

    /**
     * Creates a output file out put stream with given file name.
     * @return The output stream of the output file
     * @throws IOException If the stream couldn't be created
     */
    FileOutputStream getFileOutputStream(final String fileName) throws IOException {
        final File file = new File(OUTPUT_DIRECTORY);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new CitrusRuntimeException("Unable to create folder structure for test documentation");
            }
        }

        return new FileOutputStream(file.getAbsolutePath() + File.separator + fileName);
    }

    /**
     * Generates the test documentation.
     */
    public abstract void doBody(OutputStream buffered)
            throws TransformerException, IOException, SAXException;

    /**
     * Generates the test documentation.
     */
    public abstract void doHeader(OutputStream buffered)
            throws TransformerException, IOException, SAXException;

    /**
     * Gets the test doc properties.
     * @return
     */
    protected abstract Properties getTestDocProperties();

    /**
     * Gets all test files from test directory.
     * @return
     * @throws IOException
     */
    List<File> getTestFiles() throws IOException {
        if (testFiles == null) {
            testFiles = FileUtils.findFiles(Paths.get(srcDirectory, "resources").toString(), CitrusSettings.getXmlTestFileNamePattern());
        }

        return testFiles;
    }

    /**
     * Gets a document builder instance properly configured.
     * @return
     */
    DocumentBuilder getDocumentBuilder() {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            return documentBuilderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Gets a transformer with proper configuration.
     * @param fileName
     * @return
     */
    Transformer getTransformer(final String fileName, final String mediaType, final String method) {
        try {
            final Source source = new StreamSource(Resources.create(fileName, getClass()).getInputStream());

            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer t = factory.newTransformer(source);

            t.setOutputProperty(OutputKeys.MEDIA_TYPE, mediaType);
            t.setOutputProperty(OutputKeys.METHOD, method);

            return t;
        } catch (final TransformerException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * @param srcDirectory the srcDirectory to set
     */
    void setSrcDirectory(final String srcDirectory) {
        this.srcDirectory = srcDirectory;
    }

    /**
     * @return the srcDirectory
     */
    public String getSrcDirectory() {
        return srcDirectory;
    }

    /**
     * @param outputFile the outputFile to set
     */
    void setOutputFile(final String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * @return the outputFile
     */
    String getOutputFile() {
        return outputFile;
    }

    /**
     * Gets the outputDirectory.
     * @return the outputDirectory
     */
    static String getOutputDirectory() {
        return OUTPUT_DIRECTORY;
    }
}
