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

package org.citrusframework.actions;

import javax.script.ScriptException;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateVariablesActionTest extends UnitTestSupport {

	@Test
	public void testCreateSingleVariable() {
		CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder().variable("myVariable", "value").build();

		createVariablesAction.execute(context);

		Assertions.assertNotNull(context.getVariable("${myVariable}"));
        Assertions.assertEquals("value", context.getVariable("${myVariable}"));
	}

	@Test
    public void testCreateVariables() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "value1")
                .variable("anotherVariable", "value2")
                .build();

        createVariablesAction.execute(context);

        Assertions.assertNotNull(context.getVariable("${myVariable}"));
        Assertions.assertEquals("value1", context.getVariable("${myVariable}"));
        Assertions.assertNotNull(context.getVariable("${anotherVariable}"));
        Assertions.assertEquals("value2", context.getVariable("${anotherVariable}"));
    }

	@Test
    public void testOverwriteVariables() {
	    context.setVariable("myVariable", "initialValue");

	    CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "newValue")
                .build();

        createVariablesAction.execute(context);

        Assertions.assertNotNull(context.getVariable("${myVariable}"));
        Assertions.assertEquals("newValue", context.getVariable("${myVariable}"));
    }

	@Test
    public void testCreateSingleVariableWithFunctionValue() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "citrus:concat('Hello ', 'Citrus')")
                .build();

        createVariablesAction.execute(context);

        Assertions.assertNotNull(context.getVariable("${myVariable}"));
        Assertions.assertEquals("Hello Citrus", context.getVariable("${myVariable}"));
    }

	@Test
    public void testCreateVariableFromScript() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<groovy>5+5")
                .build();

        createVariablesAction.execute(context);

        Assertions.assertNotNull(context.getVariable("${myVariable}"));
        Assertions.assertEquals("10", context.getVariable("${myVariable}"));
    }

	@Test
    public void testCreateVariableFromScriptVariableSupport() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<groovy>${number}+${number}")
                .build();

        context.setVariable("number", "5");

        createVariablesAction.execute(context);

        Assertions.assertNotNull(context.getVariable("${myVariable}"));
        Assertions.assertEquals("10", context.getVariable("${myVariable}"));
    }

	@Test
    public void testCreateVariableFromScriptInvalidScriptEngine() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<invalidScriptEngine>5+5")
                .build();

        try {
            createVariablesAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains("invalidScriptEngine"));
            return;
        }

        Assertions.fail("Missing CitrusRuntimeException because of invalid script engine");
    }

    @Test
    public void testInvalidScript() {
        CreateVariablesAction createVariablesAction = new CreateVariablesAction.Builder()
                .variable("myVariable", "script:<groovy>a")
                .build();

        try {
            createVariablesAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getCause() instanceof ScriptException);
            return;
        }

        Assertions.fail("Missing CitrusRuntimeException because of invalid groovy script");
    }
}
