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

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.json.JsonUtils;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.context.ValidationContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class MessageValidatorRegistryTest {

    @Mock
    private MessageValidator<?> plainTextMessageValidator;
    @Mock
    private MessageValidator<?> xmlMessageValidator;
    @Mock
    private MessageValidator<?> xhtmlMessageValidator;
    @Mock
    private MessageValidator<?> jsonTextMessageValidator;
    @Mock
    private MessageValidator<?> jsonPathMessageValidator;
    @Mock
    private MessageValidator<?> groovyScriptMessageValidator;
    @Mock
    private MessageValidator<?> groovyJsonMessageValidator;
    @Mock
    private MessageValidator<?> groovyXmlMessageValidator;
    @Mock
    private MessageValidator<?> xPathMessageValidator;
    @Mock
    private MessageValidator<?> xhtmlXpathMessageValidator;
    @Mock
    private MessageValidator<?> binaryMessageValidator;
    @Mock
    private MessageValidator<?> binaryBase64MessageValidator;
    @Mock
    private MessageValidator<?> gzipBinaryBase64MessageValidator;

    private MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);

        when(plainTextMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.PLAINTEXT.name()));
        when(xmlMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()) && XMLUtils.hasXmlPayload(invocation.getArgument(1)));
        when(xhtmlMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XHTML.name()) && XMLUtils.hasXmlPayload(invocation.getArgument(1)));
        when(jsonTextMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.JSON.name()) && JsonUtils.hasJsonPayload(invocation.getArgument(1)));
        when(jsonPathMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.JSON.name()) && JsonUtils.hasJsonPayload(invocation.getArgument(1)));
        when(groovyScriptMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.PLAINTEXT.name()));
        when(groovyJsonMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.JSON.name()) && JsonUtils.hasJsonPayload(invocation.getArgument(1)));
        when(groovyXmlMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()) && XMLUtils.hasXmlPayload(invocation.getArgument(1)));
        when(xPathMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()) && XMLUtils.hasXmlPayload(invocation.getArgument(1)));
        when(xhtmlXpathMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XHTML.name()) && XMLUtils.hasXmlPayload(invocation.getArgument(1)));
        when(binaryMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.BINARY.name()));
        when(binaryBase64MessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.BINARY_BASE64.name()));
        when(gzipBinaryBase64MessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.GZIP_BASE64.name()));

        messageValidatorRegistry.getMessageValidators().add(xmlMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(xPathMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(groovyXmlMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(jsonTextMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(jsonPathMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(plainTextMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(new DefaultMessageHeaderValidator());
        messageValidatorRegistry.getMessageValidators().add(binaryMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(binaryBase64MessageValidator);
        messageValidatorRegistry.getMessageValidators().add(gzipBinaryBase64MessageValidator);

        messageValidatorRegistry.getMessageValidators().add(groovyJsonMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(groovyScriptMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(xhtmlMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(xhtmlXpathMessageValidator);
    }

    @Test
    public void testFindMessageValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

        List<MessageValidator<? extends ValidationContext>> messageValidators = new ArrayList<>();
        messageValidators.add(plainTextMessageValidator);

        messageValidatorRegistry.setMessageValidators(messageValidators);
        messageValidatorRegistry.afterPropertiesSet();

        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);

        try {
            messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));
            Assert.fail("Missing exception due to no matching validator implementation");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Could not find proper message validator for message type"));
        }

        messageValidatorRegistry.getMessageValidators().add(xmlMessageValidator);
        messageValidatorRegistry.getMessageValidators().add(groovyScriptMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), groovyScriptMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0), xmlMessageValidator);
    }

    @Test
    public void testMessageValidatorRegistryXmlConfig() throws Exception {
        //non XML message type
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2), groovyScriptMessageValidator);

        //XML message type and empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0), xmlMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), xPathMessageValidator);
        Assert.assertEquals(matchingValidators.get(2), groovyXmlMessageValidator);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        //XML message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0), xmlMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), xPathMessageValidator);
        Assert.assertEquals(matchingValidators.get(2), groovyXmlMessageValidator);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);
    }

    @Test
    public void testMessageValidatorRegistryJsonConfig() throws Exception {
        //JSON message type and empty payload
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0), jsonTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), jsonPathMessageValidator);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3), groovyJsonMessageValidator);

        //JSON message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0), jsonTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), jsonPathMessageValidator);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3), groovyJsonMessageValidator);
    }

    @Test
    public void testMessageValidatorRegistryPlaintextConfig() throws Exception {
        //Plaintext message type and empty payload
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2), groovyScriptMessageValidator);

        //Plaintext message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("id=12345"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2), groovyScriptMessageValidator);
    }

    @Test
    public void testMessageValidatorRegistryFallback() throws Exception {
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0), jsonTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), jsonPathMessageValidator);
        Assert.assertEquals(matchingValidators.get(2).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(3), groovyJsonMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 4L);
        Assert.assertEquals(matchingValidators.get(0), xmlMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), xPathMessageValidator);
        Assert.assertEquals(matchingValidators.get(2), groovyXmlMessageValidator);
        Assert.assertEquals(matchingValidators.get(3).getClass(), DefaultMessageHeaderValidator.class);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("<id>12345</id>"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2), groovyScriptMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("{ \"id\": 12345 }"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2), groovyScriptMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("id=12345"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2), groovyScriptMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("id=12345"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 3L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(2), groovyScriptMessageValidator);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testEmptyListOfMessageValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        messageValidatorRegistry.afterPropertiesSet();
    }
}