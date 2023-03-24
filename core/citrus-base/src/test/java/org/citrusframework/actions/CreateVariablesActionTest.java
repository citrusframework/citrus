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

package org.citrusframework.actions;

import javax.script.ScriptException;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CreateVariablesActionTest extends UnitTestSupport {

	@Test
	public void testCreateSingleVariable() {
		CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder().variable("myVariable", "value").build();

		createVariablesAction.execute(context);

		Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertEquals(context.getVariable("${myVariable}"), "value");
	}

	@Test
    public void testCreateVariables() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "value1")
                .variable("anotherVariable", "value2")
                .build();

        createVariablesAction.execute(context);

        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertEquals(context.getVariable("${myVariable}"), "value1");
        Assert.assertNotNull(context.getVariable("${anotherVariable}"));
        Assert.assertEquals(context.getVariable("${anotherVariable}"), "value2");
    }

	@Test
    public void testOverwriteVariables() {
	    context.setVariable("myVariable", "initialValue");

	    CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "newValue")
                .build();

        createVariablesAction.execute(context);

        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertEquals(context.getVariable("${myVariable}"), "newValue");
    }

	@Test
    public void testCreateSingleVariableWithFunctionValue() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "citrus:concat('Hello ', 'Citrus')")
                .build();

        createVariablesAction.execute(context);

        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertEquals(context.getVariable("${myVariable}"), "Hello Citrus");
    }

	@Test
    public void testCreateVariableFromScript() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<groovy>5+5")
                .build();

        createVariablesAction.execute(context);

        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertEquals(context.getVariable("${myVariable}"), "10");
    }

	@Test
    public void testCreateVariableFromScriptVariableSupport() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<groovy>${number}+${number}")
                .build();

        context.setVariable("number", "5");

        createVariablesAction.execute(context);

        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertEquals(context.getVariable("${myVariable}"), "10");
    }

	@Test
    public void testCreateVariableFromScriptInvalidScriptEngine() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<invalidScriptEngine>5+5")
                .build();

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
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<groovy>a")
                .build();

        try {
            createVariablesAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof ScriptException);
            return;
        }

        Assert.fail("Missing CitrusRuntimeException because of invalid groovy script");
    }
}
