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

package com.consol.citrus.script;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class GroovyActionTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testScript() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("println 'Hello TestFramework!'");
        bean.execute(context);
    }
    
    @Test
    public void testScriptResource() {
        GroovyAction bean = new GroovyAction();
        bean.setScriptResourcePath("classpath:com/consol/citrus/script/example.groovy");
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testScriptFailure() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("Some wrong script");
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testScriptResourceNotFound() {
        GroovyAction bean = new GroovyAction();
        bean.setScriptResourcePath("file:some/wrong/path/test.groovy");
        bean.execute(context);
    }
    
    @Test
    public void testCustomScriptExecutorImplementation() {
        GroovyAction bean = new GroovyAction();
        
        String script = "import com.consol.citrus.*\n" +
        		"import com.consol.citrus.variable.*\n" +
        		"import com.consol.citrus.context.TestContext\n" +
        		"import com.consol.citrus.script.GroovyAction.ScriptExecutor\n\n" +
        		"public class GScript implements ScriptExecutor {\n" +
        		"public void execute(TestContext context) {\n" +
        		    "context.setVariable('text', 'Script with class definition test successful.')\n" +
        		    "println context.getVariable('text')\n" +
        		"}}";
        
        bean.setScript(script);
        bean.execute(context);
    }
    
    @Test
    public void testCustomClassImplementation() {
        GroovyAction bean = new GroovyAction();
        
        String script = "public class CustomClass {\n" +
                "public void run() {\n" +
                    "println 'Just executed custom class implementation'\n" +
                "}}";
        
        bean.setScript(script);
        bean.execute(context);
    }
    
    @Test
    public void testNoScriptTemplate() {
        GroovyAction bean = new GroovyAction();
        
        bean.setUseScriptTemplate(false);
        
        String script = "println 'Just executed pure groovy code'";
        
        bean.setScript(script);
        bean.execute(context);
    }
    
    @Test
    public void testAutomaticScriptExecutorWrapper() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("context.setVariable('text', 'Automatic script wrapping works!')\n" +
        		       "println context.getVariable('text')");
        bean.execute(context);
    }
    
    @Test
    public void testCustomScriptTemplate() {
        GroovyAction bean = new GroovyAction();
        
        bean.setScriptTemplatePath("classpath:com/consol/citrus/script/custom-script-template.groovy");
        
        bean.setScript("Assert.assertEquals(context.getVariable('scriptTemplateVar'), 'It works!')");
        bean.execute(context);
    }
    
    @Test
    public void testInvalidScriptTemplate() {
        GroovyAction bean = new GroovyAction();
        
        bean.setScriptTemplatePath("classpath:com/consol/citrus/script/invalid-script-template.groovy");
        bean.setScript("println 'This should not work!'");
        
        try {
            bean.execute(context);
            Assert.fail("Missing exception because of invalid script template");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid script template"));
        }
    }
}
