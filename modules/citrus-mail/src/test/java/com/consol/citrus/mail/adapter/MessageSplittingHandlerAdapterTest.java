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

package com.consol.citrus.mail.adapter;

import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.message.MessageHandler;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.Message;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageSplittingHandlerAdapterTest {

    private MessageHandler messageHandlerMock = EasyMock.createMock(MessageHandler.class);

    @Test
    @SuppressWarnings("unchecked")
    public void testTextMessage() throws IOException {
        MessageSplittingHandlerAdapter messageHandlerAdapter = new MessageSplittingHandlerAdapter(messageHandlerMock);

        reset(messageHandlerMock);

        expect(messageHandlerMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message<?> answer() throws Throwable {
                Message<?> message = (Message<?>) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertNull(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com,copy@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "foobar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "secret@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_REPLY_TO), "reply@mail.com");
                Assert.assertNull(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_DATE));
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "Testmail");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload().toString()),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("text_mail.xml",
                                    MessageSplittingHandlerAdapterTest.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(messageHandlerMock);

        messageHandlerAdapter.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("text_mail.txt", MessageSplittingHandlerAdapterTest.class).getInputStream());

        verify(messageHandlerMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipartMessage() throws IOException {
        MessageSplittingHandlerAdapter messageHandlerAdapter = new MessageSplittingHandlerAdapter(messageHandlerMock);

        reset(messageHandlerMock);

        expect(messageHandlerMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message<?> answer() throws Throwable {
                Message<?> message = (Message<?>) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertNull(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_DATE), "2006-10-26T13:10:50+0200");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain; charset=utf-8");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload().toString()),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("multipart_mail_1.xml",
                                    MessageSplittingHandlerAdapterTest.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        expect(messageHandlerMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message<?> answer() throws Throwable {
                Message<?> message = (Message<?>) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertNull(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MESSAGE_ID));
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_REPLY_TO), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_DATE), "2006-10-26T13:10:50+0200");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/html; charset=utf-8");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FILENAME), "index.html");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload().toString()),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("multipart_mail_2.xml",
                                    MessageSplittingHandlerAdapterTest.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(messageHandlerMock);

        messageHandlerAdapter.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("multipart_mail.txt", MessageSplittingHandlerAdapterTest.class).getInputStream());

        verify(messageHandlerMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBinaryMessage() throws IOException {
        MessageSplittingHandlerAdapter messageHandlerAdapter = new MessageSplittingHandlerAdapter(messageHandlerMock);

        reset(messageHandlerMock);

        expect(messageHandlerMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message<?> answer() throws Throwable {
                Message<?> message = (Message<?>) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@consol.de>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_DATE), "2013-12-06T10:27:41+0100");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "text/plain; charset=ISO-8859-15; format=flowed");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload().toString()),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("binary_mail_1.xml",
                                    MessageSplittingHandlerAdapterTest.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        expect(messageHandlerMock.handleMessage(anyObject(Message.class))).andAnswer(new IAnswer() {
            @Override
            public Message<?> answer() throws Throwable {
                Message<?> message = (Message<?>) getCurrentArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MESSAGE_ID), "<52A1988D.2060403@consol.de>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "FooBar <foobar@mail.com>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_REPLY_TO), "Foo <foo@mail.com>");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_DATE), "2013-12-06T10:27:41+0100");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE), "image/png;");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FILENAME), "brand_logo.png");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload().toString()),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("binary_mail_2.xml",
                                    MessageSplittingHandlerAdapterTest.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return null;
            }
        }).once();

        replay(messageHandlerMock);

        messageHandlerAdapter.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("binary_mail.txt", MessageSplittingHandlerAdapterTest.class).getInputStream());

        verify(messageHandlerMock);
    }
}
