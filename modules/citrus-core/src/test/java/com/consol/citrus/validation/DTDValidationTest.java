package com.consol.citrus.validation;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;

public class DTDValidationTest extends AbstractBaseTest {
    @Autowired
    XMLMessageValidator validator;
    
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
        
        expect(messageReceiver.receive(anyLong())).andReturn(message);
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
    public void testExternalDTD() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<!DOCTYPE root SYSTEM \"com/consol/citrus/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>").build();
        
        expect(messageReceiver.receive(anyLong())).andReturn(message);
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
