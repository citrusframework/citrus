/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.validation.xhtml;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class XhtmlMessageValidatorTest extends AbstractTestNGUnitTest {
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Consumer consumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    private ReceiveMessageAction receiveMessageBean;

    private XhtmlMessageValidator xhtmlMessageValidator = new XhtmlMessageValidator();
    
    @Override
    @BeforeMethod
    public void prepareTest() {
        super.prepareTest();
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);
        
        try {
            xhtmlMessageValidator.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        receiveMessageBean.setValidator(xhtmlMessageValidator);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testXhtmlConversion() throws Exception {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);
        
        Message message = new DefaultMessage("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
                        + "<html>"
                            + "<body>"
                                + "<p>Hello TestFramework!</p>"
                            + "</body>"
                        + "</html>");
        
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                            + "<head>"
                                + "<title/>"
                            + "</head>"
                            + "<body>"
                                + "<p>Hello TestFramework!</p>"
                            + "</body>"
                        + "</html>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testXhtmlValidation() throws Exception {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);
        
        Message message = new DefaultMessage("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                            + "<head>"
                                + "<title>Sample XHTML content</title>"
                            + "</head>"    
                            + "<body>"
                                + "<p>Hello TestFramework!</p>"
                            + "</body>"
                        + "</html>");
        
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                            + "<head>"
                                + "<title>Sample XHTML content</title>"
                            + "</head>"
                            + "<body>"
                                + "<p>Hello TestFramework!</p>"
                            + "</body>"
                        + "</html>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
}
