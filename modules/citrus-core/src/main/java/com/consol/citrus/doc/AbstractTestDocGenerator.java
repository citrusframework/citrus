/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.doc;

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractTestDocGenerator implements TestDocGenerator {
    
    /** Logger */
    protected Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String OVERVIEW_PLACEHOLDER = "+++++ OVERVIEW +++++";
    private static final String BODY_PLACEHOLDER = "+++++ BODY +++++";

    private static final String OUTPUT_DIRECTORY = "test-output" + File.separator + "doc";
    
    protected String srcDirectory = Citrus.DEFAULT_TEST_SRC_DIRECTORY;
    protected String testDocTemplate;
    protected String outputFile;
    
    private List<File> testFiles = null;
    
    /**
     * Default constructor using template name.
     */
    public AbstractTestDocGenerator(String outputFile, String testDocTemplate) {
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
            Properties props = getTestDocProperties();
            
            fos = getFileOutputStream(outputFile);
            buffered = new BufferedOutputStream(fos);

            reader = new BufferedReader(new InputStreamReader(ExcelTestDocGenerator.class.getResourceAsStream(testDocTemplate)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(OVERVIEW_PLACEHOLDER)) {
                    doHeader(buffered);
                } else if (line.trim().equalsIgnoreCase(BODY_PLACEHOLDER)) {
                    doBody(buffered);
                } else {
                    buffered.write((PropertyUtils.replacePropertiesInString(line, props) + "\n").getBytes("UTF-8"));
                }
            }
        } catch (TransformerException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (SAXException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Failed to close reader", e);
                }
            }
            
            if (buffered != null) {
                try {
                    buffered.flush();
                } catch (IOException e) {
                    log.error("Failed to close output stream", e);
                }
            }
            
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Failed to close file", e);
                }
            }
        }
    }

    /**
     * Creates a output file out put stream with given file name.
     * @return
     * @throws IOException 
     */
    protected FileOutputStream getFileOutputStream(String fileName) throws IOException {
        File file = new File(OUTPUT_DIRECTORY);
        if (!file.exists()) {
            boolean success = file.mkdirs();
            
            if (!success) {
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
    protected List<File> getTestFiles() throws IOException {
        if (testFiles == null) {
            testFiles = FileUtils.findFiles(srcDirectory + "resources" + File.separator, Citrus.getXmlTestFileNamePattern());
        }
        
        return testFiles;
    }
    
    /**
     * Gets a document builder instance properly configured.
     * @return
     */
    protected DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Gets a transformer with proper configuration.
     * @param fileName
     * @return
     */
    protected Transformer getTransformer(String fileName, String mediaType, String method) {
        try {
            Source source = new StreamSource(new ClassPathResource(fileName, getClass()).getInputStream());
            
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer t = factory.newTransformer(source);

            t.setOutputProperty(OutputKeys.MEDIA_TYPE, mediaType);
            t.setOutputProperty(OutputKeys.METHOD, method);
            
            return t;
        } catch (TransformerException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * @param srcDirectory the srcDirectory to set
     */
    public void setSrcDirectory(String srcDirectory) {
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
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * @return the outputFile
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Gets the outputDirectory.
     * @return the outputDirectory
     */
    public static String getOutputDirectory() {
        return OUTPUT_DIRECTORY;
    }
}
