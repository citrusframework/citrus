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

import static org.easymock.EasyMock.*;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.util.TestCaseCreator;
import com.consol.citrus.util.TestCaseCreator.UnitFramework;

/**
 * @author Christoph Deppisch
 */
public class CreateTestsFromWsdlMojoTest {

    private Prompter prompter = EasyMock.createMock(Prompter.class);
    
    private TestCaseCreator testCaseCreator = EasyMock.createMock(TestCaseCreator.class);
    
    private CreateTestsFromWsdlMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new CreateTestsFromWsdlMojo() {
            public TestCaseCreator getTestCaseCreator() {
                return testCaseCreator;
            };
        };
        
        mojo.setPrompter(prompter);
        mojo.setInteractiveMode(true);
    }
    
    @Test
    public void testSuiteFromWsdl() throws MojoExecutionException, PrompterException {
        
        reset(prompter, testCaseCreator);
        
        expect(prompter.prompt(contains("path"))).andReturn("classpath:wsdl/BookStore.wsdl").once();
        expect(prompter.prompt(contains("prefix"), anyObject(String.class))).andReturn("IT_").once();
        expect(prompter.prompt(contains("suffix"), anyObject(String.class))).andReturn("_Test").once();
        expect(prompter.prompt(contains("author"), anyObject(String.class))).andReturn("UnknownAuthor").once();
        expect(prompter.prompt(contains("description"), anyObject(String.class))).andReturn("TODO").once();
        expect(prompter.prompt(contains("package"), anyObject(String.class))).andReturn("com.consol.citrus.wsdl").once();
        expect(prompter.prompt(contains("framework"), anyObject(String.class))).andReturn("testng").once();
        expect(prompter.prompt(contains("Confirm"), anyObject(List.class), anyObject(String.class))).andReturn("y").once();
        
        expect(testCaseCreator.withFramework(UnitFramework.TESTNG)).andReturn(testCaseCreator).times(3);
        expect(testCaseCreator.withAuthor("UnknownAuthor")).andReturn(testCaseCreator).times(3);
        expect(testCaseCreator.withDescription("TODO")).andReturn(testCaseCreator).times(3);
        expect(testCaseCreator.usePackage("com.consol.citrus.wsdl")).andReturn(testCaseCreator).times(3);
        
        // addBook
        expect(testCaseCreator.withXmlRequest(anyObject(String.class))).andAnswer(new IAnswer<TestCaseCreator>() {
            public TestCaseCreator answer() throws Throwable {
                String requestXml = getCurrentArguments()[0].toString();
                
                Assert.assertTrue(requestXml.contains("<book:addBook xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(requestXml.contains("<author>string</author>"));
                Assert.assertTrue(requestXml.contains("<title>string</title>"));
                Assert.assertTrue(requestXml.contains("<isbn>string</isbn>"));
                Assert.assertTrue(requestXml.contains("<year>string</year>"));
                Assert.assertTrue(requestXml.contains("</book:addBook>"));
                
                return testCaseCreator;
            }
        }).once();
        
        expect(testCaseCreator.withXmlResponse(anyObject(String.class))).andAnswer(new IAnswer<TestCaseCreator>() {
            public TestCaseCreator answer() throws Throwable {
                String responseXml = getCurrentArguments()[0].toString();
                
                Assert.assertTrue(responseXml.contains("<book:addBookResponse xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(responseXml.contains("<success>false</success>"));
                Assert.assertTrue(responseXml.contains("</book:addBookResponse>"));
                        
                return testCaseCreator;
            }
            
        }).once();
        
        expect(testCaseCreator.withName("IT_addBook_Test")).andReturn(testCaseCreator).once();
        
       // addBookAudio
        expect(testCaseCreator.withXmlRequest(anyObject(String.class))).andAnswer(new IAnswer<TestCaseCreator>() {
            public TestCaseCreator answer() throws Throwable {
                String requestXml = getCurrentArguments()[0].toString();
                
                Assert.assertTrue(requestXml.contains("<aud:addBookAudio xmlns:aud=\"http://www.citrusframework.org/bookstore/audio\">"));
                Assert.assertTrue(requestXml.contains("<author>string</author>"));
                Assert.assertTrue(requestXml.contains("<title>string</title>"));
                Assert.assertTrue(requestXml.contains("<isbn>string</isbn>"));
                Assert.assertTrue(requestXml.contains("<year>string</year>"));
                Assert.assertTrue(requestXml.contains("<length>100</length>"));
                Assert.assertTrue(requestXml.contains("</aud:addBookAudio>"));
                
                return testCaseCreator;
            }
        }).once();
        
        expect(testCaseCreator.withXmlResponse(anyObject(String.class))).andAnswer(new IAnswer<TestCaseCreator>() {
            public TestCaseCreator answer() throws Throwable {
                String responseXml = getCurrentArguments()[0].toString();
                
                Assert.assertTrue(responseXml.contains("<aud:addBookAudioResponse xmlns:aud=\"http://www.citrusframework.org/bookstore/audio\">"));
                Assert.assertTrue(responseXml.contains("<success>false</success>"));
                Assert.assertTrue(responseXml.contains("</aud:addBookAudioResponse>"));
                        
                return testCaseCreator;
            }
            
        }).once();
        
        expect(testCaseCreator.withName("IT_addBookAudio_Test")).andReturn(testCaseCreator).once();
        
        // deleteBook
        expect(testCaseCreator.withXmlRequest(anyObject(String.class))).andAnswer(new IAnswer<TestCaseCreator>() {
            public TestCaseCreator answer() throws Throwable {
                String requestXml = getCurrentArguments()[0].toString();
                
                Assert.assertTrue(requestXml.contains("<book:deleteBook xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(requestXml.contains("<isbn>string</isbn>"));
                Assert.assertTrue(requestXml.contains("</book:deleteBook>"));
                
                return testCaseCreator;
            }
        }).once();
        
        expect(testCaseCreator.withXmlResponse(anyObject(String.class))).andAnswer(new IAnswer<TestCaseCreator>() {
            public TestCaseCreator answer() throws Throwable {
                String responseXml = getCurrentArguments()[0].toString();
                
                Assert.assertTrue(responseXml.contains("<book:deleteBookResponse xmlns:book=\"http://www.citrusframework.org/bookstore/\">"));
                Assert.assertTrue(responseXml.contains("<success>false</success>"));
                Assert.assertTrue(responseXml.contains("</book:deleteBookResponse>"));
                        
                return testCaseCreator;
            }
            
        }).once();
        
        expect(testCaseCreator.withName("IT_deleteBook_Test")).andReturn(testCaseCreator).once();
        
        testCaseCreator.createTestCase();
        expectLastCall().times(3);
        
        replay(prompter, testCaseCreator);
        
        mojo.execute();
        
        verify(prompter, testCaseCreator);
    }
    
    @Test
    public void testSuiteFromWsdlAbort() throws MojoExecutionException, PrompterException {
        
        reset(prompter, testCaseCreator);
        
        expect(prompter.prompt(contains("path"))).andReturn("classpath:wsdl/BookStore.wsdl").once();
        expect(prompter.prompt(contains("prefix"), anyObject(String.class))).andReturn("IT_").once();
        expect(prompter.prompt(contains("suffix"), anyObject(String.class))).andReturn("_Test").once();
        expect(prompter.prompt(contains("author"), anyObject(String.class))).andReturn("UnknownAuthor").once();
        expect(prompter.prompt(contains("description"), anyObject(String.class))).andReturn("TODO").once();
        expect(prompter.prompt(contains("package"), anyObject(String.class))).andReturn("com.consol.citrus.wsdl").once();
        expect(prompter.prompt(contains("framework"), anyObject(String.class))).andReturn("testng").once();
        expect(prompter.prompt(contains("Confirm"), anyObject(List.class), anyObject(String.class))).andReturn("n").once();
        
        replay(prompter, testCaseCreator);
        
        mojo.execute();
        
        verify(prompter, testCaseCreator);
    }
    
    @Test
    public void testEmptyWsdlPath() throws PrompterException {
        try {
            mojo.setInteractiveMode(false);
            mojo.execute();
            Assert.fail("Missing exception due to invalid WSDL path");
        } catch (MojoExecutionException e) {
            Assert.assertTrue(e.getMessage().contains("Please provide proper path to WSDL file"));
        }
    }
    
}
