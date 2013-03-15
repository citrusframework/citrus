/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import static org.easymock.EasyMock.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.script.GroovyJsonMessageValidator;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.text.PlainTextMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.XpathPayloadVariableExtractor;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageDefinitionTest {
    
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    
    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    
    @Test
    public void testReceiveBuilder() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build());
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertTrue(((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).getMessage().getHeaders().containsKey("operation"));
    }
    
    @Test
    public void testReceiveBuilderWithPayloadString() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
    }
    
    @Test
    public void testReceiveBuilderWithPayloadResource() throws IOException {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload(resource);
            }
        };
        
        reset(resource);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("somePayload".getBytes())).once();
        replay(resource);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "somePayload");
        
        verify(resource);
    }
    
    @Test
    public void testReceiveBuilderWithReceiverName() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive("fooMessageReceiver")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("fooMessageReceiver", MessageReceiver.class)).andReturn(messageReceiver).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        verify(applicationContext);
    }
    
    @Test
    public void testReceiveBuilderWithTimeout() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .timeout(1000L);
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getReceiveTimeout(), 1000L);
    }
    
    @Test
    public void testReceiveBuilderWithHeaders() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("operation", "sayHello")
                    .header("foo", "bar");
                
                receive(messageReceiver)
                    .header("operation", "sayHello")
                    .header("foo", "bar")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 2);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("foo"));
        
        action = ((ReceiveMessageAction)builder.testCase().getActions().get(1));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("foo"));
    }
    
    @Test
    public void testReceiveBuilderWithHeaderData() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
                
                receive(messageReceiver)
                    .message(MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build())
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 2);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaderData(), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertNull(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaderResourcePath());
        
        action = ((ReceiveMessageAction)builder.testCase().getActions().get(1));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaderData(), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertNull(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaderResourcePath());
    }
    
    @Test
    public void testReceiveBuilderWithHeaderResource() throws IOException {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header(resource);
                
                receive(messageReceiver)
                    .message(MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build())
                    .header(resource);
            }
        };
        
        reset(resource);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("someHeaderData".getBytes())).once();
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("otherHeaderData".getBytes())).once();
        replay(resource);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 2);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaderData(), "someHeaderData");
        
        action = ((ReceiveMessageAction)builder.testCase().getActions().get(1));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaderData(), "otherHeaderData");
        
        verify(resource);
    }
    
    @Test
    public void testReceiveBuilderWithValidator() {
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();
        
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validator(validator);
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "TestMessage");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
    }
    
    @Test
    public void testReceiveBuilderWithValidatorName() {
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();
        
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validator("plainTextValidator");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("plainTextValidator", MessageValidator.class)).andReturn(validator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "TestMessage");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        
        verify(applicationContext);
    }
    
    @Test
    public void testReceiveBuilderWithSelector() {
        final Map<String, String> messageSelector = new HashMap<String, String>();
        messageSelector.put("operation", "sayHello");
        
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .selector(messageSelector);
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        Assert.assertEquals(action.getMessageSelector(), messageSelector);
    }
    
    @Test
    public void testReceiveBuilderWithSelectorExpression() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .selector("operation = 'sayHello'");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertEquals(action.getMessageSelectorString(), "operation = 'sayHello'");
    }
    
    @Test
    public void testReceiveBuilderExtractFromPayload() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromPayload("/TestRequest/Message", "text")
                    .extractFromPayload("/TestRequest/Message/@lang", "language");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getxPathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getxPathExpressions().containsKey("/TestRequest/Message/@lang"));
    }
    
    @Test
    public void testReceiveBuilderExtractFromHeader() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromHeader("operation", "ops")
                    .extractFromHeader("requestId", "id");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));
    }
    
    @Test
    public void testReceiveBuilderExtractCombined() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromHeader("operation", "ops")
                    .extractFromHeader("requestId", "id")
                    .extractFromPayload("/TestRequest/Message", "text")
                    .extractFromPayload("/TestRequest/Message/@lang", "language");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));
        
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(1)).getxPathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(1)).getxPathExpressions().containsKey("/TestRequest/Message/@lang"));
    }
    
    @Test
    public void testReceiveBuilderWithValidationCallback() {
        final ValidationCallback callback = EasyMock.createMock(ValidationCallback.class);
        
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validationCallback(callback);
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidationCallback(), callback);
        
        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "TestMessage");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
    }
    
    @Test
    public void testReceiveBuilderWithValidatonScript() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();
        
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.JSON)
                    .validateScript("assert true")
                    .validator("groovyMessageValidator");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("groovyMessageValidator", MessageValidator.class)).andReturn(validator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        ScriptValidationContext validationContext = (ScriptValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "assert true");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());
        
        verify(applicationContext);
    }
    
    @Test
    public void testReceiveBuilderWithValidatonScriptResource() throws IOException {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();
        
        File resourceFile = EasyMock.createMock(File.class);
        
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.JSON)
                    .validateScript(resource)
                    .validator("groovyMessageValidator");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext, resource, resourceFile);
        
        expect(applicationContext.getBean("groovyMessageValidator", MessageValidator.class)).andReturn(validator).once();
        
        expect(resource.getFile()).andReturn(resourceFile).once();
        expect(resourceFile.getAbsolutePath()).andReturn("/path/to/file/File.groovy").once();
        
        replay(applicationContext, resource, resourceFile);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        ScriptValidationContext validationContext = (ScriptValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "");
        Assert.assertEquals(validationContext.getValidationScriptResourcePath(), "/path/to/file/File.groovy");
        
        verify(applicationContext, resource, resourceFile);
    }
    
    @Test
    public void testReceiveBuilderWithValidatonScriptAndHeader() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();
        
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.JSON)
                    .validateScript("assert true")
                    .validator("groovyMessageValidator")
                    .header("operation", "sayHello");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("groovyMessageValidator", MessageValidator.class)).andReturn(validator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidator(), validator);
        
        Assert.assertEquals(action.getValidationContexts().size(), 2L);
        
        ScriptValidationContext validationContext = (ScriptValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "assert true");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());
        
        ControlMessageValidationContext headerValidationContext = (ControlMessageValidationContext) action.getValidationContexts().get(1);
        
        Assert.assertTrue(headerValidationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertNull(((PayloadTemplateMessageBuilder)headerValidationContext.getMessageBuilder()).getPayloadData());
        Assert.assertNull(((PayloadTemplateMessageBuilder)headerValidationContext.getMessageBuilder()).getPayloadResourcePath());
        Assert.assertTrue(((PayloadTemplateMessageBuilder)headerValidationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        
        verify(applicationContext);
    }
    
    @Test
    public void testReceiveBuilderWithNamespaceValidation() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                    .validateNamespace("pfx", "http://www.consol.de/schemas/test");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), 
                "<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getControlNamespaces().get("pfx"), "http://www.consol.de/schemas/test");
    }
    
    @Test
    public void testReceiveBuilderWithPathValidationExpressions() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .validate("Foo.operation", "foo")
                    .validate("Foo.message", "control");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(validationContext.getPathValidationExpressions().size(), 2L);
        Assert.assertEquals(validationContext.getPathValidationExpressions().get("Foo.operation"), "foo");
        Assert.assertEquals(validationContext.getPathValidationExpressions().get("Foo.message"), "control");
    }
}
