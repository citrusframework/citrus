package com.consol.citrus;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageBean;
import com.consol.citrus.service.Service;
import com.consol.citrus.validation.XMLMessageValidator;

public class XPathTest extends AbstractBaseTest {
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
    public void testUsingXPath() {
        reset(service);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns='http://test' xmlns:ns1='http://testsuite'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</ns1:root>")
                        .build();
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("//:element/:sub-elementB", "text-value");
        validateMessageElements.put("//:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("//ns1:ns-element", "namespace");
        validateMessageElements.put("//*[.='search-for']", "search-for");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testUsingXPathWithDefaultNamespace() {
        reset(service);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://test'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns-element>namespace</ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>")
                        .build();
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("//:element/:sub-elementB", "text-value");
        validateMessageElements.put("//:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("//:ns-element", "namespace");
        validateMessageElements.put("//*[.='search-for']", "search-for");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testUsingXPathWithExplicitNamespace() {
        reset(service);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://test' xmlns:ns1='http://testsuite'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>")
                        .build();
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:ns-element", "namespace");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testUsingXPathWithExplicitNamespaceInElementDefinition() {
        reset(service);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://test'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element xmlns:ns1='http://testsuite'>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>")
                        .build();
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:ns-element", "namespace");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns1", "http://testsuite");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
}
