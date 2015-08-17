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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.TestRequest;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.script.GroovyJsonMessageValidator;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.text.PlainTextMessageValidator;
import com.consol.citrus.validation.xml.*;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.XsdSchema;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;

import javax.xml.transform.Source;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageTestRunnerTest extends AbstractTestNGUnitTest {
    
    private Endpoint messageEndpoint = EasyMock.createMock(Endpoint.class);
    private Consumer messageConsumer = EasyMock.createMock(Consumer.class);
    private EndpointConfiguration configuration = EasyMock.createMock(EndpointConfiguration.class);
    private Resource resource = EasyMock.createMock(Resource.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    private XStreamMarshaller marshaller = new XStreamMarshaller();

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.getXStream().processAnnotations(TestRequest.class);
    }

    @Test
    public void testReceiveEmpty() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage()).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 0);

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilder() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage("Foo").setHeader("operation", "foo")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.PLAINTEXT)
                                .message(new DefaultMessage("Foo").setHeader("operation", "foo"));
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), ControlMessageValidationContext.class);

        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertNotNull(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getHeader("operation"));

        verify(messageEndpoint, messageConsumer, configuration);

    }

    @Test
    public void testReceiveBuilderWithPayloadModel() {
        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        expect(applicationContextMock.getBean(Marshaller.class)).andReturn(marshaller).once();
        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payloadModel(new TestRequest("Hello Citrus!"));
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithPayloadModelExplicitMarshaller() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload(new TestRequest("Hello Citrus!"), marshaller);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithPayloadModelExplicitMarshallerName() {
        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        expect(applicationContextMock.getBean("myMarshaller", Marshaller.class)).andReturn(marshaller).once();
        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload(new TestRequest("Hello Citrus!"), "myMarshaller");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithPayloadString() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

        verify(messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithPayloadResource() throws IOException {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes())).once();
        replay(resource, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload(resource);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        verify(resource, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithEndpointName() {
        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("fooMessageEndpoint", Endpoint.class)).andReturn(messageEndpoint).atLeastOnce();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("fooMessageEndpoint")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithTimeout() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .timeout(1000L);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getReceiveTimeout(), 1000L);

        verify(messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithHeaders() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).times(2);
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")
                        .setHeader("foo", "bar")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("operation", "sayHello")
                                .header("foo", "bar");
                    }
                });

                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .header("operation", "sayHello")
                                .header("foo", "bar")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("foo"));

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("foo"));

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithHeaderData() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).times(2);
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
                    }
                });

                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                                .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().size(), 1L);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderResources().size(), 0L);

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().size(), 1L);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderResources().size(), 0L);

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderData() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).times(2);
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo2</Value></Header>")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                                .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");
                    }
                });

                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                                .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                                .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().size(), 2L);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderResources().size(), 0L);

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().size(), 2L);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderResources().size(), 0L);

        verify(messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithHeaderResource() throws IOException {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).times(2);
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>")).once();

        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "bar")
                        .addHeaderData("<Header><Name>operation</Name><Value>bar</Value></Header>")).once();

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes())).once();
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes())).once();
        replay(resource, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header(resource);
                    }
                });

                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                                .header(resource);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().size(), 1L);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        
        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().size(), 1L);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>bar</Value></Header>");
        
        verify(resource, messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderResource() throws IOException {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).times(2);
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>bar</Value></Header>")).times(2);

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes())).once();
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes())).once();
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes())).once();
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes())).once();
        replay(resource, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                                .header(resource)
                                .header(resource);
                    }
                });

                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                                .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                                .header(resource)
                                .header(resource);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getHeaderData().size(), 3L);
        Assert.assertEquals(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getHeaderData().get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder) validationContext.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageContentBuilder) validationContext.getMessageBuilder()).getHeaderData().size(), 3L);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getHeaderData().get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

        verify(resource, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithValidator() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        final PlainTextMessageValidator validator = new PlainTextMessageValidator();

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.PLAINTEXT)
                                .payload("TestMessage")
                                .header("operation", "sayHello")
                                .validator(validator);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidator(), validator);

        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getPayloadData(), "TestMessage");
        Assert.assertTrue(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));

        verify(messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithValidatorName() {
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();

        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("plainTextValidator", MessageValidator.class)).andReturn(validator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.PLAINTEXT)
                                .payload("TestMessage")
                                .header("operation", "sayHello")
                                .validator("plainTextValidator");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidator(), validator);

        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "TestMessage");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));

        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithSelector() {
        SelectiveConsumer selectiveConsumer = EasyMock.createMock(SelectiveConsumer.class);

        reset(messageEndpoint, selectiveConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(selectiveConsumer).times(2);
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(selectiveConsumer.receive(eq("operation = 'sayHello'"), anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")).atLeastOnce();
        replay(messageEndpoint, selectiveConsumer, configuration);

        final Map<String, String> messageSelector = new HashMap<>();
        messageSelector.put("operation", "sayHello");

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .selector(messageSelector);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getMessageSelector(), messageSelector);

        verify(messageEndpoint, selectiveConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithSelectorExpression() {
        SelectiveConsumer selectiveConsumer = EasyMock.createMock(SelectiveConsumer.class);

        reset(messageEndpoint, selectiveConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(selectiveConsumer).times(2);
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(selectiveConsumer.receive(eq("operation = 'sayHello'"), anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")).atLeastOnce();
        replay(messageEndpoint, selectiveConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .selector("operation = 'sayHello'");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertEquals(action.getMessageSelectorString(), "operation = 'sayHello'");

        verify(messageEndpoint, selectiveConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderExtractFromPayload() {
        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .extractFromPayload("/TestRequest/Message", "text")
                                .extractFromPayload("/TestRequest/Message/@lang", "language");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("language"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("language"), "ENG");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderExtractJsonPathFromPayload() {
        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .extractFromPayload("$.text", "text")
                                .extractFromPayload("$.person", "person");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
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
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderExtractFromHeader() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")
                        .setHeader("requestId", "123456")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .extractFromHeader("operation", "operationHeader")
                                .extractFromHeader("requestId", "id");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

        verify(messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderExtractCombined() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")
                        .setHeader("requestId", "123456")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .extractFromHeader("operation", "operationHeader")
                                .extractFromHeader("requestId", "id")
                                .extractFromPayload("/TestRequest/Message", "text")
                                .extractFromPayload("/TestRequest/Message/@lang", "language");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("language"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("language"), "ENG");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

        verify(messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithValidationCallback() {
        final ValidationCallback callback = EasyMock.createMock(ValidationCallback.class);

        reset(callback, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello")).atLeastOnce();

        callback.setApplicationContext(applicationContext);
        expectLastCall().once();
        callback.validate(anyObject(Message.class));
        expectLastCall().once();
        replay(callback, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.PLAINTEXT)
                                .payload("TestMessage")
                                .header("operation", "sayHello")
                                .validationCallback(callback);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidationCallback(), callback);

        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getPayloadData(), "TestMessage");
        Assert.assertTrue(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));

        verify(callback, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithValidatonScript() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("groovyMessageValidator", MessageValidator.class)).andReturn(validator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .validateScript("assert json.message == 'Hello Citrus!'")
                                .validator("groovyMessageValidator");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        ScriptValidationContext validationContext = (ScriptValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());
        
        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithValidatonScriptResource() throws IOException {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();
        final File file = EasyMock.createMock(File.class);

        reset(resource, file, applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello")).atLeastOnce();

        expect(resource.getFile()).andReturn(file).once();
        expect(file.getAbsolutePath()).andReturn("classpath:com/consol/citrus/dsl/runner/validation.groovy").once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("groovyMessageValidator", MessageValidator.class)).andReturn(validator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(resource, file, applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .validateScript(resource)
                                .validator("groovyMessageValidator");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        ScriptValidationContext validationContext = (ScriptValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "");
        Assert.assertEquals(validationContext.getValidationScriptResourcePath(), "classpath:com/consol/citrus/dsl/runner/validation.groovy");
        
        verify(resource, file, applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithValidatonScriptAndHeader() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(applicationContextMock, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("groovyMessageValidator", MessageValidator.class)).andReturn(validator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .validateScript("assert json.message == 'Hello Citrus!'")
                                .validator("groovyMessageValidator")
                                .header("operation", "sayHello");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        Assert.assertEquals(action.getValidationContexts().size(), 2L);
        
        ScriptValidationContext validationContext = (ScriptValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());
        
        ControlMessageValidationContext headerValidationContext = (ControlMessageValidationContext) action.getValidationContexts().get(1);
        
        Assert.assertTrue(headerValidationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertNull(((PayloadTemplateMessageBuilder)headerValidationContext.getMessageBuilder()).getPayloadData());
        Assert.assertNull(((PayloadTemplateMessageBuilder)headerValidationContext.getMessageBuilder()).getPayloadResourcePath());
        Assert.assertTrue(((PayloadTemplateMessageBuilder)headerValidationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        
        verify(applicationContextMock, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithNamespaceValidation() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")).atLeastOnce();
        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                                .validateNamespace("pfx", "http://www.consol.de/schemas/test");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(),
                "<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getControlNamespaces().get("pfx"), "http://www.consol.de/schemas/test");

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithXpathExpressions() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message><Operation>SayHello</Operation></TestRequest>")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message><Operation>SayHello</Operation></TestRequest>")
                                .validate("TestRequest.Message", "Hello World!")
                                .validate("TestRequest.Operation", "SayHello");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XpathMessageValidationContext.class);

        XpathMessageValidationContext validationContext = (XpathMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(validationContext.getXpathExpressions().size(), 2L);
        Assert.assertEquals(validationContext.getXpathExpressions().get("TestRequest.Message"), "Hello World!");
        Assert.assertEquals(validationContext.getXpathExpressions().get("TestRequest.Operation"), "SayHello");

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithJsonPathExpressions() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .validate("$.person.name", "John")
                                .validate("$.text", "Hello World!");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), JsonMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), JsonPathMessageValidationContext.class);

        JsonPathMessageValidationContext validationContext = (JsonPathMessageValidationContext) action.getValidationContexts().get(1);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(validationContext.getJsonPathExpressions().size(), 2L);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.person.name"), "John");
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.text"), "Hello World!");

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testReceiveBuilderWithJsonPathExpressionsFailure() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        try {
            new MockTestRunner(getClass().getSimpleName(), applicationContext) {
                @Override
                public void execute() {
                    receive(new BuilderSupport<ReceiveMessageBuilder>() {
                        @Override
                        public void configure(ReceiveMessageBuilder builder) {
                            builder.endpoint(messageEndpoint)
                                    .messageType(MessageType.JSON)
                                    .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                    .validate("$.person.name", "John")
                                    .validate("$.text", "Hello Citrus!");
                        }
                    });
                }
            };
        } finally {
            verify(messageEndpoint, messageConsumer, configuration);
        }
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testReceiveBuilderWithJsonValidationFailure() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        try {
            new MockTestRunner(getClass().getSimpleName(), applicationContext) {
                @Override
                public void execute() {
                    receive(new BuilderSupport<ReceiveMessageBuilder>() {
                        @Override
                        public void configure(ReceiveMessageBuilder builder) {
                            builder.endpoint(messageEndpoint)
                                    .messageType(MessageType.JSON)
                                    .payload("{\"text\":\"Hello Citrus!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                    .validate("$.person.name", "John")
                                    .validate("$.text", "Hello World!");
                        }
                    });
                }
            };
        } finally {
            verify(messageEndpoint, messageConsumer, configuration);
        }
    }
    
    @Test
    public void testReceiveBuilderWithIgnoreElementsXpath() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>?</Message></TestRequest>")
                                .ignore("TestRequest.Message");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>?</Message></TestRequest>");
        Assert.assertEquals(validationContext.getIgnoreExpressions().size(), 1L);
        Assert.assertEquals(validationContext.getIgnoreExpressions().iterator().next(), "TestRequest.Message");

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithIgnoreElementsJson() {
        reset(messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        replay(messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}")
                                .ignore("$..text")
                                .ignore("$.person.surname")
                                .ignore("$.index");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), JsonMessageValidationContext.class);

        JsonMessageValidationContext validationContext = (JsonMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getPayloadData(), "{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}");
        Assert.assertEquals(validationContext.getIgnoreExpressions().size(), 3L);
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$..text"));
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$.person.surname"));
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$.index"));

        verify(messageEndpoint, messageConsumer, configuration);
    }

    @Test
    public void testReceiveBuilderWithSchema() throws IOException {
        XsdSchema schema = applicationContext.getBean("testSchema", XsdSchema.class);
        XmlValidator validator = EasyMock.createMock(XmlValidator.class);

        reset(applicationContextMock, schema, validator, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        expect(schema.createValidator()).andReturn(validator).once();
        expect(validator.validate(anyObject(Source.class))).andReturn(new SAXParseException[] {}).once();

        replay(applicationContextMock, schema, validator, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                                .xsd("testSchema");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder) validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getSchema(), "testSchema");

        verify(applicationContextMock, schema, validator, messageEndpoint, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithSchemaRepository() throws IOException {
        XsdSchema schema = applicationContext.getBean("testSchema", XsdSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageEndpoint.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(
                new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")).atLeastOnce();

        expect(schema.getTargetNamespace()).andReturn("http://citrusframework.org/test").atLeastOnce();
        expect(schema.getSource()).andReturn(new StringSource("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "     xmlns=\"http://citrusframework.org/test\"\n" +
                "     targetNamespace=\"http://citrusframework.org/test\"\n" +
                "     elementFormDefault=\"qualified\"\n" +
                "     attributeFormDefault=\"unqualified\">\n" +
                "    <xs:element name=\"TestRequest\">\n" +
                "      <xs:complexType>\n" +
                "          <xs:sequence>\n" +
                "            <xs:element name=\"Message\" type=\"xs:string\"/>\n" +
                "          </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "    </xs:element></xs:schema>")).once();
        replay(schema, messageEndpoint, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                                .xsdSchemaRepository("customSchemaRepository");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getSchemaRepository(), "customSchemaRepository");

        verify(schema, messageEndpoint, messageConsumer, configuration);
    }
}
