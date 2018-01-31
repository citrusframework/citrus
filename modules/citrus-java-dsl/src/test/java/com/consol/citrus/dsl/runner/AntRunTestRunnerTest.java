/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.AntRunAction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class AntRunTestRunnerTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testAntRunBuilder() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                antrun(builder -> builder.buildFilePath("com/consol/citrus/dsl/runner/build.xml")
                    .target("sayHello"));
            }
        };
        
        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);

        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/citrus/dsl/runner/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
    }
    
    @Test
    public void testAntRunBuilderWithTargets() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                antrun(builder -> builder.buildFilePath("com/consol/citrus/dsl/runner/build.xml")
                        .targets("sayHello", "sayGoodbye"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/citrus/dsl/runner/build.xml");
        Assert.assertNull(action.getTarget());
        Assert.assertEquals(action.getTargets(), "sayHello,sayGoodbye");
    }
    
    @Test
    public void testAntRunBuilderWithProperty() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                antrun(builder -> builder.buildFilePath("com/consol/citrus/dsl/runner/build.xml")
                        .target("sayHello")
                        .property("welcomeText", "Hi everybody!")
                        .property("goodbyeText", "Goodbye!"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/citrus/dsl/runner/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertEquals(action.getProperties().size(), 2L);
        Assert.assertEquals(action.getProperties().getProperty("welcomeText"), "Hi everybody!");
        Assert.assertEquals(action.getProperties().getProperty("goodbyeText"), "Goodbye!");
    }
    
    @Test
    public void testAntRunBuilderWithPropertyFile() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                variable("checked", true);

                antrun(builder -> builder.buildFilePath("com/consol/citrus/dsl/runner/build.xml")
                        .target("checkMe")
                        .propertyFile("classpath:com/consol/citrus/dsl/runner/build.properties"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/citrus/dsl/runner/build.xml");
        Assert.assertEquals(action.getTarget(), "checkMe");
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertEquals(action.getPropertyFilePath(), "classpath:com/consol/citrus/dsl/runner/build.properties");
    }
    
    @Test
    public void testAntRunBuilderWithBuildListener() {
        final BuildListener buildListener = Mockito.mock(BuildListener.class);

        reset(buildListener);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                antrun(builder -> builder.buildFilePath("com/consol/citrus/dsl/runner/build.xml")
                        .target("sayHello")
                        .listener(buildListener));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);

        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildListener(), buildListener);

        verify(buildListener).taskStarted(any(BuildEvent.class));
        verify(buildListener).targetStarted(any(BuildEvent.class));
        verify(buildListener, times(3)).messageLogged(any(BuildEvent.class));
        verify(buildListener).targetFinished(any(BuildEvent.class));
        verify(buildListener).taskFinished(any(BuildEvent.class));
    }
}
