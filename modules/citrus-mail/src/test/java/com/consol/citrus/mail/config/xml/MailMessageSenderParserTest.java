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

import com.consol.citrus.mail.message.MailMessageSender;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailMessageSenderParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testMailMessageSenderParser() {
        Map<String, MailMessageSender> senders = beanDefinitionContext.getBeansOfType(MailMessageSender.class);

        Assert.assertEquals(senders.size(), 3);

        // 1st mail sender
        MailMessageSender sender = senders.get("mailSender1");
        Assert.assertEquals(sender.getName(), "mailSender1");
        Assert.assertEquals(sender.getJavaMailSender().getHost(), "localhost");
        Assert.assertEquals(sender.getJavaMailSender().getPort(), 25000);
        Assert.assertNull(sender.getActor());

        // 2nd mail sender
        sender = senders.get("mailSender2");
        Assert.assertEquals(sender.getName(), "mailSender2");
        Assert.assertEquals(sender.getJavaMailSender().getHost(), "localhost");
        Assert.assertEquals(sender.getJavaMailSender().getPort(), 25000);
        Assert.assertEquals(sender.getJavaMailSender().getUsername(), "mailus");
        Assert.assertEquals(sender.getJavaMailSender().getPassword(), "secret");
        Assert.assertEquals(sender.getJavaMailSender().getJavaMailProperties().get("mail.smtp.auth"), "true");
        Assert.assertNull(sender.getActor());

        // 3rd mail sender
        sender = senders.get("mailSender3");
        Assert.assertEquals(sender.getName(), "mailSender3");
        Assert.assertEquals(sender.getJavaMailSender().getHost(), "localhost");
        Assert.assertEquals(sender.getJavaMailSender().getPort(), 25000);
        Assert.assertNotNull(sender.getActor());
        Assert.assertEquals(sender.getJavaMailSender().getJavaMailProperties().get("mail.transport.protocol"), "smtp");
    }
}
