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

package com.consol.citrus.actions;

import javax.xml.transform.Result;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.spi.ReferenceResolver;
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
import com.consol.citrus.xml.StringResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiveMessageBuilderTest {

	@Mock
	private Resource resource;

	@Test
	void constructor() {
		assertNotNull(new ReceiveMessageAction.Builder().build());
	}

	@Test
	void endpoint_fromEndpoint() {

	    //GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
        final Endpoint endpoint = mock(Endpoint.class);

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.endpoint(endpoint);

        //THEN
		assertSame(copy, builder);
		assertEquals(endpoint, builder.build().getEndpoint());
	}

	@Test
	void endpoint_fromUri() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String uri = "http://localhost:8080/foo/bar";

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.endpoint(uri);

        //THEN
		assertSame(copy, builder);
		assertEquals(uri, builder.build().getEndpointUri());
	}

	@Test
	void timeout() {
		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.timeout(1000L);

        //THEN
        assertSame(copy, builder);
		assertEquals(1000L, builder.build().getReceiveTimeout());
	}

	@Test
	void message() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
        final Message message = mock(Message.class);

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.message(message);

        //THEN
		assertSame(copy, builder);
		assertNotNull(builder.build().getMessageBuilder());
	}

	@Test
	void messageName() {
		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.messageName("foo");

        //THEN
		assertSame(copy, builder);
		assertTrue(builder.build().getMessageBuilder() instanceof AbstractMessageContentBuilder);
		assertEquals("foo", ((AbstractMessageContentBuilder) builder.build().getMessageBuilder()).getMessageName());
	}

	@Test
	void payload_asString() {
		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payload("payload");

		//THEN
		assertSame(copy, builder);
		assertEquals("payload", getPayloadData(builder));
	}

    @Test
    void testSetPayloadWithContentBuilderGeneration() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		builder.messageBuilder(null);

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.payload("payload");

        //THEN
        assertSame(copy, builder);
        assertEquals("payload", getPayloadData(builder));
        assertNotNull(copy.build().getMessageBuilder());
    }

    @Test
    void testSetPayloadWithStaticMessageContentBuilder() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		builder.messageBuilder(new StaticMessageContentBuilder(new DefaultMessage()));

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.payload("payload");

        //THEN
        assertSame(copy, builder);
        final Object payload = ((StaticMessageContentBuilder)
				builder.build().getMessageBuilder()).getMessage().getPayload();
        assertEquals("payload", payload);
    }

    @Test
    void testErrorIsThrownOnUnknownMessageBuilder() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		builder.messageBuilder(new AbstractMessageContentBuilder() {
			@Override
			public Object buildMessagePayload(final TestContext context, final String messageType) {
				return null;
			}
		});

        //WHEN
        final Executable setPayload = () -> builder.payload("payload");

        //THEN
        assertThrows(CitrusRuntimeException.class, setPayload);
    }

	@Test
	void payload_asResource() {
		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payload(this.resource);

		//THEN
		assertSame(copy, builder);
		assertNotNull(getPayloadData(builder));
	}

	@Test
	void payload_asResourceWithCharset() {
		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payload(this.resource, Charset.defaultCharset());

		//THEN
		assertSame(copy, builder);
		assertNotNull(getPayloadData(builder));
	}

    @Test
    void testSetPayloadWithResourceIoExceptionsIsWrapped() throws IOException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
        when(resource.getInputStream()).thenThrow(IOException.class);

        //WHEN
        final Executable setPayload = () -> builder.payload(this.resource, Charset.defaultCharset());

        //THEN
        assertThrows(CitrusRuntimeException.class, setPayload, "Failed to read payload resource");
    }

	@Test
	void payload_asObjectWithMarshaller() throws IOException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object payload = "<hello/>";
		final Marshaller marshaller = mock(Marshaller.class);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payload(payload, marshaller);

		//THEN
		verify(marshaller).marshal(eq(payload), any(StringResult.class));
		assertSame(copy, builder);
		assertNotNull(getPayloadData(builder));
	}

	@Test
	void testSetPayloadWithIoExceptionIsWrapped() throws IOException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Marshaller marshaller = mock(Marshaller.class);
		doThrow(IOException.class).when(marshaller).marshal(any(), any(Result.class));

		//WHEN
		final Executable setPayload = () -> builder.payload("", marshaller);

		//THEN
		assertThrows(CitrusRuntimeException.class, setPayload, "Failed to marshal object graph for message payload");
	}

	@Test
	void testSetPayloadWithXmlMappingExceptionIsWrapped() throws IOException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		class myXmlMappingException extends XmlMappingException {
			public myXmlMappingException(final String msg) {
				super(msg);
			}
		}

		final Marshaller marshaller = mock(Marshaller.class);
		doThrow(myXmlMappingException.class).when(marshaller).marshal(any(), any(Result.class));

		//WHEN
		final Executable setPayload = () -> builder.payload("", marshaller);

		//THEN
		assertThrows(CitrusRuntimeException.class, setPayload, "Failed to marshal object graph for message payload");
	}

	@Test
	void payload_asObjectWithMapper() throws Exception {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object payload = "{hello}";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(payload)).thenReturn("hello");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payload(payload, mapper);

		//THEN
		assertSame(copy, builder);
		assertEquals("hello", getPayloadData(builder));
	}

	@Test
	void testSetPayloadWithJsonProcessingExceptionIsWrapped() throws JsonProcessingException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

		//THEN
		//WHEN
		final Executable setPayload = () -> builder.payload("", mapper);

		//THEN
		assertThrows(CitrusRuntimeException.class, setPayload, "Failed to map object graph for message payload");
	}

	@Test
	void payload_asObjectWithString_toObjectMarshaller() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object payload = "{hello}";
		final String mapperName = "mapper";

		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		when(referenceResolver.isResolvable(mapperName)).thenReturn(true);
		final Marshaller marshaller = mock(Marshaller.class);
		when(referenceResolver.resolve(mapperName)).thenReturn(marshaller);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payload(payload, mapperName);

		//THEN
		assertSame(copy, builder);
		assertNotNull(getPayloadData(builder));
	}

	@Test
	void payload_asObjectWithString_toObjectMapper() throws Exception {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object payload = "{hello}";
		final String mapperName = "mapper";

		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		when(referenceResolver.isResolvable(mapperName)).thenReturn(true);
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(referenceResolver.resolve(mapperName)).thenReturn(mapper);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(payload)).thenReturn("hello");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payload(payload, mapperName);

		//THEN
		assertSame(copy, builder);
		assertNotNull(getPayloadData(builder));
		assertEquals("hello", getPayloadData(builder));
	}

	@Test
	void payloadModel_withMarshaller() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object payload = "<hello/>";
		final Marshaller marshaller = mock(Marshaller.class);
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		final Map<String, Marshaller> map = Collections.singletonMap("marshaller", marshaller);
		when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(map);
		when(referenceResolver.resolve(Marshaller.class)).thenReturn(marshaller);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payloadModel(payload);

		//THEN
		assertSame(copy, builder);
		assertNotNull(getPayloadData(builder));
	}

	@Test
	void payloadModel_withObjectMapper() throws JsonProcessingException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object payload = "{hello}";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		final Map<String, ObjectMapper> map = Collections.singletonMap("mapper", mapper);
		doReturn(Collections.emptyMap()).when(referenceResolver).resolveAll(Marshaller.class);
		doReturn(map).when(referenceResolver).resolveAll(ObjectMapper.class);
		when(referenceResolver.resolve(ObjectMapper.class)).thenReturn(mapper);
		final ObjectWriter writerMock = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writerMock);
		when(writerMock.writeValueAsString(payload)).thenReturn("hello");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.payloadModel(payload);

		//THEN
		assertSame(copy, builder);
		assertEquals("hello", getPayloadData(builder));
	}

	@Test
	void header_withStringObject() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String headerName = "header";
		final Integer headerValue = 45;

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.header(headerName, headerValue);

		//THEN
		assertSame(copy, builder);
		assertEquals(headerValue, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getMessageHeaders().get(headerName));
	}

	@Test
	void headers() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Map<String, Object> headers = new HashMap<>();
		headers.put("foo", 10);
		headers.put("bar", "hello");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headers(headers);

		//THEN
		assertSame(copy, builder);
		assertEquals(headers, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getMessageHeaders());
	}

	@Test
	void header_withString() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String data = "hello";


		//WHEN
		final ReceiveMessageAction.Builder copy = builder.header(data);

		//THEN
		assertSame(copy, builder);
		assertEquals(Collections.singletonList(data),
				((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void headerFragment_withObjectAndMarshaller() throws IOException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		doAnswer(invocation -> {
			((StringResult) invocation.getArgument(1)).getWriter().write("hello");
			return null;
		}).when(marshaller).marshal(eq("hello"), any(StringResult.class));

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerFragment(model, marshaller);

		//THEN
		assertSame(copy, builder);
		assertEquals(Collections.singletonList("hello"),
				((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void headerFragment_withObjectAndObjectMapper() throws Exception {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object model = "15";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(model)).thenReturn("15");

		final List<String> expectedHeaderData = Collections.singletonList("15");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerFragment(model, mapper);

		//THEN
		assertSame(copy, builder);
		assertEquals(expectedHeaderData, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void headerFragment_withObjectAndMapperName_toMarshaller() throws IOException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		doAnswer(invocation -> {
			((StringResult) invocation.getArgument(1)).getWriter().write("hello");
			return null;
		}).when(marshaller).marshal(eq("hello"), any(StringResult.class));

		final String mapperName = "marshaller";
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		when(referenceResolver.isResolvable(mapperName)).thenReturn(true);
		when(referenceResolver.resolve(mapperName)).thenReturn(marshaller);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		final List<String> expected = Collections.singletonList("hello");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerFragment(model, mapperName);

		//THEN
		assertSame(copy, builder);
		assertEquals(expected, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void headerFragment_withObjectAndMapperName_toObjectMapper() throws Exception {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object model = "hello";
		final ObjectMapper objectMapper = mock(ObjectMapper.class);
		final ObjectWriter objectWriter = mock(ObjectWriter.class);
		when(objectMapper.writer()).thenReturn(objectWriter);
		when(objectWriter.writeValueAsString(model)).thenReturn("hello");
		final String mapperName = "object";
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		when(referenceResolver.isResolvable(mapperName)).thenReturn(true);
		when(referenceResolver.resolve(mapperName)).thenReturn(objectMapper);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		final List<String> expected = Collections.singletonList("hello");


		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerFragment(model, mapperName);

		//THEN
		assertSame(copy, builder);
		assertEquals(expected, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void headerFragment_withObjectOfMarshaller() throws IOException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		doAnswer(invocation -> {
			((StringResult) invocation.getArgument(1)).getWriter().write("hello");
			return null;
		}).when(marshaller).marshal(eq("hello"), any(StringResult.class));

		final String mapperName = "marshaller";
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		final Map<String, Marshaller> beans = Collections.singletonMap(mapperName, marshaller);
		when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(beans);
		when(referenceResolver.resolve(Marshaller.class)).thenReturn(marshaller);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		final List<String> expected = Collections.singletonList("hello");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerFragment(model);

		//THEN
		assertSame(copy, builder);
		assertEquals(expected, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void headerFragment_withObjectOfObjectMapper() throws JsonProcessingException {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final Object model = "hello";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(writer.writeValueAsString(model)).thenReturn("hello");
		when(mapper.writer()).thenReturn(writer);
		final String mapperName = "object";
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		final Map<String, Marshaller> empty = new HashMap<>();
		final Map<String, ObjectMapper> beans = new HashMap<>();
		beans.put(mapperName, mapper);
		doReturn(empty).when(referenceResolver).resolveAll(Marshaller.class);
		doReturn(beans).when(referenceResolver).resolveAll(ObjectMapper.class);
		when(referenceResolver.resolve(ObjectMapper.class)).thenReturn(mapper);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);
		final List<String> expected = Collections.singletonList("hello");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerFragment(model);

		//THEN
		assertSame(copy, builder);
		assertEquals(expected, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void header_fromResource() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final List<String> expected = Collections.singletonList("");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.header(resource);

		//THEN
		assertSame(copy, builder);
		assertEquals(expected, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void header_fromResourceAndCharset() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final List<String> expected = Collections.singletonList("");

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.header(resource, Charset.defaultCharset());

		//THEN
		assertSame(copy, builder);
		assertEquals(expected, ((AbstractMessageContentBuilder)builder.build().getMessageBuilder()).getHeaderData());
	}

	@Test
	void headerNameIgnoreCase() {
		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerNameIgnoreCase(false);

		//THEN
		assertSame(copy, builder);
		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(builder, HeaderValidationContext.class, "headerValidationContext");
		assertNotNull(headerValidationContext);
		assertFalse((boolean)ReflectionTestUtils.getField(headerValidationContext, "headerNameIgnoreCase"));
	}

	@Test
	void validationScript_fromString() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String validationScript = "validation.txt";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validateScript(validationScript);

		//THEN
		assertSame(copy, builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(builder, ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("validation.txt", scriptValidationContext.getValidationScript());
	}

	@Test
	void validationScript_fromResource() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validateScript(resource);

		//THEN
		assertSame(copy, builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(builder, ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("", scriptValidationContext.getValidationScript());
	}

	@Test
	void validationScript_fromResourceAndCharset() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validateScript(resource, Charset.defaultCharset());

		//THEN
		assertSame(copy, builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(builder, ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("", scriptValidationContext.getValidationScript());
	}

	@Test
	void validateScriptResource() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String validationScript = "validation.txt";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validateScriptResource(validationScript);

		//THEN
		assertSame(copy, builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(builder, ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("validation.txt", scriptValidationContext.getValidationScriptResourcePath());
	}

	@Test
	void validateScriptType() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String scriptType = "bash";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validateScriptType(scriptType);

		//THEN
		assertSame(copy, builder);
		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(builder, ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("bash", scriptValidationContext.getScriptType());
	}

	@Test
	void messageType_fromEnum() {
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final MessageType messageType = MessageType.JSON;
		final ReceiveMessageAction.Builder copy = builder.messageType(messageType);
		assertSame(copy, builder);
		assertEquals(messageType.name(), ReflectionTestUtils.getField(builder, "messageType"));
	}

	@Test
	void messageType_fromName() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String messageType = "JSON";

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.messageType(messageType);

		//THEN
		assertSame(copy, builder);
		assertEquals(messageType, ReflectionTestUtils.getField(builder, "messageType"));
		assertEquals(messageType, builder.build().getMessageType());
		assertEquals(2, builder.build().getValidationContexts().size());
		assertTrue(builder.build().getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
		assertTrue(builder.build().getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
	}

	@Test
	void schemaValidation() {
		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.schemaValidation(true);

		//THEN
		assertSame(copy, builder);
		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.isSchemaValidationEnabled());

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(builder, JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertTrue(jsonMessageValidationContext.isSchemaValidationEnabled());
	}

	@Test
	void validateNamespace() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String prefix = "foo";
		final String uri = "http://foo.com";

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validateNamespace(prefix, uri);

		//THEN
		assertSame(copy, builder);
		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("http://foo.com", xmlMessageValidationContext.getControlNamespaces().get("foo"));
	}

	@Test
	void validate_json() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "$ResultCode";
		final String controlValue = "Success";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validate(path, controlValue);

		//THEN
		assertSame(copy, builder);
		final JsonPathMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(builder, JsonPathMessageValidationContext.class, "jsonPathValidationContext");
		assertEquals("Success", jsonMessageValidationContext.getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	void validate_xml() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "//ResultCode";
		final String controlValue = "Success";
		builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validate(path, controlValue);

		//THEN
		assertSame(copy, builder);
		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("Success", xmlMessageValidationContext.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void validate_xmlMap() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
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
		builder.messageType( MessageType.XML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validate(map);

		//THEN
		assertSame(copy, builder);

		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(value1, xmlMessageValidationContext.getXpathExpressions().get(key1));
		assertEquals(value2, xmlMessageValidationContext.getXpathExpressions().get(key2));
		assertEquals(value3, xmlMessageValidationContext.getXpathExpressions().get(key3));
	}


	@Test
	void validate_jsonMap() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
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
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validate(map);

		//THEN
		assertSame(copy, builder);

		final JsonPathMessageValidationContext jsonPathValidationContext =
				getFieldFromBuilder(builder, JsonPathMessageValidationContext.class, "jsonPathValidationContext");

		assertEquals(value1, jsonPathValidationContext.getJsonPathExpressions().get(key1));
		assertEquals(value2, jsonPathValidationContext.getJsonPathExpressions().get(key2));
		assertEquals(value3, jsonPathValidationContext.getJsonPathExpressions().get(key3));
	}

	@Test
	void ignore_json() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "$ResultCode";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.ignore(path);

		//THEN
		assertSame(copy, builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(builder, JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertTrue(jsonMessageValidationContext.getIgnoreExpressions().contains("$ResultCode"));
	}

	@Test
	void ignore_xml() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "//ResultCode";
		builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.ignore(path);

		//THEN
		assertSame(copy, builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	void ignore_xhtml() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "//ResultCode";
		builder.messageType(MessageType.XHTML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.ignore(path);

		//THEN
		assertSame(copy, builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	void xpath() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "//ResultCode";
		final String controlValue = "Success";
		builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.xpath(path, controlValue);

		//THEN
		assertSame(copy, builder);

		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("Success", xmlMessageValidationContext.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void jsonPath() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "$ResultCode";
		final String controlValue = "Success";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.jsonPath(path, controlValue);

		//THEN
		assertSame(copy, builder);

		final JsonPathMessageValidationContext jsonPathValidationContext =
				getFieldFromBuilder(builder, JsonPathMessageValidationContext.class, "jsonPathValidationContext");
		assertEquals("Success", jsonPathValidationContext.getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	void xsd() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String schemaName = "foo.xsd";
		builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.xsd(schemaName);

		//THEN
		assertSame(copy, builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(schemaName,xmlMessageValidationContext.getSchema());
	}

	@Test
	void jsonSchema() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String schemaName = "foo.json";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.jsonSchema(schemaName);

		//THEN
		assertSame(copy, builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(builder, JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertEquals(schemaName, jsonMessageValidationContext.getSchema());
	}

	@Test
	void xsdSchemaRepository() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String schemaRepository = "/schemas";
		builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.xsdSchemaRepository(schemaRepository);

		//THEN
		assertSame(copy, builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(schemaRepository, xmlMessageValidationContext.getSchemaRepository());
	}

	@Test
	void jsonSchemaRepository() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String schemaRepository = "/schemas";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.jsonSchemaRepository(schemaRepository);

		//THEN
		assertSame(copy, builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(builder, JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertEquals(schemaRepository, jsonMessageValidationContext.getSchemaRepository());
	}

	@Test
	void namespace() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String prefix = "foo";
		final String uri = "http://foo.com";

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.namespace(prefix, uri);

		//THEN
		assertSame(copy, builder);

		final XpathPayloadVariableExtractor xpathExtractor =
				getFieldFromBuilder(builder, XpathPayloadVariableExtractor.class, "xpathExtractor");
		assertEquals(uri, xpathExtractor.getNamespaces().get(prefix));

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(uri, xmlMessageValidationContext.getNamespaces().get(prefix));
	}

    @Test
    void setNamespaceAsMap() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
        final String prefix = "foo";
        final String uri = "http://foo.com";
        final Map<String, String> namespaceMap = Collections.singletonMap(prefix, uri);

        //WHEN
        final ReceiveMessageAction.Builder copy = builder.namespaces(namespaceMap);

        //THEN
        assertSame(copy, builder);

        final XpathPayloadVariableExtractor xpathExtractor =
				getFieldFromBuilder(builder, XpathPayloadVariableExtractor.class, "xpathExtractor");
        assertEquals(uri, xpathExtractor.getNamespaces().get(prefix));

        final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(builder, XmlMessageValidationContext.class, "xmlMessageValidationContext");
        assertEquals(uri, xmlMessageValidationContext.getNamespaces().get(prefix));
    }

	@Test
	void selector_fromString() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String selector = "selector";

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.selector(selector);

		//THEN
		assertSame(copy, builder);
		assertEquals(selector, builder.build().getMessageSelector());
	}

	@Test
	void selector_fromMap() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String selectorKey = "selector";
		final String selectorValue = "value";
		final Map<String, String> selectors = Collections.singletonMap(selectorKey, selectorValue);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.selector(selectors);

		//THEN
		assertSame(copy, builder);
		assertEquals(selectors.toString(), builder.build().getMessageSelectorMap().toString());
	}

	@Test
	void validator_fromMessageValidators() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final MessageValidator<?> validator1 = mock(MessageValidator.class);
		final MessageValidator<?> validator2 = mock(MessageValidator.class);
		final MessageValidator<?> validator3 = mock(MessageValidator.class);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validators(Arrays.asList(validator1, validator2, validator3));

		//THEN
		assertSame(copy, builder);
		assertEquals(3, builder.build().getValidators().size());
	}

	@Test
	void validator_fromNames() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final MessageValidator<?> validator1 = mock(MessageValidator.class);
		final MessageValidator<?> validator2 = mock(MessageValidator.class);
		final MessageValidator<?> validator3 = mock(MessageValidator.class);
		final String name1 = "validator1";
		final String name2 = "validator2";
		final String name3 = "validator3";
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		doReturn(validator1).when(referenceResolver).resolve(name1, MessageValidator.class);
		doReturn(validator2).when(referenceResolver).resolve(name2, MessageValidator.class);
		doReturn(validator3).when(referenceResolver).resolve(name3, MessageValidator.class);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validator(name1, name2, name3);

		//THEN
		assertSame(copy, builder);
		assertEquals(3, builder.build().getValidators().size());
	}

	@Test
	void headerValidator_fromHeaderValidators() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final HeaderValidator validator1 = mock(HeaderValidator.class);
		final HeaderValidator validator2 = mock(HeaderValidator.class);
		final HeaderValidator validator3 = mock(HeaderValidator.class);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerValidator(validator1, validator2, validator3);

		//THEN
		assertSame(copy, builder);

		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(builder, HeaderValidationContext.class, "headerValidationContext");
		assertEquals(3, headerValidationContext.getValidators().size());
	}

	@Test
	void headerValidator_fromNames() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final HeaderValidator validator1 = mock(HeaderValidator.class);
		final HeaderValidator validator2 = mock(HeaderValidator.class);
		final HeaderValidator validator3 = mock(HeaderValidator.class);
		final String name1 = "validator1";
		final String name2 = "validator2";
		final String name3 = "validator3";
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		doReturn(validator1).when(referenceResolver).resolve(name1, HeaderValidator.class);
		doReturn(validator2).when(referenceResolver).resolve(name2, HeaderValidator.class);
		doReturn(validator3).when(referenceResolver).resolve(name3, HeaderValidator.class);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.headerValidator(name1, name2, name3);

		//THEN
		assertSame(copy, builder);

		builder.build();
		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(builder, HeaderValidationContext.class, "headerValidationContext");
		assertEquals(3, headerValidationContext.getValidators().size());
	}

	@Test
	void dictionary() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final DataDictionary dataDictionary = mock(DataDictionary.class);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.dictionary(dataDictionary);

		//THEN
		assertSame(copy, builder);
		assertEquals(dataDictionary, builder.build().getDataDictionary());
	}

	@Test
	void dictionary_byName() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String name = "dictionary";
		final DataDictionary dataDictionary = mock(DataDictionary.class);
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		when(referenceResolver.resolve(name, DataDictionary.class)).thenReturn(dataDictionary);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.dictionary(name);

		//THEN

		assertSame(copy, builder);
		assertEquals(dataDictionary, builder.build().getDataDictionary());
	}

	@Test
	void extractFromHeader() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String name = "foo";
		final String variable = "bar";

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.extractFromHeader(name, variable);

		//THEN
		assertSame(copy, builder);
		assertNotNull(builder.build().getVariableExtractors());
		assertEquals(1, builder.build().getVariableExtractors().size());

		final MessageHeaderVariableExtractor headerExtractor =
				getFieldFromBuilder(builder, MessageHeaderVariableExtractor.class, "headerExtractor");
		assertEquals("bar", headerExtractor.getHeaderMappings().get("foo"));
	}

	@Test
	void extractFromPayload_xpath() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "//ResultCode";
		final String controlValue = "Success";
		builder.messageType(MessageType.XML);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.extractFromPayload(path, controlValue);

		//THEN
		assertSame(copy, builder);
		assertNotNull(builder.build().getVariableExtractors());
		assertEquals(1, builder.build().getVariableExtractors().size());

		final XpathPayloadVariableExtractor xpathExtractor =
				getFieldFromBuilder(builder, XpathPayloadVariableExtractor.class, "xpathExtractor");
		assertEquals("Success", xpathExtractor.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void extractFromPayload_json() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final String path = "$ResultCode";
		final String controlValue = "Success";
		builder.messageType(MessageType.JSON);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.extractFromPayload(path, controlValue);

		//THEN
		assertSame(copy, builder);
		assertNotNull(builder.build().getVariableExtractors());
		assertEquals(1, builder.build().getVariableExtractors().size());

		final JsonPathVariableExtractor jsonPathExtractor =
				getFieldFromBuilder(builder, JsonPathVariableExtractor.class, "jsonPathExtractor");
		assertEquals("Success", jsonPathExtractor.getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	void validationCallback() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final ValidationCallback callback = mock(ValidationCallback.class);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.validationCallback(callback);

		//THEN
		assertSame(copy, builder);
		assertEquals(callback, builder.build().getValidationCallback());
	}

	@Test
	void withReferenceResolver() {

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
		final ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
		ReflectionTestUtils.setField(builder, "referenceResolver", referenceResolver);

		//WHEN
		final ReceiveMessageAction.Builder copy = builder.withReferenceResolver(referenceResolver);

		//THEN
		assertSame(copy, builder);
		assertEquals(referenceResolver, ReflectionTestUtils.getField(builder, "referenceResolver"));
	}

    @Test
    void testSetMessageType(){

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
        final MessageType messageType = MessageType.BINARY_BASE64;

        //WHEN
        builder.messageType(messageType);

        //THEN
        final Object currentMessageType = ReflectionTestUtils.getField(builder, "messageType");
        assertNotNull(currentMessageType);
        assertEquals(messageType.toString(), currentMessageType.toString());
    }

    @Test
    void testSetMessageTypeAsString(){

		//GIVEN
		final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();
        final String messageType = "postalMessage";

        //WHEN
        builder.messageType(messageType);

        //THEN
        assertEquals(messageType, ReflectionTestUtils.getField(builder, "messageType"));
    }

	private <T> T getFieldFromBuilder(ReceiveMessageAction.Builder builder, final Class<T> targetClass, final String fieldName) {
		final T validationContext = targetClass.cast(
				ReflectionTestUtils.getField(builder, fieldName));
		assertNotNull(validationContext);
		return validationContext;
	}

	private String getPayloadData(ReceiveMessageAction.Builder builder) {
		return ((PayloadTemplateMessageBuilder) builder.build().getMessageBuilder())
				.getPayloadData();
	}
}
