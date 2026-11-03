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

package org.citrusframework.validation.groovy;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.citrusframework.validation.context.script.ScriptValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.citrusframework.validation.groovy.GroovyScriptMessageValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroovyScriptMessageValidatorTest extends AbstractUnitTest {

    GroovyScriptMessageValidator validator = new GroovyScriptMessageValidator();

    private Message message;

    @BeforeEach
    public void prepareTestData() {
        message = new DefaultMessage("This is plain text!").setHeader("operation", "unitTesting");
    }

    @Test
    public void testGroovyScriptValidation() throws ValidationException {
        String validationScript = """
                assert headers.operation == 'unitTesting'
                assert payload == 'This is plain text!'
                assert payload.contains('!')""";

        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationVariableSupport() throws ValidationException {
        String validationScript = """
                assert headers.operation == 'unitTesting'
                assert payload == '${plainText}'
                assert payload.contains('!')""";

        context.setVariable("plainText", "This is plain text!");

        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationWrongValue() {
        String validationScript = """
                assert headers.operation == 'somethingElse'
                assert payload == 'This is plain text!'
                assert payload.contains('!')""";

        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> validator.validateMessage(message, new DefaultMessage(), context, validationContext));
        Assertions.assertTrue(e.getCause() instanceof AssertionError);
    }

    @Test
    public void testTestContextSupport() throws ValidationException {
        String validationScript = "context.setVariable('operation', 'unitTesting')\n" +
                "context.setVariable('text', 'This is plain text!')";

        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        Assertions.assertNull(context.getVariables().get("operation"));
        Assertions.assertNull(context.getVariables().get("text"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);

        Assertions.assertNotNull(context.getVariables().get("operation"));
        Assertions.assertNotNull(context.getVariables().get("text"));
        Assertions.assertEquals("unitTesting", context.getVariable("operation"));
        Assertions.assertEquals("This is plain text!", context.getVariable("text"));
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());
        validationContexts.add(new DefaultScriptValidationContext("scala"));

        Assertions.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new DefaultScriptValidationContext(ScriptTypes.GROOVY));

        Assertions.assertNotNull(validator.findValidationContext(validationContexts));
    }
}
