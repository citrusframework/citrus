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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.xml.transform.StringResult;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.HeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@ExtendWith(MockitoExtension.class)
class ReceiveMessageBuilderTest {
	
	private ReceiveMessageBuilder builder = new ReceiveMessageBuilder();
	
	@Mock
	private Endpoint endpoint;
	
	@Mock
	private Message message;
	
	@Mock
	private Resource resource;
	
	@Test
	void constructor() {
		assertNotNull(this.builder);
		assertNotNull(this.builder.getAction());
	}
	
	@Test
	void constructor_withAction() {
		final ReceiveMessageAction action = new ReceiveMessageAction();
		this.builder = new ReceiveMessageBuilder<>(action);
		assertNotNull(this.builder);
		assertEquals(action, this.builder.getAction());
	}

	@Test
	void constructor_withDelegatingTestAction() {
		final DelegatingTestAction<ReceiveMessageAction> action = new DelegatingTestAction<>(new ReceiveMessageAction());
		this.builder = new ReceiveMessageBuilder(action);
		assertNotNull(this.builder);
		assertEquals(action.getDelegate(), this.builder.getAction());
	}
	
	@Test
	void endpoint_fromEndpoint() {
		final ReceiveMessageBuilder copy = this.builder.endpoint(this.endpoint);
		assertSame(copy, this.builder);
		assertEquals(this.endpoint, this.builder.getAction().getEndpoint());
	}

	@Test
	void endpoint_fromUri() {
		final String uri = "http://localhost:8080/foo/bar";
		final ReceiveMessageBuilder copy = this.builder.endpoint(uri);
		assertSame(copy, this.builder);
		assertEquals(uri, this.builder.getAction().getEndpointUri());
	}

	@Test
	void timeout() {
		final ReceiveMessageBuilder copy = this.builder.timeout(1000L);
		assertSame(copy, this.builder);
		assertEquals(1000L, this.builder.getAction().getReceiveTimeout());
	}

	@Test
	void message() {
		final ReceiveMessageBuilder copy = this.builder.message(this.message);
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getMessageBuilder());
	}

	@Test
	void name() {
		final ReceiveMessageBuilder copy = this.builder.name("foo");
		assertSame(copy, this.builder);
		assertEquals("foo", this.builder.getMessageContentBuilder().getMessageName());
	}

	@Test
	void payload_asString() {
		final ReceiveMessageBuilder copy = this.builder.payload("payload");
		assertSame(copy, this.builder);
		assertEquals("payload", ((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	void payload_asResource() {
		final ReceiveMessageBuilder copy = this.builder.payload(this.resource);
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	void payload_asResourceWithCharset() {
		final ReceiveMessageBuilder copy = this.builder.payload(this.resource, Charset.defaultCharset());
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	void payload_asObjectWithMarshaller() {
		final Object payload = "<hello/>";
		final Marshaller marshaller = mock(Marshaller.class);
		final ReceiveMessageBuilder copy = this.builder.payload(payload, marshaller);
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	void payload_asObjectWithMapper() throws Exception {
		final Object payload = "{hello}";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(payload)).thenReturn("hello");
		final ReceiveMessageBuilder copy = this.builder.payload(payload, mapper);
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		assertEquals("hello", ((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	void payload_asObjectWithString_toObjectMarshaller() throws Exception {
		final Object payload = "{hello}";
		final String mapperName = "mapper";
		
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		final Marshaller marshaller = mock(Marshaller.class);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(marshaller);
		lenient().doNothing().when(marshaller).marshal(payload, new StringResult());
		final ReceiveMessageBuilder copy = this.builder.payload(payload, mapperName);
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	void payload_asObjectWithString_toObjectMapper() throws Exception {
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
		final ReceiveMessageBuilder copy = this.builder.payload(payload, mapperName);
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		assertEquals("hello", ((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	void payloadModel_withMarshaller() {
		final Object payload = "<hello/>";
		final Marshaller marshaller = mock(Marshaller.class);
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final Map<String, Marshaller> map = new HashMap<>();
		map.put("marshaller", marshaller);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(map);
		when(mockApplicationContext.getBean(Marshaller.class)).thenReturn(marshaller);
		final ReceiveMessageBuilder copy = this.builder.payloadModel(payload);
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Disabled
	@Test
	void payloadModel_withObjectMapper() {
		final Object payload = "<hello/>";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final Map<String, ObjectMapper> map = new HashMap<>();
		map.put("mapper", mapper);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(new HashMap<>());
		when(mockApplicationContext.getBeansOfType(ObjectMapper.class)).thenReturn(map);
		when(mockApplicationContext.getBean(ObjectMapper.class)).thenReturn(mapper);
		final ReceiveMessageBuilder copy = this.builder.payloadModel(payload);
		assertSame(copy, this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	void header_withStringObject() {
		final String headerName = "header";
		final Integer headerValue = 45;
		final ReceiveMessageBuilder copy = this.builder.header(headerName, headerValue);
		assertSame(copy, this.builder);
		assertEquals(headerValue, this.builder.getMessageContentBuilder().getMessageHeaders().get(headerName));
	}
	
	@Test
	void headers() {
		final Map<String, Object> headers = new HashMap<>();
		headers.put("foo", 10);
		headers.put("bar", "hello");
		final ReceiveMessageBuilder copy = this.builder.headers(headers);
		assertSame(copy, this.builder);
		assertEquals(headers, this.builder.getMessageContentBuilder().getMessageHeaders());
	}
	
	@Test
	void header_withString() {
		final String data = "hello";
		final ReceiveMessageBuilder copy = this.builder.header(data);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add(data);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void doHeaderFragment_withObjectAndMarshaller() {
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		final StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		
		final ReceiveMessageBuilder copy = this.builder.doHeaderFragment(model, marshaller, stringResult);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void headerFragment_withObjectAndObjectMapper() throws Exception {
		final Object model = "15";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(model)).thenReturn("15");
		
		final ReceiveMessageBuilder copy = this.builder.headerFragment(model, mapper);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("15");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void doHeaderFragment_withObjectAndMapperName_toMarshaller() {
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		final StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		final String mapperName = "marshaller";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(marshaller);
		
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final ReceiveMessageBuilder copy = this.builder.doHeaderFragment(model, mapperName, stringResult);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	void headerFragment_withObjectAndMapperName_toObjectMapper() throws Exception {
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
		final ReceiveMessageBuilder copy = this.builder.headerFragment(model, mapperName);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	void doHeaderFragment_withObjectOfMarshaller() {
		final Object model = "hello";
		final Marshaller marshaller = mock(Marshaller.class);
		final StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		final String mapperName = "marshaller";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		final Map<String, Marshaller> beans = new HashMap<>();
		beans.put(mapperName, marshaller);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(beans);
		when(mockApplicationContext.getBean(Marshaller.class)).thenReturn(marshaller);
		
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final ReceiveMessageBuilder copy = this.builder.doHeaderFragment(model, stringResult);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Disabled
	@Test
	void headerFragment_withObjectOfObjectMapper() {
		final Object model = "hello";
		final ObjectMapper mapper = mock(ObjectMapper.class);
		final String mapperName = "object";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		final Map<String, Marshaller> empty = new HashMap<>();
		final Map<String, ObjectMapper> beans = new HashMap<>();
		beans.put(mapperName, mapper);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(empty);
		when(mockApplicationContext.getBeansOfType(ObjectMapper.class)).thenReturn(beans);
		when(mockApplicationContext.getBean(ObjectMapper.class)).thenReturn(mapper);
		
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final ReceiveMessageBuilder copy = this.builder.headerFragment(model);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	void header_fromResource() {
		final Resource resource = mock(Resource.class);
		
		final ReceiveMessageBuilder copy = this.builder.header(resource);
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}

	@Test
	void header_fromResourceAndCharset() {
		final Resource resource = mock(Resource.class);
		
		final ReceiveMessageBuilder copy = this.builder.header(resource, Charset.defaultCharset());
		assertSame(copy, this.builder);
		final List<String> expected = new ArrayList<>();
		expected.add("");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	void headerNameIgnoreCase() {
		final ReceiveMessageBuilder copy = this.builder.headerNameIgnoreCase(false);
		assertSame(copy, this.builder);
		final HeaderValidationContext headerValidationContext = (HeaderValidationContext)ReflectionTestUtils.getField(this.builder, "headerValidationContext");
		assertNotNull(headerValidationContext);
		assertFalse((boolean)ReflectionTestUtils.getField(headerValidationContext, "headerNameIgnoreCase"));
	}
	
	@Test
	void validationScript_messageTypeNotInitialized() {
		final String validationScript = "validation.txt";
		
		assertThrows(IllegalArgumentException.class, () -> this.builder.validateScript(validationScript));
	}

	@Test
	void validationScript_fromString() {
		final String validationScript = "validation.txt";
		
		this.builder.messageType(MessageType.JSON);
		final ReceiveMessageBuilder copy = this.builder.validateScript(validationScript);
		assertSame(copy, this.builder);

		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("validation.txt", scriptValidationContext.getValidationScript());
	}

	@Test
	void validationScript_fromResource() {
		final Resource validationScript = mock(Resource.class);
		
		this.builder.messageType(MessageType.JSON);
		final ReceiveMessageBuilder copy = this.builder.validateScript(validationScript);
		assertSame(copy, this.builder);

		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("", scriptValidationContext.getValidationScript());
	}

	@Test
	void validationScript_fromResourceAndCharset() {
		final Resource validationScript = mock(Resource.class);
		
		this.builder.messageType(MessageType.JSON);
		final ReceiveMessageBuilder copy = this.builder.validateScript(validationScript, Charset.defaultCharset());
		assertSame(copy, this.builder);

		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("", scriptValidationContext.getValidationScript());
	}
	
	@Test
	void validateScriptResource() {
		final String validationScript = "validation.txt";
		
		this.builder.messageType(MessageType.JSON);
		final ReceiveMessageBuilder copy = this.builder.validateScriptResource(validationScript);
		assertSame(copy, this.builder);

		final ScriptValidationContext scriptValidationContext =
				getFieldFromBuilder(ScriptValidationContext.class, "scriptValidationContext");
		assertEquals("validation.txt", scriptValidationContext.getValidationScriptResourcePath());
	}

	@Test
	void validateScriptType() {
		final String scriptType = "bash";
		
		this.builder.messageType(MessageType.JSON);
		final ReceiveMessageBuilder copy = this.builder.validateScriptType(scriptType);
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
		final String messageType = "JSON";
		final ReceiveMessageBuilder copy = this.builder.messageType(messageType);
		assertSame(copy, this.builder);
		assertEquals(messageType, ReflectionTestUtils.getField(this.builder, "messageType"));
		assertEquals(messageType, this.builder.getAction().getMessageType());
		assertEquals(3, this.builder.getAction().getValidationContexts().size());
	}
	
	@Test
	void schemaValidation() {
		final ReceiveMessageBuilder copy = this.builder.schemaValidation(true);
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
		final String prefix = "foo";
		final String uri = "http://foo.com";
		
		final ReceiveMessageBuilder copy = this.builder.validateNamespace(prefix, uri);
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("http://foo.com", xmlMessageValidationContext.getControlNamespaces().get("foo"));
	}

	@Test
	void validate_json() {
		final String path = "$ResultCode";
		final String controlValue = "Success";
		final MessageType messageType = MessageType.JSON;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.validate(path, controlValue);
		assertSame(copy, this.builder);

		final JsonPathMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonPathMessageValidationContext.class, "jsonPathValidationContext");
		assertEquals("Success", jsonMessageValidationContext.getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	void validate_xml() {
		final String path = "//ResultCode";
		final String controlValue = "Success";
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.validate(path, controlValue);
		assertSame(copy, this.builder);

		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("Success", xmlMessageValidationContext.getXpathExpressions().get("//ResultCode"));
	}

	/**
	 * New capability
	 */
	@Test
	void validate_xmlMap() {
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
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.validateXpath(map);
		assertSame(copy, this.builder);

		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("Success", xmlMessageValidationContext.getXpathExpressions().get("//ResultCode"));
		assertEquals("Bar", xmlMessageValidationContext.getXpathExpressions().get("//Foo"));
		assertEquals("Goodbye", xmlMessageValidationContext.getXpathExpressions().get("//Hello"));
	}

	/**
	 * New capability
	 */
	@Test
	void validate_jsonMap() {
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
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.validateJsonPath(map);
		assertSame(copy, this.builder);

		final JsonPathMessageValidationContext jsonPathValidationContext =
				getFieldFromBuilder(JsonPathMessageValidationContext.class, "jsonPathValidationContext");
		assertEquals("Success", jsonPathValidationContext.getJsonPathExpressions().get("$ResultCode"));
		assertEquals("Bar", jsonPathValidationContext.getJsonPathExpressions().get("$Foo"));
		assertEquals("Goodbye", jsonPathValidationContext.getJsonPathExpressions().get("$Hello"));
	}

	@Test
	void ignore_json() {
		final String path = "$ResultCode";
		final MessageType messageType = MessageType.JSON;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.ignore(path);
		assertSame(copy, this.builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertTrue(jsonMessageValidationContext.getIgnoreExpressions().contains("$ResultCode"));
	}

	@Test
	void ignore_xml() {
		final String path = "//ResultCode";
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.ignore(path);
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	void ignore_xhtml() {
		final String path = "//ResultCode";
		final MessageType messageType = MessageType.XHTML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.ignore(path);
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertTrue(xmlMessageValidationContext.getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	void xpath() {
		final String path = "//ResultCode";
		final String controlValue = "Success";
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.xpath(path, controlValue);
		assertSame(copy, this.builder);

		final XpathMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XpathMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("Success", xmlMessageValidationContext.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void jsonPath() {
		final String path = "$ResultCode";
		final String controlValue = "Success";
		final MessageType messageType = MessageType.JSON;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.jsonPath(path, controlValue);
		assertSame(copy, this.builder);

		final JsonPathMessageValidationContext jsonPathValidationContext =
				getFieldFromBuilder(JsonPathMessageValidationContext.class, "jsonPathValidationContext");
		assertEquals("Success", jsonPathValidationContext.getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	void xsd() {
		final String schemaName = "foo.xsd";
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.xsd(schemaName);
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(schemaName,xmlMessageValidationContext.getSchema());
	}

	@Test
	void jsonSchema() {
		final String schemaName = "foo.json";
		final MessageType messageType = MessageType.JSON;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.jsonSchema(schemaName);
		assertSame(copy, this.builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertEquals(schemaName, jsonMessageValidationContext.getSchema());
	}

	@Test
	void xsdSchemaRepository() {
		final String schemaRepository = "/schemas";
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.xsdSchemaRepository(schemaRepository);
		assertSame(copy, this.builder);

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals(schemaRepository, xmlMessageValidationContext.getSchemaRepository());
	}

	@Test
	void jsonSchemaRepository() {
		final String schemaRepository = "/schemas";
		final MessageType messageType = MessageType.JSON;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.jsonSchemaRepository(schemaRepository);
		assertSame(copy, this.builder);

		final JsonMessageValidationContext jsonMessageValidationContext =
				getFieldFromBuilder(JsonMessageValidationContext.class, "jsonMessageValidationContext");
		assertEquals(schemaRepository, jsonMessageValidationContext.getSchemaRepository());
	}

	@Test
	void namespace() {
		final String prefix = "foo";
		final String uri = "http://foo.com";
		
		final ReceiveMessageBuilder copy = this.builder.namespace(prefix, uri);
		assertSame(copy, this.builder);

		final XpathPayloadVariableExtractor xpathExtractor =
				getFieldFromBuilder(XpathPayloadVariableExtractor.class, "xpathExtractor");
		assertEquals("http://foo.com", xpathExtractor.getNamespaces().get("foo"));

		final XmlMessageValidationContext xmlMessageValidationContext =
				getFieldFromBuilder(XmlMessageValidationContext.class, "xmlMessageValidationContext");
		assertEquals("http://foo.com", xmlMessageValidationContext.getNamespaces().get("foo"));
	}
	
	@Test
	void selector_fromString() {
		final String selector = "selector";
		
		final ReceiveMessageBuilder copy = this.builder.selector(selector);
		assertSame(copy, this.builder);
		assertEquals(selector, this.builder.getAction().getMessageSelector());
	}

	@Test
	void selector_fromMap() {
		final String selectorKey = "selector";
		final Object selectorValue = mock(Object.class);
		final Map<String, Object> selectors = new HashMap<>();
		selectors.put(selectorKey, selectorValue);
		
		final ReceiveMessageBuilder copy = this.builder.selector(selectors);
		assertSame(copy, this.builder);
		assertEquals(selectors, this.builder.getAction().getMessageSelectorMap());
	}
	
	@Test
	void validator_fromMessageValidators() {
		final MessageValidator validator1 = mock(MessageValidator.class);
		final MessageValidator validator2 = mock(MessageValidator.class);
		final MessageValidator validator3 = mock(MessageValidator.class);
		
		final ReceiveMessageBuilder copy = this.builder.validator(validator1, validator2, validator3);
		assertSame(copy, this.builder);
		assertEquals(3, this.builder.getAction().getValidators().size());
	}

	@Disabled
	@Test
	void validator_fromNames() {
		final MessageValidator validator1 = mock(MessageValidator.class);
		final MessageValidator validator2 = mock(MessageValidator.class);
		final MessageValidator validator3 = mock(MessageValidator.class);
		final String name1 = "validator1";
		final String name2 = "validator2";
		final String name3 = "validator3";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.getBean(name1, MessageValidator.class)).thenReturn(validator1);
		when(mockApplicationContext.getBean(name2, MessageValidator.class)).thenReturn(validator2);
		when(mockApplicationContext.getBean(name3, MessageValidator.class)).thenReturn(validator3);
		
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final ReceiveMessageBuilder copy = this.builder.validator(name1, name2, name3);
		assertSame(copy, this.builder);
		assertEquals(3, this.builder.getAction().getValidators().size());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	void headerValidator_fromHeaderValidators() {
		final HeaderValidator validator1 = mock(HeaderValidator.class);
		final HeaderValidator validator2 = mock(HeaderValidator.class);
		final HeaderValidator validator3 = mock(HeaderValidator.class);
		
		final ReceiveMessageBuilder copy = this.builder.headerValidator(validator1, validator2, validator3);
		assertSame(copy, this.builder);

		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(HeaderValidationContext.class, "headerValidationContext");
		assertEquals(3, headerValidationContext.getValidators().size());
	}

	@Disabled
	@Test
	void headerValidator_fromNames() {
		final HeaderValidator validator1 = mock(HeaderValidator.class);
		final HeaderValidator validator2 = mock(HeaderValidator.class);
		final HeaderValidator validator3 = mock(HeaderValidator.class);
		final String name1 = "validator1";
		final String name2 = "validator2";
		final String name3 = "validator3";
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.getBean(name1, HeaderValidator.class)).thenReturn(validator1);
		when(mockApplicationContext.getBean(name2, HeaderValidator.class)).thenReturn(validator2);
		when(mockApplicationContext.getBean(name3, HeaderValidator.class)).thenReturn(validator3);
		
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final ReceiveMessageBuilder copy = this.builder.headerValidator(name1, name2, name3);
		assertSame(copy, this.builder);

		final HeaderValidationContext headerValidationContext =
				getFieldFromBuilder(HeaderValidationContext.class, "headerValidationContext");
		assertEquals(3, headerValidationContext.getValidators().size());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	void dictionary() {
		final DataDictionary dataDictionary = mock(DataDictionary.class);
		
		final ReceiveMessageBuilder copy = this.builder.dictionary(dataDictionary);
		assertSame(copy, this.builder);
		assertEquals(dataDictionary, this.builder.getAction().getDataDictionary());
	}

	@Test
	void dictionary_byName() {
		final String name = "dictionary";
		final DataDictionary dataDictionary = mock(DataDictionary.class);
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		
		when(mockApplicationContext.getBean(name, DataDictionary.class)).thenReturn(dataDictionary);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final ReceiveMessageBuilder copy = this.builder.dictionary(name);
		assertSame(copy, this.builder);
		assertEquals(dataDictionary, this.builder.getAction().getDataDictionary());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	void extractFromHeader() {
		final String name = "foo";
		final String variable = "bar";
		
		final ReceiveMessageBuilder copy = this.builder.extractFromHeader(name, variable);
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());

		final MessageHeaderVariableExtractor headerExtractor =
				getFieldFromBuilder(MessageHeaderVariableExtractor.class, "headerExtractor");
		assertEquals("bar", headerExtractor.getHeaderMappings().get("foo"));
	}

	@Test
	void extractFromPayload_xpath() {
		final String path = "//ResultCode";
		final String controlValue = "Success";
		final MessageType messageType = MessageType.XML;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.extractFromPayload(path, controlValue);
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());

		final XpathPayloadVariableExtractor xpathExtractor =
				getFieldFromBuilder(XpathPayloadVariableExtractor.class, "xpathExtractor");
		assertEquals("Success", xpathExtractor.getXpathExpressions().get("//ResultCode"));
	}

	@Test
	void extractFromPayload_json() {
		final String path = "$ResultCode";
		final String controlValue = "Success";
		final MessageType messageType = MessageType.JSON;
		
		this.builder.messageType(messageType);
		final ReceiveMessageBuilder copy = this.builder.extractFromPayload(path, controlValue);
		assertSame(copy, this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());

		final JsonPathVariableExtractor jsonPathExtractor =
				getFieldFromBuilder(JsonPathVariableExtractor.class, "jsonPathExtractor");
		assertEquals("Success", jsonPathExtractor.getJsonPathExpressions().get("$ResultCode"));
	}
	
	@Test
	void validationCallback() {
		final ValidationCallback callback = mock(ValidationCallback.class);
		
		final ReceiveMessageBuilder copy = this.builder.validationCallback(callback);
		assertSame(copy, this.builder);
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
		assertEquals(callback, this.builder.getAction().getValidationCallback());
	}
	
	@Test
	void withApplicationContext() {
		final ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		final ReceiveMessageBuilder copy = this.builder.withApplicationContext(mockApplicationContext);
		assertSame(copy, this.builder);
		assertEquals(mockApplicationContext, ReflectionTestUtils.getField(this.builder, "applicationContext"));
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}


	private <T> T getFieldFromBuilder(final Class<T> targetClass, final String fieldName) {
		final T scriptValidationContext = targetClass.cast(
				ReflectionTestUtils.getField(this.builder, fieldName));
		assertNotNull(scriptValidationContext);
		return scriptValidationContext;
	}
}
