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
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.Message;
import org.springframework.util.FileCopyUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageHandlerAdapterTest {

    @Test
    public void testTextMessage() throws IOException {
        MessageHandlerAdapter messageHandlerAdapter = new MessageHandlerAdapter(new MessageHandler() {
            @Override
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "Testmail");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MIME_TYPE), "text/plain");

                try {
                    Assert.assertEquals(message.getPayload().toString(),
                            FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("text_mail.xml",
                                    MessageHandlerAdapterTest.class).getInputStream())));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
                return null;
            }
        });

        messageHandlerAdapter.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("text_mail.txt", MessageHandlerAdapterTest.class).getInputStream());
    }

    @Test
    public void testMultipartMessage() throws IOException {
        MessageHandlerAdapter messageHandlerAdapter = new MessageHandlerAdapter(new MessageHandler() {
            @Override
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "Multipart Testmail");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MIME_TYPE), "multipart/mixed");

                try {
                    Assert.assertEquals(message.getPayload().toString(),
                            FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("multipart_mail.xml",
                                    MessageHandlerAdapterTest.class).getInputStream())));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
                return null;
            }
        });

        messageHandlerAdapter.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("multipart_mail.txt", MessageHandlerAdapterTest.class).getInputStream());
    }

    @Test
    public void testBinaryMessage() throws IOException {
        MessageHandlerAdapter messageHandlerAdapter = new MessageHandlerAdapter(new MessageHandler() {
            @Override
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_FROM), "foo@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_TO), "bar@mail.com");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_CC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_BCC), "");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_SUBJECT), "This is brand_logo.png");
                Assert.assertEquals(message.getHeaders().get(CitrusMailMessageHeaders.MAIL_MIME_TYPE), "multipart/mixed");

                try {
                    Assert.assertEquals(message.getPayload().toString(),
                            FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("binary_mail.xml",
                                    MessageHandlerAdapterTest.class).getInputStream())));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
                return null;
            }
        });

        messageHandlerAdapter.deliver("foo@mail.com", "bar@mail.com",
                new ClassPathResource("binary_mail.txt", MessageHandlerAdapterTest.class).getInputStream());
    }
}
