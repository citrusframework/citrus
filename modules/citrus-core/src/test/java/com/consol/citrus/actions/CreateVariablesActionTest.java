/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import java.util.*;

import org.springframework.util.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

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
