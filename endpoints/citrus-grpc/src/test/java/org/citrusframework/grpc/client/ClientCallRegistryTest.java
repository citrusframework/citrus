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

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.ClientCall;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

public class ClientCallRegistryTest {

    private ClientCallRegistry registry;

    @BeforeMethod
    public void setUp() {
        registry = new ClientCallRegistry();
    }

    @Test
    public void shouldRegisterAndGetCall() {
        ClientCallQueue queue = new ClientCallQueue("call-1", 10);

        registry.register("call-1", queue, () -> {
        });

        assertThat(registry.get("call-1")).isSameAs(queue);
        assertThat(registry.getCallIds()).containsExactly("call-1");
        assertThat(registry.getLiveCall("call-1")).isNull();
    }

    @Test
    public void shouldReturnNullForUnknownCall() {
        assertThat(registry.get("unknown")).isNull();
        assertThat(registry.getLiveCall("unknown")).isNull();
        assertThat(registry.getCallIds()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSendMessageOnLiveCall() {
        ClientCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ClientCall.class);
        Descriptors.MethodDescriptor method = methodDescriptor();

        registry.register("call-1", new ClientCallQueue("call-1", 10), call, method, () -> {
        });

        assertThat(registry.getLiveCall("call-1")).isNotNull();
        assertThat(registry.getLiveCall("call-1").method()).isSameAs(method);

        registry.sendMessage("call-1", DynamicMessage.newBuilder(HelloRequest.getDescriptor()).build());

        Mockito.verify(call).sendMessage(any(DynamicMessage.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCompleteLiveCall() {
        ClientCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ClientCall.class);

        registry.register("call-1", new ClientCallQueue("call-1", 10), call, methodDescriptor(), () -> {
        });

        registry.complete("call-1");

        Mockito.verify(call).halfClose();
    }

    @Test
    public void shouldFailToSendMessageOnUnknownCall() {
        assertThatThrownBy(() -> registry.sendMessage("unknown", DynamicMessage.newBuilder(HelloRequest.getDescriptor()).build()))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("is not open for sending");

        assertThatThrownBy(() -> registry.complete("unknown"))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("is not open for sending");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRemoveCall() {
        ClientCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ClientCall.class);

        registry.register("call-1", new ClientCallQueue("call-1", 10), call, methodDescriptor(), () -> {
        });

        registry.remove("call-1");

        assertThat(registry.get("call-1")).isNull();
        assertThat(registry.getLiveCall("call-1")).isNull();
        assertThat(registry.getCallIds()).isEmpty();
    }

    @Test
    public void shouldCancelCallAndClearBufferedMessages() {
        AtomicBoolean cancelled = new AtomicBoolean();
        ClientCallQueue queue = new ClientCallQueue("call-1", 10);
        queue.offer(new GrpcMessage("first"));

        registry.register("call-1", queue, () -> cancelled.set(true));

        registry.cancel("call-1");

        assertThat(cancelled.get()).isTrue();
        assertThat(registry.get("call-1")).isNull();
        assertThat(queue.size()).isZero();
    }

    @Test
    public void shouldCancelAllCalls() {
        AtomicBoolean first = new AtomicBoolean();
        AtomicBoolean second = new AtomicBoolean();

        registry.register("call-1", new ClientCallQueue("call-1", 10), () -> first.set(true));
        registry.register("call-2", new ClientCallQueue("call-2", 10), () -> second.set(true));

        registry.cancelAll();

        assertThat(first.get()).isTrue();
        assertThat(second.get()).isTrue();
        assertThat(registry.getCallIds()).isEmpty();
    }

    private static Descriptors.MethodDescriptor methodDescriptor() {
        return HelloRequest.getDescriptor().getFile().getServices().get(0).getMethods().get(0);
    }
}
