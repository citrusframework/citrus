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

package com.consol.citrus.actions;

import java.util.*;

import javax.script.ScriptException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class CreateVariablesActionTest extends AbstractTestNGUnitTest {
	
	@Test
	public void testCreateSingleVariable() {
		CreateVariablesAction createVariablesAction = new CreateVariablesAction();
		Map<String, String> variables = Collections.singletonMap("myVariable", "value");
		createVariablesAction.setVariables(variables);
		
		createVariablesAction.execute(context);
		
		Assert.assertNotNull(context.getVariable("${myVariable}"));
		Assert.assertTrue(context.getVariable("${myVariable}").equals("value"));
	}
	
	@Test
    public void testCreateVariables() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("myVariable", "value1");
        variables.put("anotherVariable", "value2");
        
        createVariablesAction.setVariables(variables);
        
        createVariablesAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertTrue(context.getVariable("${myVariable}").equals("value1"));
        Assert.assertNotNull(context.getVariable("${anotherVariable}"));
        Assert.assertTrue(context.getVariable("${anotherVariable}").equals("value2"));
    }
	
	@Test
    public void testOverwriteVariables() {
	    context.setVariable("myVariable", "initialValue");
	    
	    CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "newValue");
        createVariablesAction.setVariables(variables);
        
        createVariablesAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertTrue(context.getVariable("${myVariable}").equals("newValue"));
    }
	
	@Test
    public void testCreateSingleVariableWithFunctionValue() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "citrus:concat('Hello ', 'Citrus')");
        createVariablesAction.setVariables(variables);
        
        createVariablesAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertTrue(context.getVariable("${myVariable}").equals("Hello Citrus"));
    }
	
	@Test
    public void testCreateVariableFromScript() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "script:<groovy>5+5");
        createVariablesAction.setVariables(variables);
        
        createVariablesAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertTrue(context.getVariable("${myVariable}").equals("10"));
    }
	
	@Test
    public void testCreateVariableFromScriptVariableSupport() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "script:<groovy>${number}+${number}");
        createVariablesAction.setVariables(variables);
        
        context.setVariable("number", "5");
        
        createVariablesAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertTrue(context.getVariable("${myVariable}").equals("10"));
    }
	
	@Test
    public void testCreateVariableFromScriptInvalidScriptEngine() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "script:<invalidScriptEngine>5+5");
        createVariablesAction.setVariables(variables);
        
        try {
            createVariablesAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("invalidScriptEngine"));
            return;
        }
        
        Assert.fail("Missing CitrusRuntimeException because of invalid script engine");
    }
	
    @Test
    public void testInvalidScript() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "script:<groovy>a");
        createVariablesAction.setVariables(variables);
        
        try {
            createVariablesAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof ScriptException);
            return;
        }
        
        Assert.fail("Missing CitrusRuntimeException because of invalid groovy script");
    }
}
