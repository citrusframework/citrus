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

import org.springframework.util.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class CreateVariablesActionTest extends AbstractBaseTest {
	
	@Test
	public void testCreateSingleVariable() {
		CreateVariablesAction createVariablesAction = new CreateVariablesAction();
		Map<String, String> variables = Collections.singletonMap("myVariable", "value");
		createVariablesAction.setVariables(variables);
		
		createVariablesAction.execute(context);
		
		Assert.notNull(context.getVariable("${myVariable}"));
		Assert.isTrue(context.getVariable("${myVariable}").equals("value"));
	}
	
	@Test
    public void testCreateVariables() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("myVariable", "value1");
        variables.put("anotherVariable", "value2");
        
        createVariablesAction.setVariables(variables);
        
        createVariablesAction.execute(context);
        
        Assert.notNull(context.getVariable("${myVariable}"));
        Assert.isTrue(context.getVariable("${myVariable}").equals("value1"));
        Assert.notNull(context.getVariable("${anotherVariable}"));
        Assert.isTrue(context.getVariable("${anotherVariable}").equals("value2"));
    }
	
	@Test
    public void testOverwriteVariables() {
	    context.setVariable("myVariable", "initialValue");
	    
	    CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "newValue");
        createVariablesAction.setVariables(variables);
        
        createVariablesAction.execute(context);
        
        Assert.notNull(context.getVariable("${myVariable}"));
        Assert.isTrue(context.getVariable("${myVariable}").equals("newValue"));
    }
	
	@Test
    public void testCreateSingleVariableWithFunctionValue() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction();
        Map<String, String> variables = Collections.singletonMap("myVariable", "citrus:concat('Hello ', 'Citrus')");
        createVariablesAction.setVariables(variables);
        
        createVariablesAction.execute(context);
        
        Assert.notNull(context.getVariable("${myVariable}"));
        Assert.isTrue(context.getVariable("${myVariable}").equals("Hello Citrus"));
    }
}
