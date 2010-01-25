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

package com.consol.citrus.validation;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class DTDValidationTest extends AbstractBaseTest {
    @Autowired
    MessageValidator validator;
    
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
        
        receiveMessageBean.setMessageData("<!DOCTYPE root [ "
                + "<!ELEMENT root (message)>"
                + "<!ELEMENT message (text)>"
                + "<!ELEMENT text (#PCDATA)>"
                + " ]>"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");
        
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
        
        receiveMessageBean.setMessageData("<!DOCTYPE root SYSTEM \"com/consol/citrus/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");
        
        receiveMessageBean.execute(context);
    }
    
}
