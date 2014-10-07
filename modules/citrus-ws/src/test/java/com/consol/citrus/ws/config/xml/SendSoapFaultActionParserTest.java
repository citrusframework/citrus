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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.ws.message.builder.SoapFaultAwareMessageBuilder;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionParserTest extends AbstractActionParserTest<SendMessageAction> {

    @Test
    public void testSendMessageActionParser() {
        assertActionCount(2);
        assertActionClassAndName(SendMessageAction.class, "send-fault");
        
        // 1st action
        SendMessageAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("soapServer"));
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), SoapFaultAwareMessageBuilder.class);
        
        SoapFaultAwareMessageBuilder messageBuilder = (SoapFaultAwareMessageBuilder)action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1);
        Assert.assertEquals(messageBuilder.getFaultDetails().size(), 1);
        Assert.assertTrue(messageBuilder.getFaultDetails().get(0).startsWith("<ns0:FaultDetail xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">"));
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "sendFault");
        Assert.assertEquals(messageBuilder.getFaultCode(), "{http://www.citrusframework.org/faults}citrus-ns:FAULT-1000");
        Assert.assertEquals(messageBuilder.getFaultString(), "FaultString");
        Assert.assertNull(messageBuilder.getFaultActor());
        
        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("soapServer"));
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), SoapFaultAwareMessageBuilder.class);
        
        messageBuilder = (SoapFaultAwareMessageBuilder)action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1);
        Assert.assertEquals(messageBuilder.getFaultDetails().size(), 1);
        Assert.assertEquals(messageBuilder.getFaultDetails().get(0), SoapMessageHeaders.SOAP_FAULT_DETAIL_RESOURCE + "(classpath:com/consol/citrus/ws/actions/test-fault-detail.xml)");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "sendFault");
        Assert.assertEquals(messageBuilder.getFaultCode(), "{http://www.citrusframework.org/faults}citrus-ns:FAULT-1001");
        Assert.assertEquals(messageBuilder.getFaultString(), "FaultString");
        Assert.assertEquals(messageBuilder.getFaultActor(), "FaultActor");
    }
}
