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

package com.consol.citrus.validation;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.builder.PayloadTemplateControlMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContextBuilder;

/**
 * @author Christoph Deppisch
 */
public class DTDValidationTest extends AbstractBaseTest {
    @Autowired
    MessageValidator<ValidationContext> validator;
    
    MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    ReceiveMessageAction receiveMessageBean;
    
    @Override
    @BeforeMethod
    public void setup() {
        super.setup();
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        
        receiveMessageBean.setValidator(validator);
    }
    
	@Test
	@SuppressWarnings("unchecked")
    public void testInlineDTD() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<!DOCTYPE root [ "
                + "<!ELEMENT root (message)>"
                + "<!ELEMENT message (text)>"
                + "<!ELEMENT text (#PCDATA)>"
                + " ]>"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateControlMessageBuilder controlMessageBuilder = new PayloadTemplateControlMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<!DOCTYPE root [ "
                + "<!ELEMENT root (message)>"
                + "<!ELEMENT message (text)>"
                + "<!ELEMENT text (#PCDATA)>"
                + " ]>"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExternalDTD() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<!DOCTYPE root SYSTEM \"com/consol/citrus/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateControlMessageBuilder controlMessageBuilder = new PayloadTemplateControlMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<!DOCTYPE root SYSTEM \"com/consol/citrus/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
}
