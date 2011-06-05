/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus;

import static org.easymock.EasyMock.*;

import java.util.List;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestEngineFailedException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class CitrusCliTest extends AbstractBaseTest {
    
    private TestNG testngMock = EasyMock.createMock(TestNG.class);
    
    @Test
    public void testRunSingleTest() {
        Citrus.main(new String[] {"-test", "SampleTest", "-testdir", "../src/citrus/tests"});
    }
    
    @Test
    public void testCustomTestDir() {
        Citrus.main(new String[] {"-test", "SampleTest", "-testdir", "../src/test/resources"});
    }
    
    @Test
    public void testRunTestsInPackage() {
        Citrus.main(new String[] {"-package", "com.consol.citrus.sample"});
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleTests() throws ParseException {
        reset(testngMock);

        testngMock.setXmlSuites((List<XmlSuite>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<XmlSuite> suites = (List<XmlSuite>)getCurrentArguments()[0];
                Assert.assertEquals(suites.size(), 1);
                Assert.assertEquals(suites.get(0).getTests().size(), 2);
                Assert.assertEquals(suites.get(0).getName(), "citrus-test-suite");
                Assert.assertEquals(suites.get(0).getTests().get(0).getName(), "SampleTest");
                Assert.assertEquals(suites.get(0).getTests().get(1).getName(), "SampleTest");
                return null;
            }
        }).once();
        
        testngMock.run();
        expectLastCall().once();
        
        expect(testngMock.hasFailure()).andReturn(false).once();
        
        replay(testngMock);
        
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] {"-test", "SampleTest", "SampleTest", "-testdir", "../src/citrus/tests"}));
        citrus.setTestNG(testngMock);
        citrus.run();
        
        verify(testngMock);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMultiplePackages() throws ParseException {
        reset(testngMock);

        testngMock.setXmlSuites((List<XmlSuite>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<XmlSuite> suites = (List<XmlSuite>)getCurrentArguments()[0];
                Assert.assertEquals(suites.size(), 1);
                Assert.assertEquals(suites.get(0).getTests().size(), 2);
                Assert.assertEquals(suites.get(0).getTests().get(0).getName(), "com.consol.citrus.sample");
                Assert.assertEquals(suites.get(0).getTests().get(1).getName(), "com.consol.citrus.aop");
                return null;
            }
        }).once();
        
        testngMock.run();
        expectLastCall().once();
        
        expect(testngMock.hasFailure()).andReturn(false).once();
        
        replay(testngMock);
        
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] {"-package", "com.consol.citrus.sample", "com.consol.citrus.aop" , "-testdir", "../src/citrus/tests"}));
        citrus.setTestNG(testngMock);
        citrus.run();
        
        verify(testngMock);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCustomSuiteNameTest() throws ParseException {
        final String customSuiteName = "customSuite";
        
        reset(testngMock);

        testngMock.setXmlSuites((List<XmlSuite>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<XmlSuite> suites = (List<XmlSuite>)getCurrentArguments()[0];
                Assert.assertEquals(suites.size(), 1);
                Assert.assertEquals(suites.get(0).getName(), customSuiteName);
                return null;
            }
        }).once();
        
        testngMock.run();
        expectLastCall().once();
        
        expect(testngMock.hasFailure()).andReturn(false).once();
        
        replay(testngMock);
        
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] {"-test", "SampleTest", "-suitename", customSuiteName, "-testdir", "../src/citrus/tests"}));
        citrus.setTestNG(testngMock);
        citrus.run();
        
        verify(testngMock);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testTestNgXml() throws ParseException {
        reset(testngMock);

        testngMock.setXmlSuites((List<XmlSuite>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<XmlSuite> suites = (List<XmlSuite>)getCurrentArguments()[0];
                Assert.assertEquals(suites.size(), 1);
                Assert.assertEquals(suites.get(0).getTests().size(), 0);
                Assert.assertEquals(suites.get(0).getName(), "citrus-test-suite");
                return null;
            }
        }).once();
        
        testngMock.setTestSuites((List<String>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<String> suites = (List<String>)getCurrentArguments()[0];
                Assert.assertEquals(suites.size(), 1);
                Assert.assertEquals(suites.get(0), "testng-suite.xml");
                return null;
            }
        });
        
        testngMock.run();
        expectLastCall().once();
        
        expect(testngMock.hasFailure()).andReturn(false).once();
        
        replay(testngMock);
        
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] {"testng-suite.xml"}));
        citrus.setTestNG(testngMock);
        citrus.run();
        
        verify(testngMock);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testHasFailures() throws ParseException {
        
        reset(testngMock);

        testngMock.setXmlSuites((List<XmlSuite>)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<XmlSuite> suites = (List<XmlSuite>)getCurrentArguments()[0];
                Assert.assertEquals(suites.size(), 1);
                Assert.assertEquals(suites.get(0).getName(), "citrus-test-suite");
                return null;
            }
        }).once();
        
        testngMock.run();
        expectLastCall().once();
        
        expect(testngMock.hasFailure()).andReturn(true).once();
        
        replay(testngMock);
        
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] {"-test", "SampleTest", "-testdir", "../src/citrus/tests"}));
        citrus.setTestNG(testngMock);
        
        try {
            citrus.run();
            Assert.fail("Missing TestEngineFailedException due to failures in TestNG");
        } catch(TestEngineFailedException e) {
            verify(testngMock);
        }
    }
    
    @Test
    public void testHelp() {
        Citrus.main(new String[] {"-help"});
    }
    
    @Test
    public void testNoArguments() {
        Citrus.main(new String[] {});
    }
    
    @Test
    public void testUnknownArgument() {
        Citrus.main(new String[] {"-unknown"});
    }
    
    @Test
    public void testNameNotFound() {
        try {
            Citrus.main(new String[] {"-test", "UnknownTest"});
            Assert.fail("Missing exception for unknown test");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("'UnknownTest'"));
        }
    }
}
