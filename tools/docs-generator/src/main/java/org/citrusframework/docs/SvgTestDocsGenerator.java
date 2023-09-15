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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Class to automatically generate a visual representation of a {@link org.citrusframework.TestCase} in SVG.
 * 
 * @author Christoph Deppisch
 * @since 2007
 */
public final class SvgTestDocsGenerator extends AbstractTestDocsGenerator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SvgTestDocsGenerator.class);

    /**
     * Default constructor.
     */
    public SvgTestDocsGenerator() {
        super("", "");
    }
    
    /**
     * Generates the test documentation.
     */
    public void generateDoc() {
        FileOutputStream fos = null;
        BufferedOutputStream buffered = null;
        
        Transformer t = getTransformer("generate-svg-doc.xslt", "text/xml", "xml");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        try {
            List<File> testFiles = getTestFiles();

            for (File testFile : testFiles) {
                logger.info("Working on test " + testFile.getName());

                fos = getFileOutputStream(testFile.getName().substring(0, testFile.getName().lastIndexOf('.')) + ".svg");
                buffered = new BufferedOutputStream(fos);
                
                Source xml = new DOMSource(getDocumentBuilder().parse(testFile));
                StreamResult res = new StreamResult(buffered);
                
                t.transform(xml, res);
                
                logger.info("Finished test " + testFile.getName());
                
                buffered.flush();
                fos.close();
            }
        } catch (TransformerException e) {
            throw new CitrusRuntimeException(e);
        } catch (SAXException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            if (buffered != null) {
                try {
                    buffered.flush();
                } catch (IOException e) {
                    logger.error("Failed to close output stream", e);
                }
            }
            
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("Failed to close file", e);
                }
            }
        }
    }
    
    /**
     * Builds a new test doc generator.
     * @return
     */
    public static SvgTestDocsGenerator build() {
        return new SvgTestDocsGenerator();
    }
    
    /**
     * Adds a custom test source directory.
     * @param testDir the test source directory.
     * @return
     */
    public SvgTestDocsGenerator useSrcDirectory(String testDir) {
        this.setSrcDirectory(testDir);
        return this;
    }
    
    @Override
    public void doBody(OutputStream buffered) throws TransformerException,
            IOException, SAXException {
        // no body information here.
    }

    @Override
    public void doHeader(OutputStream buffered) throws TransformerException,
            IOException, SAXException {
        // no header information here.
    }

    @Override
    protected Properties getTestDocProperties() {
        return null;
    }
}
