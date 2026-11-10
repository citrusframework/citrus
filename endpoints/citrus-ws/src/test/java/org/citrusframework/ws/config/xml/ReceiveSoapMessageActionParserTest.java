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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;

public class ReceiveSoapMessageActionParserTest extends AbstractActionParserTest<ReceiveSoapMessageAction> {

    @Test
    public void testReceiveSoapMessageActionParser() {
        assertActionCount(3);
        assertActionClassAndName(ReceiveSoapMessageAction.class, "receive");

        // 1st action
        ReceiveSoapMessageAction action = getNextTestActionFromTest();
        Assertions.assertEquals(action.getAttachmentValidator(), beanDefinitionContext.getBean("soapAttachmentValidator"));
        Assertions.assertEquals(1L, action.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", action.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", action.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", action.getAttachments().get(0).getContentType());

        // 2nd action
        action = getNextTestActionFromTest();
        Assertions.assertEquals(action.getAttachmentValidator(), beanDefinitionContext.getBean("mySoapAttachmentValidator"));
        Assertions.assertEquals(1L, action.getAttachments().size());
        Assertions.assertNotNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("MySoapAttachment", action.getAttachments().get(0).getContentId());
        Assertions.assertEquals("application/xml", action.getAttachments().get(0).getContentType());
        Assertions.assertEquals("UTF-8", action.getAttachments().get(0).getCharsetName());

        // 3rd action
        action = getNextTestActionFromTest();
        Assertions.assertEquals(action.getAttachmentValidator(), beanDefinitionContext.getBean("soapAttachmentValidator"));
        Assertions.assertEquals(2L, action.getAttachments().size());
        Assertions.assertEquals("This is an attachment!", action.getAttachments().get(0).getContent().trim());
        Assertions.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assertions.assertEquals("FirstSoapAttachment", action.getAttachments().get(0).getContentId());
        Assertions.assertEquals("text/plain", action.getAttachments().get(0).getContentType());
        Assertions.assertNotNull(action.getAttachments().get(1).getContentResourcePath());
        Assertions.assertEquals("SecondSoapAttachment", action.getAttachments().get(1).getContentId());
        Assertions.assertEquals("application/xml", action.getAttachments().get(1).getContentType());
        Assertions.assertEquals("UTF-8", action.getAttachments().get(1).getCharsetName());
    }
}
