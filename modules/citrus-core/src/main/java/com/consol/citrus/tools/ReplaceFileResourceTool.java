package com.consol.citrus.tools;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

import com.consol.citrus.util.FileUtils;

/**
 * Tool will replace all resource elements in test case definitions. The tool will read the
 * referenced file content and place inline data definitions instead into the test case.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2008
 */
public class ReplaceFileResourceTool {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ReplaceFileResourceTool.class);

    /**
     * @param args
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     */
    public static void main(String[] args) throws ClassCastException,
    ClassNotFoundException, InstantiationException,
    IllegalAccessException, IOException {

        String testDirectory = "tests/mig";

        if (args.length > 0) {
            testDirectory = args[0];
        }

        log.info("ReplaceFileResourceTool starting ...");
        log.info("Using test directory; " + testDirectory);

        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS domImpl = (DOMImplementationLS) registry.getDOMImplementation("LS");

        LSParser parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        parser.getDomConfig().setParameter("cdata-sections", true);
        parser.getDomConfig().setParameter("split-cdata-sections", false);

        log.info("Searching for test case files");
        List files = FileUtils.getTestFiles(testDirectory, 
                FileUtils.XML_FILE_EXTENSION).getFileNames();
        log.info("Found " + files.size() + " test cases in directory");

        for (Iterator iter = files.iterator(); iter.hasNext();) {
            String filePath = (String) iter.next();
            Resource testcase = new FileSystemResource(filePath);

            log.info("Parsing test: " + testcase.getFilename());
            Document doc = parser.parseURI(testcase.getFile().toURI().toString());

            LSSerializer serializer = domImpl.createLSSerializer();
            serializer.getDomConfig().setParameter("split-cdata-sections", false);

            serializer.setFilter(new FileResourceSerializerFilter());

            log.info("Serialize test: " + testcase.getFilename());
            serializer.writeToURI(doc, testcase.getFile().toURI().toString());
        }

        log.info("ReplaceFileResourceTool finished successfully!");
    }
}
