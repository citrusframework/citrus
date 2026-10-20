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
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.DefaultMessageQueue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcServerConsumerTest {

    private static final String CORRELATION_KEY = "grpcServer";

    private DefaultMessageQueue queue;
    private GrpcServerConsumer consumer;
    private TestContext context;

    @BeforeMethod
    public void setUp() {
        queue = new DefaultMessageQueue("grpcServer.inbound");
        consumer = new GrpcServerConsumer(queue, CORRELATION_KEY, 1000L);
        context = new TestContext();
    }

    @Test
    public void shouldStoreCallIdOfReceivedCall() {
        queue.send(new GrpcMessage("{}")
                .callId("call-1")
                .kind(GrpcMessageHeaders.KIND_CALL));

        GrpcMessage message = (GrpcMessage) consumer.receive(context);

        assertThat(message.getCallId()).isEqualTo("call-1");
        assertThat(context.getVariables().get(CORRELATION_KEY)).isEqualTo("call-1");
    }

    @Test
    public void shouldNotFailOnMessageWithoutCallId() {
        queue.send(new GrpcMessage("{}").kind(GrpcMessageHeaders.KIND_CALL));

        GrpcMessage message = (GrpcMessage) consumer.receive(context);

        assertThat(message.getCallId()).isNull();
        assertThat(context.getVariables().get(CORRELATION_KEY)).isNull();
    }

    @Test
    public void shouldRemoveStaleCallIdOnMessageWithoutCallId() {
        context.getVariables().put(CORRELATION_KEY, "call-1");

        queue.send(new GrpcMessage("{}").kind(GrpcMessageHeaders.KIND_CALL));

        consumer.receive(context);

        assertThat(context.getVariables().get(CORRELATION_KEY)).isNull();
    }
}
