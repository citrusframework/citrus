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

package org.citrusframework.citrus.dsl.runner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.script.GroovyAction;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class GroovyTestRunnerTest extends UnitTestSupport {
    private Resource scriptResource = Mockito.mock(Resource.class);
    private Resource scriptTemplate = Mockito.mock(Resource.class);
    private File file = Mockito.mock(File.class);

    @Test
    public void testGroovyBuilderWithResource() throws IOException {
        reset(scriptResource);
        when(scriptResource.getInputStream()).thenReturn(new ByteArrayInputStream("println 'Wow groovy!'".getBytes()));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                groovy(builder -> builder.script(scriptResource)
                        .skipTemplate());
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "groovy");

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Wow groovy!'");
        Assert.assertEquals(action.isUseScriptTemplate(), false);

    }

    @Test
    public void testGroovyBuilderWithScript() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                groovy(builder -> builder.script("println 'Groovy!'")
                        .skipTemplate());
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Groovy!'");
        Assert.assertEquals(action.isUseScriptTemplate(), false);
    }

    @Test
    public void testGroovyBuilderWithTemplate() throws IOException {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                groovy(builder -> builder.script("context.setVariable('message', 'Groovy!')")
                        .template(new ClassPathResource("org/citrusframework/citrus/script/script-template.groovy")));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Groovy!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertNotNull(action.getScriptTemplate());
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }

    @Test
    public void testGroovyBuilderWithTemplatePath() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                groovy(builder -> builder.script("context.setVariable('message', 'Groovy!')")
                        .template("classpath:org/citrusframework/citrus/script/script-template.groovy"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Groovy!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertNotNull(action.getScriptTemplatePath());
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }
}
