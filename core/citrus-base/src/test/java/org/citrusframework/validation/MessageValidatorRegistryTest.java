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

package org.citrusframework.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.MessageUtils;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.context.ValidationContext;
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
    @Mock
    private SchemaValidator<?> jsonSchemaValidator;
    @Mock
    private SchemaValidator<?> xmlSchemaValidator;

    private final MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);

        when(plainTextMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.PLAINTEXT.name()));
        when(xmlMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()) && MessageUtils.hasXmlPayload(invocation.getArgument(1)));
        when(xhtmlMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XHTML.name()) && MessageUtils.hasXmlPayload(invocation.getArgument(1)));
        when(jsonTextMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.JSON.name()) && MessageUtils.hasJsonPayload(invocation.getArgument(1)));
        when(jsonPathMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.JSON.name()) && MessageUtils.hasJsonPayload(invocation.getArgument(1)));
        when(groovyScriptMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.PLAINTEXT.name()));
        when(groovyJsonMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.JSON.name()) && MessageUtils.hasJsonPayload(invocation.getArgument(1)));
        when(groovyXmlMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()) && MessageUtils.hasXmlPayload(invocation.getArgument(1)));
        when(xPathMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()) && MessageUtils.hasXmlPayload(invocation.getArgument(1)));
        when(xhtmlXpathMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XHTML.name()) && MessageUtils.hasXmlPayload(invocation.getArgument(1)));
        when(binaryMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.BINARY.name()));
        when(binaryBase64MessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.BINARY_BASE64.name()));
        when(gzipBinaryBase64MessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.GZIP_BASE64.name()));

        // Schema validators
        when(jsonSchemaValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.JSON.name()));
        when(xmlSchemaValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()));

        messageValidatorRegistry.addMessageValidator("xmlMessageValidator", xmlMessageValidator);
        messageValidatorRegistry.addMessageValidator("xPathMessageValidator", xPathMessageValidator);
        messageValidatorRegistry.addMessageValidator("groovyXmlMessageValidator", groovyXmlMessageValidator);
        messageValidatorRegistry.addMessageValidator("jsonTextMessageValidator", jsonTextMessageValidator);
        messageValidatorRegistry.addMessageValidator("jsonPathMessageValidator", jsonPathMessageValidator);
        messageValidatorRegistry.addMessageValidator("plainTextMessageValidator", plainTextMessageValidator);
        messageValidatorRegistry.addMessageValidator("headerMessageValidator", new DefaultMessageHeaderValidator());
        messageValidatorRegistry.addMessageValidator("binaryMessageValidator", binaryMessageValidator);
        messageValidatorRegistry.addMessageValidator("binaryBase64MessageValidator", binaryBase64MessageValidator);
        messageValidatorRegistry.addMessageValidator("gzipBinaryBase64MessageValidator", gzipBinaryBase64MessageValidator);

        messageValidatorRegistry.addMessageValidator("groovyJsonMessageValidator", groovyJsonMessageValidator);
        messageValidatorRegistry.addMessageValidator("groovyScriptMessageValidator", groovyScriptMessageValidator);
        messageValidatorRegistry.addMessageValidator("xhtmlMessageValidator", xhtmlMessageValidator);
        messageValidatorRegistry.addMessageValidator("xhtmlXpathMessageValidator", xhtmlXpathMessageValidator);

        messageValidatorRegistry.addSchemaValidator("jsonSchemaValidator", jsonSchemaValidator);
        messageValidatorRegistry.addSchemaValidator("xmlSchemaValidator", xmlSchemaValidator);
    }

    @Test
    public void testFindMessageValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

        Map<String, MessageValidator<? extends ValidationContext>> messageValidators = new HashMap<>();
        messageValidators.put("plainTextMessageValidator", plainTextMessageValidator);

        messageValidatorRegistry.setMessageValidators(messageValidators);

        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("Hello"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0), plainTextMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));
        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DefaultEmptyMessageValidator.class);

        messageValidatorRegistry.addMessageValidator("xmlMessageValidator", xmlMessageValidator);
        messageValidatorRegistry.addMessageValidator("groovyScriptMessageValidator", groovyScriptMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0), groovyScriptMessageValidator);
        Assert.assertEquals(matchingValidators.get(1), plainTextMessageValidator);

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

    @Test
    public void shouldAddDefaultEmptyMessagePayloadValidator() {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DefaultEmptyMessageValidator.class);

        messageValidatorRegistry.addMessageValidator("jsonTextMessageValidator", jsonTextMessageValidator);
        messageValidatorRegistry.addMessageValidator("headerMessageValidator", new DefaultMessageHeaderValidator());

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultEmptyMessageValidator.class);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("{}"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0), jsonTextMessageValidator);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultMessageHeaderValidator.class);

        try {
            messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("Hello"), true);
            Assert.fail("Missing exception due to no proper message validator found");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to find proper message validator for message");
        }

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("Hello"));
        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(1).getClass(), DefaultTextEqualsMessageValidator.class);

        messageValidatorRegistry.addMessageValidator("plainTextMessageValidator", plainTextMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("Hello"));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 2L);
        Assert.assertEquals(matchingValidators.get(0).getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertEquals(matchingValidators.get(1), plainTextMessageValidator);
    }

    @Test
    public void testSchemaValidators() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

        Map<String, SchemaValidator<? extends SchemaValidationContext>> schemaValidators = new HashMap<>();
        schemaValidators.put("jsonSchema", jsonSchemaValidator);
        schemaValidators.put("xmlSchema", xmlSchemaValidator);

        messageValidatorRegistry.setSchemaValidators(schemaValidators);

        List<SchemaValidator<? extends SchemaValidationContext>> matchingValidators = messageValidatorRegistry.findSchemaValidators(MessageType.JSON.name(), new DefaultMessage(""));

        Assert.assertNotNull(matchingValidators);
        Assert.assertEquals(matchingValidators.size(), 1L);
        Assert.assertEquals(matchingValidators.get(0), jsonSchemaValidator);

        Optional<SchemaValidator<? extends SchemaValidationContext>> jsonSchema = messageValidatorRegistry.findSchemaValidator("jsonSchema");

        Assert.assertTrue(jsonSchema.isPresent());
        Assert.assertEquals(jsonSchema.get(), jsonSchemaValidator);
    }

}
