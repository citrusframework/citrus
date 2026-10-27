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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @since 2.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    public void setupMocks() {
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
    public void testFindMessageValidators() {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

        Map<String, MessageValidator<? extends ValidationContext>> messageValidators = new HashMap<>();
        messageValidators.put("plainTextMessageValidator", plainTextMessageValidator);

        messageValidatorRegistry.setMessageValidators(messageValidators);

        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(1L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("Hello"));

        assertNotNull(matchingValidators);
        assertEquals(1L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));
        assertNotNull(matchingValidators);
        assertEquals(1L, matchingValidators.size());
        assertEquals(DefaultEmptyMessageValidator.class, matchingValidators.get(0).getClass());

        messageValidatorRegistry.addMessageValidator("xmlMessageValidator", xmlMessageValidator);
        messageValidatorRegistry.addMessageValidator("groovyScriptMessageValidator", groovyScriptMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(2L, matchingValidators.size());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(0));
        assertEquals(plainTextMessageValidator, matchingValidators.get(1));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(1L, matchingValidators.size());
        assertEquals(xmlMessageValidator, matchingValidators.get(0));
    }

    @Test
    public void testMessageValidatorRegistryXmlConfig() {
        //non XML message type
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(3L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(2));

        //XML message type and empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(4L, matchingValidators.size());
        assertEquals(xmlMessageValidator, matchingValidators.get(0));
        assertEquals(xPathMessageValidator, matchingValidators.get(1));
        assertEquals(groovyXmlMessageValidator, matchingValidators.get(2));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(3).getClass());

        //XML message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("<id>12345</id>"));

        assertNotNull(matchingValidators);
        assertEquals(4L, matchingValidators.size());
        assertEquals(xmlMessageValidator, matchingValidators.get(0));
        assertEquals(xPathMessageValidator, matchingValidators.get(1));
        assertEquals(groovyXmlMessageValidator, matchingValidators.get(2));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(3).getClass());
    }

    @Test
    public void testMessageValidatorRegistryJsonConfig() {
        //JSON message type and empty payload
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(4L, matchingValidators.size());
        assertEquals(jsonTextMessageValidator, matchingValidators.get(0));
        assertEquals(jsonPathMessageValidator, matchingValidators.get(1));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(2).getClass());
        assertEquals(groovyJsonMessageValidator, matchingValidators.get(3));

        //JSON message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("{ \"id\": 12345 }"));

        assertNotNull(matchingValidators);
        assertEquals(4L, matchingValidators.size());
        assertEquals(jsonTextMessageValidator, matchingValidators.get(0));
        assertEquals(jsonPathMessageValidator, matchingValidators.get(1));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(2).getClass());
        assertEquals(groovyJsonMessageValidator, matchingValidators.get(3));
    }

    @Test
    public void testMessageValidatorRegistryPlaintextConfig() {
        //Plaintext message type and empty payload
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(3L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(2));

        //Plaintext message type and non empty payload
        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("id=12345"));

        assertNotNull(matchingValidators);
        assertEquals(3L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(2));
    }

    @Test
    public void testMessageValidatorRegistryFallback() {
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("{ \"id\": 12345 }"));

        assertNotNull(matchingValidators);
        assertEquals(4L, matchingValidators.size());
        assertEquals(jsonTextMessageValidator, matchingValidators.get(0));
        assertEquals(jsonPathMessageValidator, matchingValidators.get(1));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(2).getClass());
        assertEquals(groovyJsonMessageValidator, matchingValidators.get(3));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("<id>12345</id>"));

        assertNotNull(matchingValidators);
        assertEquals(4L, matchingValidators.size());
        assertEquals(xmlMessageValidator, matchingValidators.get(0));
        assertEquals(xPathMessageValidator, matchingValidators.get(1));
        assertEquals(groovyXmlMessageValidator, matchingValidators.get(2));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(3).getClass());

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("<id>12345</id>"));

        assertNotNull(matchingValidators);
        assertEquals(3L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(2));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage("{ \"id\": 12345 }"));

        assertNotNull(matchingValidators);
        assertEquals(3L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(2));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.XML.name(), new DefaultMessage("id=12345"));

        assertNotNull(matchingValidators);
        assertEquals(3L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(2));

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("id=12345"));

        assertNotNull(matchingValidators);
        assertEquals(3L, matchingValidators.size());
        assertEquals(plainTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());
        assertEquals(groovyScriptMessageValidator, matchingValidators.get(2));
    }

    @Test
    public void shouldAddDefaultEmptyMessagePayloadValidator() {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        List<MessageValidator<? extends ValidationContext>> matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(1L, matchingValidators.size());
        assertEquals(DefaultEmptyMessageValidator.class, matchingValidators.get(0).getClass());

        messageValidatorRegistry.addMessageValidator("jsonTextMessageValidator", jsonTextMessageValidator);
        messageValidatorRegistry.addMessageValidator("headerMessageValidator", new DefaultMessageHeaderValidator());

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.PLAINTEXT.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(2L, matchingValidators.size());
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(0).getClass());
        assertEquals(DefaultEmptyMessageValidator.class, matchingValidators.get(1).getClass());

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("{}"));

        assertNotNull(matchingValidators);
        assertEquals(2L, matchingValidators.size());
        assertEquals(jsonTextMessageValidator, matchingValidators.get(0));
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(1).getClass());

        try {
            messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("Hello"), true);
            Assertions.fail("Missing exception due to no proper message validator found");
        } catch (CitrusRuntimeException e) {
            assertEquals("Failed to find proper message validator for message", e.getMessage());
        }

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("Hello"));
        assertNotNull(matchingValidators);
        assertEquals(2L, matchingValidators.size());
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(0).getClass());
        assertEquals(DefaultTextEqualsMessageValidator.class, matchingValidators.get(1).getClass());

        messageValidatorRegistry.addMessageValidator("plainTextMessageValidator", plainTextMessageValidator);

        matchingValidators = messageValidatorRegistry.findMessageValidators(MessageType.JSON.name(), new DefaultMessage("Hello"));

        assertNotNull(matchingValidators);
        assertEquals(2L, matchingValidators.size());
        assertEquals(DefaultMessageHeaderValidator.class, matchingValidators.get(0).getClass());
        assertEquals(plainTextMessageValidator, matchingValidators.get(1));
    }

    @Test
    public void testSchemaValidators() {
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

        Map<String, SchemaValidator<? extends SchemaValidationContext>> schemaValidators = new HashMap<>();
        schemaValidators.put("jsonSchema", jsonSchemaValidator);
        schemaValidators.put("xmlSchema", xmlSchemaValidator);

        messageValidatorRegistry.setSchemaValidators(schemaValidators);

        List<SchemaValidator<? extends SchemaValidationContext>> matchingValidators = messageValidatorRegistry.findSchemaValidators(MessageType.JSON.name(), new DefaultMessage(""));

        assertNotNull(matchingValidators);
        assertEquals(1L, matchingValidators.size());
        assertEquals(jsonSchemaValidator, matchingValidators.get(0));

        Optional<SchemaValidator<? extends SchemaValidationContext>> jsonSchema = messageValidatorRegistry.findSchemaValidator("jsonSchema");

        assertTrue(jsonSchema.isPresent());
        assertEquals(jsonSchemaValidator, jsonSchema.get());
    }
}
