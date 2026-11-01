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

package org.citrusframework.json.actions.dsl;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.networknt.schema.Schema;
import org.citrusframework.TestCase;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.json.UnitTestSupport;
import org.citrusframework.json.message.builder.ObjectMappingHeaderDataBuilder;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spring.context.SpringBeanReferenceResolver;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.json.JsonPathVariableExtractor;
import org.hamcrest.core.AnyOf;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static java.util.Collections.emptyList;
import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.message.MessageType.PLAINTEXT;
import static org.citrusframework.message.MessageType.XML;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

public class ReceiveMessageActionBuilderTest extends UnitTestSupport implements TestActionSupport {

    private final Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private final Consumer messageConsumer = Mockito.mock(Consumer.class);
    private final EndpointConfiguration configuration = Mockito.mock(EndpointConfiguration.class);
    private final Resource resource = Mockito.mock(Resource.class);
    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    private final JsonMapper mapper = JsonMapper.shared();

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
                .body(buildPayload().json().marshal(new TestRequest("Hello Citrus!"))));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}",
                ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
                .body(buildPayload().json().marshal(new TestRequest("Hello Citrus!"), mapper)));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));

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
                .body(buildPayload().json().marshal(new TestRequest("Hello Citrus!"), "myObjectMapper")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}",
                ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

    }

    @Test
    public void testReceiveBuilderWithHeaderResource() {
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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        Assertions.assertEquals(1L, ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderResource() {
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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(3L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>sayHello</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        Assertions.assertEquals(3L, ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>sayHello</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2));

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
                .type(JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(extractor().fromBody()
                        .expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("person"));
        Assertions.assertNotNull(context.getVariable("payload"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertEquals("{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}", context.getVariable("payload"));
        Assertions.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof DelegatingPayloadVariableExtractor);
        Assertions.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.text"));
        Assertions.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.person"));
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
                .type(JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(extractor().path()
                        .expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("person"));
        Assertions.assertNotNull(context.getVariable("payload"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertEquals("{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}", context.getVariable("payload"));
        Assertions.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof DelegatingPayloadVariableExtractor);
        Assertions.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.text"));
        Assertions.assertTrue(((DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(0)).getPathExpressions().containsKey("$.person"));
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
                .type(JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(extractor().jsonPath()
                        .expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("person"));
        Assertions.assertNotNull(context.getVariable("payload"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertEquals("{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}", context.getVariable("payload"));
        Assertions.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assertions.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assertions.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

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
                .type(JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\",\"active\": true}, \"index\":5, \"id\":\"x123456789x\"}")
                .validate(validation().jsonPath()
                        .expression("$.person.name", "John")
                        .expression("$.person.active", true)
                        .expression("$.id", anyOf(containsString("123456789"), nullValue()))
                        .expression("$.text", "Hello World!")
                        .expression("$.index", 5)));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(3, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonPathMessageValidationContext.class::isInstance));

        JsonPathMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonPathMessageValidationContext.class::isInstance).findFirst()
                .map(JsonPathMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(5L, validationContext.getJsonPathExpressions().size());
        Assertions.assertEquals("John", validationContext.getJsonPathExpressions().get("$.person.name"));
        Assertions.assertEquals(true, validationContext.getJsonPathExpressions().get("$.person.active"));
        Assertions.assertEquals("Hello World!", validationContext.getJsonPathExpressions().get("$.text"));
        Assertions.assertEquals(5, validationContext.getJsonPathExpressions().get("$.index"));
        Assertions.assertEquals(AnyOf.class, validationContext.getJsonPathExpressions().get("$.id").getClass());
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
                .type(JSON)
                .body("{\"text\":\"Citrus rocks!\", \"user\":\"christoph\"}")
                .validate(validation().jsonPath()
                        .expression("$.user", "christoph"))
                .validate(validation().jsonPath()
                        .expression("$.text", "Citrus rocks!")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(4, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assertions.assertEquals(2L, action.getValidationContexts().stream().filter(JsonPathMessageValidationContext.class::isInstance).count());

        Map<String, Object> jsonPathExpressions = action.getValidationContexts().stream()
                .filter(JsonPathMessageValidationContext.class::isInstance)
                .map(JsonPathMessageValidationContext.class::cast)
                .map(JsonPathMessageValidationContext::getJsonPathExpressions)
                .reduce((collect, map) -> {
                    collect.putAll(map);
                    return collect;
                })
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(2L, jsonPathExpressions.size());
        Assertions.assertEquals("christoph", jsonPathExpressions.get("$.user"));
        Assertions.assertEquals("Citrus rocks!", jsonPathExpressions.get("$.text"));
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
                .type(JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(extractor().jsonPath().expression("$.text", "text")
                        .expression("$.toString()", "payload")
                        .expression("$.person", "person")));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("person"));
        Assertions.assertNotNull(context.getVariable("payload"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertEquals("{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}", context.getVariable("payload"));
        Assertions.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assertions.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assertions.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.toString()"));
        Assertions.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

    }

    @Test
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
        Assertions.assertThrows(TestCaseFailedException.class, () -> runner.run(receive(messageEndpoint)
                .message()
                .type(JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .validate(validation().jsonPath()
                        .expression("$.person.name", "John")
                        .expression("$.text", "Hello Citrus!"))));
    }

    @Test
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
        Assertions.assertThrows(TestCaseFailedException.class, () -> runner.run(receive(messageEndpoint)
                .message()
                .type(JSON)
                .body("{\"text\":\"Hello Citrus!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .validate(validation().json()
                        .expression("$.person.name", "John")
                        .expression("$.text", "Hello World!"))));
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
                .type(JSON)
                .body("{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}")
                .validate(validation().json()
                        .ignore("$..text")
                        .ignore("$.person.surname")
                        .ignore("$.index")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(JSON.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(3L, validationContext.getIgnoreExpressions().size());
        Assertions.assertTrue(validationContext.getIgnoreExpressions().contains("$..text"));
        Assertions.assertTrue(validationContext.getIgnoreExpressions().contains("$.person.surname"));
        Assertions.assertTrue(validationContext.getIgnoreExpressions().contains("$.index"));

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

        Schema jsonSchemaMock = mock(Schema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(emptyList());
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(validation().json()
                        .schemaRepository("customJsonSchemaRepository")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("{}", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals("customJsonSchemaRepository", validationContext.getSchemaRepository());

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

        Schema jsonSchemaMock = mock(Schema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(emptyList());
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(validation().json()
                        .schema("jsonTestSchema")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("{}", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals("jsonTestSchema", validationContext.getSchema());

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

        Schema jsonSchemaMock = mock(Schema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(emptyList());
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(validation().json()
                        .schemaValidation(true)));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext jsonMessageValidationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assertions.assertTrue(jsonMessageValidationContext.isSchemaValidationEnabled());

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
                .validate(validation().json()
                        .schemaValidation(false)));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext jsonMessageValidationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assertions.assertFalse(jsonMessageValidationContext.isSchemaValidationEnabled());
    }
}
