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

package org.citrusframework.ws.config.xml;

import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.ws.actions.SendSoapFaultAction;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionParserTest extends AbstractActionParserTest<SendSoapFaultAction> {

    @Test
    public void testSendSoapFaultActionParser() {
        assertActionCount(2);
        assertActionClassAndName(SendSoapFaultAction.class, "send-fault");

        // 1st action
        SendSoapFaultAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("soapServer"));
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sendFault");
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}citrus-ns:FAULT-1000");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 1);
        Assert.assertTrue(action.getFaultDetails().get(0).startsWith("<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">"));
        Assert.assertNull(action.getFaultActor());

        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("soapServer"));
        Assert.assertNotNull(action.getMessageBuilder());
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sendFault");
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}citrus-ns:FAULT-1001");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 0);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:org/citrusframework/ws/actions/test-fault-detail.xml");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
    }
}
