/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.mail.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import com.icegreen.greenmail.mail.MailAddress;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.mail.message.CitrusMailMessageHeaders;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.TestUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class MailServerTest {

    private AutoCloseable mockitoContext;

    @Mock
    private EndpointAdapter endpointAdapterMock;

    private MailServer fixture;

    @BeforeMethod
    void beforeMethodSetup() {
        mockitoContext = MockitoAnnotations.openMocks(this);

        fixture = new MailServer();
        fixture.setEndpointAdapter(endpointAdapterMock);
    }

    @Test
    void testTextMessage() throws IOException, MessagingException {
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com,copy@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "foobar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "secret@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "reply@mail.com");
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Testmail");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain");

            try {
                Assert.assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("text_mail.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        })
                .when(endpointAdapterMock).handleMessage(any(Message.class));

        MimeMessage message = new MimeMessage(fixture.getSession(), Resources.create("text_mail.txt", MailServer.class).getInputStream());
        fixture.deliver(message);

        // Because of autoAccept = true
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));
    }

    @Test
    void testMultipartMessage() throws IOException, MessagingException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");

            // compare the Date as a Date rather than a String, otherwise this test fails outside the "+1" timezone
            Date actualDate = dateFormat.parse((String)message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Date expectedDateDate = dateFormat.parse("2006-10-26T13:10:50+0200");
            Assert.assertEquals(actualDate, expectedDateDate);

            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "multipart/mixed");

            try {
                Assert.assertEquals(
                        TestUtils.normalizeLineEndings(
                            message.getPayload(String.class).replaceAll("\\s", "")
                        ),
                        TestUtils.normalizeLineEndings(
                            FileUtils.readToString(Resources.create("multipart_mail.xml", MailServer.class)).replaceAll("\\s", "")
                        )
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        }).when(endpointAdapterMock).handleMessage(any(Message.class));

        MimeMessage message = new MimeMessage(fixture.getSession(), Resources.create("multipart_mail.txt", MailServer.class).getInputStream());
        fixture.deliver(message);

        // Because of autoAccept = true
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));
    }

    @Test
    void testBinaryMessage() throws IOException, MessagingException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@foo.bar>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");

            // compare the Date as a Date rather than a String, otherwsie this test fails outside the "+1" timezone
            Date actualDate = dateFormat.parse((String)message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Date expectedDateDate = dateFormat.parse("2013-12-06T10:27:41+0100");
            Assert.assertEquals(actualDate, expectedDateDate);

            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "multipart/mixed");

            try {
                Assert.assertEquals(
                        message.getPayload(String.class).replaceAll("\\s", ""),
                        FileUtils.readToString(Resources.create("binary_mail.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        }).when(endpointAdapterMock).handleMessage(any(Message.class));

        MimeMessage message = new MimeMessage(fixture.getSession(), Resources.create("binary_mail.txt", MailServer.class).getInputStream());
        fixture.deliver(message);

        // Because of autoAccept = true
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));
    }

    @Test
    void testAutoAcceptDisabled() {
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());

            try {
                Assert.assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("accept-request.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return new DefaultMessage(
                FileUtils.readToString(Resources.create("accept-response.xml",MailServer.class))
            );
        }).when(endpointAdapterMock).handleMessage(any(Message.class));

        fixture.setAutoAccept(false);
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));
    }

    @Test
    void testAutoAcceptDisabledWithTimeout() {
        when(endpointAdapterMock.handleMessage(any(Message.class))).thenReturn(null);
        fixture.setAutoAccept(false);

        try {
            fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com")));
            Assert.fail("Missing runtime exception due to missing accept response");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Did not receive accept response"));
        }
    }

    @Test
    void testAutoAcceptDisabledWithInvalidAcceptResponse() {
        when(endpointAdapterMock.handleMessage(any(Message.class))).thenReturn(new DefaultMessage(99L));
        fixture.setAutoAccept(false);

        try {
            fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com")));
            Assert.fail("Missing runtime exception due to invalid accept response");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to read accept response"));
        }
    }

    @Test
    void testTextMessageSplitting() throws IOException, MessagingException {
        fixture.setSplitMultipart(true);

        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com,copy@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "foobar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "secret@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "reply@mail.com");
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Testmail");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain");

            try {
                Assert.assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("text_mail.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        }).when(endpointAdapterMock).handleMessage(any(Message.class));

        MimeMessage message = new MimeMessage(fixture.getSession(), Resources.create("text_mail.txt", MailServer.class).getInputStream());
        fixture.deliver(message);

        // Because of autoAccept = true
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));
    }

    @Test
    void testMultipartMessageSplitting() throws IOException, MessagingException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        fixture.setSplitMultipart(true);

        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");

            // compare dates as Date rather than String otherwise this test fails outside the "+1" timezone
            Date actualDate = dateFormat.parse((String)message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Date expectedDateDate = dateFormat.parse("2006-10-26T13:10:50+0200");
            Assert.assertEquals(actualDate, expectedDateDate);

            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain; charset=utf-8");

            try {
                Assert.assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("multipart_mail_1.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        }).doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");

            // compare dates as Date rather than String otherwise this test fails outside the "+1" timezone
            Date actualDate = dateFormat.parse((String)message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Date expectedDateDate = dateFormat.parse("2006-10-26T13:10:50+0200");
            Assert.assertEquals(actualDate, expectedDateDate);

            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/html; charset=utf-8");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FILENAME), "index.html");

            try {
                Assert.assertEquals(
                        TestUtils.normalizeLineEndings(
                            message.getPayload(String.class).replaceAll("\\s", "")
                        ),
                        TestUtils.normalizeLineEndings(
                            FileUtils.readToString(Resources.create("multipart_mail_2.xml", MailServer.class)).replaceAll("\\s", "")
                        )
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        }).when(endpointAdapterMock).handleMessage(any(Message.class));

        MimeMessage message = new MimeMessage(fixture.getSession(), Resources.create("multipart_mail.txt", MailServer.class).getInputStream());
        fixture.deliver(message);

        // Because of autoAccept = true
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));
    }

    @Test
    void testBinaryMessageSplitting() throws IOException, MessagingException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        fixture.setSplitMultipart(true);

        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@foo.bar>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");

            // compare dates as Date rather than String otherwise this test fails outside the "+1" timezone
            Date actualDate = dateFormat.parse((String)message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Date expectedDateDate = dateFormat.parse("2013-12-06T10:27:41+0100");
            Assert.assertEquals(actualDate, expectedDateDate);

            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain; charset=ISO-8859-15; format=flowed");

            try {
                Assert.assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("binary_mail_1.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        }).doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@foo.bar>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");

            // compare dates as Date rather than String otherwise this test fails outside the "+1" timezone
            Date actualDate = dateFormat.parse((String)message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Date expectedDateDate = dateFormat.parse("2013-12-06T10:27:41+0100");
            Assert.assertEquals(actualDate, expectedDateDate);

            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "image/png");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FILENAME), "brand_logo.png");

            try {
                Assert.assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("binary_mail_2.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return null;
        }).when(endpointAdapterMock).handleMessage(any(Message.class));

        MimeMessage message = new MimeMessage(fixture.getSession(), Resources.create("binary_mail.txt", MailServer.class).getInputStream());
        fixture.deliver(message);

        // Because of autoAccept = true
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));
    }

    @Test
    void testSimulateError() throws IOException, MessagingException {
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            Assert.assertNotNull(message.getPayload());
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com,copy@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "foobar@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "secret@mail.com");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "reply@mail.com");
            Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE));
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Testmail");
            Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain");

            try {
                Assert.assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("text_mail.xml", MailServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }

            return new DefaultMessage(
                    FileUtils.readToString(Resources.create("error-response.xml", MailServer.class)));
        }).when(endpointAdapterMock).handleMessage(any(Message.class));

        MimeMessage message = new MimeMessage(fixture.getSession(), Resources.create("text_mail.txt", MailServer.class).getInputStream());
        Assert.assertTrue(fixture.accept("foo@mail.com", Collections.singletonList(new MailAddress("bar@mail.com"))));

        try {
            fixture.deliver(message);
            throw new CitrusRuntimeException("Missing reject exception due to simulated error");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "443 Failed!");
        }
    }

    @AfterMethod
    void afterMethodTeardown() throws Exception {
        mockitoContext.close();
    }
}
