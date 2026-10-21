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

package org.citrusframework.http.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.report.MessageListener;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the {@link LoggingServerInterceptor}.
 */
public class LoggingServerInterceptorTest extends AbstractTestNGUnitTest {

    private final RecordingMessageListener messageListener = new RecordingMessageListener();

    @BeforeMethod
    public void resetRecordedMessages() {
        messageListener.inboundMessages.clear();
        messageListener.outboundMessages.clear();
    }

    @Test
    public void shouldReportRequestAndReplyToMessageListeners() {
        MessageListeners messageListeners = new MessageListeners();
        messageListeners.addMessageListener(messageListener);

        LoggingServerInterceptor interceptor = new LoggingServerInterceptor();
        interceptor.setMessageListener(messageListeners);

        HttpMessage request = new HttpMessage("request body")
                .method(HttpMethod.POST)
                .version("HTTP/1.1")
                .uri("/orders/42")
                .header("X-Trace", "abc");

        Message reply = interceptor.intercept(request, chain -> new HttpMessage("reply body")
                .status(HttpStatus.CREATED)
                .version("HTTP/1.1"));

        assertThat(reply.getPayload()).isEqualTo("reply body");

        assertThat(messageListener.inboundMessages).hasSize(1);
        assertThat(messageListener.inboundMessages.get(0).getPayload().toString())
                .contains("HTTP/1.1 POST /orders/42")
                .contains("X-Trace:abc")
                .contains("request body")
                .doesNotContain("citrus_");

        assertThat(messageListener.outboundMessages).hasSize(1);
        assertThat(messageListener.outboundMessages.get(0).getPayload().toString())
                .contains("HTTP/1.1 201")
                .contains("reply body")
                .doesNotContain("citrus_");
    }

    @Test
    public void shouldReportQueryParametersInStartLine() {
        MessageListeners messageListeners = new MessageListeners();
        messageListeners.addMessageListener(messageListener);

        LoggingServerInterceptor interceptor = new LoggingServerInterceptor();
        interceptor.setMessageListener(messageListeners);

        HttpMessage request = new HttpMessage()
                .method(HttpMethod.GET)
                .version("HTTP/1.1")
                .uri("/orders/42")
                .queryParams("type=gold&id=7");

        interceptor.intercept(request, chain -> new HttpMessage("reply"));

        assertThat(messageListener.inboundMessages.get(0).getPayload().toString())
                .contains("HTTP/1.1 GET /orders/42?type=gold&id=7");
    }

    @Test
    public void shouldReportNoReply() {
        MessageListeners messageListeners = new MessageListeners();
        messageListeners.addMessageListener(messageListener);

        LoggingServerInterceptor interceptor = new LoggingServerInterceptor();
        interceptor.setMessageListener(messageListeners);

        HttpMessage request = new HttpMessage("request body")
                .method(HttpMethod.GET)
                .version("HTTP/1.1")
                .uri("/orders/42");

        Message reply = interceptor.intercept(request, chain -> null);

        assertThat(reply).isNull();
        assertThat(messageListener.outboundMessages).hasSize(1);
        assertThat(messageListener.outboundMessages.get(0).getPayload().toString())
                .contains("<no reply — default status>");
    }

    @Test
    public void shouldNotReportWithoutMessageListeners() {
        LoggingServerInterceptor interceptor = new LoggingServerInterceptor();

        assertThat(interceptor.hasMessageListeners()).isFalse();

        interceptor.intercept(new HttpMessage("request body"), chain -> new HttpMessage("reply"));

        assertThat(messageListener.inboundMessages).isEmpty();
        assertThat(messageListener.outboundMessages).isEmpty();
    }

    @Test
    public void shouldPassRequestToTheChain() {
        LoggingServerInterceptor interceptor = new LoggingServerInterceptor();

        HttpMessage request = new HttpMessage("request body");
        Message reply = interceptor.intercept(request, chain -> {
            assertThat(chain).isNotNull();
            return new HttpMessage("reply");
        });

        assertThat(reply.getPayload()).isEqualTo("reply");
    }

    private static final class RecordingMessageListener implements MessageListener {

        private final List<Message> inboundMessages = new ArrayList<>();
        private final List<Message> outboundMessages = new ArrayList<>();

        @Override
        public void onInboundMessage(Message message, TestContext context) {
            inboundMessages.add(message);
        }

        @Override
        public void onOutboundMessage(Message message, TestContext context) {
            outboundMessages.add(message);
        }
    }
}
