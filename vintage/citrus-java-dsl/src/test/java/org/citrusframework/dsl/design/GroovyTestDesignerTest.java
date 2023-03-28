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

package org.citrusframework.dsl.design;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.citrusframework.TestCase;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class GroovyTestDesignerTest extends UnitTestSupport {
    private Resource scriptResource = Mockito.mock(Resource.class);
    private Resource scriptTemplate = Mockito.mock(Resource.class);
    private File file = Mockito.mock(File.class);

    @Test
    public void testGroovyBuilderWithResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                groovy(scriptResource)
                    .skipTemplate();
            }
        };

        reset(scriptResource);
        when(scriptResource.getInputStream()).thenReturn(new ByteArrayInputStream("someScript".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "groovy");

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "someScript");
        Assert.assertEquals(action.isUseScriptTemplate(), false);

    }

    @Test
    public void testGroovyBuilderWithScript() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                groovy("println 'Groovy!'")
                    .skipTemplate();
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Groovy!'");
        Assert.assertEquals(action.isUseScriptTemplate(), false);
    }

    @Test
    public void testGroovyBuilderWithTemplate() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                groovy("println 'Groovy!'")
                    .template(scriptTemplate);
            }
        };

        reset(scriptTemplate, file);
        when(scriptTemplate.getFile()).thenReturn(file);
        when(scriptTemplate.getInputStream()).thenReturn(new ByteArrayInputStream("println 'hello'".getBytes()));
        when(file.getAbsolutePath()).thenReturn("classpath:some.file");
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScriptTemplate(), "println 'hello'");
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }

    @Test
    public void testGroovyBuilderWithTemplatePath() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                groovy("println 'Groovy!'")
                    .template("classpath:script-template.groovy");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertNotNull(action.getScriptTemplatePath());
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }
}
