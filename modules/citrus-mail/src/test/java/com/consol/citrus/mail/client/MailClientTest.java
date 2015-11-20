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

import com.consol.citrus.mail.model.MailMarshaller;
import com.consol.citrus.mail.model.MailMessage;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.*;
import javax.mail.internet.*;
import javax.xml.transform.stream.StreamSource;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailClientTest extends AbstractTestNGUnitTest {

    private JavaMailSenderImpl javaMailSender = Mockito.mock(JavaMailSenderImpl.class);

    private MailClient mailClient = new MailClient();

    @BeforeClass
    public void setup() {
        mailClient.getEndpointConfiguration().setJavaMailSender(javaMailSender);
    }

    @Test
    public void testSendMailMessageObject() throws Exception {
        MailMessage mailMessage = (MailMessage) new MailMarshaller().unmarshal(new StreamSource(
            new ClassPathResource("text_mail.xml", MailServer.class).getInputStream()));

        reset(javaMailSender);
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                MimeMessage mimeMessage = (MimeMessage) invocation.getArguments()[0];
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
        }).when(javaMailSender).send(any(MimeMessage.class));

        mailClient.send(new DefaultMessage(mailMessage), context);

    }

    @Test
    public void testSendMultipartMailMessageObject() throws Exception {
        MailMessage mailMessage = (MailMessage) new MailMarshaller().unmarshal(new StreamSource(
                new ClassPathResource("multipart_mail.xml", MailServer.class).getInputStream()));

        reset(javaMailSender);
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                MimeMessage mimeMessage = (MimeMessage) invocation.getArguments()[0];
                Assert.assertEquals(getAddresses(mimeMessage.getFrom()), "foo@mail.com");
                Assert.assertEquals(getAddresses(mimeMessage.getRecipients(Message.RecipientType.TO)), "bar@mail.com");
                Assert.assertEquals(getAddresses(mimeMessage.getReplyTo()), "foo@mail.com");
                Assert.assertNotNull(mimeMessage.getSentDate());
                Assert.assertEquals(mimeMessage.getSubject(), "Multipart Testmail");
                Assert.assertEquals(mimeMessage.getContentType(), "text/plain");

                Assert.assertEquals(mimeMessage.getContent().getClass(), MimeMultipart.class);

                MimeMultipart multipart = (MimeMultipart) mimeMessage.getContent();

                Assert.assertEquals(multipart.getCount(), 2L);
                Assert.assertTrue(multipart.getContentType().startsWith("multipart/mixed"));
                Assert.assertTrue(((MimeMultipart) multipart.getBodyPart(0).getContent()).getContentType().startsWith("multipart/related"));
                Assert.assertEquals(((MimeMultipart) multipart.getBodyPart(0).getContent()).getCount(), 1L);
                Assert.assertEquals(((MimeMultipart) multipart.getBodyPart(0).getContent()).getBodyPart(0).getContent().toString(), "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.");
                Assert.assertEquals(((MimeMultipart) multipart.getBodyPart(0).getContent()).getBodyPart(0).getContentType(), "text/plain");
                Assert.assertEquals(StringUtils.trimAllWhitespace(multipart.getBodyPart(1).getContent().toString()), "<html><head></head><body><h1>HTMLAttachment</h1></body></html>");
                Assert.assertEquals(multipart.getBodyPart(1).getFileName(), "index.html");
                Assert.assertEquals(multipart.getBodyPart(1).getDisposition(), "attachment");
                return null;
            }
        }).when(javaMailSender).send(any(MimeMessage.class));

        mailClient.send(new DefaultMessage(mailMessage), context);

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
