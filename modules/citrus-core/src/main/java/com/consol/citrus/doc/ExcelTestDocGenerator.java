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

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
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
public class ExcelTestDocGenerator {
    private final static String DEFAULT_OUTPUT_FILE = "doc/consol/test_documentation.xls";
    private final static String DEFAULT_XSLT_SOURCE = "generate-xls-doc-2.0.xslt";
    private final static String DEFAULT_TEST_DIRECTORY = "tests";
    private final static String DEFAULT_TESTDOC_TEMPLATE = "testdoc.xls.template";
    private final static String DEFAULT_PROPERTIES_FILE = "testdoc.properties";

    //	private final static String OVERVIEW_PLACEHOLDER = "+++++ OVERVIEW +++++";
    private final static String BODY_PLACEHOLDER = "+++++ BODY +++++";

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExcelTestDocGenerator.class);

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
                props.load(ExcelTestDocGenerator.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE));
            }

            List<String> fileNames = FileUtils.getTestFiles(testDirectory);

            Collections.sort(fileNames);

            Source xsl = new StreamSource(ExcelTestDocGenerator.class.getResourceAsStream(xslSource), 
                    ExcelTestDocGenerator.class.getResource(xslSource).getPath());
            
            log.info("XSLT stylesheet was set: " + xsl.getSystemId());

            TransformerFactory factory = TransformerFactory.newInstance();

            Transformer t = factory.newTransformer(xsl);
            log.info("XSL transformer was created: " + t);

            t.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
            t.setOutputProperty(OutputKeys.METHOD, "xml");

            FileOutputStream file = new FileOutputStream(filename);
            OutputStream buffered = new BufferedOutputStream(file);
            StreamResult res = new StreamResult(buffered);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            documentBuilderFactory.setNamespaceAware(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(ExcelTestDocGenerator.class.getResourceAsStream(testdocTemplate)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(BODY_PLACEHOLDER) == false) {
                    buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
                } else {
                    break;
                }
            }

            for (int i = 0; i < fileNames.size(); i++) {
                buffered.write("<Row>".getBytes());

                String fileName = (String)fileNames.get(i);
                log.info("Working on test " + fileName);

                Source xml = new DOMSource(builder.parse(fileName));
                int testNumber = i+1;
                buffered.write(("<Cell><Data ss:Type=\"Number\">" + testNumber + "</Data></Cell>").getBytes());

                t.transform(xml, res);

                buffered.write(("<Cell><Data ss:Type=\"String\">" + fileName + "</Data></Cell>").getBytes());

                buffered.write("</Row>".getBytes());
            }

            while ((line = reader.readLine()) != null) {
                buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
            }

            buffered.flush();
            file.close();

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
