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
import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.ControlMessageValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PlainTextMessageValidatorTest extends AbstractTestNGUnitTest {

    @Test
    public void testPlainTextValidation() {
        PlainTextMessageValidator validator = new PlainTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello World!");

        ControlMessageValidationContext validationContext = new ControlMessageValidationContext(MessageType.PLAINTEXT.toString());
        validationContext.setControlMessage(controlMessage);
        
        validator.validateMessagePayload(receivedMessage, controlMessage, validationContext, context);
    }
    
    @Test
    public void testPlainTextValidationVariableSupport() {
        PlainTextMessageValidator validator = new PlainTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello ${world}!");
        
        context.setVariable("world", "World");

        ControlMessageValidationContext validationContext = new ControlMessageValidationContext(MessageType.PLAINTEXT.toString());
        validationContext.setControlMessage(controlMessage);
        
        validator.validateMessagePayload(receivedMessage, controlMessage, validationContext, context);
    }
    
    @Test
    public void testPlainTextValidationWrongValue() {
        PlainTextMessageValidator validator = new PlainTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello Citrus!");

        ControlMessageValidationContext validationContext = new ControlMessageValidationContext(MessageType.PLAINTEXT.toString());
        validationContext.setControlMessage(controlMessage);
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, validationContext, context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'Hello Citrus!'"));
            Assert.assertTrue(e.getMessage().contains("but was 'Hello World!'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong number of JSON entries");
    }
}
