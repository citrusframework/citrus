package com.consol.citrus;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.util.*;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageBean;
import com.consol.citrus.service.Service;
import com.consol.citrus.validation.XMLMessageValidator;

public class IgnoreElementsTest extends AbstractBaseTest {
    @Autowired
    XMLMessageValidator validator;
    
    Service service = EasyMock.createMock(Service.class);
    
    ReceiveMessageBean receiveMessageBean;
    
    @Override
    @BeforeMethod
    public void setup() {
        super.setup();
        
        reset(service);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean = new ReceiveMessageBean();
        receiveMessageBean.setService(service);
        receiveMessageBean.setValidator(validator);
    }

    @Test
    public void testIgnoreElements() {
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA");
        ignoreMessageElements.add("//sub-elementB");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributes() {
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                + "<sub-elementB attribute='no validation'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA/@attribute");
        ignoreMessageElements.add("//sub-elementB/@attribute");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributesAll() {
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>" //TODO fix this
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//@attribute");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributesUsingArrays() {
        reset(service);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-element attribute='A'>text-value</sub-element>"
                            + "<sub-element attribute='B'>text-value</sub-element>"
                            + "<sub-element attribute='C'>text-value</sub-element>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-element attribute='no validation'>text-value</sub-element>"
                + "<sub-element attribute='no validation'>text-value</sub-element>"
                + "<sub-element attribute='C'>text-value</sub-element>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//sub-element[1]/@attribute");
        ignoreMessageElements.add("//sub-element[2]/@attribute");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreRootElement() {
        reset(service);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element>Text</element>" 
                        + "</root>").build();
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element additonal-attribute='some'>Wrong text</element>" 
                        + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreElementsAndValidate() {
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA");
        ignoreMessageElements.add("//sub-elementB");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        Map<String, String> validateElements = new HashMap<String, String>();
        validateElements.put("//root/element/sub-elementA", "wrong value");
        validateElements.put("//sub-elementB", "wrong value");
        receiveMessageBean.setValidateMessageElements(validateElements);
        
        receiveMessageBean.execute(context);
    }
}
