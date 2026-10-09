/*
 * Copyright the original author or authors.
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

package org.citrusframework.xml.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class TestCaseSchemaTest {

    private static final String SCHEMA_PATH = "org/citrusframework/schema/xml/testcase/";
    private static final String SCHEMA = SCHEMA_PATH + "citrus-testcase.xsd";
    private static final String VERSIONED_SCHEMA = SCHEMA_PATH + "citrus-testcase-5.1.0-SNAPSHOT.xsd";
    private static final String TEST_CASE_NAMESPACE = "http://citrusframework.org/schema/xml/testcase";
    private static final String SAMPLE ="org/citrusframework/xml/schema/schema-sample.xml";

    @Test
    public void shouldKeepSchemaCopiesIdentical() throws IOException {
        Assert.assertEquals(readResource(VERSIONED_SCHEMA), readResource(SCHEMA));
    }

    @Test
    public void shouldCompileVersionedSchema() throws SAXException {
        Assert.assertNotNull(loadSchema(VERSIONED_SCHEMA));
    }

    @Test
    public void shouldValidateSchemaSample() throws SAXException, IOException {
        validate(loadSchema(SCHEMA), SAMPLE);
    }

    @Test(dataProvider = "xmlTestCases")
    public void shouldValidateXmlTestCase(String testCase) throws SAXException, IOException {
        validate(loadSchema(SCHEMA), testCase);
    }

    @DataProvider
    public Object[][] xmlTestCases() throws IOException, URISyntaxException {
        // test-classes/org/citrusframework/xml/schema/schema-sample.xml
        Path root = Path.of(getResource(SAMPLE).toURI()).getParent().getParent().getParent().getParent().getParent();
        try (Stream<Path> files = Files.walk(root.resolve("org/citrusframework/xml"))) {
            List<String> testCases = files.filter(file -> file.getFileName().toString().endsWith(".citrus.it.xml"))
                    .filter(TestCaseSchemaTest::usesTestCaseNamespace)
                    .map(file -> root.relativize(file).toString().replace('\\', '/'))
                    .sorted()
                    .toList();
            Assert.assertFalse(testCases.isEmpty());
            return testCases.stream().map(testCase -> new Object[] { testCase }).toArray(Object[][]::new);
        }
    }

    private static boolean usesTestCaseNamespace(Path file) {
        try {
            return Files.readString(file).contains(TEST_CASE_NAMESPACE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Loads the schema with full schema checking enabled so that strict constraints such as
     * Unique Particle Attribution are verified, too.
     */
    private Schema loadSchema(String path) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        return factory.newSchema(getResource(path));
    }

    private void validate(Schema schema, String path) throws SAXException, IOException {
        try (InputStream in = getResource(path).openStream()) {
            schema.newValidator().validate(new StreamSource(in, path));
        }
    }

    private byte[] readResource(String path) throws IOException {
        try (InputStream in = getResource(path).openStream()) {
            return in.readAllBytes();
        }
    }

    private URL getResource(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        Assert.assertNotNull(resource, "Missing classpath resource: " + path);
        return resource;
    }
}
