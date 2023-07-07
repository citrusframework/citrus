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

package org.citrusframework.actions.dsl;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.AntRunAction;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.AntRunAction.Builder.antrun;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 */
public class AntRunTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testAntRunBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(antrun("org/citrusframework/actions/dsl/build.xml")
                    .target("sayHello"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);

        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "org/citrusframework/actions/dsl/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
    }

    @Test
    public void testAntRunBuilderWithTargets() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(antrun("org/citrusframework/actions/dsl/build.xml")
                        .targets("sayHello", "sayGoodbye"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);

        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "org/citrusframework/actions/dsl/build.xml");
        Assert.assertNull(action.getTarget());
        Assert.assertEquals(action.getTargets(), "sayHello,sayGoodbye");
    }

    @Test
    public void testAntRunBuilderWithProperty() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(antrun("org/citrusframework/actions/dsl/build.xml")
                        .target("sayHello")
                        .property("welcomeText", "Hi everybody!")
                        .property("goodbyeText", "Goodbye!"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);

        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "org/citrusframework/actions/dsl/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertEquals(action.getProperties().size(), 2L);
        Assert.assertEquals(action.getProperties().getProperty("welcomeText"), "Hi everybody!");
        Assert.assertEquals(action.getProperties().getProperty("goodbyeText"), "Goodbye!");
    }

    @Test
    public void testAntRunBuilderWithPropertyFile() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("checked", true);

        builder.$(antrun("org/citrusframework/actions/dsl/build.xml")
                .target("checkMe")
                .propertyFile("classpath:org/citrusframework/actions/dsl/build.properties"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AntRunAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), AntRunAction.class);

        AntRunAction action = (AntRunAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "org/citrusframework/actions/dsl/build.xml");
        Assert.assertEquals(action.getTarget(), "checkMe");
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertEquals(action.getPropertyFilePath(), "classpath:org/citrusframework/actions/dsl/build.properties");
    }

    @Test
    public void testAntRunBuilderWithBuildListener() {
        final BuildListener buildListener = Mockito.mock(BuildListener.class);

        reset(buildListener);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(antrun("org/citrusframework/actions/dsl/build.xml")
                        .target("sayHello")
                        .listener(buildListener));

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
