package org.citrusframework.citrus.docs;

import org.citrusframework.citrus.generate.UnitFramework;
import org.citrusframework.citrus.generate.xml.XmlTestGenerator;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

public class AbstractTestDocsGeneratorTest {

    private AbstractTestDocsGenerator abstractTestDocsGenerator = new AbstractTestDocsGenerator("", "") {
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
        new XmlTestGenerator()
                .withAuthor("Christoph")
                .withDescription("This is a sample test")
                .withName("SampleIT")
                .usePackage("org.citrusframework.citrus.sample")
                .withFramework(UnitFramework.TESTNG)
                .create();

        //WHEN
        final List<File> testFiles = abstractTestDocsGenerator.getTestFiles();

        //THEN
        assertEquals(testFiles.size(), 1);
        assertEquals(testFiles.get(0).getName(), "SampleIT.xml");
    }
}
