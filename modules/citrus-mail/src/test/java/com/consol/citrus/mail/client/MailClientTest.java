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

package com.consol.citrus.mail.client;

import com.consol.citrus.mail.model.MailMessage;
import com.consol.citrus.mail.model.MailMessageMapper;
import com.consol.citrus.mail.server.MailServer;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailClientTest {

    private JavaMailSenderImpl javaMailSender = EasyMock.createMock(JavaMailSenderImpl.class);

    private MailClient mailClient = new MailClient();

    @BeforeClass
    public void setup() {
        mailClient.getEndpointConfiguration().setJavaMailSender(javaMailSender);
    }

    @Test
    public void testSendMailMessageObject() throws Exception {
        MailMessage mailMessage = (MailMessage) new MailMessageMapper().fromXML(
                new ClassPathResource("text_mail.xml", MailServer.class).getInputStream());

        reset(javaMailSender);

        expect(javaMailSender.getHost()).andReturn("localhost").times(2);
        expect(javaMailSender.getPort()).andReturn(25).times(2);
        expect(javaMailSender.getProtocol()).andReturn("smtp").times(2);
        expect(javaMailSender.createMimeMessage()).andReturn(new MimeMessage(Session.getDefaultInstance(new Properties()))).once();
        javaMailSender.send(anyObject(MimeMessage.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                MimeMessage mimeMessage = (MimeMessage) getCurrentArguments()[0];
                Assert.assertEquals(getAddresses(mimeMessage.getFrom()), "foo@mail.com");
                Assert.assertEquals(getAddresses(mimeMessage.getRecipients(Message.RecipientType.TO)), "bar@mail.com,copy@mail.com");
                Assert.assertEquals(getAddresses(mimeMessage.getRecipients(Message.RecipientType.CC)), "foobar@mail.com");
                Assert.assertEquals(getAddresses(mimeMessage.getRecipients(Message.RecipientType.BCC)), "secret@mail.com");
                Assert.assertEquals(getAddresses(mimeMessage.getReplyTo()), "foo@mail.com");
                Assert.assertNotNull(mimeMessage.getSentDate());
                Assert.assertEquals(mimeMessage.getSubject(), "Testmail");
                Assert.assertEquals(mimeMessage.getContentType(), "text/plain");

                Assert.assertEquals(mimeMessage.getContent().toString(), "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.");

                return null;
            }
        }).once();

        replay(javaMailSender);

        mailClient.send(MessageBuilder.withPayload(mailMessage).build());

        verify(javaMailSender);
    }

    private String getAddresses(Address[] addressList) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < addressList.length; i++) {
            InternetAddress address = (InternetAddress) addressList[i];

            if (i > 0) {
                builder.append("," + address.getAddress());
            } else {
                builder.append(address.getAddress());
            }
        }

        return builder.toString();
    }
}
