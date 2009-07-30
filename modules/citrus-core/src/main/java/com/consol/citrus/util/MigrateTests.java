package com.consol.citrus.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Class to automatically generate a list of all available tests.
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 02.03.2007
 */
public class MigrateTests {
    private final static String DEFAULT_XSLT_SOURCE = "migrate_tests.xsl";
    private final static String DEFAULT_TEST_DIRECTORY = "tests";

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(MigrateTests.class);

    public static void main(String[] args) {
        try {
            String testDirectory = DEFAULT_TEST_DIRECTORY;

            if (args.length > 1) {
                testDirectory = args[1];
            }

            List fileNames = FileUtils.getTestFiles(testDirectory, FileUtils.XML_FILE_EXTENSION).getFileNames();

            String xslSource;
            if (args.length > 0) {
                xslSource = args[0];
            } else {
                xslSource = DEFAULT_XSLT_SOURCE;
            }

            Source xsl = new StreamSource(new ClassPathResource(xslSource).getFile());
            log.info("XSLT stylesheet was set: " + xsl.getSystemId());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer t = factory.newTransformer(xsl);

            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "{http://www.consol.de/citrus/schema/testcase}data");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            log.info("XSL transformer was created");

            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = (String)fileNames.get(i);
                log.info("Working on test " + fileName);

                StringWriter stringWriter = new StringWriter();
                StreamSource xml = new StreamSource(fileName);
                StreamResult res = new StreamResult(stringWriter);

                try {
                    t.transform(xml, res);
                    stringWriter.flush();

                    FileWriter fileWriter = new FileWriter(fileName + ".new");
                    fileWriter.write(stringWriter.toString().replaceAll("\\]\\]><!\\[CDATA\\[", ""));
                    fileWriter.flush();
                    fileWriter.close();
                    stringWriter.close();
                } catch(TransformerException e) {
                    log.error("XSLT tranformation failed", e);
                } catch (IOException e) {
                    log.error("Exception", e);
                }
            }
        } catch (IOException e) {
            log.error("Exception", e);
        } catch (TransformerConfigurationException e) {
            log.error("Exception", e);
        }
    }
}
