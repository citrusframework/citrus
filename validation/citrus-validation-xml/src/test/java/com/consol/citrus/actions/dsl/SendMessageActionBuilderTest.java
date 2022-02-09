package com.consol.citrus.actions.dsl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.builder.MarshallingPayloadBuilder;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageProcessor;
import com.consol.citrus.xml.StringSource;
import org.mockito.Mockito;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.util.StringUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.XsdSchema;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;

import javax.xml.transform.Source;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.dsl.XmlSupport.xml;
import static com.consol.citrus.dsl.XpathSupport.xpath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionBuilderTest extends UnitTestSupport {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Producer messageProducer = Mockito.mock(Producer.class);

    private XStreamMarshaller marshaller = new XStreamMarshaller();

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.getXStream().processAnnotations(TestRequest.class);
    }

    @Test
    public void testSendBuilderWithPayloadModel() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
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
                .body(new MarshallingPayloadBuilder(new TestRequest("Hello Citrus!"))));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshaller() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .body(new MarshallingPayloadBuilder(new TestRequest("Hello Citrus!"), marshaller)));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshallerName() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
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
                .body(new MarshallingPayloadBuilder(new TestRequest("Hello Citrus!"), "myMarshaller")));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testXpathSupport() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                    "<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><TestRequest><Messagelang=\"ENG\">HelloWorld!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">?</Message></TestRequest>")
                .process(xpath()
                        .expression("/TestRequest/Message", "Hello World!")));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof XpathMessageProcessor);
        Assert.assertEquals(((XpathMessageProcessor)action.getMessageProcessors().get(0)).getXPathExpressions().get("/TestRequest/Message"), "Hello World!");

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
                .type(MessageType.JSON).body(new MarshallingPayloadBuilder(new TestRequest("Hello Citrus!"), marshaller)));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertTrue(action.isSchemaValidation());
        Assert.assertEquals(action.getSchema(), "fooSchema");
        Assert.assertEquals(action.getSchemaRepository(), "fooRepository");
    }

}
