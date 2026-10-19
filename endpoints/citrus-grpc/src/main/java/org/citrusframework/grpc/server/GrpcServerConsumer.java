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

package org.citrusframework.grpc.server;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.selector.DelegatingMessageSelector;
import org.citrusframework.messaging.SelectiveConsumer;

/**
 * Consumer of the gRPC simulator. It reads call messages from the inbound queue and stores the call
 * id under the server's correlation key so that the next producer send answers that call.
 */
public class GrpcServerConsumer implements SelectiveConsumer {

    private final MessageQueue queue;
    private final MessageCorrelator correlator;
    private final String correlationKeyName;
    private final long timeout;

    public GrpcServerConsumer(MessageQueue queue,
                              MessageCorrelator correlator,
                              String correlationKeyName,
                              long timeout) {
        this.queue = queue;
        this.correlator = correlator;
        this.correlationKeyName = correlationKeyName;
        this.timeout = timeout;
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context, timeout);
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(null, context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = selector == null
                ? queue.receive(timeout)
                : queue.receive(new DelegatingMessageSelector(selector, context), timeout);

        if (message == null) {
            throw new MessageTimeoutException(timeout, queue.getName());
        }

        context.getVariables().put(correlationKeyName, message.getHeader(GrpcMessageHeaders.CALL_ID));
        return message;
    }

    @Override
    public String getName() {
        return queue.getName();
    }
}
