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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

public class ServerCallRegistryTest {

    private ServerCallRegistry registry;
    private ScheduledExecutorService scheduler;

    @BeforeMethod
    public void setUp() {
        registry = new ServerCallRegistry();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRegisterAndGetCall() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        Metadata requestMetadata = new Metadata();

        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), requestMetadata, 0L, scheduler);

        assertThat(activeCall.getCallId()).isEqualTo("call-1");
        assertThat(activeCall.getMethod()).isSameAs(methodDescriptor());
        assertThat(activeCall.getRequestMetadata()).isSameAs(requestMetadata);
        assertThat(activeCall.isClosed()).isFalse();
        assertThat(activeCall.isCancelled()).isFalse();
        assertThat(registry.get("call-1")).isSameAs(activeCall);
        assertThat(registry.getCalls()).containsExactly(activeCall);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRemoveAndClearCalls() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        registry.register("call-1", call, methodDescriptor(), new Metadata(), 0L, scheduler);

        registry.remove("call-1");
        assertThat(registry.get("call-1")).isNull();

        registry.register("call-2", call, methodDescriptor(), new Metadata(), 0L, scheduler);
        registry.clear();
        assertThat(registry.getCalls()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSendHeadersOnlyOnce() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 0L, scheduler);

        activeCall.sendHeaders(new Metadata());
        activeCall.sendHeaders(new Metadata());

        Mockito.verify(call, Mockito.times(1)).sendHeaders(any(Metadata.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSendMessage() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 0L, scheduler);

        activeCall.sendMessage(DynamicMessage.newBuilder(HelloRequest.getDescriptor()).build());

        Mockito.verify(call).sendMessage(any(DynamicMessage.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFailToSendMessageOnClosedCall() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 0L, scheduler);

        activeCall.close(Status.OK, new Metadata());

        assertThat(activeCall.isClosed()).isTrue();
        assertThatThrownBy(() -> activeCall.sendMessage(DynamicMessage.newBuilder(HelloRequest.getDescriptor()).build()))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("is already closed");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCloseCallOnlyOnce() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 0L, scheduler);

        activeCall.close(Status.OK, new Metadata());
        activeCall.close(Status.INTERNAL, new Metadata());

        Mockito.verify(call, Mockito.times(1)).close(any(Status.class), any(Metadata.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCloseCallOnIdleTimeout() throws InterruptedException {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        CountDownLatch closed = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            closed.countDown();
            return null;
        }).when(call).close(any(Status.class), any(Metadata.class));

        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 100L, scheduler);

        activeCall.startTimeout();

        assertThat(closed.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(activeCall.isClosed()).isTrue();
        assertThat(registry.get("call-1")).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRestartTimeoutOnEverySend() throws InterruptedException {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 1000L, scheduler);

        activeCall.startTimeout();
        activeCall.restartTimeout();

        assertThat(activeCall.isClosed()).isFalse();
        assertThat(registry.get("call-1")).isSameAs(activeCall);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldMarkCallCancelled() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 0L, scheduler);

        activeCall.markCancelled();

        assertThat(activeCall.isCancelled()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCountBufferedRequestMessages() {
        ServerCall<DynamicMessage, DynamicMessage> call = Mockito.mock(ServerCall.class);
        ServerCallRegistry.ActiveCall activeCall = registry.register(
                "call-1", call, methodDescriptor(), new Metadata(), 0L, scheduler);

        assertThat(activeCall.incrementBufferedRequestCount()).isEqualTo(1);
        assertThat(activeCall.incrementBufferedRequestCount()).isEqualTo(2);
    }

    private static Descriptors.MethodDescriptor methodDescriptor() {
        return HelloRequest.getDescriptor().getFile().getServices().get(0).getMethods().get(0);
    }
}
