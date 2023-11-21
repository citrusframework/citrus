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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.script.GroovyAction.Builder.groovy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class GroovyTestActionBuilderTest extends UnitTestSupport {
    private final Resource scriptResource = Mockito.mock(Resource.class);

    @Test
    public void testGroovyBuilderWithResource() throws IOException {
        reset(scriptResource);
        when(scriptResource.exists()).thenReturn(true);
        when(scriptResource.getInputStream()).thenReturn(new ByteArrayInputStream("println 'Wow groovy!'".getBytes()));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(groovy().script(scriptResource)
                        .skipTemplate());

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "groovy");

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Wow groovy!'");
        Assert.assertFalse(action.isUseScriptTemplate());

    }

    @Test
    public void testGroovyBuilderWithScript() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(groovy().script("println 'Groovy!'")
                        .skipTemplate());

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Groovy!'");
        Assert.assertFalse(action.isUseScriptTemplate());
    }

    @Test
    public void testGroovyBuilderWithTemplate() throws IOException {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(groovy().script("context.setVariable('message', 'Groovy!')")
                        .template(Resources.fromClasspath("org/citrusframework/script/script-template.groovy")));

        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Groovy!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertNotNull(action.getScriptTemplate());
        Assert.assertTrue(action.isUseScriptTemplate());
    }

    @Test
    public void testGroovyBuilderWithTemplatePath() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(groovy().script("context.setVariable('message', 'Groovy!')")
                        .template("classpath:org/citrusframework/script/script-template.groovy"));

        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Groovy!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertNotNull(action.getScriptTemplatePath());
        Assert.assertTrue(action.isUseScriptTemplate());
    }
}
