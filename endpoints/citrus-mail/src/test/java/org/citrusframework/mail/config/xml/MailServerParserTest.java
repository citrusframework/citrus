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

package org.citrusframework.mail.config.xml;

import java.util.Map;

import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        Assert.assertFalse(server.isSplitMultipart());
        Assert.assertTrue(server.isAuthRequired());
        Assert.assertTrue(server.isAutoAccept());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), DirectEndpointAdapter.class);
        Assert.assertTrue(server.getJavaMailProperties().isEmpty());
        Assert.assertTrue(server.getKnownUsers().isEmpty());

        // 2nd mail server
        server = servers.get("mailServer2");
        Assert.assertEquals(server.getName(), "mailServer2");
        Assert.assertEquals(server.getPort(), 25000);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isSplitMultipart());
        Assert.assertFalse(server.isAuthRequired());
        Assert.assertFalse(server.isAutoAccept());
        Assert.assertTrue(server.getJavaMailProperties().isEmpty());
        Assert.assertTrue(server.getKnownUsers().isEmpty());

        // 3rd mail server
        server = servers.get("mailServer3");
        Assert.assertEquals(server.getName(), "mailServer3");
        Assert.assertEquals(server.getPort(), 25);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertTrue(server.isSplitMultipart());
        Assert.assertTrue(server.isAuthRequired());
        Assert.assertTrue(server.isAutoAccept());
        Assert.assertEquals(server.getEndpointAdapter(), beanDefinitionContext.getBean("endpointAdapter"));
        Assert.assertEquals(server.getJavaMailProperties(), beanDefinitionContext.getBean("mailProperties"));
        Assert.assertEquals(server.getMessageConverter(), beanDefinitionContext.getBean("messageConverter", MessageConverter.class));
        Assert.assertEquals(server.getMarshaller(), beanDefinitionContext.getBean("marshaller", MailMarshaller.class));
        Assert.assertFalse(server.getJavaMailProperties().isEmpty());
        Assert.assertFalse(server.getKnownUsers().isEmpty());
        Assert.assertEquals(server.getKnownUsers().size(), 2L);
        Assert.assertEquals(server.getKnownUsers().get(0), "foo@example.com:foo-user:secr3t");
        Assert.assertEquals(server.getKnownUsers().get(1), "bar@example.com:bar-user:secr3t");
    }
}
