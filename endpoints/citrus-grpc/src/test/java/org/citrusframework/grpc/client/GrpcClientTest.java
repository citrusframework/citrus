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

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import io.grpc.HandlerRegistry;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.test.proto.common.Money;
import org.citrusframework.grpc.test.proto.helloworld.GetUserRequest;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.citrusframework.grpc.test.proto.helloworld.UserInfo;
import org.citrusframework.grpc.test.proto.market.Price;
import org.citrusframework.grpc.test.proto.market.PriceSubscription;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

public class GrpcClientTest {

    private static final String SAY_HELLO = "helloworld.Greeter/SayHello";
    private static final String GET_USER = "helloworld.Greeter/GetUser";
    private static final String SUBSCRIBE = "market.Prices/Subscribe";
    private static final String WATCH = "market.Prices/Watch";

    private static final String DESCRIPTORS = "classpath:descriptors/test-protos.binpb";

    private Server server;
    private int port;

    private CountDownLatch watchStarted;
    private CountDownLatch watchCancelled;
    private AtomicReference<Metadata> receivedHeaders;

    private GrpcClient client;

    @BeforeClass
    public void startServer() throws IOException {
        port = findAvailableTcpPort();

        server = ServerBuilder.forPort(port)
                .fallbackHandlerRegistry(new HandlerRegistry() {
                    @Override
                    public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
                        return switch (methodName) {
                            case SAY_HELLO -> ServerMethodDefinition.create(
                                    method(SAY_HELLO, MethodDescriptor.MethodType.UNARY,
                                            HelloRequest.getDescriptor(), HelloReply.getDescriptor()),
                                    sayHelloHandler());
                            case GET_USER -> ServerMethodDefinition.create(
                                    method(GET_USER, MethodDescriptor.MethodType.UNARY,
                                            GetUserRequest.getDescriptor(), UserInfo.getDescriptor()),
                                    getUserHandler());
                            case SUBSCRIBE -> ServerMethodDefinition.create(
                                    method(SUBSCRIBE, MethodDescriptor.MethodType.SERVER_STREAMING,
                                            PriceSubscription.getDescriptor(), Price.getDescriptor()),
                                    subscribeHandler());
                            case WATCH -> ServerMethodDefinition.create(
                                    method(WATCH, MethodDescriptor.MethodType.SERVER_STREAMING,
                                            PriceSubscription.getDescriptor(), Price.getDescriptor()),
                                    watchHandler());
                            default -> null;
                        };
                    }
                })
                .build()
                .start();
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    @BeforeMethod
    public void setUp() {
        watchStarted = new CountDownLatch(1);
        watchCancelled = new CountDownLatch(1);
        receivedHeaders = new AtomicReference<>();

        GrpcClientEndpointConfiguration configuration = new GrpcClientEndpointConfiguration();
        configuration.setTarget("localhost:" + port);
        configuration.setDescriptors(List.of(DESCRIPTORS));
        client = new GrpcClient(configuration);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (client != null) {
            client.destroy();
        }
    }

    @Test
    public void shouldCallUnaryMethod() {
        TestContext context = new TestContext();

        client.send(new GrpcMessage("{\"name\":\"Ada\"}")
                .method(SAY_HELLO)
                .metadata("x-request-id", "42"), context);

        GrpcMessage response = (GrpcMessage) client.receive(context, 5000);

        assertThat(response.getKind()).isEqualTo(GrpcMessageHeaders.KIND_RESPONSE);
        assertThat(response.getStatusCode()).isEqualTo("OK");
        assertThat(response.getMethod()).isEqualTo(SAY_HELLO);
        assertThat(response.getMethodType()).isEqualTo(GrpcMessageHeaders.METHOD_TYPE_UNARY);
        assertThat(response.getCallId()).isNotEmpty();
        assertThat(response.isEndOfStream()).isTrue();
        assertThat(response.getPayload().toString()).contains("Hello Ada");
        assertThat(response.getHeader(GrpcMessageHeaders.TRAILER_PREFIX + "x-trailer")).isEqualTo("trailer-value");

        assertThat(receivedHeaders.get().get(Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER)))
                .isEqualTo("42");
    }

    @Test
    public void shouldReceiveStreamMessagesInOrder() {
        TestContext context = new TestContext();

        client.send(new GrpcMessage("{\"symbol\":\"ACME\"}").method(SUBSCRIBE), context);

        for (int i = 0; i < 3; i++) {
            GrpcMessage message = (GrpcMessage) client.receive(context, 5000);
            assertThat(message.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
            assertThat(message.getStreamSequence()).isEqualTo(i);
            assertThat(message.getPayload().toString()).contains("ACME");
        }

        GrpcMessage end = (GrpcMessage) client.receive(context, 5000);
        assertThat(end.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_END);
        assertThat(end.getStatusCode()).isEqualTo("OK");
        assertThat(end.isEndOfStream()).isTrue();
    }

    @Test
    public void shouldBufferMessagesArrivingBeforeReceive() throws InterruptedException {
        TestContext context = new TestContext();

        client.send(new GrpcMessage("{\"symbol\":\"ACME\"}").method(SUBSCRIBE), context);
        Thread.sleep(500);

        GrpcMessage first = (GrpcMessage) client.receive(context, 5000);
        assertThat(first.getKind()).isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        assertThat(first.getStreamSequence()).isZero();
    }

    @Test
    public void shouldFailWhenBufferBoundIsExceeded() throws InterruptedException {
        GrpcClientEndpointConfiguration configuration = new GrpcClientEndpointConfiguration();
        configuration.setTarget("localhost:" + port);
        configuration.setDescriptors(List.of(DESCRIPTORS));
        configuration.setMaxBufferedMessages(2);
        client.destroy();
        client = new GrpcClient(configuration);

        TestContext context = new TestContext();
        client.send(new GrpcMessage("{\"symbol\":\"ACME\"}").method(SUBSCRIBE), context);
        Thread.sleep(500);

        assertThatThrownBy(() -> client.receive(context, 1000))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("2");
    }

    @Test
    public void shouldReportDeadlineExceeded() {
        GrpcClientEndpointConfiguration configuration = new GrpcClientEndpointConfiguration();
        configuration.setTarget("localhost:" + port);
        configuration.setDescriptors(List.of(DESCRIPTORS));
        configuration.setDeadline(Duration.ofMillis(100));
        client.destroy();
        client = new GrpcClient(configuration);

        TestContext context = new TestContext();
        client.send(new GrpcMessage("{\"id\":\"slow\"}").method(GET_USER), context);

        GrpcMessage response = (GrpcMessage) client.receive(context, 5000);
        assertThat(response.getKind()).isEqualTo(GrpcMessageHeaders.KIND_RESPONSE);
        assertThat(response.getStatusCode()).isEqualTo("DEADLINE_EXCEEDED");
    }

    @Test
    public void shouldRaiseReceiveTimeoutInsteadOfDeadline() {
        TestContext context = new TestContext();
        client.send(new GrpcMessage("{\"id\":\"slow\"}").method(GET_USER), context);

        assertThatThrownBy(() -> client.receive(context, 200))
                .isInstanceOf(MessageTimeoutException.class);
    }

    @Test
    public void shouldCancelCall() throws InterruptedException {
        TestContext context = new TestContext();
        client.send(new GrpcMessage("{\"symbol\":\"ACME\"}").method(WATCH), context);

        assertThat(watchStarted.await(5, TimeUnit.SECONDS)).isTrue();
        client.cancel(context);

        assertThat(watchCancelled.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    public void shouldCancelOpenCallsWithFinalAction() throws Exception {
        TestContext context = new TestContext();
        client.send(new GrpcMessage("{\"symbol\":\"ACME\"}").method(WATCH), context);

        assertThat(watchStarted.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(context.getFinalActions()).hasSize(1);

        for (TestActionBuilder<?> finalAction : context.getFinalActions()) {
            finalAction.build().execute(context);
        }

        assertThat(watchCancelled.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    public void shouldKeepConcurrentCallsApart() {
        TestContext context = new TestContext();

        client.send(new GrpcMessage("{\"name\":\"Ada\"}").method(SAY_HELLO), context);
        String firstCallId = (String) context.getVariables().get(GrpcMessageHeaders.CALL_ID);

        client.send(new GrpcMessage("{\"name\":\"Grace\"}").method(SAY_HELLO), context);
        String secondCallId = (String) context.getVariables().get(GrpcMessageHeaders.CALL_ID);

        assertThat(firstCallId).isNotEqualTo(secondCallId);

        GrpcMessage first = (GrpcMessage) client.receive(
                "header:" + GrpcMessageHeaders.CALL_ID + "=" + firstCallId, context, 5000);
        assertThat(first.getCallId()).isEqualTo(firstCallId);
        assertThat(first.getPayload().toString()).contains("Hello Ada");

        GrpcMessage second = (GrpcMessage) client.receive(
                "header:" + GrpcMessageHeaders.CALL_ID + "=" + secondCallId, context, 5000);
        assertThat(second.getCallId()).isEqualTo(secondCallId);
        assertThat(second.getPayload().toString()).contains("Hello Grace");
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> sayHelloHandler() {
        return (call, headers) -> {
            receivedHeaders.set(headers);
            call.sendHeaders(new Metadata());
            call.request(2);

            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage request) {
                    String name = (String) request.getField(
                            request.getDescriptorForType().findFieldByName("name"));

                    DynamicMessage reply = DynamicMessage.newBuilder(HelloReply.getDescriptor())
                            .setField(HelloReply.getDescriptor().findFieldByName("message"), "Hello " + name)
                            .build();

                    Metadata trailers = new Metadata();
                    trailers.put(Metadata.Key.of("x-trailer", Metadata.ASCII_STRING_MARSHALLER), "trailer-value");

                    call.sendMessage(reply);
                    call.close(Status.OK, trailers);
                }
            };
        };
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> getUserHandler() {
        return (call, headers) -> {
            call.sendHeaders(new Metadata());
            call.request(2);

            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage request) {
                    String id = (String) request.getField(
                            request.getDescriptorForType().findFieldByName("id"));

                    if ("unknown".equals(id)) {
                        call.close(Status.NOT_FOUND.withDescription("unknown user"), new Metadata());
                    }
                    // otherwise never answer - used for deadline and receive timeout tests
                }
            };
        };
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> subscribeHandler() {
        return (call, headers) -> {
            call.sendHeaders(new Metadata());
            call.request(2);

            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage request) {
                    String symbol = (String) request.getField(
                            request.getDescriptorForType().findFieldByName("symbol"));

                    for (int i = 0; i < 3; i++) {
                        DynamicMessage money = DynamicMessage.newBuilder(Money.getDescriptor())
                                .setField(Money.getDescriptor().findFieldByName("currency"), "EUR")
                                .setField(Money.getDescriptor().findFieldByName("amount"), (long) i)
                                .build();

                        DynamicMessage price = DynamicMessage.newBuilder(Price.getDescriptor())
                                .setField(Price.getDescriptor().findFieldByName("symbol"), symbol)
                                .setField(Price.getDescriptor().findFieldByName("amount"), money)
                                .build();

                        call.sendMessage(price);
                    }

                    call.close(Status.OK, new Metadata());
                }
            };
        };
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> watchHandler() {
        return (call, headers) -> {
            call.sendHeaders(new Metadata());
            call.request(2);

            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage request) {
                    watchStarted.countDown();
                }

                @Override
                public void onCancel() {
                    watchCancelled.countDown();
                }
            };
        };
    }

    private static MethodDescriptor<DynamicMessage, DynamicMessage> method(
            String fullName, MethodDescriptor.MethodType type, Descriptor request, Descriptor response) {
        return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(type)
                .setFullMethodName(fullName)
                .setRequestMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(request)))
                .setResponseMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(response)))
                .build();
    }
}
