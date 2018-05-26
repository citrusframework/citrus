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
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.context.*;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.script.*;
import com.consol.citrus.validation.text.PlainTextMessageValidator;
import com.consol.citrus.validation.xml.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class MessageValidatorRegistryTest {

    @Test
    public void testFindMessageValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

        List<MessageValidator<? extends ValidationContext>> messageValidators = new ArrayList<>();
        messageValidators.add(new PlainTextMessageValidator());

        messageValidatorRegistry.setMessageValidators(messageValidators);
        messageValidatorRegistry.afterPropertiesSet();

        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);

        try {
            messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));
            Assert.fail("Missing exception due to no matching validator implementation");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Could not find proper message validator for message type"));
        }

        messageValidatorRegistry.getMessageValidators().add(new DomXmlMessageValidator());
        messageValidatorRegistry.getMessageValidators().add(new GroovyScriptMessageValidator());

        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));

        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));

        validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
    }

    @Test
    public void testMessageValidatorRegistryXmlConfig() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorConfig().getMessageValidatorRegistry();
        messageValidatorRegistry.afterPropertiesSet();

        //non XML message type
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());

        //XML message type and empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        //XML message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        //script XML validation context
        validationContexts.add(new ScriptValidationContext(MessageType.XML.name()));
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        //xpath message validation context
        validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());
        validationContexts.add(new XpathMessageValidationContext());
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));
    }

    @Test
    public void testMessageValidatorRegistryJsonConfig() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorConfig().getMessageValidatorRegistry();
        messageValidatorRegistry.afterPropertiesSet();

        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());

        //JSON message type and empty payload
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), GroovyJsonMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        //JSON message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), GroovyJsonMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        //script JSON validation context
        validationContexts.add(new ScriptValidationContext(MessageType.JSON.name()));
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), GroovyJsonMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), GroovyJsonMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        //json path message validation context
        validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());
        validationContexts.add(new JsonPathMessageValidationContext());
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), GroovyJsonMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), GroovyJsonMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));
    }

    @Test
    public void testMessageValidatorRegistryPlaintextConfig() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorConfig().getMessageValidatorRegistry();
        messageValidatorRegistry.afterPropertiesSet();

        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());

        //Plaintext message type and empty payload
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));

        //Plaintext message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("id=12345"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));

        //script plaintext validation context
        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("id=12345"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
    }

    @Test
    public void testMessageValidatorRegistryFallback() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorConfig().getMessageValidatorRegistry();
        messageValidatorRegistry.afterPropertiesSet();

        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new JsonMessageValidationContext());

        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), GroovyJsonMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(3)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);


        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("id=12345"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("id=12345"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2).getClass(), GroovyScriptMessageValidator.class);

        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(0)).findValidationContext(validationContexts));
        Assert.assertNotNull(((AbstractMessageValidator)matchingValidators.get(1)).findValidationContext(validationContexts));
        Assert.assertNull(((AbstractMessageValidator)matchingValidators.get(2)).findValidationContext(validationContexts));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testEmptyListOfMessageValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        messageValidatorRegistry.afterPropertiesSet();
    }
}
