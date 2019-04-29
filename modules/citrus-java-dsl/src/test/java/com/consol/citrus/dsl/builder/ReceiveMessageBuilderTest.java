/*
 * Copyright 2006-2019 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.HeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.Result;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiveMessageBuilderTest {
	
	private ReceiveMessageBuilder builder = new ReceiveMessageBuilder();
	
	@Mock
	private Resource resource;
	
	@Test
	void constructor() {
		assertNotNull(this.builder.getAction());
	}
	
	@Test
	void constructor_withAction() {

	    //GIVEN
        final ReceiveMessageAction action = new ReceiveMessageAction();

        //WHEN
        this.builder = new ReceiveMessageBuilder<>(action);

        //THEN
		assertEquals(action, this.builder.getAction());
	}

	@Test
	void constructor_withDelegatingTestAction() {

	    //GIVEN
        final DelegatingTestAction<ReceiveMessageAction> action = new DelegatingTestAction<>(new ReceiveMessageAction());

        //WHEN
        this.builder = new ReceiveMessageBuilder(action);

        //THEN
        assertEquals(action.getDelegate(), this.builder.getAction());
	}
	
	@Test
	void endpoint_fromEndpoint() {

	    //GIVEN
        final Endpoint endpoint = mock(Endpoint.class);

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.endpoint(endpoint);

        //THEN
		assertSame(copy, this.builder);
		assertEquals(endpoint, this.builder.getAction().getEndpoint());
	}

	@Test
	void endpoint_fromUri() {

	    //GIVEN
        final String uri = "http://localhost:8080/foo/bar";

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.endpoint(uri);

        //THEN
		assertSame(copy, this.builder);
		assertEquals(uri, this.builder.getAction().getEndpointUri());
	}

	@Test
	void timeout() {

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.timeout(1000L);

        //THEN
        assertSame(copy, this.builder);
		assertEquals(1000L, this.builder.getAction().getReceiveTimeout());
	}

	@Test
	void message() {

	    //GIVEN
        final Message message = mock(Message.class);

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.message(message);

        //THEN
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getMessageBuilder());
	}

	@Test
	void name() {

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.name("foo");

        //THEN
		assertSame(copy, this.builder);
		assertEquals("foo", this.builder.getMessageContentBuilder().getMessageName());
	}

	@Test
	void payload_asString() {

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payload("payload");

		//THEN
		assertSame(copy, this.builder);
		assertEquals("payload", getPayloadData());
	}

    @Test
    void testSetPayloadWithContentBuilderGeneration() {

	    //GIVEN
        final ReceiveMessageAction action = new ReceiveMessageAction();
        action.setMessageBuilder(null);
        this.builder = new ReceiveMessageBuilder<>(action);

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.payload("payload");

        //THEN
        assertSame(copy, this.builder);
        assertEquals("payload", getPayloadData());
        assertNotNull(copy.getAction().getMessageBuilder());
    }

    @Test
    void testSetPayloadWithStaticMessageContentBuilder() {

        //GIVEN
        final ReceiveMessageAction action = new ReceiveMessageAction();
        action.setMessageBuilder(new StaticMessageContentBuilder(new DefaultMessage()));
        this.builder = new ReceiveMessageBuilder<>(action);

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.payload("payload");

        //THEN
        assertSame(copy, this.builder);
        final Object payload = ((StaticMessageContentBuilder)
                this.builder.getMessageContentBuilder()).getMessage().getPayload();
        assertEquals("payload", payload);
    }

    @Test
    void testErrorIsThrownOnUnknownMessageBuilder() {

        //GIVEN
        final ReceiveMessageAction action = new ReceiveMessageAction();
        action.setMessageBuilder(new AbstractMessageContentBuilder() {
            @Override
            public Object buildMessagePayload(final TestContext context, final String messageType) {
                return null;
            }
        });
        this.builder = new ReceiveMessageBuilder<>(action);

        //WHEN
        final Executable setPayload = () -> this.builder.payload("payload");

        //THEN
        assertThrows(CitrusRuntimeException.class, setPayload);
    }

	@Test
	void payload_asResource() {

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payload(this.resource);

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(getPayloadData());
	}

	@Test
	void payload_asResourceWithCharset() {

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payload(this.resource, Charset.defaultCharset());

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(getPayloadData());
	}

    @Test
    void testSetPayloadWithResourceIoExceptionsIsWrapped() throws IOException {

	    //GIVEN
        when(resource.getInputStream()).thenThrow(IOException.class);

        //WHEN
        final Executable setPayload = () -> this.builder.payload(this.resource, Charset.defaultCharset());

        //THEN
        assertThrows(CitrusRuntimeException.class, setPayload, "Failed to read payload resource");
    }

	@Test
	void payload_asObjectWithMarshaller() throws IOException {

		//GIVEN
		final Object payload = "<hello/>";
		final Marshaller marshaller = mock(Marshaller.class);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payload(payload, marshaller);

		//THEN
		verify(marshaller).marshal(eq(payload), any(StringResult.class));
		assertSame(copy, this.builder);
		assertNotNull(getPayloadData());
	}

	@Test
	void testSetPayloadWithIoExceptionIsWrapped() throws IOException {

		//GIVEN
		final Marshaller marshaller = mock(Marshaller.class);
		doThrow(IOException.class).when(marshaller).marshal(any(), any(Result.class));

		//WHEN
		final Executable setPayload = () -> this.builder.payload("", marshaller);

		//THEN
		assertThrows(CitrusRuntimeException.class, setPayload, "Failed to marshal object graph for message payload");
	}

	@Test
	void testSetPayloadWithXmlMappingExceptionIsWrapped() throws IOException {

		//GIVEN
		class myXmlMappingException extends XmlMappingException {
			public myXmlMappingException(final String msg) {
				super(msg);
			}
		}

		final Marshaller marshaller = mock(Marshaller.class);
		doThrow(myXmlMappingException.class).when(marshaller).marshal(any(), any(Result.class));

		//WHEN
		final Executable setPayload = () -> this.builder.payload("", marshaller);

		//THEN
		assertThrows(CitrusRuntimeException.class, setPayload, "Failed to marshal object graph for message payload");
	}

	@Test
	void payload_asObjectWithMapper() throws Exception {

		//GIVEN
		final Object payload = "{hello}";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(payload)).thenReturn("hello");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payload(payload, mapper);

		//THEN
		assertSame(copy, this.builder);
		assertEquals("hello", getPayloadData());
	}

	@Test
	void testSetPayloadWithJsonProcessingExceptionIsWrapped() throws JsonProcessingException {

		//GIVEN
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

		//THEN
		//WHEN
		final Executable setPayload = () -> this.builder.payload("", mapper);

		//THEN
		assertThrows(CitrusRuntimeException.class, setPayload, "Failed to map object graph for message payload");
	}

	@Test
	void payload_asObjectWithString_toObjectMarshaller() {

		//GIVEN
		final Object payload = "{hello}";
		final String mapperName = "mapper";

		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		final Marshaller marshaller = mock(Marshaller.class);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(marshaller);


		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payload(payload, mapperName);

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(getPayloadData());
	}

	@Test
	void payload_asObjectWithString_toObjectMapper() throws Exception {

		//GIVEN
		final Object payload = "{hello}";
		final String mapperName = "mapper";

		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(mapper);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(payload)).thenReturn("hello");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payload(payload, mapperName);

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(getPayloadData());
		assertEquals("hello", getPayloadData());
	}

	@Test
	void payloadModel_withMarshaller() {

		//GIVEN
		final Object payload = "<hello/>";
		final Marshaller marshaller = mock(Marshaller.class);
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final Map<String, Marshaller> map = Collections.singletonMap("marshaller", marshaller);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(map);
		when(mockApplicationContext.getBean(Marshaller.class)).thenReturn(marshaller);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payloadModel(payload);

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(getPayloadData());
	}

	@Test
	void payloadModel_withObjectMapper() throws JsonProcessingException {

		//GIVEN
		final Object payload = "{hello}";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final Map<String, ObjectMapper> map = Collections.singletonMap("mapper", mapper);
		doReturn(Collections.emptyMap()).when(mockApplicationContext).getBeansOfType(Marshaller.class);
		doReturn(map).when(mockApplicationContext).getBeansOfType(ObjectMapper.class);
		when(mockApplicationContext.getBean(ObjectMapper.class)).thenReturn(mapper);
		final ObjectWriter writerMock = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writerMock);
		when(writerMock.writeValueAsString(payload)).thenReturn("hello");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.payloadModel(payload);

		//THEN
		assertSame(copy, this.builder);
		assertEquals("hello", getPayloadData());
	}

	@Test
	void header_withStringObject() {

		//GIVEN
		final String headerName = "header";
		final Integer headerValue = 45;

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.header(headerName, headerValue);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(headerValue, this.builder.getMessageContentBuilder().getMessageHeaders().get(headerName));
	}
	
	@Test
	void headers() {

		//GIVEN
		final Map<String, Object> headers = new HashMap<>();
		headers.put("foo", 10);
		headers.put("bar", "hello");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.headers(headers);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(headers, this.builder.getMessageContentBuilder().getMessageHeaders());
	}
	
	@Test
	void header_withString() {

		//GIVEN
		final String data = "hello";


		//WHEN
		final ReceiveMessageBuilder copy = this.builder.header(data);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(Collections.singletonList(data),
					this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void doHeaderFragment_withObjectAndMarshaller() {

		//GIVEN
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		final StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.doHeaderFragment(model, marshaller, stringResult);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(Collections.singletonList("hello"),
					this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void headerFragment_withObjectAndObjectMapper() throws Exception {

		//GIVEN
		final Object model = "15";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(model)).thenReturn("15");

		final List<String> expectedHeaderData = Collections.singletonList("15");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.headerFragment(model, mapper);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(expectedHeaderData, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void doHeaderFragment_withObjectAndMapperName_toMarshaller() {

		//GIVEN
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		final StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		final String mapperName = "marshaller";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(marshaller);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final List<String> expected = Collections.singletonList("hello");


		//WHEN
		final ReceiveMessageBuilder copy = this.builder.doHeaderFragment(model, mapperName, stringResult);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}

	@Test
	void headerFragment_withObjectAndMapperName_toObjectMapper() throws Exception {

		//GIVEN
		final Object model = "hello";
		final ObjectMapper objectMapper = mock(ObjectMapper.class);
		final ObjectWriter objectWriter = mock(ObjectWriter.class);
		when(objectMapper.writer()).thenReturn(objectWriter);
		when(objectWriter.writeValueAsString(model)).thenReturn("hello");
		final String mapperName = "object";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(objectMapper);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final List<String> expected = Collections.singletonList("hello");


		//WHEN
		final ReceiveMessageBuilder copy = this.builder.headerFragment(model, mapperName);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void doHeaderFragment_withObjectOfMarshaller() {

		//GIVEN
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		final StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		final String mapperName = "marshaller";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		final Map<String, Marshaller> beans = Collections.singletonMap(mapperName, marshaller);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(beans);
		when(mockApplicationContext.getBean(Marshaller.class)).thenReturn(marshaller);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final List<String> expected = Collections.singletonList("hello");


		//WHEN
		final ReceiveMessageBuilder copy = this.builder.doHeaderFragment(model, stringResult);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}

	@Test
	void headerFragment_withObjectOfObjectMapper() throws JsonProcessingException {

		//GIVEN
		final Object model = "hello";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(writer.writeValueAsString(model)).thenReturn("hello");
		when(mapper.writer()).thenReturn(writer);
		final String mapperName = "object";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		final Map<String, Marshaller> empty = new HashMap<>();
		final Map<String, ObjectMapper> beans = new HashMap<>();
		beans.put(mapperName, mapper);
		doReturn(empty).when(mockApplicationContext).getBeansOfType(Marshaller.class);
		doReturn(beans).when(mockApplicationContext).getBeansOfType(ObjectMapper.class);
		when(mockApplicationContext.getBean(ObjectMapper.class)).thenReturn(mapper);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final List<String> expected = Collections.singletonList("hello");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.headerFragment(model);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void header_fromResource() {

		//GIVEN
		final List<String> expected = Collections.singletonList("");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.header(resource);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}

	@Test
	void header_fromResourceAndCharset() {

		//GIVEN
		final List<String> expected = Collections.singletonList("");

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.header(resource, Charset.defaultCharset());

		//THEN
		assertSame(copy, this.builder);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void headerNameIgnoreCase() {

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.headerNameIgnoreCase(false);

		//THEN
		assertSame(copy, this.builder);
		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(HeaderValidationContext.class, "headerValidationContext");
		assertNotNull(headerValidationContext);
		assertFalse((boolean)ReflectionTestUtils.getField(headerValidationContext, "headerNameIgnoreCase"));
	}
	
	@Test
	void validationScript_messageTypeNotInitialized() {

		//GIVEN
		final String validationScript = "validation.txt";

		//WHEN
		final Executable validateScript = () -> this.builder.validateScript(validationScript);

		//THEN
		assertThrows(IllegalArgumentException.class, validateScript, "Message Type is not initialized!");
	}

	@Test
	void validationScript_fromString() {

		//GIVEN
		final String validationScript = "validation.txt";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validateScript(validationScript);

		//THEN
		assertSame(copy, this.builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("validation.txt", scriptValidationContext.getValidationScript());
	}

	@Test
	void validationScript_fromResource() {

		//GIVEN
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validateScript(resource);

		//THEN
		assertSame(copy, this.builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("", scriptValidationContext.getValidationScript());
	}

	@Test
	void validationScript_fromResourceAndCharset() {

		//GIVEN
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validateScript(resource, Charset.defaultCharset());

		//THEN
		assertSame(copy, this.builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("", scriptValidationContext.getValidationScript());
	}
	
	@Test
	void validateScriptResource() {

		//GIVEN
		final String validationScript = "validation.txt";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validateScriptResource(validationScript);

		//THEN
		assertSame(copy, this.builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("validation.txt", scriptValidationContext.getValidationScriptResourcePath());
	}

	@Test
	void validateScriptType() {

		//GIVEN
		final String scriptType = "bash";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validateScriptType(scriptType);

		//THEN
		assertSame(copy, this.builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("bash", scriptValidationContext.getScriptType());
	}
	
	@Test
	void messageType_fromEnum() {
		final MessageType messageType = MessageType.JSON;
		final ReceiveMessageBuilder copy = this.builder.messageType(messageType);
		assertSame(copy, this.builder);
		assertEquals(messageType.name(), ReflectionTestUtils.getField(this.builder, "messageType"));
	}

	@Test
	void messageType_fromName() {

		//GIVEN
		final String messageType = "JSON";

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.messageType(messageType);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(messageType, ReflectionTestUtils.getField(this.builder, "messageType"));
		assertEquals(messageType, this.builder.getAction().getMessageType());
		assertEquals(3, this.builder.getAction().getValidationContexts().size());
	}
	
	@Test
	void schemaValidation() {

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.schemaValidation(true);

		//THEN
		assertSame(copy, this.builder);
		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.isSchemaValidationEnabled());

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertTrue(jsonMessageValidationContext.isSchemaValidationEnabled());
	}

	@Test
	void validateNamespace() {

		//GIVEN
		final String prefix = "foo";
		final String uri = "http://foo.com";

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validateNamespace(prefix, uri);

		//THEN
		assertSame(copy, this.builder);
		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("http://foo.com", xmlMessageValidationContext.getControlNamespaces().get("foo"));
	}

	@Test
	void validate_json() {

		//GIVEN
		final String path = "$ResultCode";
		final String controlValue = "Success";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validate(path, controlValue);

		//THEN
		assertSame(copy, this.builder);
		final JsonPathMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonPathMessageValidationContext.class, "jsonPathValidationContext");
		assertEquals("Success", jsonMessageValidationContext.getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	void validate_xml() {

		//GIVEN
		final String path = "//ResultCode";
		final String controlValue = "Success";
		this.builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validate(path, controlValue);

		//THEN
		assertSame(copy, this.builder);
		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("Success", xmlMessageValidationContext.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void validate_xmlMap() {

		//GIVEN
		final Map<String, Object> map = new HashMap<>();
		final String key1 = "//ResultCode";
		final String value1 = "Success";
		final String key2 = "//Foo";
		final String value2 = "Bar";
		final String key3 = "//Hello";
		final String value3 = "Goodbye";
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		this.builder.messageType( MessageType.XML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validate(map);

		//THEN
		assertSame(copy, this.builder);

		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(value1, xmlMessageValidationContext.getXpathExpressions().get(key1));
		assertEquals(value2, xmlMessageValidationContext.getXpathExpressions().get(key2));
		assertEquals(value3, xmlMessageValidationContext.getXpathExpressions().get(key3));
	}


	@Test
	void validate_jsonMap() {

		//GIVEN
		final Map<String, Object> map = new HashMap<>();
		final String key1 = "$ResultCode";
		final String value1 = "Success";
		final String key2 = "$Foo";
		final String value2 = "Bar";
		final String key3 = "$Hello";
		final String value3 = "Goodbye";
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validate(map);

		//THEN
		assertSame(copy, this.builder);

		final JsonPathMessageValidationContext jsonPathValidationContext =
				getFieldFromBuilder(JsonPathMessageValidationContext.class, "jsonPathValidationContext");

		assertEquals(value1, jsonPathValidationContext.getJsonPathExpressions().get(key1));
		assertEquals(value2, jsonPathValidationContext.getJsonPathExpressions().get(key2));
		assertEquals(value3, jsonPathValidationContext.getJsonPathExpressions().get(key3));
	}

	@Test
	void ignore_json() {

		//GIVEN
		final String path = "$ResultCode";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.ignore(path);

		//THEN
		assertSame(copy, this.builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertTrue(jsonMessageValidationContext.getIgnoreExpressions().contains("$ResultCode"));
	}

	@Test
	void ignore_xml() {

		//GIVEN
		final String path = "//ResultCode";
		this.builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.ignore(path);

		//THEN
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	void ignore_xhtml() {

		//GIVEN
		final String path = "//ResultCode";
		this.builder.messageType(MessageType.XHTML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.ignore(path);

		//THEN
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	void xpath() {

		//GIVEN
		final String path = "//ResultCode";
		final String controlValue = "Success";
		this.builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.xpath(path, controlValue);

		//THEN
		assertSame(copy, this.builder);

		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("Success", xmlMessageValidationContext.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void jsonPath() {

		//GIVEN
		final String path = "$ResultCode";
		final String controlValue = "Success";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.jsonPath(path, controlValue);

		//THEN
		assertSame(copy, this.builder);

		final JsonPathMessageValidationContext jsonPathValidationContext =
				getFieldFromBuilder(JsonPathMessageValidationContext.class, "jsonPathValidationContext");
		assertEquals("Success", jsonPathValidationContext.getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	void xsd() {

		//GIVEN
		final String schemaName = "foo.xsd";
		this.builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.xsd(schemaName);

		//THEN
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(schemaName,xmlMessageValidationContext.getSchema());
	}

	@Test
	void jsonSchema() {

		//GIVEN
		final String schemaName = "foo.json";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.jsonSchema(schemaName);

		//THEN
		assertSame(copy, this.builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertEquals(schemaName, jsonMessageValidationContext.getSchema());
	}

	@Test
	void xsdSchemaRepository() {

		//GIVEN
		final String schemaRepository = "/schemas";
		this.builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.xsdSchemaRepository(schemaRepository);

		//THEN
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(schemaRepository, xmlMessageValidationContext.getSchemaRepository());
	}

	@Test
	void jsonSchemaRepository() {

		//GIVEN
		final String schemaRepository = "/schemas";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.jsonSchemaRepository(schemaRepository);

		//THEN
		assertSame(copy, this.builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertEquals(schemaRepository, jsonMessageValidationContext.getSchemaRepository());
	}

	@Test
	void namespace() {

		//GIVEN
		final String prefix = "foo";
		final String uri = "http://foo.com";

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.namespace(prefix, uri);

		//THEN
		assertSame(copy, this.builder);

		final XpathPayloadVariableExtractor xpathExtractor =
				getFieldFromBuilder(XpathPayloadVariableExtractor.class, "xpathExtractor");
		assertEquals(uri, xpathExtractor.getNamespaces().get(prefix));

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(uri, xmlMessageValidationContext.getNamespaces().get(prefix));
	}

    @Test
    void setNamespaceAsMap() {

        //GIVEN
        final String prefix = "foo";
        final String uri = "http://foo.com";
        final Map<String, String> namespaceMap = Collections.singletonMap(prefix, uri);

        //WHEN
        final ReceiveMessageBuilder copy = this.builder.namespaces(namespaceMap);

        //THEN
        assertSame(copy, this.builder);

        final XpathPayloadVariableExtractor xpathExtractor =
                getFieldFromBuilder(XpathPayloadVariableExtractor.class, "xpathExtractor");
        assertEquals(uri, xpathExtractor.getNamespaces().get(prefix));

        final XmlMessageValidationContext xmlMessageValidationContext =
                getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
        assertEquals(uri, xmlMessageValidationContext.getNamespaces().get(prefix));
    }
	
	@Test
	void selector_fromString() {

		//GIVEN
		final String selector = "selector";

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.selector(selector);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(selector, this.builder.getAction().getMessageSelector());
	}

	@Test
	void selector_fromMap() {

		//GIVEN
		final String selectorKey = "selector";
		final Object selectorValue = mock(Object.class);
		final Map<String, Object> selectors = Collections.singletonMap(selectorKey, selectorValue);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.selector(selectors);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(selectors, this.builder.getAction().getMessageSelectorMap());
	}
	
	@Test
	void validator_fromMessageValidators() {

		//GIVEN
		final MessageValidator validator1 = mock(MessageValidator.class);
		final MessageValidator validator2 = mock(MessageValidator.class);
		final MessageValidator validator3 = mock(MessageValidator.class);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validator(validator1, validator2, validator3);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(3, this.builder.getAction().getValidators().size());
	}

	@Test
	void validator_fromNames() {

		//GIVEN
		final MessageValidator validator1 = mock(MessageValidator.class);
		final MessageValidator validator2 = mock(MessageValidator.class);
		final MessageValidator validator3 = mock(MessageValidator.class);
		final String name1 = "validator1";
		final String name2 = "validator2";
		final String name3 = "validator3";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		doReturn(validator1).when(mockApplicationContext).getBean(name1, MessageValidator.class);
		doReturn(validator2).when(mockApplicationContext).getBean(name2, MessageValidator.class);
		doReturn(validator3).when(mockApplicationContext).getBean(name3, MessageValidator.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validator(name1, name2, name3);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(3, this.builder.getAction().getValidators().size());
	}

	@Test
	void headerValidator_fromHeaderValidators() {

		//GIVEN
		final HeaderValidator validator1 = mock(HeaderValidator.class);
		final HeaderValidator validator2 = mock(HeaderValidator.class);
		final HeaderValidator validator3 = mock(HeaderValidator.class);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.headerValidator(validator1, validator2, validator3);

		//THEN
		assertSame(copy, this.builder);

		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(HeaderValidationContext.class, "headerValidationContext");
		assertEquals(3, headerValidationContext.getValidators().size());
	}

	@Test
	void headerValidator_fromNames() {

		//GIVEN
		final HeaderValidator validator1 = mock(HeaderValidator.class);
		final HeaderValidator validator2 = mock(HeaderValidator.class);
		final HeaderValidator validator3 = mock(HeaderValidator.class);
		final String name1 = "validator1";
		final String name2 = "validator2";
		final String name3 = "validator3";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		doReturn(validator1).when(mockApplicationContext).getBean(name1, HeaderValidator.class);
		doReturn(validator2).when(mockApplicationContext).getBean(name2, HeaderValidator.class);
		doReturn(validator3).when(mockApplicationContext).getBean(name3, HeaderValidator.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);


		//WHEN
		final ReceiveMessageBuilder copy = this.builder.headerValidator(name1, name2, name3);

		//THEN
		assertSame(copy, this.builder);

		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(HeaderValidationContext.class, "headerValidationContext");
		assertEquals(3, headerValidationContext.getValidators().size());
	}
	
	@Test
	void dictionary() {

		//GIVEN
		final DataDictionary dataDictionary = mock(DataDictionary.class);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.dictionary(dataDictionary);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(dataDictionary, this.builder.getAction().getDataDictionary());
	}

	@Test
	void dictionary_byName() {

		//GIVEN
		final String name = "dictionary";
		final DataDictionary dataDictionary = mock(DataDictionary.class);
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.getBean(name, DataDictionary.class)).thenReturn(dataDictionary);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.dictionary(name);

		//THEN

		assertSame(copy, this.builder);
		assertEquals(dataDictionary, this.builder.getAction().getDataDictionary());
	}
	
	@Test
	void extractFromHeader() {

		//GIVEN
		final String name = "foo";
		final String variable = "bar";

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.extractFromHeader(name, variable);

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());

		final MessageHeaderVariableExtractor headerExtractor =
				getFieldFromBuilder(MessageHeaderVariableExtractor.class, "headerExtractor");
		assertEquals("bar", headerExtractor.getHeaderMappings().get("foo"));
	}

	@Test
	void extractFromPayload_xpath() {

		//GIVEN
		final String path = "//ResultCode";
		final String controlValue = "Success";
		this.builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.extractFromPayload(path, controlValue);

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());

		final XpathPayloadVariableExtractor xpathExtractor =
				getFieldFromBuilder(XpathPayloadVariableExtractor.class, "xpathExtractor");
		assertEquals("Success", xpathExtractor.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void extractFromPayload_json() {

		//GIVEN
		final String path = "$ResultCode";
		final String controlValue = "Success";
		this.builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.extractFromPayload(path, controlValue);

		//THEN
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());

		final JsonPathVariableExtractor jsonPathExtractor =
				getFieldFromBuilder(JsonPathVariableExtractor.class, "jsonPathExtractor");
		assertEquals("Success", jsonPathExtractor.getJsonPathExpressions().get("$ResultCode"));
	}
	
	@Test
	void validationCallback() {

		//GIVEN
		final ValidationCallback callback = mock(ValidationCallback.class);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.validationCallback(callback);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(callback, this.builder.getAction().getValidationCallback());
	}
	
	@Test
	void withApplicationContext() {

		//GIVEN
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);

		//WHEN
		final ReceiveMessageBuilder copy = this.builder.withApplicationContext(mockApplicationContext);

		//THEN
		assertSame(copy, this.builder);
		assertEquals(mockApplicationContext, ReflectionTestUtils.getField(this.builder, "applicationContext"));
	}

	@Test
    void testSetXpathExtractor(){

	    //GIVEN
        final XpathPayloadVariableExtractor extractor = mock(XpathPayloadVariableExtractor.class);

        //WHEN
        builder.setXpathExtractor(extractor);

        //THEN
        assertEquals(extractor, ReflectionTestUtils.getField(this.builder, "xpathExtractor"));
    }

    @Test
    void testSetJsonPathExtractor(){

        //GIVEN
        final JsonPathVariableExtractor extractor = mock(JsonPathVariableExtractor.class);

        //WHEN
        builder.setJsonPathExtractor(extractor);

        //THEN
        assertEquals(extractor, ReflectionTestUtils.getField(this.builder, "jsonPathExtractor"));
    }

    @Test
    void testSetMessageType(){

        //GIVEN
        final MessageType messageType = MessageType.BINARY_BASE64;

        //WHEN
        builder.setMessageType(messageType);

        //THEN
        final Object currentMessageType = ReflectionTestUtils.getField(this.builder, "messageType");
        assertNotNull(currentMessageType);
        assertEquals(messageType.toString(), currentMessageType.toString());
    }

    @Test
    void testSetMessageTypeAsString(){

        //GIVEN
        final String messageType = "postalMessage";

        //WHEN
        builder.setMessageType(messageType);

        //THEN
        assertEquals(messageType, ReflectionTestUtils.getField(this.builder, "messageType"));
    }

    @Test
    void testSetHeaderExtractor(){

        //GIVEN
        final MessageHeaderVariableExtractor extractor = mock(MessageHeaderVariableExtractor.class);

        //WHEN
        builder.setHeaderExtractor(extractor);

        //THEN
        assertEquals(extractor, ReflectionTestUtils.getField(this.builder, "headerExtractor"));
    }

    @Test
    void testSetScriptValidationContext(){

        //GIVEN
        final ScriptValidationContext context = mock(ScriptValidationContext.class);

        //WHEN
        builder.setScriptValidationContext(context);

        //THEN
        assertEquals(context, ReflectionTestUtils.getField(this.builder, "scriptValidationContext"));
    }

    @Test
    void testSetJsonPathValidationContext(){

        //GIVEN
        final JsonPathMessageValidationContext context = mock(JsonPathMessageValidationContext.class);

        //WHEN
        builder.setJsonPathValidationContext(context);

        //THEN
        assertEquals(context, ReflectionTestUtils.getField(this.builder, "jsonPathValidationContext"));
    }

    @Test
    void testSetXmlMessageValidationContext(){

        //GIVEN
        final XmlMessageValidationContext context = mock(XmlMessageValidationContext.class);

        //WHEN
        builder.setXmlMessageValidationContext(context);

        //THEN
        assertEquals(context, ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext"));
    }

    @Test
    void testSetJsonMessageValidationContext(){

        //GIVEN
        final JsonMessageValidationContext context = mock(JsonMessageValidationContext.class);

        //WHEN
        builder.setJsonMessageValidationContext(context);

        //THEN
        assertEquals(context, ReflectionTestUtils.getField(this.builder, "jsonMessageValidationContext"));
    }

    @Test
    void testSetHeaderValidationContext(){

        //GIVEN
        final HeaderValidationContext context = mock(HeaderValidationContext.class);

        //WHEN
        builder.setHeaderValidationContext(context);

        //THEN
        assertEquals(context, ReflectionTestUtils.getField(this.builder, "headerValidationContext"));
    }


	private <T> T getFieldFromBuilder(final Class<T> targetClass, final String fieldName) {
		final T scriptValidationContext = targetClass.cast(
				ReflectionTestUtils.getField(this.builder, fieldName));
		assertNotNull(scriptValidationContext);
		return scriptValidationContext;
	}

	private String getPayloadData() {
		return ((PayloadTemplateMessageBuilder) this.builder.getMessageContentBuilder())
				.getPayloadData();
	}
}
