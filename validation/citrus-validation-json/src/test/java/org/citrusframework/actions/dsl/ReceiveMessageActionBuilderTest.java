/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.actions.dsl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.ObjectMappingHeaderDataBuilder;
import org.citrusframework.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.json.JsonPathVariableExtractor;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.hamcrest.core.AnyOf;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.dsl.JsonSupport.json;
import static org.citrusframework.dsl.MessageSupport.MessageBodySupport.fromBody;
import static org.citrusframework.dsl.PathExpressionSupport.path;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionBuilderTest extends UnitTestSupport {

    private final Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private final Consumer messageConsumer = Mockito.mock(Consumer.class);
    private final EndpointConfiguration configuration = Mockito.mock(EndpointConfiguration.class);
    private final Resource resource = Mockito.mock(Resource.class);
    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testReceiveBuilderWithPayloadModel() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"message\": \"Hello Citrus!\"}")
                        .setHeader("operation", "foo"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(ObjectMapper.class)).thenReturn(Collections.<String, ObjectMapper>singletonMap("mapper", mapper));
        when(referenceResolver.resolve(ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .body(new ObjectMappingPayloadBuilder(new TestRequest("Hello Citrus!"))));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()),
                "{\"message\":\"Hello Citrus!\"}");
    }

    @Test
    public void testReceiveBuilderWithPayloadModelExplicitObjectMapper() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"message\": \"Hello Citrus!\"}")
                        .setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .body(new ObjectMappingPayloadBuilder(new TestRequest("Hello Citrus!"), mapper)));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{\"message\":\"Hello Citrus!\"}");

    }

    @Test
    public void testReceiveBuilderWithPayloadModelExplicitObjectMapperName() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"message\": \"Hello Citrus!\"}")
                        .setHeader("operation", "foo"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myObjectMapper")).thenReturn(true);
        when(referenceResolver.resolve("myObjectMapper", ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .body(new ObjectMappingPayloadBuilder(new TestRequest("Hello Citrus!"), "myObjectMapper")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{\"message\":\"Hello Citrus!\"}");

    }

    @Test
    public void testReceiveBuilderWithHeaderFragment() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage()
                        .addHeaderData("{\"message\": \"Hello Citrus!\"}")
                        .setHeader("operation", "foo"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(ObjectMapper.class)).thenReturn(Collections.<String, ObjectMapper>singletonMap("mapper", mapper));
        when(referenceResolver.resolve(ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .header(new ObjectMappingHeaderDataBuilder(new TestRequest("Hello Citrus!"))));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0),
                "{\"message\":\"Hello Citrus!\"}");

    }

    @Test
    public void testReceiveBuilderWithHeaderFragmentExplicitObjectMapper() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage()
                        .addHeaderData("{\"message\": \"Hello Citrus!\"}")
                        .setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .header(new ObjectMappingHeaderDataBuilder(new TestRequest("Hello Citrus!"), mapper)));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "{\"message\":\"Hello Citrus!\"}");

    }

    @Test
    public void testReceiveBuilderWithHeaderFragmentExplicitObjectMapperName() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage()
                        .addHeaderData("{\"message\": \"Hello Citrus!\"}")
                        .setHeader("operation", "foo"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myObjectMapper")).thenReturn(true);
        when(referenceResolver.resolve("myObjectMapper", ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .header(new ObjectMappingHeaderDataBuilder(new TestRequest("Hello Citrus!"), "myObjectMapper")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "{\"message\":\"Hello Citrus!\"}");

    }

    @Test
    public void testReceiveBuilderWithHeaderResource() throws IOException {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong()))
                .thenReturn(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>"))
                .thenReturn(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "bar")
                        .addHeaderData("<Header><Name>operation</Name><Value>bar</Value></Header>"));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header(resource));

        runner.run(receive(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header(resource));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>bar</Value></Header>");

    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderResource() throws IOException {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>bar</Value></Header>"));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                        .header(resource)
                        .header(resource));

        runner.run(receive(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                        .header(resource)
                        .header(resource));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 3L);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 3L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

    }

    @Test
    public void testReceiveBuilderExtractFromBody() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(fromBody()
                        .expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertNotNull(context.getVariable("payload"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("payload"), "{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}");
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof DelegatingPayloadVariableExtractor);
        Assert.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.person"));
    }

    @Test
    public void testReceiveBuilderExtractFromPathExpression() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(path()
                        .expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertNotNull(context.getVariable("payload"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("payload"), "{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}");
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof DelegatingPayloadVariableExtractor);
        Assert.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.person"));
    }

    @Test
    public void testReceiveBuilderExtractJsonPathFromJsonPathExpression() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(jsonPath()
                        .expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertNotNull(context.getVariable("payload"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("payload"), "{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}");
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

    }

    @Test
    public void testReceiveBuilderWithJsonPathExpressions() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\",\"active\": true}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\",\"active\": true}, \"index\":5, \"id\":\"x123456789x\"}")
                                .validate(jsonPath()
                                        .expression("$.person.name", "John")
                                        .expression("$.person.active", true)
                                        .expression("$.id", anyOf(containsString("123456789"), nullValue()))
                                        .expression("$.text", "Hello World!")
                                        .expression("$.index", 5)));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonPathMessageValidationContext.class::isInstance));

        JsonPathMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonPathMessageValidationContext.class::isInstance).findFirst()
                .map(JsonPathMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(validationContext.getJsonPathExpressions().size(), 5L);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.person.name"), "John");
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.person.active"), true);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.text"), "Hello World!");
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.index"), 5);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.id").getClass(), AnyOf.class);
    }

    @Test
    public void testReceiveBuilderWithMultipleJsonPathExpressions() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Citrus rocks!\", \"user\": \"christoph\"}")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .body("{\"text\":\"Citrus rocks!\", \"user\":\"christoph\"}")
                                .validate(jsonPath()
                                        .expression("$.user", "christoph"))
                                .validate(jsonPath()
                                        .expression("$.text", "Citrus rocks!")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assert.assertEquals(action.getValidationContexts().stream().filter(JsonPathMessageValidationContext.class::isInstance).count(), 2L);

        Map<String, Object> jsonPathExpressions = action.getValidationContexts().stream()
                .filter(JsonPathMessageValidationContext.class::isInstance)
                .map(JsonPathMessageValidationContext.class::cast)
                .map(JsonPathMessageValidationContext::getJsonPathExpressions)
                .reduce((collect, map) -> {
                    collect.putAll(map);
                    return collect;
                })
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(jsonPathExpressions.size(), 2L);
        Assert.assertEquals(jsonPathExpressions.get("$.user"), "christoph");
        Assert.assertEquals(jsonPathExpressions.get("$.text"), "Citrus rocks!");
    }

    @Test
    public void testReceiveBuilderExtractJsonPathFromPayload() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(receive().endpoint(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(jsonPath().expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertNotNull(context.getVariable("payload"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("payload"), "{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}");
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.toString()"));
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testReceiveBuilderWithJsonPathExpressionsFailure() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .validate(jsonPath()
                                        .expression("$.person.name", "John")
                                        .expression("$.text", "Hello Citrus!")));
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testReceiveBuilderWithJsonValidationFailure() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .body("{\"text\":\"Hello Citrus!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .validate(json()
                                        .expression("$.person.name", "John")
                                        .expression("$.text", "Hello World!")));
    }

    @Test
    public void testReceiveBuilderWithIgnoreElementsJson() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .body("{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}")
                                .validate(json()
                                        .ignore("$..text")
                                        .ignore("$.person.surname")
                                        .ignore("$.index")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}");
        Assert.assertEquals(validationContext.getIgnoreExpressions().size(), 3L);
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$..text"));
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$.person.surname"));
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$.index"));

    }

    @Test
    public void testReceiveBuilderWithJsonSchemaRepository() {
        SimpleJsonSchema schema = applicationContext.getBean("jsonTestSchema", SimpleJsonSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        JsonSchema jsonSchemaMock = mock(JsonSchema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(Collections.emptySet());
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(json()
                        .schemaRepository("customJsonSchemaRepository")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{}");
        Assert.assertEquals(validationContext.getSchemaRepository(), "customJsonSchemaRepository");

    }

    @Test
    public void testReceiveBuilderWithJsonSchema() {
        SimpleJsonSchema schema = applicationContext.getBean("jsonTestSchema", SimpleJsonSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        JsonSchema jsonSchemaMock = mock(JsonSchema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(Collections.emptySet());
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(json()
                        .schema("jsonTestSchema")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{}");
        Assert.assertEquals(validationContext.getSchema(), "jsonTestSchema");

    }

    @Test
    public void testActivateSchemaValidation() {
        SimpleJsonSchema schema = applicationContext.getBean("jsonTestSchema", SimpleJsonSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        JsonSchema jsonSchemaMock = mock(JsonSchema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(Collections.emptySet());
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(json()
                        .schemaValidation(true)));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext jsonMessageValidationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assert.assertTrue(jsonMessageValidationContext.isSchemaValidationEnabled());

    }

    @Test
    public void testDeactivateSchemaValidation() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(json()
                        .schemaValidation(false)));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext jsonMessageValidationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assert.assertFalse(jsonMessageValidationContext.isSchemaValidationEnabled());
    }
}
