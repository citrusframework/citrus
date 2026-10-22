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
import java.util.function.Function;

import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;

/**
 * Endpoint adapter used by the HTTP server contract test suite. The adapter records every request
 * that reached the server and lets the test decide which reply should be sent back. This keeps the
 * contract tests independent of the underlying transport.
 */
public class ContractEndpointAdapter implements EndpointAdapter {

    private final BlockingQueue<HttpMessage> receivedRequests = new LinkedBlockingQueue<>();
    private final Endpoint endpoint = new ContractEndpoint();

    private volatile Function<HttpMessage, Message> replyProvider = request -> null;

    @Override
    public Message handleMessage(Message message) {
        HttpMessage request = message instanceof HttpMessage httpMessage ? httpMessage : new HttpMessage(message);
        receivedRequests.add(request);
        return replyProvider.apply(request);
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return endpoint.getEndpointConfiguration();
    }

    /**
     * Sets the function providing the reply for a received request. The default implementation does
     * not reply at all, so the server answers with the default status and an empty body.
     *
     * @param replyProvider the reply provider.
     */
    public void setReplyProvider(Function<HttpMessage, Message> replyProvider) {
        this.replyProvider = replyProvider;
    }

    /**
     * Waits for the next request that reached the server.
     *
     * @param timeout the maximum time to wait.
     * @param unit the time unit of the timeout.
     * @return the received request.
     */
    public HttpMessage takeRequest(long timeout, TimeUnit unit) {
        try {
            HttpMessage request = receivedRequests.poll(timeout, unit);
            if (request == null) {
                throw new AssertionError("Timed out while waiting for a request to reach the server");
            }
            return request;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for a request to reach the server", e);
        }
    }

    /**
     * Checks whether the server received any request so far.
     *
     * @return true if at least one request reached the server.
     */
    public boolean hasReceivedRequests() {
        return !receivedRequests.isEmpty();
    }

    /**
     * Endpoint implementation required by the endpoint adapter contract.
     */
    private static final class ContractEndpoint extends AbstractEndpoint {

        private ContractEndpoint() {
            super(new DirectSyncEndpointConfiguration());
        }

        @Override
        public Producer createProducer() {
            return null;
        }

        @Override
        public Consumer createConsumer() {
            return null;
        }
    }
}
