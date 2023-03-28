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

import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;

/**
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageActionParserTest extends AbstractActionParserTest<ReceiveSoapMessageAction> {

    @Test
    public void testReceiveSoapMessageActionParser() {
        assertActionCount(3);
        assertActionClassAndName(ReceiveSoapMessageAction.class, "receive");
        
        // 1st action
        ReceiveSoapMessageAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getAttachmentValidator(), beanDefinitionContext.getBean("soapAttachmentValidator"));
        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertEquals(action.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), "text/plain");

        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getAttachmentValidator(), beanDefinitionContext.getBean("mySoapAttachmentValidator"));
        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNotNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), "application/xml");
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), "UTF-8");

        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getAttachmentValidator(), beanDefinitionContext.getBean("soapAttachmentValidator"));
        Assert.assertEquals(action.getAttachments().size(), 2L);
        Assert.assertEquals(action.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), "FirstSoapAttachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), "text/plain");
        Assert.assertNotNull(action.getAttachments().get(1).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(1).getContentId(), "SecondSoapAttachment");
        Assert.assertEquals(action.getAttachments().get(1).getContentType(), "application/xml");
        Assert.assertEquals(action.getAttachments().get(1).getCharsetName(), "UTF-8");
    }
}
