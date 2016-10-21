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

package com.consol.citrus;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class IgnoreElementsTest extends AbstractTestNGUnitTest {
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Consumer consumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);
    
    private ReceiveMessageAction receiveMessageBean;
    
    @Override
    @BeforeMethod
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void prepareTest() {
        super.prepareTest();

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);
        
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);
    }

    @Test
    public void testIgnoreElements() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                    + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA");
        ignoreMessageElements.add("//sub-elementB");
        validationContext.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }

    @Test
    public void testIgnoreNodeListElements() {
        reset(consumer);
        Message message = new DefaultMessage("<root>"
                + "<element>"
                    + "<sub-element attribute='A'>text-value</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element>"
                    + "<sub-element attribute='A'>no validation</sub-element>"
                    + "<sub-element attribute='B'>no validation</sub-element>"
                    + "<sub-element attribute='C'>no validation</sub-element>"
                + "</element>"
                + "</root>");

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//sub-element");
        validationContext.setIgnoreExpressions(ignoreMessageElements);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);

        receiveMessageBean.execute(context);
    }

    @Test
    public void testIgnoreMultipleElements() {
        reset(consumer);
        Message message = new DefaultMessage("<root>"
                + "<element>"
                    + "<sub-element attribute='A'>text-value</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                + "</element>"
                + "</root>");

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//sub-element[1]");
        ignoreMessageElements.add("//sub-element[3]");
        validationContext.setIgnoreExpressions(ignoreMessageElements);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);

        receiveMessageBean.execute(context);
    }

    @Test
    public void testIgnoreAllElements() {
        reset(consumer);
        Message message = new DefaultMessage("<root>"
                + "<element>"
                    + "<another-element attribute='Z'>text-value</another-element>"
                    + "<sub-element attribute='A'>text-value</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                    + "<another-element attribute='Z'>text-value</another-element>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element>"
                    + "<another-element>no validation</another-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                + "</element>"
                + "</root>");

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("/*");
        validationContext.setIgnoreExpressions(ignoreMessageElements);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);

        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributes() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='no validation'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA/@attribute");
        ignoreMessageElements.add("//sub-elementB/@attribute");
        validationContext.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributesAll() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>" //TODO fix this
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//@attribute");
        validationContext.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testIgnoreAttributesUsingArrays() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-element attribute='A'>text-value</sub-element>"
                            + "<sub-element attribute='B'>text-value</sub-element>"
                            + "<sub-element attribute='C'>text-value</sub-element>"
                        + "</element>" 
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-element attribute='no validation'>text-value</sub-element>"
                    + "<sub-element attribute='no validation'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                + "</element>"
                + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//sub-element[1]/@attribute");
        ignoreMessageElements.add("//sub-element[2]/@attribute");
        validationContext.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testIgnoreRootElement() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);
        
        Message message = new DefaultMessage("<root>"
                + "<element>Text</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element additonal-attribute='some'>Wrong text</element>" 
                        + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root");
        validationContext.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreElementsAndValidate() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                    + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA");
        ignoreMessageElements.add("//sub-elementB");
        validationContext.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        Map<String, Object> validateElements = new HashMap<>();
        validateElements.put("//root/element/sub-elementA", "wrong value");
        validateElements.put("//sub-elementB", "wrong value");
        validationContext.setXpathExpressions(validateElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreElementsByPlaceholder() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>@ignore@</sub-elementA>"
                    + "<sub-elementB attribute='B'> @ignore@ </sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreSubElementsByPlaceholder() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >@ignore@</element>" 
            + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributesByPlaceholder() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='@ignore@'>text-value</sub-elementA>"
                    + "<sub-elementB attribute=' @ignore@ '>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "No result for XPath expression: '//something-else'")
    public void testIgnoreElementsNoMatch() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        receiveMessageBean.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//something-else");
        validationContext.setIgnoreExpressions(ignoreMessageElements);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);

        receiveMessageBean.execute(context);
    }
}
