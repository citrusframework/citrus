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

package com.consol.citrus.validation.text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import com.consol.citrus.validation.script.ScriptValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PlainTextMessageValidatorTest extends AbstractTestNGUnitTest {

    private PlainTextMessageValidator validator = new PlainTextMessageValidator();
    private final ValidationContext validationContext = new DefaultValidationContext();

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());

        validator = new PlainTextMessageValidator();
        return factory;
    }

    @Test
    public void testPlainTextValidation() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello World!");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationWithIgnore() {
        Message receivedMessage = new DefaultMessage(String.format("Hello World, time is %s!", System.currentTimeMillis()));
        Message controlMessage = new DefaultMessage("Hello World, time is @ignore@!");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        controlMessage = new DefaultMessage("Hello @ignore@, time is @ignore@!");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        controlMessage = new DefaultMessage("Hello @ignore@, time is @ignore@!");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        controlMessage = new DefaultMessage("Hello @ignore@, time is @ignore(100)@");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        controlMessage = new DefaultMessage("@ignore(11)@, time is @ignore@!");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        controlMessage = new DefaultMessage("@ignore@");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        receivedMessage = new DefaultMessage(UUID.randomUUID().toString());
        controlMessage = new DefaultMessage("@ignore@-@ignore@-@ignore@-@ignore@-@ignore@");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        receivedMessage = new DefaultMessage("1a2b3c4d_5e6f7g8h");
        controlMessage = new DefaultMessage("1a@ignore(4)@4d_@ignore(6)@8h");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        receivedMessage = new DefaultMessage("Your id is 1a2b3c4d_5e6f7g8h");
        controlMessage = new DefaultMessage("Your id is @ignore@");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationCreateVariable() {
        Long time = System.currentTimeMillis();
        Message receivedMessage = new DefaultMessage(String.format("Hello World, time is %s!", time));
        Message controlMessage = new DefaultMessage("Hello World, time is @variable(time)@!");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        Assert.assertEquals(context.getVariable("time"), time.toString());

        controlMessage = new DefaultMessage("Hello @variable('world')@, time is @variable(time)@!");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        Assert.assertEquals(context.getVariable("world"), "World");
        Assert.assertEquals(context.getVariable("time"), time.toString());

        String id = UUID.randomUUID().toString();
        receivedMessage = new DefaultMessage(id);
        controlMessage = new DefaultMessage("@variable('id')@");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        Assert.assertEquals(context.getVariable("id"), id);

        receivedMessage = new DefaultMessage("Today is 24.12.2017");
        controlMessage = new DefaultMessage("Today is @variable('date')@");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        Assert.assertEquals(context.getVariable("date"), "24.12.2017");

        receivedMessage = new DefaultMessage("Today is 2017-12-24");
        controlMessage = new DefaultMessage("Today is @variable('date')@");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        Assert.assertEquals(context.getVariable("date"), "2017-12-24");
    }

    @Test
    public void testPlainTextValidationWithIgnoreFail() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello @ignore@");

        try {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertFalse(e.getMessage().contains("only whitespaces!"));

            Assert.assertTrue(e.getMessage().contains("expected 'Hello World'"));
            Assert.assertTrue(e.getMessage().contains("but was 'Hello World!'"));

            return;
        }

        Assert.fail("Missing validation exception due to wrong number of JSON entries");
    }

    @Test
    public void testPlainTextValidationContains() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("@contains('World!')@");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testPlainTextValidationContainsError() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("@contains('Space!')@");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationVariableSupport() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello ${world}!");

        context.setVariable("world", "World");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationWrongValue() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello Citrus!");

        try {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertFalse(e.getMessage().contains("only whitespaces!"));

            Assert.assertTrue(e.getMessage().contains("expected 'Hello Citrus!'"));
            Assert.assertTrue(e.getMessage().contains("but was 'Hello World!'"));

            return;
        }

        Assert.fail("Missing validation exception due to wrong number of JSON entries");
    }

    @Test
    public void testPlainTextValidationLeadingTrailingWhitespace() {
        Message receivedMessage = new DefaultMessage("   Hello World!   ");
        Message controlMessage = new DefaultMessage("Hello World!");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationMultiline() {
        Message receivedMessage = new DefaultMessage("Hello\nWorld!\n");
        Message controlMessage = new DefaultMessage("Hello\nWorld!\n");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationNormalizeWhitespaces() {
        Message receivedMessage = new DefaultMessage(" Hello\r\n\n  \t World!\t\t\n\n    ");
        Message controlMessage = new DefaultMessage("Hello\n World!\n");

        try {
            validator.setIgnoreNewLineType(true);
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing exception due to non matching new line whitespaces");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("only whitespaces!"));
            validator.setIgnoreWhitespace(true);
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        }
    }

    @Test
    public void testPlainTextValidationNormalizeNewLineTypeCRLF() {
        Message receivedMessage = new DefaultMessage("Hello\nWorld!\n");
        Message controlMessage = new DefaultMessage("Hello\r\nWorld!\r\n");

        try {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing exception due to non matching new line whitespaces");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("only whitespaces!"));
            validator.setIgnoreNewLineType(true);
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        }
    }

    @Test
    public void testPlainTextValidationNormalizeNewLineTypeCR() {
        Message receivedMessage = new DefaultMessage("Hello\nWorld!\n");
        Message controlMessage = new DefaultMessage("Hello\rWorld!\r");

        try {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing exception due to non matching new line whitespaces");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("only whitespaces!"));
            validator.setIgnoreNewLineType(true);
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        }
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();

        Assert.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new DefaultValidationContext());

        Assert.assertNotNull(validator.findValidationContext(validationContexts));

        validationContexts.clear();
        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));

        Assert.assertNotNull(validator.findValidationContext(validationContexts));
    }
}
