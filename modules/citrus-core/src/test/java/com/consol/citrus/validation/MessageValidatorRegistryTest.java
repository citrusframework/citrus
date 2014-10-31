/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.validation;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.script.GroovyScriptMessageValidator;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.text.PlainTextMessageValidator;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class MessageValidatorRegistryTest {

    @Test
    public void testFindMessageValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

        List<MessageValidator<? extends ValidationContext>> messageValidators = new ArrayList<MessageValidator<? extends ValidationContext>>();
        messageValidators.add(new PlainTextMessageValidator());

        messageValidatorRegistry.setMessageValidators(messageValidators);
        messageValidatorRegistry.afterPropertiesSet();

        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""), Collections.<ValidationContext>singletonList(new XmlMessageValidationContext()));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);

        try {
            messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""), Collections.<ValidationContext>singletonList(new ControlMessageValidationContext(MessageType.JSON.name())));
            Assert.fail("Missing exception due to no matching validator implementation");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Could not find proper message validator for message type"));
        }

        messageValidatorRegistry.getMessageValidators().add(new DomXmlMessageValidator());
        messageValidatorRegistry.getMessageValidators().add(new GroovyScriptMessageValidator());

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(new XmlMessageValidationContext());
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""), validationContexts);

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(matchingValidators.get(0).findValidationContext(validationContexts));
        Assert.assertNull(matchingValidators.get(1).findValidationContext(validationContexts));

        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""), validationContexts);

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(matchingValidators.get(0).findValidationContext(validationContexts));
        Assert.assertNotNull(matchingValidators.get(1).findValidationContext(validationContexts));

        validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(new XmlMessageValidationContext());
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""), validationContexts);

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testEmptyListOfMessageValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        messageValidatorRegistry.afterPropertiesSet();
    }
}
