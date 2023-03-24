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

package org.citrusframework.mvn.plugin;

import org.citrusframework.generate.TestGenerator;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.generate.javadsl.*;
import org.citrusframework.generate.xml.*;
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
    
    private XmlTestGenerator xmlTestGenerator = Mockito.mock(XmlTestGenerator.class);
    private XsdXmlTestGenerator xsdXmlTestGenerator = Mockito.mock(XsdXmlTestGenerator.class);
    private WsdlXmlTestGenerator wsdlXmlTestGenerator = Mockito.mock(WsdlXmlTestGenerator.class);
    private SwaggerXmlTestGenerator swaggerXmlTestGenerator = Mockito.mock(SwaggerXmlTestGenerator.class);

    private JavaDslTestGenerator javaTestGenerator = Mockito.mock(JavaDslTestGenerator.class);
    private XsdJavaTestGenerator xsdJavaTestGenerator = Mockito.mock(XsdJavaTestGenerator.class);
    private WsdlJavaTestGenerator wsdlJavaTestGenerator = Mockito.mock(WsdlJavaTestGenerator.class);
    private SwaggerJavaTestGenerator swaggerJavaTestGenerator = Mockito.mock(SwaggerJavaTestGenerator.class);

    private CreateTestMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new CreateTestMojo(xmlTestGenerator,
                                    xsdXmlTestGenerator,
                                    wsdlXmlTestGenerator,
                                    swaggerXmlTestGenerator,
                                    javaTestGenerator,
                                    xsdJavaTestGenerator,
                                    wsdlJavaTestGenerator,
                                    swaggerJavaTestGenerator);
        mojo.setPrompter(prompter);
    }
    
    @Test
    public void testCreate() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, xmlTestGenerator);

        when(prompter.prompt(contains("test name"))).thenReturn("FooTest");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("org.citrusframework.foo");
        when(prompter.prompt(contains("type"), any(List.class), nullable(String.class))).thenReturn("xml");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with Swagger API"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(xmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(xmlTestGenerator);
        when(xmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(xmlTestGenerator);
        when(xmlTestGenerator.withDescription("TODO")).thenReturn(xmlTestGenerator);
        when(xmlTestGenerator.usePackage("org.citrusframework.foo")).thenReturn(xmlTestGenerator);
        when(xmlTestGenerator.withName("FooTest")).thenReturn(xmlTestGenerator);
        
        mojo.execute();

        verify(xmlTestGenerator).create();
    }

    @Test
    public void testAbort() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(prompter, xmlTestGenerator);

        when(prompter.prompt(contains("test name"))).thenReturn("FooTest");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("org.citrusframework.foo");
        when(prompter.prompt(contains("type"), any(List.class), nullable(String.class))).thenReturn("xml");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with Swagger API"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        mojo.execute();

        verify(xmlTestGenerator, times(0)).create();
    }

    @Test
    public void testSuiteFromXsd() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, xsdXmlTestGenerator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:xsd/BookStore.xsd");
        when(prompter.prompt(contains("request"))).thenReturn("BookRequest");
        when(prompter.prompt(contains("response"), nullable(String.class))).thenReturn("BookResponse");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("org.citrusframework.xsd");
        when(prompter.prompt(contains("mode"), any(List.class), nullable(String.class))).thenReturn(TestGenerator.GeneratorMode.CLIENT.name());
        when(prompter.prompt(contains("type"), any(List.class), nullable(String.class))).thenReturn("xml");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with Swagger API"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(xsdXmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.withDescription("TODO")).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.usePackage("org.citrusframework.wsdl")).thenReturn(xsdXmlTestGenerator);

        when(xsdXmlTestGenerator.withXsd("classpath:xsd/BookStore.xsd")).thenReturn(xsdXmlTestGenerator);

        when(xsdXmlTestGenerator.withName("BookStore")).thenReturn(xsdXmlTestGenerator);

        mojo.execute();

        verify(xsdXmlTestGenerator).create();
        verify(xsdXmlTestGenerator).withXsd("classpath:xsd/BookStore.xsd");
        verify(xsdXmlTestGenerator).withRequestMessage("BookRequest");
        verify(xsdXmlTestGenerator).withResponseMessage("BookResponse");
    }

    @Test
    public void testSuiteFromXsdAbort() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, xsdXmlTestGenerator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("request"))).thenReturn("BookRequest");
        when(prompter.prompt(contains("response"), nullable(String.class))).thenReturn("BookResponse");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("org.citrusframework.wsdl");
        when(prompter.prompt(contains("mode"), any(List.class), nullable(String.class))).thenReturn(TestGenerator.GeneratorMode.CLIENT.name());
        when(prompter.prompt(contains("type"), any(List.class), nullable(String.class))).thenReturn("xml");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with Swagger API"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        when(xsdXmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.withDescription("TODO")).thenReturn(xsdXmlTestGenerator);
        when(xsdXmlTestGenerator.usePackage("org.citrusframework.wsdl")).thenReturn(xsdXmlTestGenerator);

        when(xsdXmlTestGenerator.withName("BookStore")).thenReturn(xsdXmlTestGenerator);

        mojo.execute();

        verify(xsdXmlTestGenerator, times(0)).create();
    }
    
    @Test
    public void testSuiteFromWsdl() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, wsdlXmlTestGenerator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("prefix"), nullable(String.class))).thenReturn("BookStore_");
        when(prompter.prompt(contains("suffix"), nullable(String.class))).thenReturn("_Test");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("org.citrusframework.wsdl");
        when(prompter.prompt(contains("mode"), any(List.class), nullable(String.class))).thenReturn(TestGenerator.GeneratorMode.CLIENT.name());
        when(prompter.prompt(contains("type"), any(List.class), nullable(String.class))).thenReturn("xml");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("operation"), nullable(String.class))).thenReturn("all");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Create test with Swagger API"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(wsdlXmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withDescription("TODO")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.usePackage("org.citrusframework.wsdl")).thenReturn(wsdlXmlTestGenerator);

        when(wsdlXmlTestGenerator.withWsdl("classpath:wsdl/BookStore.wsdl")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withNameSuffix("_Test")).thenReturn(wsdlXmlTestGenerator);

        when(wsdlXmlTestGenerator.withName("BookStore")).thenReturn(wsdlXmlTestGenerator);

        mojo.execute();

        verify(wsdlXmlTestGenerator).create();
        verify(wsdlXmlTestGenerator).withWsdl("classpath:wsdl/BookStore.wsdl");
        verify(wsdlXmlTestGenerator).withNameSuffix("_Test");
    }

    @Test
    public void testSuiteFromWsdlAbort() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, wsdlXmlTestGenerator);

        when(prompter.prompt(contains("test name"))).thenReturn("BookStore");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("prefix"), nullable(String.class))).thenReturn("BookStore_");
        when(prompter.prompt(contains("suffix"), nullable(String.class))).thenReturn("_Test");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("org.citrusframework.wsdl");
        when(prompter.prompt(contains("mode"), any(List.class), nullable(String.class))).thenReturn(TestGenerator.GeneratorMode.CLIENT.name());
        when(prompter.prompt(contains("type"), any(List.class), nullable(String.class))).thenReturn("xml");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("operation"), nullable(String.class))).thenReturn("all");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Create test with Swagger API"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        when(wsdlXmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.withDescription("TODO")).thenReturn(wsdlXmlTestGenerator);
        when(wsdlXmlTestGenerator.usePackage("org.citrusframework.wsdl")).thenReturn(wsdlXmlTestGenerator);

        when(wsdlXmlTestGenerator.withName("BookStore")).thenReturn(wsdlXmlTestGenerator);

        mojo.execute();

        verify(wsdlXmlTestGenerator, times(0)).create();
    }

    @Test
    public void testSuiteFromSwagger() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(prompter, swaggerXmlTestGenerator);

        when(prompter.prompt(contains("test name"))).thenReturn("UserLoginService");
        when(prompter.prompt(contains("path"))).thenReturn("classpath:swagger/user-login-api.json");
        when(prompter.prompt(contains("prefix"), nullable(String.class))).thenReturn("UserLoginService_");
        when(prompter.prompt(contains("suffix"), nullable(String.class))).thenReturn("_IT");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("org.citrusframework.swagger");
        when(prompter.prompt(contains("mode"), any(List.class), nullable(String.class))).thenReturn(TestGenerator.GeneratorMode.CLIENT.name());
        when(prompter.prompt(contains("type"), any(List.class), nullable(String.class))).thenReturn("xml");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("operation"), nullable(String.class))).thenReturn("all");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with Swagger API"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(swaggerXmlTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(swaggerXmlTestGenerator);
        when(swaggerXmlTestGenerator.withAuthor("UnknownAuthor")).thenReturn(swaggerXmlTestGenerator);
        when(swaggerXmlTestGenerator.withDescription("TODO")).thenReturn(swaggerXmlTestGenerator);
        when(swaggerXmlTestGenerator.usePackage("org.citrusframework.swagger")).thenReturn(swaggerXmlTestGenerator);

        when(swaggerXmlTestGenerator.withSpec("classpath:swagger/user-login-api.json")).thenReturn(swaggerXmlTestGenerator);
        when(swaggerXmlTestGenerator.withNameSuffix("_IT")).thenReturn(swaggerXmlTestGenerator);

        when(swaggerXmlTestGenerator.withName("UserLoginService")).thenReturn(swaggerXmlTestGenerator);

        mojo.execute();

        verify(swaggerXmlTestGenerator).create();
        verify(swaggerXmlTestGenerator).withSpec("classpath:swagger/user-login-api.json");
        verify(swaggerXmlTestGenerator).withNameSuffix("_IT");
    }
}
