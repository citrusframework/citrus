package com.consol.citrus;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageBean;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.XMLMessage;
import com.consol.citrus.service.Service;
import com.consol.citrus.validation.XMLMessageValidator;

public class HeaderValuesTest extends AbstractBaseTest {
    @Autowired
    XMLMessageValidator validator;
    
    Service service = EasyMock.createMock(Service.class);
    
    ReceiveMessageBean receiveMessageBean;
    
    @Test
    public void testValidateHeaderValues() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        message.addHeaderElement("header-valueA", "A");
        message.addHeaderElement("header-valueB", "B");
        message.addHeaderElement("header-valueC", "C");
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> validateHeaderValues = new HashMap<String, String>();
        validateHeaderValues.put("header-valueA", "A");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testValidateHeaderValuesComplete() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        message.addHeaderElement("header-valueA", "A");
        message.addHeaderElement("header-valueB", "B");
        message.addHeaderElement("header-valueC", "C");
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> validateHeaderValues = new HashMap<String, String>();
        validateHeaderValues.put("header-valueA", "A");
        validateHeaderValues.put("header-valueB", "B");
        validateHeaderValues.put("header-valueC", "C");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateHeaderValuesWrongExpectedValue() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        message.addHeaderElement("header-valueA", "A");
        message.addHeaderElement("header-valueB", "B");
        message.addHeaderElement("header-valueC", "C");
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> validateHeaderValues = new HashMap<String, String>();
        validateHeaderValues.put("header-valueA", "wrong");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateHeaderValuesForWrongElement() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        message.addHeaderElement("header-valueA", "A");
        message.addHeaderElement("header-valueB", "B");
        message.addHeaderElement("header-valueC", "C");
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> validateHeaderValues = new HashMap<String, String>();
        validateHeaderValues.put("header-wrong", "A");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testValidateEmptyHeaderValues() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        message.addHeaderElement("header-valueA", "");
        message.addHeaderElement("header-valueB", "");
        message.addHeaderElement("header-valueC", "");
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> validateHeaderValues = new HashMap<String, String>();
        validateHeaderValues.put("header-valueA", "");
        validateHeaderValues.put("header-valueB", "");
        validateHeaderValues.put("header-valueC", "");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateHeaderValuesNullComparison() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        message.addHeaderElement("header-valueA", "");
        message.addHeaderElement("header-valueB", "");
        message.addHeaderElement("header-valueC", "");
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> validateHeaderValues = new HashMap<String, String>();
        validateHeaderValues.put("header-valueA", "null");
        validateHeaderValues.put("header-valueB", "null");
        validateHeaderValues.put("header-valueC", "null");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }

    @Test
    public void testExtractHeaderValues() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        message.addHeaderElement("header-valueA", "A");
        message.addHeaderElement("header-valueB", "B");
        message.addHeaderElement("header-valueC", "C");
        
        message.setMessagePayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> extractHeaderValues = new HashMap<String, String>();
        extractHeaderValues.put("header-valueA", "${valueA}");
        extractHeaderValues.put("header-valueB", "${valueB}");
        
        receiveMessageBean.setExtractHeaderValues(extractHeaderValues);
        
        receiveMessageBean.execute(context);
        
        Assert.assertTrue(context.getVariables().containsKey("valueA"));
        Assert.assertEquals(context.getVariables().get("valueA"), "A");
        Assert.assertTrue(context.getVariables().containsKey("valueB"));
        Assert.assertEquals(context.getVariables().get("valueB"), "B");
    }
}
