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

package org.citrusframework.validation.script;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GroovyXmlMessageValidatorTest extends AbstractTestNGUnitTest {

    private final GroovyXmlMessageValidator validator = new GroovyXmlMessageValidator();

    private Message message;

    @BeforeMethod
    public void prepareTestData() {
        message = new DefaultMessage("<RequestMessage Id=\"123456789\" xmlns=\"http://citrus/test\">"
                + "<CorrelationId>Kx1R123456789</CorrelationId>"
                + "<BookingId>Bx1G987654321</BookingId>"
                + "<Text>Hello TestFramework</Text>"
            + "</RequestMessage>");
    }

    @Test
    public void testGroovyScriptValidation() throws ValidationException {
        String validationScript = "assert root.children().size() == 3 \n" +
                        "assert root.CorrelationId.text() == 'Kx1R123456789' \n" +
                        "assert root.BookingId.text() == 'Bx1G987654321' \n" +
                        "assert root.Text.text() == 'Hello TestFramework'";

        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationVariableSupport() {
        context.setVariable("user", "TestFramework");
        context.setVariable("correlationId", "Kx1R123456789");

        String validationScript = "assert root.children().size() == 3 \n" +
                        "assert root.CorrelationId.text() == '${correlationId}' \n" +
                        "assert root.BookingId.text() == 'Bx1G987654321' \n" +
                        "assert root.Text.text() == 'Hello ' + context.getVariable(\"user\")";

        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationFailed() {
        String validationScript = "assert root.children().size() == 3 \n" +
                        "assert root.CorrelationId.text() == 'Kx1R123456789' \n" +
                        "assert root.BookingId.text() == 'Bx1G987654321' \n" +
                        "assert root.Text == 'Hello Citrus'"; //should fail

        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Groovy script validation failed"));
            Assert.assertTrue(e.getMessage().contains("Hello Citrus"));
            Assert.assertTrue(e.getMessage().contains("Hello TestFramework"));
            return;
        }

        Assert.fail("Missing script validation exception caused by wrong control value");
    }

    @Test
    public void testEmptyValidationScript() {
        String validationScript = "";
        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new XpathMessageValidationContext());
        validationContexts.add(new ScriptValidationContext("scala"));

        Assert.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new ScriptValidationContext(ScriptTypes.GROOVY));

        Assert.assertNotNull(validator.findValidationContext(validationContexts));
    }
}
