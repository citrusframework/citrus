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

package com.consol.citrus.mvn.plugin;

import com.consol.citrus.generate.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class CreateTestMojoTest {

    private Prompter prompter = Mockito.mock(Prompter.class);
    
    private XmlTestGenerator testCaseCreator = Mockito.mock(XmlTestGenerator.class);
    private XsdXmlTestGenerator xsdXmlTestCaseCreator = Mockito.mock(XsdXmlTestGenerator.class);
    private WsdlXmlTestGenerator wsdlXmlTestCaseCreator = Mockito.mock(WsdlXmlTestGenerator.class);

    private CreateTestMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new CreateTestMojo(testCaseCreator, xsdXmlTestCaseCreator, wsdlXmlTestCaseCreator);
        mojo.setPrompter(prompter);
    }
    
    @Test
    public void testCreate() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, testCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("FooTest");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.foo");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(testCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(testCaseCreator);
        when(testCaseCreator.withAuthor("UnknownAuthor")).thenReturn(testCaseCreator);
        when(testCaseCreator.withDescription("TODO")).thenReturn(testCaseCreator);
        when(testCaseCreator.usePackage("com.consol.citrus.foo")).thenReturn(testCaseCreator);
        when(testCaseCreator.withName("FooTest")).thenReturn(testCaseCreator);
        
        mojo.execute();

        verify(testCaseCreator).create();
    }

    @Test
    public void testAbort() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, testCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("FooTest");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.foo");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        mojo.execute();

        verify(testCaseCreator, times(0)).create();
    }

    @Test
    public void testSuiteFromXsd() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, xsdXmlTestCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:xsd/BookStore.xsd");
        when(prompter.prompt(contains("request"))).thenReturn("BookRequest");
        when(prompter.prompt(contains("response"), nullable(String.class))).thenReturn("BookResponse");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.xsd");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(xsdXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.withDescription("TODO")).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.usePackage("com.consol.citrus.wsdl")).thenReturn(xsdXmlTestCaseCreator);

        when(xsdXmlTestCaseCreator.withXsd("classpath:xsd/BookStore.xsd")).thenReturn(xsdXmlTestCaseCreator);

        when(xsdXmlTestCaseCreator.withName("BookStore")).thenReturn(xsdXmlTestCaseCreator);

        mojo.execute();

        verify(xsdXmlTestCaseCreator).create();
        verify(xsdXmlTestCaseCreator).withXsd("classpath:xsd/BookStore.xsd");
        verify(xsdXmlTestCaseCreator).withRequestMessage("BookRequest");
        verify(xsdXmlTestCaseCreator).withResponseMessage("BookResponse");
    }

    @Test
    public void testSuiteFromXsdAbort() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, xsdXmlTestCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("request"))).thenReturn("BookRequest");
        when(prompter.prompt(contains("response"), nullable(String.class))).thenReturn("BookResponse");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.wsdl");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        when(xsdXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.withDescription("TODO")).thenReturn(xsdXmlTestCaseCreator);
        when(xsdXmlTestCaseCreator.usePackage("com.consol.citrus.wsdl")).thenReturn(xsdXmlTestCaseCreator);

        when(xsdXmlTestCaseCreator.withName("BookStore")).thenReturn(xsdXmlTestCaseCreator);

        mojo.execute();

        verify(xsdXmlTestCaseCreator, times(0)).create();
    }
    
    @Test
    public void testSuiteFromWsdl() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, wsdlXmlTestCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("prefix"), nullable(String.class))).thenReturn("BookStore_");
        when(prompter.prompt(contains("suffix"), nullable(String.class))).thenReturn("_Test");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.wsdl");
        when(prompter.prompt(contains("actor"), any(List.class), nullable(String.class))).thenReturn("client");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("operation"), nullable(String.class))).thenReturn("all");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(wsdlXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withDescription("TODO")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.usePackage("com.consol.citrus.wsdl")).thenReturn(wsdlXmlTestCaseCreator);

        when(wsdlXmlTestCaseCreator.withWsdl("classpath:wsdl/BookStore.wsdl")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withNameSuffix("_Test")).thenReturn(wsdlXmlTestCaseCreator);

        when(wsdlXmlTestCaseCreator.withName("BookStore")).thenReturn(wsdlXmlTestCaseCreator);

        mojo.execute();

        verify(wsdlXmlTestCaseCreator).create();
        verify(wsdlXmlTestCaseCreator).withWsdl("classpath:wsdl/BookStore.wsdl");
        verify(wsdlXmlTestCaseCreator).withNameSuffix("_Test");
    }

    @Test
    public void testSuiteFromWsdlAbort() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, wsdlXmlTestCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("prefix"), nullable(String.class))).thenReturn("BookStore_");
        when(prompter.prompt(contains("suffix"), nullable(String.class))).thenReturn("_Test");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.wsdl");
        when(prompter.prompt(contains("actor"), any(List.class), nullable(String.class))).thenReturn("client");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("operation"), nullable(String.class))).thenReturn("all");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        when(wsdlXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.withDescription("TODO")).thenReturn(wsdlXmlTestCaseCreator);
        when(wsdlXmlTestCaseCreator.usePackage("com.consol.citrus.wsdl")).thenReturn(wsdlXmlTestCaseCreator);

        when(wsdlXmlTestCaseCreator.withName("BookStore")).thenReturn(wsdlXmlTestCaseCreator);

        mojo.execute();

        verify(wsdlXmlTestCaseCreator, times(0)).create();
    }
}
