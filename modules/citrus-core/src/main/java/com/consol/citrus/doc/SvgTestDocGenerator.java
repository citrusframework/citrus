package com.consol.citrus.doc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.*;
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

            List<String> fileNames = FileUtils.getTestFiles(testDirectory);

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

            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = (String)fileNames.get(i);
                log.info("Working on test " + fileName);

                StringWriter stringWriter = new StringWriter();
                StreamSource xml = new StreamSource(fileName);
                StreamResult res = new StreamResult(stringWriter);

                try {
                    t.transform(xml, res);
                    stringWriter.flush();

                    String fileContent = stringWriter.toString();
                    stringWriter.close();

                    if (fileContent!= null && fileContent.indexOf("svg")!=-1) {
                        log.info("Created file " + fileName.substring(0, fileName.lastIndexOf('.')) + ".svg");
                        FileWriter fileWriter = new FileWriter(fileName.substring(0, fileName.lastIndexOf('.')) + ".svg");
                        fileWriter.write(stringWriter.toString());
                        fileWriter.flush();
                        fileWriter.close();
                    } else {
                        log.warn("Could not create file " + fileName.substring(0, fileName.lastIndexOf('.')) + ".svg");
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
