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

package org.citrusframework.mail.client;

import javax.xml.transform.stream.StreamSource;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.model.MailRequest;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

/**
 * @since 1.4
 */
@ExtendWith(MockitoExtension.class)
class MailClientTest extends AbstractUnitTest {

    @Mock
    private MailSender mailSenderMock;

    private MailClient fixture;

    @BeforeEach
    public void beforeMethodSetup() {
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
            Assertions.assertEquals("foo@mail.com", getAddresses(mimeMessage.getFrom()));
            Assertions.assertEquals("bar@mail.com,copy@mail.com", getAddresses(mimeMessage.getRecipients(Message.RecipientType.TO)));
            Assertions.assertEquals("foobar@mail.com", getAddresses(mimeMessage.getRecipients(Message.RecipientType.CC)));
            Assertions.assertEquals("secret@mail.com", getAddresses(mimeMessage.getRecipients(Message.RecipientType.BCC)));
            Assertions.assertEquals("foo@mail.com", getAddresses(mimeMessage.getReplyTo()));
            Assertions.assertNotNull(mimeMessage.getSentDate());
            Assertions.assertEquals("Testmail", mimeMessage.getSubject());
            Assertions.assertEquals("text/plain", mimeMessage.getContentType());

            Assertions.assertEquals("Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.", mimeMessage.getContent().toString());
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
            Assertions.assertEquals("foo@mail.com", getAddresses(mimeMessage.getFrom()));
            Assertions.assertEquals("bar@mail.com", getAddresses(mimeMessage.getRecipients(Message.RecipientType.TO)));
            Assertions.assertEquals("foo@mail.com", getAddresses(mimeMessage.getReplyTo()));
            Assertions.assertNotNull(mimeMessage.getSentDate());
            Assertions.assertEquals("Multipart Testmail", mimeMessage.getSubject());
            Assertions.assertEquals("text/plain", mimeMessage.getContentType());

            Assertions.assertEquals(MimeMultipart.class, mimeMessage.getContent().getClass());

            MimeMultipart multipart = (MimeMultipart) mimeMessage.getContent();

            Assertions.assertEquals(2L, multipart.getCount());
            Assertions.assertTrue(multipart.getContentType().startsWith("multipart/mixed"));
            Assertions.assertTrue(((MimeMultipart) multipart.getBodyPart(0).getContent()).getContentType().startsWith("multipart/related"));
            Assertions.assertEquals(1L, ((MimeMultipart) multipart.getBodyPart(0).getContent()).getCount());
            Assertions.assertEquals("Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.", ((MimeMultipart) multipart.getBodyPart(0).getContent()).getBodyPart(0).getContent().toString());
            Assertions.assertEquals("text/plain", ((MimeMultipart) multipart.getBodyPart(0).getContent()).getBodyPart(0).getContentType());
            Assertions.assertEquals("<html><head></head><body><h1>HTMLAttachment</h1></body></html>", multipart.getBodyPart(1).getContent().toString().replaceAll("\\s", ""));
            Assertions.assertEquals("index.html", multipart.getBodyPart(1).getFileName());
            Assertions.assertEquals("attachment", multipart.getBodyPart(1).getDisposition());
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
}
