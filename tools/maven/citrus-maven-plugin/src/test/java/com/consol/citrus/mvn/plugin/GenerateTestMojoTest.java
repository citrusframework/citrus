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

import com.consol.citrus.creator.*;
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

    private XmlTestCreator testCaseCreator = Mockito.mock(XmlTestCreator.class);
    private XsdXmlTestCreator xsdXmlTestCaseCreator = Mockito.mock(XsdXmlTestCreator.class);
    private WsdlXmlTestCreator wsdlXmlTestCaseCreator = Mockito.mock(WsdlXmlTestCreator.class);

    private GenerateTestMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new GenerateTestMojo(testCaseCreator, xsdXmlTestCaseCreator, wsdlXmlTestCaseCreator);
    }
    
    @Test
    public void testCreate() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(testCaseCreator);

        TestConfiguration configuration = new TestConfiguration();
        configuration.setName("FooTest");
        configuration.setAuthor("UnknownAuthor");
        configuration.setDescription("TODO");
        configuration.setPackageName("com.consol.citrus.foo");

        when(testCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(testCaseCreator);
        when(testCaseCreator.withAuthor("UnknownAuthor")).thenReturn(testCaseCreator);
        when(testCaseCreator.withDescription("TODO")).thenReturn(testCaseCreator);
        when(testCaseCreator.usePackage("com.consol.citrus.foo")).thenReturn(testCaseCreator);
        when(testCaseCreator.withName("FooTest")).thenReturn(testCaseCreator);
        when(testCaseCreator.useSrcDirectory("target/generated/citrus")).thenReturn(testCaseCreator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(testCaseCreator).create();
    }

    @Test
    public void testSuiteFromXsd() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(xsdXmlTestCaseCreator);

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

        when(xsdXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.withDescription("TODO")).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.usePackage("com.consol.citrus.xsd")).thenReturn(xsdXmlTestCaseCreator);

        when(xsdXmlTestCaseCreator.withXsd("classpath:xsd/BookStore.xsd")).thenReturn(xsdXmlTestCaseCreator);

        when(xsdXmlTestCaseCreator.withName("BookStore")).thenReturn(xsdXmlTestCaseCreator);
        when(testCaseCreator.useSrcDirectory("target/generated/citrus")).thenReturn(testCaseCreator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(xsdXmlTestCaseCreator).create();
        verify(xsdXmlTestCaseCreator).withXsd("classpath:xsd/BookStore.xsd");
        verify(xsdXmlTestCaseCreator).withRequestMessage("BookRequest");
        verify(xsdXmlTestCaseCreator).withResponseMessage("BookResponse");
    }

    @Test
    public void testSuiteFromWsdl() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(wsdlXmlTestCaseCreator);

        TestConfiguration configuration = new TestConfiguration();
        configuration.setName("BookStore");
        configuration.setAuthor("UnknownAuthor");
        configuration.setDescription("TODO");
        configuration.setPackageName("com.consol.citrus.wsdl");
        configuration.setSuffix("_Test");

        WsdlConfiguration wsdlConfiguration = new WsdlConfiguration();
        wsdlConfiguration.setFile("classpath:wsdl/BookStore.wsdl");
        configuration.setWsdl(wsdlConfiguration);

        when(wsdlXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withDescription("TODO")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.usePackage("com.consol.citrus.wsdl")).thenReturn(wsdlXmlTestCaseCreator);

        when(wsdlXmlTestCaseCreator.withWsdl("classpath:wsdl/BookStore.wsdl")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withNameSuffix("_Test")).thenReturn(wsdlXmlTestCaseCreator);

        when(wsdlXmlTestCaseCreator.withName("BookStore")).thenReturn(wsdlXmlTestCaseCreator);
        when(testCaseCreator.useSrcDirectory("target/generated/citrus")).thenReturn(testCaseCreator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(wsdlXmlTestCaseCreator).create();
        verify(wsdlXmlTestCaseCreator).withWsdl("classpath:wsdl/BookStore.wsdl");
        verify(wsdlXmlTestCaseCreator).withNameSuffix("_Test");
    }
}
