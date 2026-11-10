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

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.message.MessageConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @since 1.4
 */
public class MailClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMailClientParser() {
        Map<String, MailClient> senders = beanDefinitionContext.getBeansOfType(MailClient.class);

        Assertions.assertEquals(3, senders.size());

        // 1st mail sender
        MailClient sender = senders.get("mailClient1");
        Assertions.assertEquals("mailClient1", sender.getName());
        Assertions.assertEquals("localhost", sender.getEndpointConfiguration().getHost());
        Assertions.assertEquals(25000, sender.getEndpointConfiguration().getPort());
        Assertions.assertEquals("localhost", sender.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.host"));
        Assertions.assertEquals("25000", sender.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.port"));
        Assertions.assertNull(sender.getActor());

        // 2nd mail sender
        sender = senders.get("mailClient2");
        Assertions.assertEquals("mailClient2", sender.getName());
        Assertions.assertEquals("localhost", sender.getEndpointConfiguration().getHost());
        Assertions.assertEquals(25000, sender.getEndpointConfiguration().getPort());
        Assertions.assertEquals("mailus", sender.getEndpointConfiguration().getUsername());
        Assertions.assertEquals("secret", sender.getEndpointConfiguration().getPassword());
        Assertions.assertEquals("true", sender.getEndpointConfiguration().getJavaMailProperties().get("mail.smtp.auth"));
        Assertions.assertNull(sender.getActor());

        // 3rd mail sender
        sender = senders.get("mailClient3");
        Assertions.assertEquals("mailClient3", sender.getName());
        Assertions.assertEquals("localhost", sender.getEndpointConfiguration().getHost());
        Assertions.assertEquals(25000, sender.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(sender.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter", MessageConverter.class), sender.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(beanDefinitionContext.getBean("marshaller", MailMarshaller.class), sender.getEndpointConfiguration().getMarshaller());
        Assertions.assertEquals("smtp", sender.getEndpointConfiguration().getJavaMailProperties().get("mail.transport.protocol"));
    }
}
