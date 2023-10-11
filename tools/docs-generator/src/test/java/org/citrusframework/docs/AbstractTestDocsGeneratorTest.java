package org.citrusframework.docs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.TransformerException;

import org.citrusframework.generate.UnitFramework;
import org.citrusframework.generate.xml.XmlTestGenerator;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import static org.testng.Assert.assertEquals;

public class AbstractTestDocsGeneratorTest {

    private final AbstractTestDocsGenerator abstractTestDocsGenerator = new AbstractTestDocsGenerator("", "") {
        @Override
        public void doBody(final OutputStream buffered) throws TransformerException, IOException, SAXException {

        }

        @Override
        public void doHeader(final OutputStream buffered) throws TransformerException, IOException, SAXException {

        }

        @Override
        protected Properties getTestDocProperties() {
            return null;
        }
    };


    @Test
    public void testGetTestFiles() throws IOException {

        //GIVEN
        new XmlTestGenerator<>()
                .withAuthor("Christoph")
                .withDescription("This is a sample test")
                .withName("SampleIT")
                .usePackage("org.citrusframework.sample")
                .withFramework(UnitFramework.TESTNG)
                .create();

        //WHEN
        final List<File> testFiles = abstractTestDocsGenerator.getTestFiles();

        //THEN
        assertEquals(testFiles.size(), 1);
        assertEquals(testFiles.get(0).getName(), "SampleIT.xml");
    }
}
