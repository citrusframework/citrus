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

package com.consol.citrus.mail.server;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.subethamail.smtp.RejectException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class MailServerTest {

    private EndpointAdapter endpointAdapterMock = EasyMock.createMock(EndpointAdapter.class);

    @Test
    @SuppressWarnings("unchecked")
    public void testTextMessage() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

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
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("text_mail.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(endpointAdapterMock);

        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));
        mailServer.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("text_mail.txt", MailServer.class).getInputStream());

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipartMessage() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE), "2006-10-26T13:10:50+0200");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "multipart/mixed");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("multipart_mail.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(endpointAdapterMock);

        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));
        mailServer.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("multipart_mail.txt", MailServer.class).getInputStream());

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBinaryMessage() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@consol.de>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE), "2013-12-06T10:27:41+0100");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "multipart/mixed");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("binary_mail.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(endpointAdapterMock);

        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));
        mailServer.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("binary_mail.txt", MailServer.class).getInputStream());

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAutoAcceptDisabled() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("accept-request.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return new DefaultMessage(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("accept-response.xml",
                        MailServer.class).getInputStream())));
            }
        }).once();

        replay(endpointAdapterMock);

        mailServer.setAutoAccept(false);
        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAutoAcceptDisabledWithTimeout() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);

        reset(endpointAdapterMock);
        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andReturn(null).once();
        replay(endpointAdapterMock);

        mailServer.setAutoAccept(false);
        try {
            mailServer.accept("foo@mail.com", "bar@mail.com");
            Assert.fail("Missing runtime exception due to missing accept response");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Did not receive accept response"));
        }

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAutoAcceptDisabledWithInvalidAcceptResponse() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);

        reset(endpointAdapterMock);
        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andReturn(new DefaultMessage(99L)).once();
        replay(endpointAdapterMock);

        mailServer.setAutoAccept(false);
        try {
            mailServer.accept("foo@mail.com", "bar@mail.com");
            Assert.fail("Missing runtime exception due to invalid accept response");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to read accept response"));
        }

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTextMessageSplitting() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);
        mailServer.setSplitMultipart(true);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

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
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("text_mail.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(endpointAdapterMock);

        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));
        mailServer.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("text_mail.txt", MailServer.class).getInputStream());

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipartMessageSplitting() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);
        mailServer.setSplitMultipart(true);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE), "2006-10-26T13:10:50+0200");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain; charset=utf-8");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("multipart_mail_1.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertNull(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE), "2006-10-26T13:10:50+0200");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/html; charset=utf-8");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FILENAME), "index.html");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("multipart_mail_2.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(endpointAdapterMock);

        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));
        mailServer.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("multipart_mail.txt", MailServer.class).getInputStream());

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBinaryMessageSplitting() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);
        mailServer.setSplitMultipart(true);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@consol.de>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE), "2013-12-06T10:27:41+0100");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain; charset=ISO-8859-15; format=flowed");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("binary_mail_1.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@consol.de>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_DATE), "2013-12-06T10:27:41+0100");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "image/png");
                Assert.assertEquals(message.getHeader(CitrusMailMessageHeaders.MAIL_FILENAME), "brand_logo.png");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("binary_mail_2.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(endpointAdapterMock);

        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));
        mailServer.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("binary_mail.txt", MailServer.class).getInputStream());

        verify(endpointAdapterMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSimulateError() throws IOException {
        MailServer mailServer = new MailServer();
        mailServer.setEndpointAdapter(endpointAdapterMock);

        reset(endpointAdapterMock);

        expect(endpointAdapterMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];

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
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("text_mail.xml",
                                    MailServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return new DefaultMessage(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("error-response.xml",
                        MailServer.class).getInputStream())));
            }
        }).once();

        replay(endpointAdapterMock);

        Assert.assertTrue(mailServer.accept("foo@mail.com", "bar@mail.com"));

        try {
            mailServer.deliver("foo@mail.com", "bar@mail.com",
                    new ClassPathResource("text_mail.txt", MailServer.class).getInputStream());
            throw new CitrusRuntimeException("Missing reject exception due to simulated error");
        } catch (RejectException e) {
            Assert.assertEquals(e.getCode(), 443);
            Assert.assertEquals(e.getErrorResponse(), "443 Failed!");
        }

        verify(endpointAdapterMock);
    }
}
