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

package org.citrusframework.mail.config.xml;

import java.util.Map;

import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.message.MessageConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 1.4
 */
public class MailServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMailServerParser() {
        Map<String, MailServer> servers = beanDefinitionContext.getBeansOfType(MailServer.class);

        Assertions.assertEquals(3, servers.size());

        // 1st mail server
        MailServer server = servers.get("mailServer1");
        Assertions.assertEquals("mailServer1", server.getName());
        Assertions.assertEquals(25, server.getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isSplitMultipart());
        Assertions.assertTrue(server.isAuthRequired());
        Assertions.assertTrue(server.isAutoAccept());
        Assertions.assertEquals(DirectEndpointAdapter.class, server.getEndpointAdapter().getClass());
        Assertions.assertTrue(server.getJavaMailProperties().isEmpty());
        Assertions.assertTrue(server.getKnownUsers().isEmpty());

        // 2nd mail server
        server = servers.get("mailServer2");
        Assertions.assertEquals("mailServer2", server.getName());
        Assertions.assertEquals(25000, server.getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isSplitMultipart());
        Assertions.assertFalse(server.isAuthRequired());
        Assertions.assertFalse(server.isAutoAccept());
        Assertions.assertTrue(server.getJavaMailProperties().isEmpty());
        Assertions.assertTrue(server.getKnownUsers().isEmpty());

        // 3rd mail server
        server = servers.get("mailServer3");
        Assertions.assertEquals("mailServer3", server.getName());
        Assertions.assertEquals(25, server.getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertTrue(server.isSplitMultipart());
        Assertions.assertTrue(server.isAuthRequired());
        Assertions.assertTrue(server.isAutoAccept());
        Assertions.assertEquals(beanDefinitionContext.getBean("endpointAdapter"), server.getEndpointAdapter());
        Assertions.assertEquals(beanDefinitionContext.getBean("mailProperties"), server.getJavaMailProperties());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter", MessageConverter.class), server.getMessageConverter());
        Assertions.assertEquals(beanDefinitionContext.getBean("marshaller", MailMarshaller.class), server.getMarshaller());
        Assertions.assertFalse(server.getJavaMailProperties().isEmpty());
        Assertions.assertFalse(server.getKnownUsers().isEmpty());
        Assertions.assertEquals(2L, server.getKnownUsers().size());
        Assertions.assertEquals("foo@example.com:foo-user:secr3t", server.getKnownUsers().get(0));
        Assertions.assertEquals("bar@example.com:bar-user:secr3t", server.getKnownUsers().get(1));
    }
}
