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

package org.citrusframework.mail.client;

import javax.xml.transform.stream.StreamSource;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.model.MailRequest;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
class MailClientTest extends AbstractTestNGUnitTest {

    private AutoCloseable mockitoContext;

    @Mock
    private MailSender mailSenderMock;

    private MailClient fixture;

    @BeforeMethod
    public void beforeMethodSetup() {
        mockitoContext = MockitoAnnotations.openMocks(this);

        fixture = new MailClient();
        fixture.setMailSender(mailSenderMock);
    }

    @Test
    void testSendMailMessageObject() throws Exception {
        MailRequest mailRequest = (MailRequest) new MailMarshaller().unmarshal(
                new StreamSource(
                    Resources.create("text_mail.xml", MailServer.class).getInputStream()
                )
        );

        doAnswer(invocation -> {
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
        }).when(mailSenderMock).send(any(MimeMessage.class));

        fixture.send(new DefaultMessage(mailRequest), context);
    }

    @Test
    void testSendMultipartMailMessageObject() throws Exception {
        MailRequest mailRequest = (MailRequest) new MailMarshaller().unmarshal(
                new StreamSource(
                    Resources.create("multipart_mail.xml", MailServer.class).getInputStream()
                )
        );

        doAnswer(invocation -> {
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
            Assert.assertEquals(multipart.getBodyPart(1).getContent().toString().replaceAll("\\s", ""), "<html><head></head><body><h1>HTMLAttachment</h1></body></html>");
            Assert.assertEquals(multipart.getBodyPart(1).getFileName(), "index.html");
            Assert.assertEquals(multipart.getBodyPart(1).getDisposition(), "attachment");
            return null;
        }).when(mailSenderMock).send(any(MimeMessage.class));

        fixture.send(new DefaultMessage(mailRequest), context);

    }

    private String getAddresses(Address[] addressList) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < addressList.length; i++) {
            InternetAddress address = (InternetAddress) addressList[i];

            if (i > 0) {
                builder.append(",").append(address.getAddress());
            } else {
                builder.append(address.getAddress());
            }
        }

        return builder.toString();
    }

    @AfterMethod
    void afterMethodTeardown() throws Exception {
        mockitoContext.close();
    }
}
