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

import com.consol.citrus.creator.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class CreateTestMojoTest {

    private Prompter prompter = Mockito.mock(Prompter.class);
    
    private XmlTestCreator testCaseCreator = Mockito.mock(XmlTestCreator.class);
    private ReqResXmlTestCreator reqResXmlTestCaseCreator = Mockito.mock(ReqResXmlTestCreator.class);

    private CreateTestMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new CreateTestMojo() {
            public XmlTestCreator getXmlTestCaseCreator() {
                return testCaseCreator;
            }

            @Override
            public ReqResXmlTestCreator getReqResXmlTestCaseCreator() {
                return reqResXmlTestCaseCreator;
            }
        };

        mojo.setPrompter(prompter);
        mojo.setInteractiveMode(true);
    }
    
    @Test
    public void testCreate() throws PrompterException, MojoExecutionException {
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
    public void testAbort() throws PrompterException, MojoExecutionException {
        reset(prompter, testCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("FooTest");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.foo");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        when(testCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(testCaseCreator);
        when(testCaseCreator.withAuthor("UnknownAuthor")).thenReturn(testCaseCreator);
        when(testCaseCreator.withDescription("TODO")).thenReturn(testCaseCreator);
        when(testCaseCreator.usePackage("com.consol.citrus.foo")).thenReturn(testCaseCreator);
        when(testCaseCreator.withName("FooTest")).thenReturn(testCaseCreator);
        
        mojo.execute();
    }
    
    @Test
    public void testEmptyTestName() throws PrompterException {
        try {
            mojo.setInteractiveMode(false);
            mojo.execute();
            Assert.fail("Missing exception due to invalid test name");
        } catch (MojoExecutionException e) {
            Assert.assertTrue(e.getMessage().contains("Please provide proper test name"));
        }
    }

    @Test
    public void testSuiteFromWsdl() throws MojoExecutionException, PrompterException {
        reset(prompter, reqResXmlTestCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("IT_");
        when(prompter.prompt(contains("path"), nullable(String.class))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("suffix"), nullable(String.class))).thenReturn("_Test");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.wsdl");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("y");

        when(reqResXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.withDescription("TODO")).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.usePackage("com.consol.citrus.wsdl")).thenReturn(reqResXmlTestCaseCreator);

        when(reqResXmlTestCaseCreator.getName()).thenReturn("IT_");

        when(reqResXmlTestCaseCreator.withName("IT_")).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.withName("IT_addBook_Test")).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.withName("IT_addBookAudio_Test")).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.withName("IT_deleteBook_Test")).thenReturn(reqResXmlTestCaseCreator);

        // addBook
        doAnswer(new Answer<ReqResXmlTestCreator>() {
            @Override
            public ReqResXmlTestCreator answer(InvocationOnMock invocation) throws Throwable {
                String requestXml = invocation.getArguments()[0].toString();

                Assert.assertTrue(requestXml.contains("<book:addBook xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(requestXml.contains("<author>string</author>"));
                Assert.assertTrue(requestXml.contains("<title>string</title>"));
                Assert.assertTrue(requestXml.contains("<isbn>string</isbn>"));
                Assert.assertTrue(requestXml.contains("<year>string</year>"));
                Assert.assertTrue(requestXml.contains("</book:addBook>"));

                return reqResXmlTestCaseCreator;
            }
        }).doAnswer(new Answer<ReqResXmlTestCreator>() { // addBookAudio
            @Override
            public ReqResXmlTestCreator answer(InvocationOnMock invocation) throws Throwable {
                String requestXml = invocation.getArguments()[0].toString();

                Assert.assertTrue(requestXml.contains("<aud:addBookAudio xmlns:aud=\"http://www.citrusframework.org/bookstore/audio\">"));
                Assert.assertTrue(requestXml.contains("<author>string</author>"));
                Assert.assertTrue(requestXml.contains("<title>string</title>"));
                Assert.assertTrue(requestXml.contains("<isbn>string</isbn>"));
                Assert.assertTrue(requestXml.contains("<year>string</year>"));
                Assert.assertTrue(requestXml.contains("<length>100</length>"));
                Assert.assertTrue(requestXml.contains("</aud:addBookAudio>"));

                return reqResXmlTestCaseCreator;
            }
        }).doAnswer(new Answer<ReqResXmlTestCreator>() { // deleteBook
            @Override
            public ReqResXmlTestCreator answer(InvocationOnMock invocation) throws Throwable {
                String requestXml = invocation.getArguments()[0].toString();

                Assert.assertTrue(requestXml.contains("<book:deleteBook xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(requestXml.contains("<isbn>string</isbn>"));
                Assert.assertTrue(requestXml.contains("</book:deleteBook>"));

                return reqResXmlTestCaseCreator;
            }
        }).when(reqResXmlTestCaseCreator).withRequest(any(String.class));

        doAnswer(new Answer<ReqResXmlTestCreator>() {
            @Override
            public ReqResXmlTestCreator answer(InvocationOnMock invocation) throws Throwable {
                String responseXml = invocation.getArguments()[0].toString();

                Assert.assertTrue(responseXml.contains("<book:addBookResponse xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(responseXml.contains("<success>false</success>"));
                Assert.assertTrue(responseXml.contains("</book:addBookResponse>"));

                return reqResXmlTestCaseCreator;
            }
        }).doAnswer(new Answer<ReqResXmlTestCreator>() { // addBookAudio
            @Override
            public ReqResXmlTestCreator answer(InvocationOnMock invocation) throws Throwable {
                String responseXml = invocation.getArguments()[0].toString();

                Assert.assertTrue(responseXml.contains("<aud:addBookAudioResponse xmlns:aud=\"http://www.citrusframework.org/bookstore/audio\">"));
                Assert.assertTrue(responseXml.contains("<success>false</success>"));
                Assert.assertTrue(responseXml.contains("</aud:addBookAudioResponse>"));

                return reqResXmlTestCaseCreator;
            }
        }).doAnswer(new Answer<ReqResXmlTestCreator>() { // deleteBook
            @Override
            public ReqResXmlTestCreator answer(InvocationOnMock invocation) throws Throwable {
                String responseXml = invocation.getArguments()[0].toString();

                Assert.assertTrue(responseXml.contains("<book:deleteBookResponse xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(responseXml.contains("<success>false</success>"));
                Assert.assertTrue(responseXml.contains("</book:deleteBookResponse>"));

                return reqResXmlTestCaseCreator;
            }
        }).when(reqResXmlTestCaseCreator).withResponse(nullable(String.class));

        mojo.execute();

        verify(reqResXmlTestCaseCreator, times(3)).create();
    }

    @Test
    public void testSuiteFromWsdlAbort() throws MojoExecutionException, PrompterException {
        reset(prompter, reqResXmlTestCaseCreator);

        when(prompter.prompt(contains("test name"))).thenReturn("IT_");
        when(prompter.prompt(contains("path"), nullable(String.class))).thenReturn("classpath:wsdl/BookStore.wsdl");
        when(prompter.prompt(contains("suffix"), nullable(String.class))).thenReturn("_Test");
        when(prompter.prompt(contains("author"), nullable(String.class))).thenReturn("UnknownAuthor");
        when(prompter.prompt(contains("description"), nullable(String.class))).thenReturn("TODO");
        when(prompter.prompt(contains("package"), nullable(String.class))).thenReturn("com.consol.citrus.wsdl");
        when(prompter.prompt(contains("framework"), any(List.class), nullable(String.class))).thenReturn("testng");
        when(prompter.prompt(contains("Create test with XML schema"), any(List.class), eq("n"))).thenReturn("n");
        when(prompter.prompt(contains("Create test with WSDL"), any(List.class), eq("n"))).thenReturn("y");
        when(prompter.prompt(contains("Confirm"), any(List.class), eq("y"))).thenReturn("n");

        when(reqResXmlTestCaseCreator.withFramework(UnitFramework.TESTNG)).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.withAuthor("UnknownAuthor")).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.withDescription("TODO")).thenReturn(reqResXmlTestCaseCreator);
        when(reqResXmlTestCaseCreator.usePackage("com.consol.citrus.wsdl")).thenReturn(reqResXmlTestCaseCreator);

        when(reqResXmlTestCaseCreator.withName("IT_")).thenReturn(reqResXmlTestCaseCreator);

        mojo.execute();
    }
}
