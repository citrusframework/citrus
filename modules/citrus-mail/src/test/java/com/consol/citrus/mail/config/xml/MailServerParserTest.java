/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.mail.config.xml;

import com.consol.citrus.adapter.handler.EmptyResponseProducingMessageHandler;
import com.consol.citrus.mail.adapter.MessageHandlerAdapter;
import com.consol.citrus.mail.adapter.MessageSplittingHandlerAdapter;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMailServerParser() {
        Map<String, MailServer> servers = beanDefinitionContext.getBeansOfType(MailServer.class);

        Assert.assertEquals(servers.size(), 3);

        // 1st mail server
        MailServer server = servers.get("mailServer1");
        Assert.assertEquals(server.getName(), "mailServer1");
        Assert.assertEquals(server.getPort(), 25);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertEquals(server.getMessageHandlerAdapter().getClass(), MessageHandlerAdapter.class);
        Assert.assertEquals(server.getMessageHandlerAdapter().getMessageHandler().getClass(),
                EmptyResponseProducingMessageHandler.class);

        // 2nd mail server
        server = servers.get("mailServer2");
        Assert.assertEquals(server.getName(), "mailServer2");
        Assert.assertEquals(server.getPort(), 25000);
        Assert.assertFalse(server.isAutoStart());

        // 3rd mail server
        server = servers.get("mailServer3");
        Assert.assertEquals(server.getName(), "mailServer3");
        Assert.assertEquals(server.getPort(), 25000);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertEquals(server.getMessageHandlerAdapter().getClass(), MessageSplittingHandlerAdapter.class);
    }
}
