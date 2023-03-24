/*
 * Copyright 2006-2011 the original author or authors.
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

package org.citrusframework.validation.script;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GroovyScriptMessageValidatorTest extends AbstractTestNGUnitTest {

    GroovyScriptMessageValidator validator = new GroovyScriptMessageValidator();

    private Message message;

    @BeforeMethod
    public void prepareTestData() {
        message = new DefaultMessage("This is plain text!").setHeader("operation", "unitTesting");
    }

    @Test
    public void testGroovyScriptValidation() throws ValidationException {
        String validationScript = "assert headers.operation == 'unitTesting'\n" +
        		"assert payload == 'This is plain text!'\n" +
        		"assert payload.contains('!')";

        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationVariableSupport() throws ValidationException {
        String validationScript = "assert headers.operation == 'unitTesting'\n" +
                "assert payload == '${plainText}'\n" +
                "assert payload.contains('!')";

        context.setVariable("plainText", "This is plain text!");

        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationWrongValue() throws ValidationException {
        String validationScript = "assert headers.operation == 'somethingElse'\n" +
                "assert payload == 'This is plain text!'\n" +
                "assert payload.contains('!')";

        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getCause() instanceof AssertionError);
            return;
        }

        Assert.fail("Missing validation exception due to wrong value");
    }

    @Test
    public void testTestContextSupport() throws ValidationException {
        String validationScript = "context.setVariable('operation', 'unitTesting')\n" +
                "context.setVariable('text', 'This is plain text!')";

        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        Assert.assertNull(context.getVariables().get("operation"));
        Assert.assertNull(context.getVariables().get("text"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);

        Assert.assertNotNull(context.getVariables().get("operation"));
        Assert.assertNotNull(context.getVariables().get("text"));
        Assert.assertEquals(context.getVariable("operation"), "unitTesting");
        Assert.assertEquals(context.getVariable("text"), "This is plain text!");
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());
        validationContexts.add(new ScriptValidationContext("scala"));

        Assert.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new ScriptValidationContext(ScriptTypes.GROOVY));

        Assert.assertNotNull(validator.findValidationContext(validationContexts));
    }
}
