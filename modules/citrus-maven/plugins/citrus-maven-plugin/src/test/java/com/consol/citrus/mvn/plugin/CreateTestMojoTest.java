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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.util.TestCaseCreator;
import com.consol.citrus.util.TestCaseCreator.UnitFramework;

/**
 * @author Christoph Deppisch
 */
public class CreateTestMojoTest {

    private Prompter prompter = EasyMock.createMock(Prompter.class);
    
    private TestCaseCreator testCaseCreator = EasyMock.createMock(TestCaseCreator.class);
    
    private CreateTestMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new CreateTestMojo() {
            public TestCaseCreator getTestCaseCreator() {
                return testCaseCreator;
            };
        };
        
        mojo.setPrompter(prompter);
        mojo.setInteractiveMode(true);
    }
    
    @Test
    public void testCreate() throws PrompterException, MojoExecutionException {
        reset(prompter, testCaseCreator);
        
        expect(prompter.prompt(contains("test name"))).andReturn("FooTest").once();
        expect(prompter.prompt(contains("author"), anyObject(String.class))).andReturn("UnknownAuthor").once();
        expect(prompter.prompt(contains("description"), anyObject(String.class))).andReturn("TODO").once();
        expect(prompter.prompt(contains("package"), anyObject(String.class))).andReturn("com.consol.citrus.foo").once();
        expect(prompter.prompt(contains("framework"), anyObject(String.class))).andReturn("testng").once();
        expect(prompter.prompt(contains("Confirm"), anyObject(List.class), anyObject(String.class))).andReturn("y").once();
        
        expect(testCaseCreator.withFramework(UnitFramework.TESTNG)).andReturn(testCaseCreator).once();
        expect(testCaseCreator.withAuthor("UnknownAuthor")).andReturn(testCaseCreator).once();
        expect(testCaseCreator.withDescription("TODO")).andReturn(testCaseCreator).once();
        expect(testCaseCreator.usePackage("com.consol.citrus.foo")).andReturn(testCaseCreator).once();
        expect(testCaseCreator.withName("FooTest")).andReturn(testCaseCreator).once();
        
        testCaseCreator.createTestCase();
        expectLastCall().once();
        
        replay(prompter, testCaseCreator);
        
        mojo.execute();
        
        verify(prompter, testCaseCreator);
    }
    
    @Test
    public void testAbort() throws PrompterException, MojoExecutionException {
        reset(prompter, testCaseCreator);
        
        expect(prompter.prompt(contains("test name"))).andReturn("FooTest").once();
        expect(prompter.prompt(contains("author"), anyObject(String.class))).andReturn("UnknownAuthor").once();
        expect(prompter.prompt(contains("description"), anyObject(String.class))).andReturn("TODO").once();
        expect(prompter.prompt(contains("package"), anyObject(String.class))).andReturn("com.consol.citrus.foo").once();
        expect(prompter.prompt(contains("framework"), anyObject(String.class))).andReturn("testng").once();
        expect(prompter.prompt(contains("Confirm"), anyObject(List.class), anyObject(String.class))).andReturn("n").once();
        
        replay(prompter, testCaseCreator);
        
        mojo.execute();
        
        verify(prompter, testCaseCreator);
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
}
