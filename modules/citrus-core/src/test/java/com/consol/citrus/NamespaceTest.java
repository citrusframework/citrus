package com.consol.citrus;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageBean;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.XMLMessage;
import com.consol.citrus.service.Service;
import com.consol.citrus.validation.XMLMessageValidator;

public class NamespaceTest extends AbstractBaseTest {
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
    public void testNamespaces() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testDifferentNamespacePrefix() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns2:root xmlns:ns2='http://testsuite'>"
                        + "<ns2:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns2:sub-element attribute='A'>text-value</ns2:sub-element>"
                        + "</ns2:element>" 
                    + "</ns2:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testAdditionalNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite' xmlns:ns2='http://testsuite/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testMissingNamespaceDeclaration() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite' xmlns:ns2='http://testsuite/default'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testDefaultNamespaces() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        receiveMessageBean.execute(context);
    }

    @Test
    public void testDefaultNamespacesInExpectedMessage() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testDefaultNamespacesInSourceMessage() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite'>"
                    + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                    + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                    + "</ns1:element>" 
                + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testMissingNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<sub-element attribute='A'>text-value</sub-element>"
                            + "</element>" 
                        + "</root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testWrongNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite/wrong'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testExpectDefaultNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testExpectNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testExpectMixedNamespaces() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testExpectMultipleNamespaces() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceError() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/wrong");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceError() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1/wrong");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMixedNamespacesError() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default/wrong");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMultipleNamespacesError() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1/wrong");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectWrongNamespacePrefix() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:nswrong", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceButNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns0:root xmlns:ns0='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<ns0:root xmlns:ns0='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButDefaultNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns0", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectAdditionalNamespace() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        expectedNamespaces.put("xmlns:ns4", "http://testsuite/ns4");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButNamespaceMissing() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2' xmlns:ns4='http://testsuite/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2' xmlns:ns4='http://testsuite/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testValidateMessageElementsWithAdditionalNamespacePrefix() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<root xmlns='http://testsuite/default'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//ns1:root/ns1:element/ns1:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:sub-elementB", "text-value");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns1", "http://testsuite/default");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testValidateMessageElementsWithDifferentNamespacePrefix() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<ns1:sub-elementA attribute='A'>text-value</ns1:sub-elementA>"
                            + "<ns1:sub-elementB attribute='B'>text-value</ns1:sub-elementB>"
                            + "<ns1:sub-elementC attribute='C'>text-value</ns1:sub-elementC>"
                        + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//pfx:root/ns1:element/pfx:sub-elementA", "text-value");
        validateMessageElements.put("//pfx:sub-elementB", "text-value");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://testsuite/default");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testWrongNamespaceContext() {
        reset(service);
        
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<ns1:root xmlns:ns1='http://testsuite/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<ns1:sub-elementA attribute='A'>text-value</ns1:sub-elementA>"
                            + "<ns1:sub-elementB attribute='B'>text-value</ns1:sub-elementB>"
                            + "<ns1:sub-elementC attribute='C'>text-value</ns1:sub-elementC>"
                        + "</ns1:element>" 
                        + "</ns1:root>");
        
        expect(service.receiveMessage()).andReturn(message);
        replay(service);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//pfx:root/ns1:element/pfx:sub-elementA", "text-value");
        validateMessageElements.put("//pfx:sub-elementB", "text-value");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://testsuite/wrong");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
}
