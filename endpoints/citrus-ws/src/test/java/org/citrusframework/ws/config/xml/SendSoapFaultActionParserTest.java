/*
 * Copyright the original author or authors.
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

import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.ws.actions.SendSoapFaultAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SendSoapFaultActionParserTest extends AbstractActionParserTest<SendSoapFaultAction> {

    @Test
    public void testSendSoapFaultActionParser() {
        assertActionCount(2);
        assertActionClassAndName(SendSoapFaultAction.class, "send-fault");

        // 1st action
        SendSoapFaultAction action = getNextTestActionFromTest();
        Assertions.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("soapServer"));
        Assertions.assertNotNull(action.getMessageBuilder());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();
        Assertions.assertEquals(1L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sendFault", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals("{http://citrusframework.org/faults}citrus-ns:FAULT-1000", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals(1, action.getFaultDetails().size());
        Assertions.assertTrue(action.getFaultDetails().get(0).startsWith("<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">"));
        Assertions.assertNull(action.getFaultActor());

        // 2nd action
        action = getNextTestActionFromTest();
        Assertions.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("soapServer"));
        Assertions.assertNotNull(action.getMessageBuilder());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sendFault", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals("{http://citrusframework.org/faults}citrus-ns:FAULT-1001", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals(0, action.getFaultDetails().size());
        Assertions.assertEquals(1, action.getFaultDetailResourcePaths().size());
        Assertions.assertEquals("classpath:org/citrusframework/ws/actions/test-fault-detail.xml", action.getFaultDetailResourcePaths().get(0));
        Assertions.assertEquals("FaultActor", action.getFaultActor());
    }
}
