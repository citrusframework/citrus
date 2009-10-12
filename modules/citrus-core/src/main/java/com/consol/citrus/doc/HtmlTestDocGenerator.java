/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.doc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;

/**
 * Class to automatically generate a list of all available tests.
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 02.03.2007
 */
public class HtmlTestDocGenerator {
    private final static String DEFAULT_OUTPUT_FILE = "doc/consol/test_documentation.html";
    private final static String DEFAULT_XSLT_SOURCE = "generate-html-doc-2.0.xsl";
    private final static String DEFAULT_TEST_DIRECTORY = "tests";
    private final static String DEFAULT_TESTDOC_TEMPLATE = "testdoc.html.template";
    private final static String DEFAULT_PROPERTIES_FILE = "testdoc.properties";

    private final static String OVERVIEW_PLACEHOLDER = "+++++ OVERVIEW +++++";
    private final static String BODY_PLACEHOLDER = "+++++ BODY +++++";

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HtmlTestDocGenerator.class);

    public static void main(String[] args) {
        try {
            String xslSource;
            if (args.length > 0) {
                xslSource = args[0];
            } else {
                xslSource = DEFAULT_XSLT_SOURCE;
            }

            String filename;
            if (args.length > 1) {
                filename = args[1];
            } else {
                filename = DEFAULT_OUTPUT_FILE;
            }

            String testDirectory;
            if (args.length > 2) {
                testDirectory = args[2];
            } else {
                testDirectory = DEFAULT_TEST_DIRECTORY;
            }

            String testdocTemplate;
            if (args.length > 3) {
                testdocTemplate = args[3];
            } else {
                testdocTemplate = DEFAULT_TESTDOC_TEMPLATE;
            }

            Properties props = new Properties();
            if (args.length > 4) {
                Resource testdocProperties = new ClassPathResource(args[4]);
                props.load(testdocProperties.getInputStream());
            } else {
                props.load(HtmlTestDocGenerator.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE));
            }

            List<File> testFiles = FileUtils.getTestFiles(testDirectory);

            Source xsl = new StreamSource(HtmlTestDocGenerator.class.getResourceAsStream(xslSource), 
                    HtmlTestDocGenerator.class.getResource(xslSource).getPath());
            
            log.info("XSLT stylesheet was set: " + xsl.getSystemId());

            TransformerFactory factory = TransformerFactory.newInstance();

            Transformer t = factory.newTransformer(xsl);
            log.info("XSL transformer was created");

            t.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/html");
            t.setOutputProperty(OutputKeys.METHOD, "html");

            File outputFile = new File(filename);
            FileOutputStream fos = new FileOutputStream(outputFile);
            OutputStream buffered = new BufferedOutputStream(fos);
            StreamResult res = new StreamResult(buffered);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            documentBuilderFactory.setNamespaceAware(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(HtmlTestDocGenerator.class.getResourceAsStream(testdocTemplate)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(OVERVIEW_PLACEHOLDER) == false) {
                    buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
                } else {
                    break;
                }
            }

            int maxEntries = testFiles.size() / new Integer(props.getProperty("overview.columns")).intValue();

            buffered.write("<td style=\"border:1px solid #bbbbbb;\">".getBytes());
            buffered.write("<ol>".getBytes());

            for (int i = 0; i < testFiles.size(); i++) {
                if (i != 0 && i % maxEntries == 0 && testFiles.size() - i >= maxEntries) {
                    buffered.write("</ol>".getBytes());
                    buffered.write("</td>".getBytes());
                    buffered.write("<td style=\"border:1px solid #bbbbbb;\">".getBytes());
                    buffered.write(("<ol start=\"" + (i+1) + "\">").getBytes());
                }

                buffered.write("<li>".getBytes());
                buffered.write(("<a href=\"#" + i + "\">").getBytes());
                buffered.write(testFiles.get(i).toString().getBytes());
                buffered.write("</a>".getBytes());
            }

            buffered.write("</ol>".getBytes());
            buffered.write("</td>".getBytes());

            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(BODY_PLACEHOLDER) == false) {
                    buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
                } else {
                    break;
                }
            }

            int testNumber = 1;
            for (File testFile : testFiles) {
                buffered.write("<tr>".getBytes());

                log.info("Working on test " + testFile.getName());

                Source xml = new DOMSource(builder.parse(testFile));
                buffered.write(("<td style=\"border:1px solid #bbbbbb\">" + testNumber + ".</td>").getBytes());

                buffered.write("<td style=\"border:1px solid #bbbbbb\">".getBytes());
                t.transform(xml, res);
                buffered.write(("<a name=\"" + testNumber + "\" href=\"file:///" + testFile.getAbsolutePath() + "\">" + testFile.getName() + "</a>").getBytes());
                buffered.write("</td>".getBytes());

                buffered.write("</tr>".getBytes());
            }

            while ((line = reader.readLine()) != null) {
                buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
            }

            buffered.flush();
            fos.flush();
            fos.close();
            
        } catch (IOException e) {
            log.error("Error during doc generation", e);
        } catch (TransformerException e) {
            log.error("Error during doc generation", e);
        } catch (SAXException e) {
            log.error("Error during doc generation", e);
        } catch (ParserConfigurationException e) {
            log.error("Error during doc generation", e);
        }
    }
}
