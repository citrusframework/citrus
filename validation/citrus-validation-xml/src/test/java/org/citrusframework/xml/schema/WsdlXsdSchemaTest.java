/*
 * Copyright 2006-2012 the original author or authors.
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

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.citrusframework.util.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * @author Christoph Deppisch
 */
public class WsdlXsdSchemaTest {

    @Test
    public void testWsdlSchema() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleService.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 2);

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaImports() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleServiceWithImports.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 2);

        Assert.assertEquals(wsdl.getTargetNamespace(), "http://www.citrusframework.org/SampleService/");

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaImportsNamespaceDiff() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleServiceWithImportsNamespaceDiff.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 4);

        Assert.assertEquals(wsdl.getTargetNamespace(), "http://www.citrusframework.org/SampleService/Commands/");

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaWsdlImports() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleServiceWithWsdlImports.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 3);

        Assert.assertEquals(wsdl.getTargetNamespace(), "http://www.citrusframework.org/SampleService/");

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaWsdlImportsFromJar() throws ParserConfigurationException, IOException, SAXException {
        ClassPathResource classPathResource = new ClassPathResource("sample.jar", WsdlXsdSchemaTest.class);
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{classPathResource.getURL()});
        URL url = urlClassLoader.getResource("SampleServiceWithWsdlImports.wsdl");
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new UrlResource(url));
        wsdl.initialize();

        Assert.assertEquals(wsdl.getSchemaResources().size(), 3);

        Assert.assertEquals(wsdl.getTargetNamespace(), "http://www.citrusframework.org/SampleService/");

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaWsdlImportsOnly() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleServiceWithWsdlImportsOnly.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 2);

        Assert.assertEquals(wsdl.getTargetNamespace(), "http://www.citrusframework.org/TestService/");

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaDuplicateImports() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleServiceWithDuplicateImports.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 3);

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaNoMatchingTargetNamespace() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleServiceNoMatchingTargetNamespace.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 2);

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testWsdlSchemaWithIncludes() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/validation/SampleServiceWithIncludes.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();
        Assert.assertEquals(wsdl.getSchemaResources().size(), 3);

        Assert.assertNotNull(wsdl.getSource());
    }

    @Test
    public void testNamespaceInheritance() throws ParserConfigurationException, IOException, SAXException {
        WsdlXsdSchema wsdl = new WsdlXsdSchema(new ClassPathResource("org/citrusframework/xml/BookStore.wsdl"));
        wsdl.initialize();
        wsdl.afterPropertiesSet();

        Assert.assertEquals(wsdl.getSchemaResources().size(), 2);

        String xsd = FileUtils.readToString(wsdl.getSchemaResources().get(0));
        Assert.assertTrue(xsd.contains("xmlns:tns=\"http://www.citrusframework.org/bookstore/\""));
        Assert.assertTrue(xsd.contains("xmlns:audio=\"http://www.citrusframework.org/bookstore/audio\""));
        Assert.assertTrue(xsd.contains("xmlns:book=\"http://www.citrusframework.org/book\""));
        Assert.assertTrue(xsd.contains("xmlns:author=\"http://www.citrusframework.org/author\""));
        Assert.assertTrue(xsd.contains("xmlns=\"http://www.citrusframework.org/bookstore/\""));

        xsd = FileUtils.readToString(wsdl.getSchemaResources().get(1));
        Assert.assertTrue(xsd.contains("xmlns:tns=\"http://www.citrusframework.org/bookstore/\""));
        Assert.assertTrue(xsd.contains("xmlns:audio=\"http://www.citrusframework.org/bookstore/audio\""));
        Assert.assertTrue(xsd.contains("xmlns:book=\"http://www.citrusframework.org/book/wsdl\""));
        Assert.assertFalse(xsd.contains("xmlns:author=\"http://www.citrusframework.org/author\""));
        Assert.assertTrue(xsd.contains("xmlns=\"http://schemas.xmlsoap.org/wsdl/soap/\""));
    }
}
