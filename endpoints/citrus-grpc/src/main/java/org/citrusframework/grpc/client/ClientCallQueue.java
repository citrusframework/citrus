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

package org.citrusframework.grpc.client;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.message.GrpcMessage;

/**
 * Bounded queue holding the messages of a single gRPC call. Messages that arrive before the test
 * reads them are buffered here. When more than {@code maxBufferedMessages} messages arrive the call
 * is marked as overflowed so the next receive fails naming the bound instead of silently dropping
 * messages.
 *
 * @since 5.1
 */
public class ClientCallQueue {

    private final BlockingDeque<GrpcMessage> messages = new LinkedBlockingDeque<>();
    private final int maxBufferedMessages;
    private final String callId;

    private volatile boolean overflowed;

    public ClientCallQueue(String callId, int maxBufferedMessages) {
        this.callId = callId;
        this.maxBufferedMessages = maxBufferedMessages;
    }

    /**
     * Buffers the given message. Marks the queue as overflowed when the bound is exceeded.
     *
     * @param message the message to buffer
     * @return {@code false} when the bound was exceeded and the message was not buffered
     */
    public boolean offer(GrpcMessage message) {
        if (messages.size() >= maxBufferedMessages) {
            overflowed = true;
            return false;
        }

        return messages.offer(message);
    }

    /**
     * Polls the next buffered message.
     *
     * @param timeout the maximum time to wait in milliseconds
     * @return the next message or {@code null} when none arrived in time
     */
    public GrpcMessage poll(long timeout) {
        try {
            return messages.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CitrusRuntimeException("Interrupted while waiting for gRPC message on call '%s'".formatted(callId), e);
        }
    }

    /**
     * @return the number of currently buffered messages
     */
    public int size() {
        return messages.size();
    }

    /**
     * @return {@code true} when more messages arrived than the configured bound
     */
    public boolean isOverflowed() {
        return overflowed;
    }

    public String getCallId() {
        return callId;
    }

    public int getMaxBufferedMessages() {
        return maxBufferedMessages;
    }

    /**
     * Discards all buffered messages.
     */
    public void clear() {
        messages.clear();
    }
}
