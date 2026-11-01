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

import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class WsdlXsdSchemaTest {

    @Test
    public void testWsdlSchema() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleService.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(2, wsdl.getSchemaResources().size());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaImports() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleServiceWithImports.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(2, wsdl.getSchemaResources().size());

        Assertions.assertEquals("http://citrusframework.org/SampleService/", wsdl.getTargetNamespace());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaImportsNamespaceDiff() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleServiceWithImportsNamespaceDiff.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(4, wsdl.getSchemaResources().size());

        Assertions.assertEquals("http://citrusframework.org/SampleService/Commands/", wsdl.getTargetNamespace());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaWsdlImports() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleServiceWithWsdlImports.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(3, wsdl.getSchemaResources().size());

        Assertions.assertEquals("http://citrusframework.org/SampleService/", wsdl.getTargetNamespace());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaWsdlImportsFromJar() throws IOException {
        Resource classPathResource = Resources.create("sample.jar", WsdlXsdSchemaTest.class);

        try (var urlClassLoader = URLClassLoader.newInstance(new URL[]{classPathResource.getURL()})) {
            URL url = urlClassLoader.getResource("SampleServiceWithWsdlImports.wsdl");
            WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.create(url));
            wsdl.initialize();

            Assertions.assertEquals(3, wsdl.getSchemaResources().size());

            Assertions.assertEquals("http://www.citrusframework.org/SampleService/", wsdl.getTargetNamespace());

            Assertions.assertNotNull(wsdl.getSource());
        }
    }

    @Test
    public void testWsdlSchemaWsdlImportsOnly() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleServiceWithWsdlImportsOnly.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(2, wsdl.getSchemaResources().size());

        Assertions.assertEquals("http://citrusframework.org/TestService/", wsdl.getTargetNamespace());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaDuplicateImports() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleServiceWithDuplicateImports.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(3, wsdl.getSchemaResources().size());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaNoMatchingTargetNamespace() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleServiceNoMatchingTargetNamespace.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(2, wsdl.getSchemaResources().size());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaWithIncludes() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/validation/SampleServiceWithIncludes.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assertions.assertEquals(3, wsdl.getSchemaResources().size());

        Assertions.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testNamespaceInheritance() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(Resources.fromClasspath("org/citrusframework/xml/BookStore.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();

        Assertions.assertEquals(2, wsdl.getSchemaResources().size());

        String xsd = FileUtils.readToString(wsdl.getSchemaResources().get(0).getInputStream());
        Assertions.assertTrue(xsd.contains("xmlns:tns=\"http://citrusframework.org/bookstore/\""));
        Assertions.assertTrue(xsd.contains("xmlns:audio=\"http://citrusframework.org/bookstore/audio\""));
        Assertions.assertTrue(xsd.contains("xmlns:book=\"http://citrusframework.org/book\""));
        Assertions.assertTrue(xsd.contains("xmlns:author=\"http://citrusframework.org/author\""));
        Assertions.assertTrue(xsd.contains("xmlns=\"http://citrusframework.org/bookstore/\""));

        xsd = FileUtils.readToString(wsdl.getSchemaResources().get(1).getInputStream());
        Assertions.assertTrue(xsd.contains("xmlns:tns=\"http://citrusframework.org/bookstore/\""));
        Assertions.assertTrue(xsd.contains("xmlns:audio=\"http://citrusframework.org/bookstore/audio\""));
        Assertions.assertTrue(xsd.contains("xmlns:book=\"http://citrusframework.org/book/wsdl\""));
        Assertions.assertFalse(xsd.contains("xmlns:author=\"http://citrusframework.org/author\""));
        Assertions.assertTrue(xsd.contains("xmlns=\"http://schemas.xmlsoap.org/wsdl/soap/\""));
    }
}
