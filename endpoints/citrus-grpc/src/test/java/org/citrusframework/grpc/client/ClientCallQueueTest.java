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

import org.citrusframework.grpc.message.GrpcMessage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientCallQueueTest {

    @Test
    public void shouldOfferAndPollMessagesInOrder() {
        ClientCallQueue queue = new ClientCallQueue("call-1", 10);

        assertThat(queue.offer(new GrpcMessage("first"))).isTrue();
        assertThat(queue.offer(new GrpcMessage("second"))).isTrue();
        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.isOverflowed()).isFalse();

        assertThat(queue.poll(100).getPayload()).isEqualTo("first");
        assertThat(queue.poll(100).getPayload()).isEqualTo("second");
        assertThat(queue.poll(100)).isNull();
    }

    @Test
    public void shouldReportOverflowWhenBoundIsExceeded() {
        ClientCallQueue queue = new ClientCallQueue("call-1", 2);

        assertThat(queue.offer(new GrpcMessage("first"))).isTrue();
        assertThat(queue.offer(new GrpcMessage("second"))).isTrue();
        assertThat(queue.offer(new GrpcMessage("third"))).isFalse();

        assertThat(queue.isOverflowed()).isTrue();
        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.getCallId()).isEqualTo("call-1");
        assertThat(queue.getMaxBufferedMessages()).isEqualTo(2);
    }

    @Test
    public void shouldClearBufferedMessages() {
        ClientCallQueue queue = new ClientCallQueue("call-1", 10);
        queue.offer(new GrpcMessage("first"));

        queue.clear();

        assertThat(queue.size()).isZero();
        assertThat(queue.poll(10)).isNull();
    }

    @Test
    public void shouldReturnNullWhenNoMessageArrivesInTime() {
        ClientCallQueue queue = new ClientCallQueue("call-1", 10);

        assertThat(queue.poll(50)).isNull();
    }
}
