/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import static org.easymock.EasyMock.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.DefaultXMLMessageValidator;
import com.consol.citrus.validation.XmlValidationContext;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionTest extends AbstractBaseTest {
	
    private MessageSender messageSender = EasyMock.createMock(MessageSender.class);
    
    @Test
    @SuppressWarnings("unchecked")
	public void testSendMessageWithMessagePayloadData() {
		SendMessageAction sendAction = new SendMessageAction();
		sendAction.setMessageSender(messageSender);
		sendAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
		
		Map<String, Object> headers = new HashMap<String, Object>();
		final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
		                        .copyHeaders(headers)
		                        .build();
		
		reset(messageSender);
		
		messageSender.send((Message)anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
		
		replay(messageSender);
		
		sendAction.execute(context);
		
		verify(messageSender);
	}
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithMessagePayloadResource() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageResource(new ClassPathResource("test-request-payload.xml", SendMessageActionTest.class));
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithMessagePayloadDataVariablesSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithMessagePayloadResourceVariablesSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageResource(new ClassPathResource("test-request-payload-with-variables.xml", SendMessageActionTest.class));
        
        context.setVariable("myText", "Hello World!");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithMessagePayloadResourceFunctionsSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageResource(new ClassPathResource("test-request-payload-with-functions.xml", SendMessageActionTest.class));
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageOverwriteMessageElementsXPath() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/TestRequest/Message", "Hello World!");
        sendAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageOverwriteMessageElementsDotNotation() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("TestRequest.Message", "Hello World!");
        sendAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageOverwriteMessageElementsXPathWithNamespace() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
        		"<ns0:Message>?</ns0:Message></ns0:TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");
        sendAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\"><ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                validationContext.setSchemaValidation(false);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageOverwriteMessageElementsXPathWithDefaultNamespace() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/:TestRequest/:Message", "Hello World!");
        sendAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest xmlns=\"http://citrusframework.org/unittest\"><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                validationContext.setSchemaValidation(false);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithMessageHeaders() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        sendAction.setHeaderValues(headers);
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                validationContext.setExpectedMessageHeaders(new MessageHeaders(controlHeaders));
                validator.setFunctionRegistry(context.getFunctionRegistry());
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithHeaderValuesVariableSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myOperation", "sayHello");
        
        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        sendAction.setHeaderValues(headers);
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                validationContext.setExpectedMessageHeaders(new MessageHeaders(controlHeaders));
                validator.setFunctionRegistry(context.getFunctionRegistry());
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    public void testSendMessageWithUnknwonVariableInMessagePayload() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        reset(messageSender);
        replay(messageSender);
        
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }
    
    @Test
    public void testSendMessageWithUnknwonVariableInHeaders() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        sendAction.setHeaderValues(headers);
        
        reset(messageSender);
        replay(messageSender);
        
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable '${myOperation}'");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithExtractHeaderValues() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(controlHeaders)
                                .build();

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        sendAction.setHeaderValues(headers);
        
        Map<String, String> extractVars = new HashMap<String, String>();
        extractVars.put("Operation", "myOperation");
        extractVars.put("springintegration_id", "correlationId");
        sendAction.setExtractHeaderValues(extractVars);
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                validationContext.setExpectedMessageHeaders(new MessageHeaders(controlHeaders));
                validator.setFunctionRegistry(context.getFunctionRegistry());
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertNotNull(context.getVariable("correlationId"));
        
        verify(messageSender);
    }
    
    @Test
    public void testMissingMessagePayload() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        
        reset(messageSender);
        replay(messageSender);
        
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Could not find message data. Either message-data or message-resource must be specified");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " missing message payload");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithXmlDeclaration() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithUTF16Encoding() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithISOEncoding() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithUnsupportedEncoding() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageData("<?xml version=\"1.0\" encoding=\"MyUnsupportedEncoding\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        reset(messageSender);
        replay(messageSender);
        
        try {
            sendAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedEncodingException);
        }
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithMessagePayloadResourceISOEncoding() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageSender(messageSender);
        sendAction.setMessageResource(new ClassPathResource("test-request-iso-encoding.xml", SendMessageActionTest.class));
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
                XmlValidationContext validationContext = new XmlValidationContext();
                validationContext.setExpectedMessage(controlMessage);
                
                validator.validateMessage(((Message)EasyMock.getCurrentArguments()[0]), context, validationContext);
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendAction.execute(context);
        
        verify(messageSender);
    }
}
