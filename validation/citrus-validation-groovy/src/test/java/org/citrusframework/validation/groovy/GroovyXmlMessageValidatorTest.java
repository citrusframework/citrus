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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.citrusframework.validation.context.script.ScriptValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.citrusframework.validation.groovy.GroovyXmlMessageValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroovyXmlMessageValidatorTest extends AbstractUnitTest {

    private final GroovyXmlMessageValidator validator = new GroovyXmlMessageValidator();

    private Message message;

    @BeforeEach
    public void prepareTestData() {
        message = new DefaultMessage("<RequestMessage Id=\"123456789\" xmlns=\"http://citrus/test\">"
                + "<CorrelationId>Kx1R123456789</CorrelationId>"
                + "<BookingId>Bx1G987654321</BookingId>"
                + "<Text>Hello TestFramework</Text>"
            + "</RequestMessage>");
    }

    @Test
    public void testGroovyScriptValidation() throws ValidationException {
        String validationScript = """
                assert root.children().size() == 3\s
                assert root.CorrelationId.text() == 'Kx1R123456789'\s
                assert root.BookingId.text() == 'Bx1G987654321'\s
                assert root.Text.text() == 'Hello TestFramework'""";

        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationVariableSupport() {
        context.setVariable("user", "TestFramework");
        context.setVariable("correlationId", "Kx1R123456789");

        String validationScript = """
                assert root.children().size() == 3\s
                assert root.CorrelationId.text() == '${correlationId}'\s
                assert root.BookingId.text() == 'Bx1G987654321'\s
                assert root.Text.text() == 'Hello ' + context.getVariable("user")""";

        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testGroovyScriptValidationFailed() {
        String validationScript = """
                assert root.children().size() == 3\s
                assert root.CorrelationId.text() == 'Kx1R123456789'\s
                assert root.BookingId.text() == 'Bx1G987654321'\s
                assert root.Text == 'Hello Citrus'"""; //should fail

        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script(validationScript)
                .build();

        CitrusRuntimeException e = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> validator.validateMessage(message, new DefaultMessage(), context, validationContext));
        Assertions.assertTrue(e.getMessage().startsWith("Groovy script validation failed"));
        Assertions.assertTrue(e.getMessage().contains("Hello Citrus"));
        Assertions.assertTrue(e.getMessage().contains("Hello TestFramework"));
    }

    @Test
    public void testEmptyValidationScript() {
        String validationScript = "";
        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
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
        validationContexts.add(new DefaultScriptValidationContext("scala"));

        Assertions.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new DefaultScriptValidationContext(ScriptTypes.GROOVY));

        Assertions.assertNotNull(validator.findValidationContext(validationContexts));
    }

    @Test
    public void shouldProcessXmlWithDTD() {
        message = new DefaultMessage("""
  <!DOCTYPE test [ <!ELEMENT test (#PCDATA)> ]>
  <test>citrus</test>""");
        String validationScript = "assert root == 'citrus'";
        ScriptValidationContext validationContext = new DefaultScriptValidationContext.Builder()
              .scriptType(ScriptTypes.GROOVY)
              .script(validationScript)
              .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }
}
