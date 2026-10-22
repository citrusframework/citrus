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

package org.citrusframework.http.server.contract;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.report.MessageListener;

/**
 * Message listener recording the raw messages reported by the server tracing interceptor.
 */
public class ContractMessageListener implements MessageListener {

    private final BlockingQueue<Message> inboundMessages = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> outboundMessages = new LinkedBlockingQueue<>();

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        inboundMessages.add(message);
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        outboundMessages.add(message);
    }

    /**
     * Takes the next inbound message that has been reported to this listener.
     *
     * @param timeout the maximum time to wait.
     * @param unit the time unit of the timeout.
     * @return the inbound message.
     */
    public Message takeInboundMessage(long timeout, TimeUnit unit) {
        return take(inboundMessages, timeout, unit, "inbound");
    }

    /**
     * Takes the next outbound message that has been reported to this listener.
     *
     * @param timeout the maximum time to wait.
     * @param unit the time unit of the timeout.
     * @return the outbound message.
     */
    public Message takeOutboundMessage(long timeout, TimeUnit unit) {
        return take(outboundMessages, timeout, unit, "outbound");
    }

    private Message take(BlockingQueue<Message> messages, long timeout, TimeUnit unit, String type) {
        try {
            Message message = messages.poll(timeout, unit);
            if (message == null) {
                throw new AssertionError("Did not receive any %s message".formatted(type));
            }
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for the %s message".formatted(type), e);
        }
    }
}
