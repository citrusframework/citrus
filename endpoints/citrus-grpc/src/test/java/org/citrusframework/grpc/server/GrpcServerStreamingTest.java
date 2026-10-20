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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * The simulator serves client-streaming and bidirectional-streaming calls: additional request
 * messages and the request completion are delivered to the test, and the replies go back on the
 * same live call.
 */
public class GrpcServerStreamingTest {

    private static final String STORE_USERS = "grpc2grpc.PetStoreService/StoreUsers";
    private static final String BULK_USERS = "grpc2grpc.PetStoreService/BulkUsers";
    private static final String DESCRIPTORS = "classpath:org/citrusframework/grpc/streaming/petstore.proto";

    private GrpcServer server;
    private ManagedChannel channel;
    private GrpcDescriptorRegistry registry;
    private TestContext context;

    @BeforeMethod
    public void setUp() {
        context = new TestContext();
        registry = new GrpcDescriptorRegistry(List.of(DESCRIPTORS));

        GrpcServerEndpointConfiguration configuration = new GrpcServerEndpointConfiguration();
        configuration.setPort(findAvailableTcpPort());
        configuration.setDescriptors(List.of(DESCRIPTORS));
        configuration.setTimeout(5000L);

        server = new GrpcServer(configuration);
        server.initialize();
        server.start();

        channel = ManagedChannelBuilder.forAddress("localhost", configuration.getPort()).usePlaintext().build();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void shouldServeClientStreamingCall() throws Exception {
        DynamicStreamClient client = new DynamicStreamClient(registry.findMethod(STORE_USERS));
        Descriptors.Descriptor userType = registry.findMessage("grpc2grpc.User");

        client.send(DynamicMessage.newBuilder(userType)
                .setField(userType.findFieldByName("id"), 1)
                .setField(userType.findFieldByName("username"), "ada")
                .setField(userType.findFieldByName("email"), "ada@example.com")
                .setField(userType.findFieldByName("phone"), "555-1")
                .build());
        client.send(DynamicMessage.newBuilder(userType)
                .setField(userType.findFieldByName("id"), 2)
                .setField(userType.findFieldByName("username"), "bob")
                .setField(userType.findFieldByName("email"), "bob@example.com")
                .setField(userType.findFieldByName("phone"), "555-2")
                .build());
        client.send(DynamicMessage.newBuilder(userType)
                .setField(userType.findFieldByName("id"), 3)
                .setField(userType.findFieldByName("username"), "cid")
                .setField(userType.findFieldByName("email"), "cid@example.com")
                .setField(userType.findFieldByName("phone"), "555-3")
                .build());
        client.halfClose();

        GrpcMessage call = receive();
        assertThat(call.getKind()).isEqualTo(GrpcMessageHeaders.KIND_CALL);
        assertThat(call.getMethod()).isEqualTo(STORE_USERS);
        assertThat(call.getMethodType()).isEqualTo(GrpcMessageHeaders.METHOD_TYPE_CLIENT_STREAMING);
        assertThat(call.getPayload(String.class)).contains("\"username\": \"ada\"");

        GrpcMessage second = receive();
        assertThat(second.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        assertThat(second.getStreamSequence()).isEqualTo(1);
        assertThat(second.getPayload(String.class)).contains("\"username\": \"bob\"");

        GrpcMessage third = receive();
        assertThat(third.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        assertThat(third.getStreamSequence()).isEqualTo(2);
        assertThat(third.getPayload(String.class)).contains("\"username\": \"cid\"");

        GrpcMessage completed = receive();
        assertThat(completed.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_END);
        assertThat(completed.getStatusCode()).isEqualTo("OK");
        assertThat(completed.isEndOfStream()).isTrue();

        server.createProducer().send(new GrpcMessage("{ \"msg\": \"received 3\" }")
                .kind(GrpcMessageHeaders.KIND_RESPONSE)
                .statusCode("OK")
                .callId(call.getCallId()), context);

        assertThat(client.await(5000L)).isTrue();
        assertThat(client.status().getCode()).isEqualTo(Status.Code.OK);
        assertThat(client.responses()).hasSize(1);
        assertThat(client.decode(0)).contains("received 3");
    }

    @Test
    public void shouldServeBidirectionalCall() throws Exception {
        DynamicStreamClient client = new DynamicStreamClient(registry.findMethod(BULK_USERS));
        Descriptors.Descriptor userType = registry.findMessage("grpc2grpc.User");

        client.send(DynamicMessage.newBuilder(userType)
                .setField(userType.findFieldByName("id"), 1)
                .setField(userType.findFieldByName("username"), "ada")
                .setField(userType.findFieldByName("email"), "ada@example.com")
                .setField(userType.findFieldByName("phone"), "555-1")
                .build());

        GrpcMessage call = receive();
        assertThat(call.getKind()).isEqualTo(GrpcMessageHeaders.KIND_CALL);
        assertThat(call.getMethodType()).isEqualTo(GrpcMessageHeaders.METHOD_TYPE_BIDI_STREAMING);

        server.createProducer().send(new GrpcMessage("{ \"id\": 10, \"username\": \"echo\", \"email\": \"echo@example.com\", \"phone\": \"555-10\" }")
                .kind(GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                .callId(call.getCallId()), context);

        assertThat(client.awaitResponses(1, 5000L)).isTrue();
        assertThat(client.decode(0)).contains("\"username\": \"echo\"");

        client.send(DynamicMessage.newBuilder(userType)
                .setField(userType.findFieldByName("id"), 2)
                .setField(userType.findFieldByName("username"), "bob")
                .setField(userType.findFieldByName("email"), "bob@example.com")
                .setField(userType.findFieldByName("phone"), "555-2")
                .build());
        GrpcMessage second = receive();
        assertThat(second.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        assertThat(second.getPayload(String.class)).contains("\"username\": \"bob\"");

        client.halfClose();
        GrpcMessage completed = receive();
        assertThat(completed.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_END);

        server.createProducer().send(new GrpcMessage("")
                .kind(GrpcMessageHeaders.KIND_STREAM_END)
                .statusCode("OK")
                .callId(call.getCallId()), context);

        assertThat(client.await(5000L)).isTrue();
        assertThat(client.status().getCode()).isEqualTo(Status.Code.OK);
    }

    private GrpcMessage receive() {
        return (GrpcMessage) server.createConsumer().receive(context, 5000L);
    }

    private static io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> method(Descriptors.MethodDescriptor method) {
        return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(method.isClientStreaming() && method.isServerStreaming()
                        ? io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING
                        : method.isClientStreaming()
                                ? io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING
                                : io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
                .setFullMethodName(method.getService().getFullName() + "/" + method.getName())
                .setRequestMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(method.getInputType())))
                .setResponseMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(method.getOutputType())))
                .build();
    }

    private final class DynamicStreamClient {

        private final List<DynamicMessage> responses = Collections.synchronizedList(new ArrayList<>());
        private final AtomicReference<Status> status = new AtomicReference<>();
        private final CountDownLatch done = new CountDownLatch(1);
        private final ClientCall<DynamicMessage, DynamicMessage> call;

        private DynamicStreamClient(Descriptors.MethodDescriptor method) {
            call = channel.newCall(method(method), CallOptions.DEFAULT);
            call.start(new ClientCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage message) {
                    responses.add(message);
                }

                @Override
                public void onClose(Status closeStatus, Metadata trailers) {
                    status.set(closeStatus);
                    done.countDown();
                }
            }, new Metadata());
            call.request(10);
        }

        private void send(DynamicMessage message) {
            call.sendMessage(message);
        }

        private void halfClose() {
            call.halfClose();
        }

        private boolean await(long timeoutMillis) throws InterruptedException {
            return done.await(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        private boolean awaitResponses(int count, long timeoutMillis) throws InterruptedException {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (System.currentTimeMillis() < deadline) {
                if (responses.size() >= count) {
                    return true;
                }
                Thread.sleep(20L);
            }
            return responses.size() >= count;
        }

        private List<DynamicMessage> responses() {
            return responses;
        }

        private Status status() {
            return status.get();
        }

        private String decode(int index) {
            return new org.citrusframework.grpc.message.GrpcMessageCodec(registry).decode(responses.get(index));
        }
    }
}
