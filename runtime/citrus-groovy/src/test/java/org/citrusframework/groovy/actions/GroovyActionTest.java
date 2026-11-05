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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;

public class GroovyActionTest extends AbstractUnitTest {

    @Test
    public void testScript() {
        GroovyAction bean = new GroovyAction.Builder()
                .script("println 'Hello TestFramework!'")
                .build();
        bean.execute(context);
    }

    @Test
    public void testScriptResource() {
        GroovyAction bean = new GroovyAction.Builder()
                .scriptResourcePath("classpath:org/citrusframework/groovy/script/example.groovy")
                .build();
        bean.execute(context);
    }

    @Test
    public void testScriptFailure() {
        GroovyAction bean = new GroovyAction.Builder()
                .script("Some wrong script")
                .build();
        Assertions.assertThrows(CitrusRuntimeException.class, () -> bean.execute(context));
    }

    @Test
    public void testScriptResourceNotFound() {
        GroovyAction bean = new GroovyAction.Builder()
                .scriptResourcePath("file:some/wrong/path/test.groovy")
                .build();
        Assertions.assertThrows(CitrusRuntimeException.class, () -> bean.execute(context));
    }

    @Test
    public void testCustomScriptExecutorImplementation() {
        String script = """
                import org.citrusframework.*
                import org.citrusframework.variable.*
                import org.citrusframework.context.TestContext
                import org.citrusframework.groovy.actions.GroovyAction.ScriptExecutor

                public class GScript implements ScriptExecutor {
                    public void execute(TestContext context) {
                        context.setVariable('text', 'Script with class definition test successful.')
                        println context.getVariable('text')
                    }
                }""";

        GroovyAction bean = new GroovyAction.Builder()
                .script(script)
                .build();
        bean.execute(context);
    }

    @Test
    public void testCustomClassImplementation() {
        String script = """
                public class CustomClass {
                    public void run() {
                        println 'Just executed custom class implementation'
                    }
                }""";

        GroovyAction bean = new GroovyAction.Builder()
                .script(script)
                .build();
        bean.execute(context);
    }

    @Test
    public void testNoScriptTemplate() {
        String script = "println 'Just executed pure groovy code'";
        GroovyAction bean = new GroovyAction.Builder()
                .useScriptTemplate(false)
                .script(script)
                .build();
        bean.execute(context);
    }

    @Test
    public void testAutomaticScriptExecutorWrapper() {
        GroovyAction bean = new GroovyAction.Builder()
                .script("context.setVariable('text', 'Automatic script wrapping works!')\n" +
        		       "println context.getVariable('text')")
                .build();
        bean.execute(context);
    }

    @Test
    public void testCustomScriptTemplate() {
        GroovyAction bean = new GroovyAction.Builder()
                .template("classpath:org/citrusframework/groovy/script/custom-script-template.groovy")
                .script("Assert.assertEquals(context.getVariable('scriptTemplateVar'), 'It works!')")
                .build();
        bean.execute(context);
    }

    @Test
    public void testInvalidScriptTemplate() {
        GroovyAction bean = new GroovyAction.Builder()
                .template("classpath:org/citrusframework/groovy/script/invalid-script-template.groovy")
                .script("println 'This should not work!'")
                .build();

        CitrusRuntimeException e = Assertions.assertThrows(CitrusRuntimeException.class, () -> bean.execute(context));
        Assertions.assertTrue(e.getMessage().startsWith("Invalid script template"));
    }
}
