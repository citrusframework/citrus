/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.mvn.plugin;

import com.consol.citrus.generate.*;
import com.consol.citrus.mvn.plugin.config.tests.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class GenerateTestMojoTest {

    private XmlTestGenerator testGenerator = Mockito.mock(XmlTestGenerator.class);
    private XsdXmlTestGenerator xsdXmlTestGenerator = Mockito.mock(XsdXmlTestGenerator.class);
    private WsdlXmlTestGenerator wsdlXmlTestGenerator = Mockito.mock(WsdlXmlTestGenerator.class);

    private GenerateTestMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new GenerateTestMojo(testGenerator, xsdXmlTestGenerator, wsdlXmlTestGenerator);
    }
    
    @Test
    public void testCreate() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(testGenerator);

        TestConfiguration configuration = new TestConfiguration();
        configuration.setName("FooTest");
        configuration.setAuthor("UnknownAuthor");
        configuration.setDescription("TODO");
        configuration.setPackageName("com.consol.citrus.foo");

        when(testGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(testGenerator);
        when(testGenerator.withAuthor("UnknownAuthor")).thenReturn(testGenerator);
        when(testGenerator.withDescription("TODO")).thenReturn(testGenerator);
        when(testGenerator.usePackage("com.consol.citrus.foo")).thenReturn(testGenerator);
        when(testGenerator.withName("FooTest")).thenReturn(testGenerator);
        when(testGenerator.useSrcDirectory("target/generated/citrus")).thenReturn(testGenerator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(testGenerator).create();
    }

    @Test
    public void testSuiteFromXsd() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(xsdXmlTestGenerator);

        TestConfiguration configuration = new TestConfiguration();
        configuration.setName("BookStore");
        configuration.setAuthor("UnknownAuthor");
        configuration.setDescription("TODO");
        configuration.setPackageName("com.consol.citrus.xsd");

        XsdConfiguration xsdConfiguration = new XsdConfiguration();
        xsdConfiguration.setFile("classpath:xsd/BookStore.xsd");
        xsdConfiguration.setRequest("BookRequest");
        xsdConfiguration.setResponse("BookResponse");
        configuration.setXsd(xsdConfiguration);

        when(xsdXmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.withDescription("TODO")).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.usePackage("com.consol.citrus.xsd")).thenReturn(xsdXmlTestGenerator);

        when(xsdXmlTestGenerator.withXsd("classpath:xsd/BookStore.xsd")).thenReturn(xsdXmlTestGenerator);

        when(xsdXmlTestGenerator.withName("BookStore")).thenReturn(xsdXmlTestGenerator);
        when(testGenerator.useSrcDirectory("target/generated/citrus")).thenReturn(testGenerator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(xsdXmlTestGenerator).create();
        verify(xsdXmlTestGenerator).withXsd("classpath:xsd/BookStore.xsd");
        verify(xsdXmlTestGenerator).withRequestMessage("BookRequest");
        verify(xsdXmlTestGenerator).withResponseMessage("BookResponse");
    }

    @Test
    public void testSuiteFromWsdl() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(wsdlXmlTestGenerator);

        TestConfiguration configuration = new TestConfiguration();
        configuration.setName("BookStore");
        configuration.setAuthor("UnknownAuthor");
        configuration.setDescription("TODO");
        configuration.setPackageName("com.consol.citrus.wsdl");
        configuration.setSuffix("_Test");

        WsdlConfiguration wsdlConfiguration = new WsdlConfiguration();
        wsdlConfiguration.setFile("classpath:wsdl/BookStore.wsdl");
        configuration.setWsdl(wsdlConfiguration);

        when(wsdlXmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withDescription("TODO")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.usePackage("com.consol.citrus.wsdl")).thenReturn(wsdlXmlTestGenerator);

        when(wsdlXmlTestGenerator.withWsdl("classpath:wsdl/BookStore.wsdl")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withNameSuffix("_Test")).thenReturn(wsdlXmlTestGenerator);

        when(wsdlXmlTestGenerator.withName("BookStore")).thenReturn(wsdlXmlTestGenerator);
        when(testGenerator.useSrcDirectory("target/generated/citrus")).thenReturn(testGenerator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(wsdlXmlTestGenerator).create();
        verify(wsdlXmlTestGenerator).withWsdl("classpath:wsdl/BookStore.wsdl");
        verify(wsdlXmlTestGenerator).withNameSuffix("_Test");
    }
}
