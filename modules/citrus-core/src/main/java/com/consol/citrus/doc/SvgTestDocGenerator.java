/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.doc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.util.FileUtils;

/**
 * Class to automatically generate a list of all available tests.
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 02.03.2007
 */
public class SvgTestDocGenerator {
    private final static String DEFAULT_XSLT_SOURCE = "generate-svg-doc.xslt";
    private final static String DEFAULT_TEST_DIRECTORY = "tests";

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SvgTestDocGenerator.class);

    public static void main(String[] args) {
        try {
            String testDirectory = DEFAULT_TEST_DIRECTORY;

            if (args.length > 1) {
                testDirectory = args[1];
            }

            List<File> testFiles = FileUtils.getTestFiles(testDirectory);

            String xslSource;
            if (args.length > 0) {
                xslSource = args[0];
            } else {
                xslSource = DEFAULT_XSLT_SOURCE;
            }

            Source xsl = new StreamSource(SvgTestDocGenerator.class.getResourceAsStream(xslSource));
            log.info("XSLT stylesheet was set: " + xsl.getSystemId());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer t = factory.newTransformer(xsl);

            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            log.info("XSL transformer was created");

            for (File testFile : testFiles) {
                log.info("Working on test " + testFile.getName());

                StringWriter stringWriter = new StringWriter();
                StreamSource xml = new StreamSource(testFile);
                StreamResult res = new StreamResult(stringWriter);

                try {
                    t.transform(xml, res);
                    stringWriter.flush();

                    String fileContent = stringWriter.toString();
                    stringWriter.close();

                    if (fileContent!= null && fileContent.indexOf("svg")!=-1) {
                        log.info("Created file " + testFile.getName().substring(0, testFile.getName().lastIndexOf('.')) + ".svg");
                        FileWriter fileWriter = new FileWriter(testFile.getName().substring(0, testFile.getName().lastIndexOf('.')) + ".svg");
                        fileWriter.write(stringWriter.toString());
                        fileWriter.flush();
                        fileWriter.close();
                    } else {
                        log.warn("Could not create file " + testFile.getName().substring(0, testFile.getName().lastIndexOf('.')) + ".svg");
                    }
                } catch(TransformerException e) {
                    log.error("XSLT tranformation failed", e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (TransformerConfigurationException e) {
            log.error("Error during doc generation", e);
        }
    }
}
