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

import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMailClientParser() {
        Map<String, MailClient> senders = beanDefinitionContext.getBeansOfType(MailClient.class);

        Assert.assertEquals(senders.size(), 3);

        // 1st mail sender
        MailClient sender = senders.get("mailClient1");
        Assert.assertEquals(sender.getName(), "mailClient1");
        Assert.assertEquals(sender.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sender.getEndpointConfiguration().getPort(), 25000);
        Assert.assertEquals(sender.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.host"), "localhost");
        Assert.assertEquals(sender.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.port"), "25000");
        Assert.assertNull(sender.getActor());

        // 2nd mail sender
        sender = senders.get("mailClient2");
        Assert.assertEquals(sender.getName(), "mailClient2");
        Assert.assertEquals(sender.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sender.getEndpointConfiguration().getPort(), 25000);
        Assert.assertEquals(sender.getEndpointConfiguration().getUsername(), "mailus");
        Assert.assertEquals(sender.getEndpointConfiguration().getPassword(), "secret");
        Assert.assertEquals(sender.getEndpointConfiguration().getJavaMailProperties().get("mail.smtp.auth"), "true");
        Assert.assertNull(sender.getActor());

        // 3rd mail sender
        sender = senders.get("mailClient3");
        Assert.assertEquals(sender.getName(), "mailClient3");
        Assert.assertEquals(sender.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sender.getEndpointConfiguration().getPort(), 25000);
        Assert.assertNotNull(sender.getActor());
        Assert.assertEquals(sender.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter", MessageConverter.class));
        Assert.assertEquals(sender.getEndpointConfiguration().getMarshaller(), beanDefinitionContext.getBean("marshaller", MailMarshaller.class));
        Assert.assertEquals(sender.getEndpointConfiguration().getJavaMailProperties().get("mail.transport.protocol"), "smtp");
    }
}
