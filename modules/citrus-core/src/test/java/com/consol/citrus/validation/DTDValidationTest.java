package com.consol.citrus.validation;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.actions.ReceiveMessageBean;
import com.consol.citrus.message.XMLMessage;
import com.consol.citrus.service.Service;

public class DTDValidationTest extends AbstractBaseTest {
    @Autowired
    XMLMessageValidator validator;
    
    Service service = EasyMock.createMock(Service.class);
    
    ReceiveMessageBean receiveMessageBean;
    
    @Override
    @BeforeMethod
    public void setup() {
        super.setup();
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
    }
    
    @Test
    public void testInlineDTD() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<!DOCTYPE root [ "
                + "<!ELEMENT root (message)>"
                + "<!ELEMENT message (text)>"
                + "<!ELEMENT text (#PCDATA)>"
                + " ]>"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
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
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<!DOCTYPE root SYSTEM \"com/consol/citrus/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<!DOCTYPE root SYSTEM \"com/consol/citrus/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");
        
        receiveMessageBean.execute(context);
    }
    
}
