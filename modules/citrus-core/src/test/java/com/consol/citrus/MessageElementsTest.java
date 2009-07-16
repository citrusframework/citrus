package com.consol.citrus;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageBean;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.XMLMessage;
import com.consol.citrus.service.Service;
import com.consol.citrus.validation.XMLMessageValidator;

public class MessageElementsTest extends AbstractBaseTest {
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
    public void testValidateMessageElements() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA", "text-value");
        validateMessageElements.put("//sub-elementB", "text-value");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testValidateEmptyMessageElements() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'></sub-elementA>"
                            + "<sub-elementB attribute='B'></sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA", "");
        validateMessageElements.put("//sub-elementB", "");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testValidateEmptyMessageAttributes() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute=''>text-value</sub-elementA>"
                            + "<sub-elementB attribute=''>text-value</sub-elementB>"
                            + "<sub-elementC attribute=''>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute", "");
        validateMessageElements.put("//root/element/sub-elementB/@attribute", "");
        validateMessageElements.put("//root/element/sub-elementC/@attribute", "");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateNullElements() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'></sub-elementA>"
                            + "<sub-elementB attribute='B'></sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA", "null");
        validateMessageElements.put("//sub-elementB", "null");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testValidateMessageElementAttributes() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute", "A");
        validateMessageElements.put("//sub-elementB/@attribute", "B");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testValidateMessageElementsWrongExpectedElement() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-element-wrong", "text-value");
        validateMessageElements.put("//sub-element-wrong", "text-value");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWrongExpectedValue() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA", "text-value-wrong");
        validateMessageElements.put("//sub-elementB", "text-value-wrong");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementAttributesWrongExpectedValue() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute", "wrong-value");
        validateMessageElements.put("//sub-elementB/@attribute", "wrong-value");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testValidateMessageElementAttributesWrongExpectedAttribute() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA/@attribute-wrong", "A");
        validateMessageElements.put("//sub-elementB/@attribute-wrong", "B");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testSetMessageElements() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
                            + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        HashMap<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("//root/element/sub-elementA", "text-value");
        messageElements.put("//sub-elementB", "text-value");
        
        receiveMessageBean.setMessageElements(messageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testSetMessageElementsUsingEmptyString() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'></sub-elementA>"
                            + "<sub-elementB attribute='B'></sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
                            + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        HashMap<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("//root/element/sub-elementA", "");
        messageElements.put("//sub-elementB", "");
        
        receiveMessageBean.setMessageElements(messageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testSetMessageElementsAndValidate() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
                            + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        HashMap<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("//root/element/sub-elementA", "text-value");
        messageElements.put("//sub-elementB", "text-value");
        
        receiveMessageBean.setMessageElements(messageElements);
        
        HashMap<String, String> validateElements = new HashMap<String, String>();
        validateElements.put("//root/element/sub-elementA", "text-value");
        validateElements.put("//sub-elementB", "text-value");
        
        receiveMessageBean.setValidateMessageElements(validateElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testSetMessageElementAttributes() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='to be overwritten'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='to be overwritten'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        HashMap<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("//root/element/sub-elementA/@attribute", "A");
        messageElements.put("//sub-elementB/@attribute", "B");
        
        receiveMessageBean.setMessageElements(messageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testSetMessageElementsError() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>to be overwritten</sub-elementA>"
                            + "<sub-elementB attribute='B'>to be overwritten</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        HashMap<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("//root/element/sub-element-wrong", "text-value");
        messageElements.put("//sub-element-wrong", "text-value");
        
        receiveMessageBean.setMessageElements(messageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testSetMessageElementAttributesError() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='to be overwritten'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='to be overwritten'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        HashMap<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("//root/element/sub-elementA/@attribute-wrong", "A");
        messageElements.put("//sub-elementB/@attribute-wrong", "B");
        
        receiveMessageBean.setMessageElements(messageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testSetMessageElementAttributesErrorWrongElement() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='to be overwritten'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='to be overwritten'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        HashMap<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("//root/element/sub-elementA-wrong/@attribute", "A");
        messageElements.put("//sub-elementB-wrong/@attribute", "B");
        
        receiveMessageBean.setMessageElements(messageElements);
        
        receiveMessageBean.execute(context);
    }

    @Test
    public void testExtractMessageElements() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        HashMap<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("//root/element/sub-elementA", "${valueA}");
        extractMessageElements.put("//root/element/sub-elementB", "${valueB}");
        
        receiveMessageBean.setExtractMessageElements(extractMessageElements);
        
        receiveMessageBean.execute(context);
        
        Assert.assertTrue(context.getVariables().containsKey("valueA"));
        Assert.assertEquals(context.getVariables().get("valueA"), "text-value");
        Assert.assertTrue(context.getVariables().containsKey("valueB"));
        Assert.assertEquals(context.getVariables().get("valueB"), "text-value");
    }
    
    @Test
    public void testExtractMessageAttributes() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        HashMap<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("//root/element/sub-elementA/@attribute", "${valueA}");
        extractMessageElements.put("//root/element/sub-elementB/@attribute", "${valueB}");
        
        receiveMessageBean.setExtractMessageElements(extractMessageElements);
        
        receiveMessageBean.execute(context);
        
        Assert.assertTrue(context.getVariables().containsKey("valueA"));
        Assert.assertEquals(context.getVariables().get("valueA"), "A");
        Assert.assertTrue(context.getVariables().containsKey("valueB"));
        Assert.assertEquals(context.getVariables().get("valueB"), "B");
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testExtractMessageElementsForWrongElement() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        HashMap<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("//root/element/sub-element-wrong", "${valueA}");
        extractMessageElements.put("//element/sub-element-wrong", "${valueB}");
        
        receiveMessageBean.setExtractMessageElements(extractMessageElements);
        
        receiveMessageBean.execute(context);
        
        Assert.assertFalse(context.getVariables().containsKey("valueA"));
        Assert.assertFalse(context.getVariables().containsKey("valueB"));
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testExtractMessageElementsForWrongAtribute() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        HashMap<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("//root/element/sub-elementA/@attribute-wrong", "${attributeA}");
        
        receiveMessageBean.setExtractMessageElements(extractMessageElements);
        
        receiveMessageBean.execute(context);
        
        Assert.assertFalse(context.getVariables().containsKey("attributeA"));
    }
}
