/*
 * Copyright the original author or authors.
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

package org.citrusframework.groovy.actions;

import java.io.ByteArrayInputStream;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.groovy.UnitTestSupport;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class GroovyTestActionBuilderTest extends UnitTestSupport implements TestActionSupport {
    private final Resource scriptResource = Mockito.mock(Resource.class);

    @Test
    public void testGroovyBuilderWithResource() {
        reset(scriptResource);
        when(scriptResource.exists()).thenReturn(true);
        when(scriptResource.getInputStream()).thenReturn(new ByteArrayInputStream("println 'Wow groovy!'".getBytes()));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(script().groovy().script(scriptResource)
                        .skipTemplate());

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(GroovyAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals("groovy", test.getActions().get(0).getName());

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assertions.assertEquals("println 'Wow groovy!'", action.getScript());
        Assertions.assertFalse(action.isUseScriptTemplate());

    }

    @Test
    public void testGroovyBuilderWithScript() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(script().groovy().script("println 'Groovy!'")
                        .skipTemplate());

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(GroovyAction.class, test.getActions().get(0).getClass());

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assertions.assertEquals("println 'Groovy!'", action.getScript());
        Assertions.assertFalse(action.isUseScriptTemplate());
    }

    @Test
    public void testGroovyBuilderWithTemplate() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(script().groovy().script("context.setVariable('message', 'Groovy!')")
                        .template(Resources.fromClasspath("org/citrusframework/script/script-template.groovy")));

        Assertions.assertNotNull(context.getVariable("message"));
        Assertions.assertEquals("Groovy!", context.getVariable("message"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(GroovyAction.class, test.getActions().get(0).getClass());

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assertions.assertNotNull(action.getScriptTemplate());
        Assertions.assertTrue(action.isUseScriptTemplate());
    }

    @Test
    public void testGroovyBuilderWithTemplatePath() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(script().groovy().script("context.setVariable('message', 'Groovy!')")
                        .template("classpath:org/citrusframework/script/script-template.groovy"));

        Assertions.assertNotNull(context.getVariable("message"));
        Assertions.assertEquals("Groovy!", context.getVariable("message"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(GroovyAction.class, test.getActions().get(0).getClass());

        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assertions.assertNotNull(action.getScriptTemplatePath());
        Assertions.assertTrue(action.isUseScriptTemplate());
    }
}
