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

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
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
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build());
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
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
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
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
    public void testReceiveBuilderWithPayloadResource() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload(resource);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadResource(), resource);
    }
    
    @Test
    public void testReceiveBuilderWithReceiverName() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
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
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        verify(applicationContext);
    }
    
    @Test
    public void testReceiveBuilderWithTimeout() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .timeout(1000L);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getReceiveTimeout(), 1000L);
    }
    
    @Test
    public void testReceiveBuilderWithHeaders() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
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
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 2);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(1).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("foo"));
        
        action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(1));
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
    public void testReceiveBuilderWithValidator() {
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();
        
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validator(validator);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
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
        
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
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
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
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
        
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .selector(messageSelector);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        Assert.assertEquals(action.getMessageSelector(), messageSelector);
    }
    
    @Test
    public void testReceiveBuilderWithSelectorExpression() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .selector("operation = 'sayHello'");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertEquals(action.getMessageSelectorString(), "operation = 'sayHello'");
    }
    
    @Test
    public void testReceiveBuilderExtractFromPayload() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromPayload("/TestRequest/Message", "text")
                    .extractFromPayload("/TestRequest/Message/@lang", "language");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
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
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromHeader("operation", "ops")
                    .extractFromHeader("requestId", "id");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
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
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
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
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
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
        
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validationCallback(callback);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidationCallback(), callback);
        
        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "TestMessage");
        Assert.assertTrue(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getMessageHeaders().containsKey("operation"));
    }
}
