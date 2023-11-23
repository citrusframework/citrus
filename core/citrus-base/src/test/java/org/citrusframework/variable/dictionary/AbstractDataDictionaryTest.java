package org.citrusframework.variable.dictionary;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.tools.ant.filters.StringInputStream;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.expectThrows;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


public class AbstractDataDictionaryTest {

    private static final String KEY = "a";
    private static final String VALUE = KEY + "-value";

    @Mock
    private Resource mappingFile;

    private AutoCloseable mockitoContext;

    private TestableDataDictionary fixture;

    @BeforeMethod
    public void setup() {
        mockitoContext = MockitoAnnotations.openMocks(this);

        fixture = new TestableDataDictionary();
        fixture.setMappingFile(mappingFile);
    }

    @Test
    public void testInitializeWithNormalPropertiesFile() {
        // Normal properties file
        InputStream inputStream = new StringInputStream(String.format("%s=%s", KEY, VALUE));

        // Setup mock resource
        doReturn(inputStream).when(mappingFile).getInputStream();
        doReturn("test.properties").when(mappingFile).getLocation();

        fixture.initialize();

        assertMappingContainsKeyAndValue();
    }

    @Test
    public void testInitializeWithXMLPropertiesFile() {
        // XML properties file
        InputStream inputStream = new StringInputStream(String.format(
                """
                        <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
                        <properties>
                          <entry key="%s">%s</entry>
                        </properties>
                        """
                , KEY, VALUE));

        // Setup mock resource
        doReturn(inputStream).when(mappingFile).getInputStream();
        doReturn("test.properties.xml").when(mappingFile).getLocation(); // Note the .xml-suffix

        fixture.initialize();

        assertMappingContainsKeyAndValue();
    }

    @Test
    public void testInitializeThrowsExceptionWithInvalidXMLPropertiesFile() {
        // Normal properties file
        InputStream inputStream = new StringInputStream(String.format("%s=%s", KEY, VALUE));

        // Setup mock resource
        doReturn(inputStream).when(mappingFile).getInputStream();
        doReturn("test.properties.xml").when(mappingFile).getLocation(); // Note the .xml-suffix

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, () -> fixture.initialize());
        assertTrue(exception.getCause() instanceof InvalidPropertiesFormatException);

        assertTrue(fixture.mappings.isEmpty());
    }

    private void assertMappingContainsKeyAndValue() {
        assertEquals(1, fixture.mappings.size());
        assertTrue(fixture.mappings.containsKey(KEY));
        assertEquals(VALUE, fixture.mappings.get(KEY));
    }

    @AfterMethod
    public void teardown() throws Exception {
        mockitoContext.close();
    }

    private static class TestableDataDictionary extends AbstractDataDictionary<Object> {

        @Override
        public <R> R translate(Object key, R value, TestContext context) {
            throw new NotImplementedException();
        }
    }
}
