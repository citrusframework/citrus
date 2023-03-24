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

package org.citrusframework.script;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class GroovyActionTest extends AbstractTestNGUnitTest {

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
                .scriptResourcePath("classpath:org/citrusframework/script/example.groovy")
                .build();
        bean.execute(context);
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testScriptFailure() {
        GroovyAction bean = new GroovyAction.Builder()
                .script("Some wrong script")
                .build();
        bean.execute(context);
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testScriptResourceNotFound() {
        GroovyAction bean = new GroovyAction.Builder()
                .scriptResourcePath("file:some/wrong/path/test.groovy")
                .build();
        bean.execute(context);
    }

    @Test
    public void testCustomScriptExecutorImplementation() {
        String script = "import org.citrusframework.*\n" +
        		"import org.citrusframework.variable.*\n" +
        		"import org.citrusframework.context.TestContext\n" +
        		"import org.citrusframework.script.GroovyAction.ScriptExecutor\n\n" +
        		"public class GScript implements ScriptExecutor {\n" +
        		"public void execute(TestContext context) {\n" +
        		    "context.setVariable('text', 'Script with class definition test successful.')\n" +
        		    "println context.getVariable('text')\n" +
        		"}}";

        GroovyAction bean = new GroovyAction.Builder()
                .script(script)
                .build();
        bean.execute(context);
    }

    @Test
    public void testCustomClassImplementation() {
        String script = "public class CustomClass {\n" +
                "public void run() {\n" +
                    "println 'Just executed custom class implementation'\n" +
                "}}";

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
                .template("classpath:org/citrusframework/script/custom-script-template.groovy")
                .script("Assert.assertEquals(context.getVariable('scriptTemplateVar'), 'It works!')")
                .build();
        bean.execute(context);
    }

    @Test
    public void testInvalidScriptTemplate() {
        GroovyAction bean = new GroovyAction.Builder()
                .template("classpath:org/citrusframework/script/invalid-script-template.groovy")
                .script("println 'This should not work!'")
                .build();

        try {
            bean.execute(context);
            Assert.fail("Missing exception because of invalid script template");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid script template"));
        }
    }
}
