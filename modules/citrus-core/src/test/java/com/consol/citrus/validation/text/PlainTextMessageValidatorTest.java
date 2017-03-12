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

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PlainTextMessageValidatorTest extends AbstractTestNGUnitTest {

    private PlainTextMessageValidator validator = new PlainTextMessageValidator();

    @Test
    public void testPlainTextValidation() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello World!");

        ValidationContext validationContext = new DefaultValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationContains() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("@contains('World!')@");

        ValidationContext validationContext = new DefaultValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testPlainTextValidationContainsError() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("@contains('Space!')@");

        ValidationContext validationContext = new DefaultValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testPlainTextValidationVariableSupport() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello ${world}!");
        
        context.setVariable("world", "World");

        ValidationContext validationContext = new DefaultValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testPlainTextValidationWrongValue() {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello Citrus!");

        ValidationContext validationContext = new DefaultValidationContext();
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

        ValidationContext validationContext = new DefaultValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationMultiline() {
        Message receivedMessage = new DefaultMessage("Hello\nWorld!\n");
        Message controlMessage = new DefaultMessage("Hello\nWorld!\n");

        ValidationContext validationContext = new DefaultValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testPlainTextValidationNormalizeWhitespaceCRLF() {
        Message receivedMessage = new DefaultMessage("Hello\nWorld!\n");
        Message controlMessage = new DefaultMessage("Hello\r\nWorld!\r\n");

        ValidationContext validationContext = new DefaultValidationContext();
        try {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing exception due to non matching new line whitespaces");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("only whitespaces!"));
            validator.setIgnoreWhitespace(true);
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            validator.setIgnoreWhitespace(false);
        }
    }

    @Test
    public void testPlainTextValidationNormalizeWhitespaceCR() {
        Message receivedMessage = new DefaultMessage("Hello\nWorld!\n");
        Message controlMessage = new DefaultMessage("Hello\rWorld!\r");

        ValidationContext validationContext = new DefaultValidationContext();
        try {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing exception due to non matching new line whitespaces");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("only whitespaces!"));
            validator.setIgnoreWhitespace(true);
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            validator.setIgnoreWhitespace(false);
        }
    }
}
