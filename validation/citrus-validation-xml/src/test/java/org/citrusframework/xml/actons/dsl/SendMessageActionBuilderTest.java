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

package org.citrusframework.xml.actons.dsl;

import java.util.Collections;
import java.util.HashMap;

import org.citrusframework.TestCase;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.api.xml.Marshaller;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Producer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.XpathMessageProcessor;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.xml.UnitTestSupport;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SendMessageActionBuilderTest extends UnitTestSupport implements TestActionSupport {

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private Endpoint messageEndpoint;
    @Mock
    private Producer messageProducer;

    private final Marshaller marshaller = new Jaxb2Marshaller(TestRequest.class);

    @BeforeEach
    public void prepareMarshaller() {
        ((Jaxb2Marshaller) marshaller).setProperty(jakarta.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    }

    @Test
    public void testSendBuilderWithPayloadModel() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.singletonMap("marshaller", marshaller));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .body(buildPayload().xml().marshal(new TestRequest("Hello Citrus!"))));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshaller() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .body(buildPayload().xml().marshal(new TestRequest("Hello Citrus!"), marshaller)));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshallerName() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .body(buildPayload().xml().marshal(new TestRequest("Hello Citrus!"), "myMarshaller")));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
    }

    @Test
    public void testSendBuilderExtractFromPayload() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(send().endpoint(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                .extract(extractor().xpath().expression("/TestRequest/Message", "text")
                                .expression("/TestRequest/Message/@lang", "language")));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("language"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertEquals("ENG", context.getVariable("language"));

        final TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assertions.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assertions.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));
    }

    @Test
    public void testXpathSupport() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><TestRequest><Messagelang=\"ENG\">HelloWorld!</Message></TestRequest>",
                    message.getPayload(String.class).replaceAll("\\s", ""));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">?</Message></TestRequest>")
                .process(processor()
                        .xpath()
                        .expression("/TestRequest/Message", "Hello World!")));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1, action.getMessageProcessors().size());
        Assertions.assertTrue(action.getMessageProcessors().get(0) instanceof XpathMessageProcessor);
        Assertions.assertEquals("Hello World!", ((XpathMessageProcessor)action.getMessageProcessors().get(0)).getXPathExpressions().get("/TestRequest/Message"));

    }

    @Test
    public void testSendWithSchemaValidation() {

        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message()
                .schemaValidation(true).schema("fooSchema").schemaRepository("fooRepository")
                .type(MessageType.JSON).body(buildPayload().xml().marshal(new TestRequest("Hello Citrus!"), marshaller)));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertTrue(action.isSchemaValidation());
        Assertions.assertEquals("fooSchema", action.getSchema());
        Assertions.assertEquals("fooRepository", action.getSchemaRepository());
    }
}
