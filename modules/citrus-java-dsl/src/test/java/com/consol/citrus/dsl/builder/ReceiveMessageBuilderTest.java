package com.consol.citrus.dsl.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class ReceiveMessageBuilderTest {
	
	private ReceiveMessageBuilder builder;
	
	@Mock
	private Endpoint endpoint;
	
	@Mock
	private Message message;
	
	@Mock
	private Resource resource;
	
	@Test
	public void constructor() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		assertNotNull(this.builder);
		assertNotNull(this.builder.getAction());
	}
	
	@Test
	public void constructor_withAction() throws Exception {
		ReceiveMessageAction action = new ReceiveMessageAction();
		this.builder = new ReceiveMessageBuilder(action);
		assertNotNull(this.builder);
		assertEquals(action, this.builder.getAction());
	}

	@Test
	public void constructor_withDelegatingTestAction() throws Exception {
		DelegatingTestAction<ReceiveMessageAction> action = new DelegatingTestAction<ReceiveMessageAction>(new ReceiveMessageAction());
		this.builder = new ReceiveMessageBuilder(action);
		assertNotNull(this.builder);
		assertEquals(action.getDelegate(), this.builder.getAction());
	}
	
	@Test
	public void endpoint_fromEndpoint() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.endpoint(this.endpoint);
		assertTrue(copy == this.builder);
		assertEquals(this.endpoint, this.builder.getAction().getEndpoint());
	}

	@Test
	public void endpoint_fromUri() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		String uri = "http://localhost:8080/foo/bar";
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.endpoint(uri);
		assertTrue(copy == this.builder);
		assertEquals(uri, this.builder.getAction().getEndpointUri());
	}

	@Test
	public void timeout() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.timeout(1000L);
		assertTrue(copy == this.builder);
		assertEquals(1000L, this.builder.getAction().getReceiveTimeout());
	}

	@Test
	public void message() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.message(this.message);
		assertTrue(copy == this.builder);
		assertNotNull(this.builder.getAction().getMessageBuilder());
	}

	@Test
	public void name() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.name("foo");
		assertTrue(copy == this.builder);
		assertEquals("foo", this.builder.getMessageContentBuilder().getMessageName());
	}

	@Test
	public void payload_asString() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payload("payload");
		assertTrue(copy == this.builder);
		assertEquals("payload", ((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	public void payload_asResource() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payload(this.resource);
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	public void payload_asResourceWithCharset() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payload(this.resource, Charset.defaultCharset());
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	public void payload_asObjectWithMarshaller() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		Object payload = "<hello/>";
		Marshaller marshaller = mock(Marshaller.class);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payload(payload, marshaller);
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	public void payload_asObjectWithMapper() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		Object payload = "{hello}";
		ObjectMapper mapper = mock(ObjectMapper.class);
		ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(payload)).thenReturn("hello");
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payload(payload, mapper);
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		assertEquals("hello", ((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
	}

	@Test
	public void payload_asObjectWithString_toObjectMarshaller() throws Exception {
		Object payload = "{hello}";
		String mapperName = "mapper";
		this.builder = new ReceiveMessageBuilder();
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		Marshaller marshaller = mock(Marshaller.class);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(marshaller);
		lenient().doNothing().when(marshaller).marshal(payload, new StringResult());
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payload(payload, mapperName);
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	public void payload_asObjectWithString_toObjectMapper() throws Exception {
		Object payload = "{hello}";
		String mapperName = "mapper";
		this.builder = new ReceiveMessageBuilder();
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		ObjectMapper mapper = mock(ObjectMapper.class);
		ObjectWriter writer = mock(ObjectWriter.class);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(mapper);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(payload)).thenReturn("hello");
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payload(payload, mapperName);
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		assertEquals("hello", ((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	public void payloadModel_withMarshaller() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		Object payload = "<hello/>";
		Marshaller marshaller = mock(Marshaller.class);
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		Map<String, Marshaller> map = new HashMap<>();
		map.put("marshaller", marshaller);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(map);
		when(mockApplicationContext.getBean(Marshaller.class)).thenReturn(marshaller);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payloadModel(payload);
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Disabled
	@Test
	public void payloadModel_withObjectMapper() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		Object payload = "<hello/>";
		ObjectMapper mapper = mock(ObjectMapper.class);
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		Map<String, ObjectMapper> map = new HashMap<>();
		map.put("mapper", mapper);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(new HashMap<String, Marshaller>());
		when(mockApplicationContext.getBeansOfType(ObjectMapper.class)).thenReturn(map);
		when(mockApplicationContext.getBean(ObjectMapper.class)).thenReturn(mapper);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.payloadModel(payload);
		assertTrue(copy == this.builder);
		assertNotNull(((PayloadTemplateMessageBuilder)this.builder.getMessageContentBuilder()).getPayloadData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	public void header_withStringObject() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		String headerName = "header";
		Integer headerValue = 45;
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.header(headerName, headerValue);
		assertTrue(copy == this.builder);
		assertEquals(headerValue, this.builder.getMessageContentBuilder().getMessageHeaders().get(headerName));
	}
	
	@Test
	public void headers() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		Map<String, Object> headers = new HashMap<>();
		headers.put("foo", 10);
		headers.put("bar", "hello");
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.headers(headers);
		assertTrue(copy == this.builder);
		assertEquals(headers, this.builder.getMessageContentBuilder().getMessageHeaders());
	}
	
	@Test
	public void header_withString() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		String data = "hello";
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.header(data);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add(data);
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	public void doHeaderFragment_withObjectAndMarshaller() throws Exception {
		Object model = "hello";
		Marshaller marshaller = mock(Marshaller.class);
		StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.doHeaderFragment(model, marshaller, stringResult);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	public void headerFragment_withObjectAndObjectMapper() throws Exception {
		Object model = "15";
		ObjectMapper mapper = mock(ObjectMapper.class);
		ObjectWriter writer = mock(ObjectWriter.class);
		when(mapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(model)).thenReturn("15");
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.headerFragment(model, mapper);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("15");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	public void doHeaderFragment_withObjectAndMapperName_toMarshaller() throws Exception {
		Object model = "hello";
		Marshaller marshaller = mock(Marshaller.class);
		StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		String mapperName = "marshaller";
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(marshaller);
		this.builder = new ReceiveMessageBuilder();
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.doHeaderFragment(model, mapperName, stringResult);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	public void headerFragment_withObjectAndMapperName_toObjectMapper() throws Exception {
		Object model = "hello";
		ObjectMapper objectMapper = mock(ObjectMapper.class);
		ObjectWriter objectWriter = mock(ObjectWriter.class);
		when(objectMapper.writer()).thenReturn(objectWriter);
		when(objectWriter.writeValueAsString(model)).thenReturn("hello");
		String mapperName = "object";
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.containsBean(mapperName)).thenReturn(true);
		when(mockApplicationContext.getBean(mapperName)).thenReturn(objectMapper);
		this.builder = new ReceiveMessageBuilder();
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.headerFragment(model, mapperName);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	public void doHeaderFragment_withObjectOfMarshaller() throws Exception {
		Object model = "hello";
		Marshaller marshaller = mock(Marshaller.class);
		StringResult stringResult = mock(StringResult.class);
		when(stringResult.toString()).thenReturn("hello");
		String mapperName = "marshaller";
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		Map<String, Marshaller> beans = new HashMap<>();
		beans.put(mapperName, marshaller);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(beans);
		when(mockApplicationContext.getBean(Marshaller.class)).thenReturn(marshaller);
		this.builder = new ReceiveMessageBuilder();
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.doHeaderFragment(model, stringResult);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Disabled
	@Test
	public void headerFragment_withObjectOfObjectMapper() throws Exception {
		Object model = "hello";
		ObjectMapper mapper = mock(ObjectMapper.class);
		String mapperName = "object";
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		Map<String, Marshaller> empty = new HashMap<>();
		Map<String, ObjectMapper> beans = new HashMap<>();
		beans.put(mapperName, mapper);
		when(mockApplicationContext.getBeansOfType(Marshaller.class)).thenReturn(empty);
		when(mockApplicationContext.getBeansOfType(ObjectMapper.class)).thenReturn(beans);
		when(mockApplicationContext.getBean(ObjectMapper.class)).thenReturn(mapper);
		this.builder = new ReceiveMessageBuilder();
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.headerFragment(model);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("hello");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	public void header_fromResource() throws Exception {
		Resource resource = mock(Resource.class);
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.header(resource);
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}

	@Test
	public void header_fromResourceAndCharset() throws Exception {
		Resource resource = mock(Resource.class);
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.header(resource, Charset.defaultCharset());
		assertTrue(copy == this.builder);
		List<String> expected = new ArrayList<>();
		expected.add("");
		assertEquals(expected, this.builder.getMessageContentBuilder().getHeaderData());
	}
	
	@Test
	public void headerNameIgnoreCase() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.headerNameIgnoreCase(false);
		assertTrue(copy == this.builder);
		HeaderValidationContext headerValidationContext = (HeaderValidationContext)ReflectionTestUtils.getField(this.builder, "headerValidationContext");
		assertNotNull(headerValidationContext);
		assertFalse((boolean)ReflectionTestUtils.getField(headerValidationContext, "headerNameIgnoreCase"));
	}
	
	@Test
	public void validationScript_messageTypeNotInitialized() throws Exception {
		String validationScript = "validation.txt";
		this.builder = new ReceiveMessageBuilder();
		assertThrows(IllegalArgumentException.class, () -> this.builder.validateScript(validationScript));
	}

	@Test
	public void validationScript_fromString() throws Exception {
		String validationScript = "validation.txt";
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(MessageType.JSON);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateScript(validationScript);
		assertTrue(copy == this.builder);
		assertEquals("validation.txt", ((ScriptValidationContext)ReflectionTestUtils.getField(this.builder, "scriptValidationContext")).getValidationScript());
	}

	@Test
	public void validationScript_fromResource() throws Exception {
		Resource validationScript = mock(Resource.class);
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(MessageType.JSON);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateScript(validationScript);
		assertTrue(copy == this.builder);
		assertEquals("", ((ScriptValidationContext)ReflectionTestUtils.getField(this.builder, "scriptValidationContext")).getValidationScript());
	}

	@Test
	public void validationScript_fromResourceAndCharset() throws Exception {
		Resource validationScript = mock(Resource.class);
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(MessageType.JSON);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateScript(validationScript, Charset.defaultCharset());
		assertTrue(copy == this.builder);
		assertEquals("", ((ScriptValidationContext)ReflectionTestUtils.getField(this.builder, "scriptValidationContext")).getValidationScript());
	}
	
	@Test
	public void validateScriptResource() throws Exception {
		String validationScript = "validation.txt";
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(MessageType.JSON);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateScriptResource(validationScript);
		assertTrue(copy == this.builder);
		assertEquals("validation.txt", ((ScriptValidationContext)ReflectionTestUtils.getField(this.builder, "scriptValidationContext")).getValidationScriptResourcePath());
	}

	@Test
	public void validateScriptType() throws Exception {
		String scriptType = "bash";
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(MessageType.JSON);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateScriptType(scriptType);
		assertTrue(copy == this.builder);
		assertEquals("bash", ((ScriptValidationContext)ReflectionTestUtils.getField(this.builder, "scriptValidationContext")).getScriptType());
	}
	
	@Test
	public void messageType_fromEnum() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		MessageType messageType = MessageType.JSON;
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.messageType(messageType);
		assertTrue(copy == this.builder);
		assertEquals(messageType.name(), (String)ReflectionTestUtils.getField(this.builder, "messageType"));
	}

	@Test
	public void messageType_fromName() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		String messageType = "JSON";
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.messageType(messageType);
		assertTrue(copy == this.builder);
		assertEquals(messageType, (String)ReflectionTestUtils.getField(this.builder, "messageType"));
		assertEquals(messageType, this.builder.getAction().getMessageType());
		assertEquals(3, ((ReceiveMessageAction)this.builder.getAction()).getValidationContexts().size());
	}
	
	@Test
	public void schemaValidation() throws Exception {
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.schemaValidation(true);
		assertTrue(copy == this.builder);
		assertTrue(((XmlMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).isSchemaValidationEnabled());
		assertTrue(((JsonMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonMessageValidationContext")).isSchemaValidationEnabled());
	}

	@Test
	public void validateNamespace() throws Exception {
		String prefix = "foo";
		String uri = "http://foo.com";
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateNamespace(prefix, uri);
		assertTrue(copy == this.builder);
		assertEquals("http://foo.com", ((XmlMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getControlNamespaces().get("foo"));
	}

	@Test
	public void validate_json() throws Exception {
		String path = "$ResultCode";
		String controlValue = "Success";
		MessageType messageType = MessageType.JSON;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validate(path, controlValue);
		assertTrue(copy == this.builder);
		assertEquals("Success", ((JsonPathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonPathValidationContext")).getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	public void validate_xml() throws Exception {
		String path = "//ResultCode";
		String controlValue = "Success";
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validate(path, controlValue);
		assertTrue(copy == this.builder);
		assertEquals("Success", ((XpathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getXpathExpressions().get("//ResultCode"));
	}

	/**
	 * New capability
	 * @throws Exception
	 */
	@Test
	public void validate_xmlMap() throws Exception {
		Map<String, Object> map = new HashMap<>();
		String key1 = "//ResultCode";
		String value1 = "Success";
		String key2 = "//Foo";
		String value2 = "Bar";
		String key3 = "//Hello";
		String value3 = "Goodbye";
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateXpath(map);
		assertTrue(copy == this.builder);
		assertEquals("Success", ((XpathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getXpathExpressions().get("//ResultCode"));
		assertEquals("Bar", ((XpathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getXpathExpressions().get("//Foo"));
		assertEquals("Goodbye", ((XpathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getXpathExpressions().get("//Hello"));
	}

	/**
	 * New capability
	 * @throws Exception
	 */
	@Test
	public void validate_jsonMap() throws Exception {
		Map<String, Object> map = new HashMap<>();
		String key1 = "$ResultCode";
		String value1 = "Success";
		String key2 = "$Foo";
		String value2 = "Bar";
		String key3 = "$Hello";
		String value3 = "Goodbye";
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validateJsonPath(map);
		assertTrue(copy == this.builder);
		assertEquals("Success", ((JsonPathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonPathValidationContext")).getJsonPathExpressions().get("$ResultCode"));
		assertEquals("Bar", ((JsonPathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonPathValidationContext")).getJsonPathExpressions().get("$Foo"));
		assertEquals("Goodbye", ((JsonPathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonPathValidationContext")).getJsonPathExpressions().get("$Hello"));
	}

	@Test
	public void ignore_json() throws Exception {
		String path = "$ResultCode";
		MessageType messageType = MessageType.JSON;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.ignore(path);
		assertTrue(copy == this.builder);
		assertTrue(((JsonMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonMessageValidationContext")).getIgnoreExpressions().contains("$ResultCode"));
	}

	@Test
	public void ignore_xml() throws Exception {
		String path = "//ResultCode";
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.ignore(path);
		assertTrue(copy == this.builder);
		assertTrue(((XmlMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	public void ignore_xhtml() throws Exception {
		String path = "//ResultCode";
		MessageType messageType = MessageType.XHTML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.ignore(path);
		assertTrue(copy == this.builder);
		assertTrue(((XmlMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getIgnoreExpressions().contains("//ResultCode"));
	}

	@Test
	public void xpath() throws Exception {
		String path = "//ResultCode";
		String controlValue = "Success";
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.xpath(path, controlValue);
		assertTrue(copy == this.builder);
		assertEquals("Success", ((XpathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getXpathExpressions().get("//ResultCode"));
	}

	@Test
	public void jsonPath() throws Exception {
		String path = "$ResultCode";
		String controlValue = "Success";
		MessageType messageType = MessageType.JSON;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.jsonPath(path, controlValue);
		assertTrue(copy == this.builder);
		assertEquals("Success", ((JsonPathMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonPathValidationContext")).getJsonPathExpressions().get("$ResultCode"));
	}

	@Test
	public void xsd() throws Exception {
		String schemaName = "foo.xsd";
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.xsd(schemaName);
		assertTrue(copy == this.builder);
		assertEquals(schemaName, ((XmlMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getSchema());
	}

	@Test
	public void jsonSchema() throws Exception {
		String schemaName = "foo.json";
		MessageType messageType = MessageType.JSON;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.jsonSchema(schemaName);
		assertTrue(copy == this.builder);
		assertEquals(schemaName, ((JsonMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonMessageValidationContext")).getSchema());
	}

	@Test
	public void xsdSchemaRepository() throws Exception {
		String schemaRepository = "/schemas";
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.xsdSchemaRepository(schemaRepository);
		assertTrue(copy == this.builder);
		assertEquals(schemaRepository, ((XmlMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getSchemaRepository());
	}

	@Test
	public void jsonSchemaRepository() throws Exception {
		String schemaRepository = "/schemas";
		MessageType messageType = MessageType.JSON;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.jsonSchemaRepository(schemaRepository);
		assertTrue(copy == this.builder);
		assertEquals(schemaRepository, ((JsonMessageValidationContext)ReflectionTestUtils.getField(this.builder, "jsonMessageValidationContext")).getSchemaRepository());
	}

	@Test
	public void namespace() throws Exception {
		String prefix = "foo";
		String uri = "http://foo.com";
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.namespace(prefix, uri);
		assertTrue(copy == this.builder);
		assertEquals("http://foo.com", ((XpathPayloadVariableExtractor)ReflectionTestUtils.getField(this.builder, "xpathExtractor")).getNamespaces().get("foo"));
		assertEquals("http://foo.com", ((XmlMessageValidationContext)ReflectionTestUtils.getField(this.builder, "xmlMessageValidationContext")).getNamespaces().get("foo"));
	}
	
	@Test
	public void selector_fromString() throws Exception {
		String selector = "selector";
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.selector(selector);
		assertTrue(copy == this.builder);
		assertEquals(selector, this.builder.getAction().getMessageSelector());
	}

	@Test
	public void selector_fromMap() throws Exception {
		String selectorKey = "selector";
		Object selectorValue = mock(Object.class);
		Map<String, Object> selectors = new HashMap<>();
		selectors.put(selectorKey, selectorValue);
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.selector(selectors);
		assertTrue(copy == this.builder);
		assertEquals(selectors, this.builder.getAction().getMessageSelectorMap());
	}
	
	@Test
	public void validator_fromMessageValidators() throws Exception {
		MessageValidator validator1 = mock(MessageValidator.class);
		MessageValidator validator2 = mock(MessageValidator.class);
		MessageValidator validator3 = mock(MessageValidator.class);
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validator(validator1, validator2, validator3);
		assertTrue(copy == this.builder);
		assertEquals(3, this.builder.getAction().getValidators().size());
	}

	@Disabled
	@Test
	public void validator_fromNames() throws Exception {
		MessageValidator validator1 = mock(MessageValidator.class);
		MessageValidator validator2 = mock(MessageValidator.class);
		MessageValidator validator3 = mock(MessageValidator.class);
		String name1 = "validator1";
		String name2 = "validator2";
		String name3 = "validator3";
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.getBean(name1, MessageValidator.class)).thenReturn(validator1);
		when(mockApplicationContext.getBean(name2, MessageValidator.class)).thenReturn(validator2);
		when(mockApplicationContext.getBean(name3, MessageValidator.class)).thenReturn(validator3);
		this.builder = new ReceiveMessageBuilder();
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validator(name1, name2, name3);
		assertTrue(copy == this.builder);
		assertEquals(3, this.builder.getAction().getValidators().size());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

	@Test
	public void headerValidator_fromHeaderValidators() throws Exception {
		HeaderValidator validator1 = mock(HeaderValidator.class);
		HeaderValidator validator2 = mock(HeaderValidator.class);
		HeaderValidator validator3 = mock(HeaderValidator.class);
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.headerValidator(validator1, validator2, validator3);
		assertTrue(copy == this.builder);
		assertEquals(3, ((HeaderValidationContext)ReflectionTestUtils.getField(this.builder, "headerValidationContext")).getValidators().size());
	}

	@Disabled
	@Test
	public void headerValidator_fromNames() throws Exception {
		HeaderValidator validator1 = mock(HeaderValidator.class);
		HeaderValidator validator2 = mock(HeaderValidator.class);
		HeaderValidator validator3 = mock(HeaderValidator.class);
		String name1 = "validator1";
		String name2 = "validator2";
		String name3 = "validator3";
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		when(mockApplicationContext.getBean(name1, HeaderValidator.class)).thenReturn(validator1);
		when(mockApplicationContext.getBean(name2, HeaderValidator.class)).thenReturn(validator2);
		when(mockApplicationContext.getBean(name3, HeaderValidator.class)).thenReturn(validator3);
		this.builder = new ReceiveMessageBuilder();
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.headerValidator(name1, name2, name3);
		assertTrue(copy == this.builder);
		assertEquals(3, ((HeaderValidationContext)ReflectionTestUtils.getField(this.builder, "headerValidationContext")).getValidators().size());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	public void dictionary() throws Exception {
		DataDictionary<String> dataDictionary = mock(DataDictionary.class);
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.dictionary(dataDictionary);
		assertTrue(copy == this.builder);
		assertEquals(dataDictionary, this.builder.getAction().getDataDictionary());
	}

	@Test
	public void dictionary_byName() throws Exception {
		String name = "dictionary";
		DataDictionary<String> dataDictionary = mock(DataDictionary.class);
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		this.builder = new ReceiveMessageBuilder();
		when(mockApplicationContext.getBean(name, DataDictionary.class)).thenReturn(dataDictionary);
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.dictionary(name);
		assertTrue(copy == this.builder);
		assertEquals(dataDictionary, this.builder.getAction().getDataDictionary());
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}
	
	@Test
	public void extractFromHeader() throws Exception {
		String name = "foo";
		String variable = "bar";
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.extractFromHeader(name, variable);
		assertTrue(copy == this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());
		assertEquals("bar", ((MessageHeaderVariableExtractor)ReflectionTestUtils.getField(this.builder, "headerExtractor")).getHeaderMappings().get("foo"));
	}

	@Test
	public void extractFromPayload_xpath() throws Exception {
		String path = "//ResultCode";
		String controlValue = "Success";
		MessageType messageType = MessageType.XML;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.extractFromPayload(path, controlValue);
		assertTrue(copy == this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());
		assertEquals("Success", ((XpathPayloadVariableExtractor)ReflectionTestUtils.getField(this.builder, "xpathExtractor")).getXpathExpressions().get("//ResultCode"));
	}

	@Test
	public void extractFromPayload_json() throws Exception {
		String path = "$ResultCode";
		String controlValue = "Success";
		MessageType messageType = MessageType.JSON;
		this.builder = new ReceiveMessageBuilder();
		this.builder.messageType(messageType);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.extractFromPayload(path, controlValue);
		assertTrue(copy == this.builder);
		assertNotNull(this.builder.getAction().getVariableExtractors());
		assertEquals(1, this.builder.getAction().getVariableExtractors().size());
		assertEquals("Success", ((JsonPathVariableExtractor)ReflectionTestUtils.getField(this.builder, "jsonPathExtractor")).getJsonPathExpressions().get("$ResultCode"));
	}
	
	@Test
	public void validationCallback() throws Exception {
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		ValidationCallback callback = mock(ValidationCallback.class);
		this.builder = new ReceiveMessageBuilder();
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.validationCallback(callback);
		assertTrue(copy == this.builder);
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
		assertEquals(callback, this.builder.getAction().getValidationCallback());
	}
	
	@Test
	public void withApplicationContext() throws Exception {
		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
		this.builder = new ReceiveMessageBuilder();
		ReflectionTestUtils.setField(this.builder, "applicationContext", mockApplicationContext);
		ReceiveMessageBuilder copy = (ReceiveMessageBuilder)this.builder.withApplicationContext(mockApplicationContext);
		assertTrue(copy == this.builder);
		assertEquals(mockApplicationContext, ReflectionTestUtils.getField(this.builder, "applicationContext"));
		ReflectionTestUtils.setField(this.builder, "applicationContext", null);
	}

}
