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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import org.citrusframework.base.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.citrusframework.message.DefaultMessage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

public class GrpcServerTest {

    private static final String DESCRIPTORS = "classpath:descriptors/test-protos.binpb";

    private final List<GrpcServer> servers = new ArrayList<>();
    private final List<ManagedChannel> channels = new ArrayList<>();
    private ExecutorService executor;

    private TestContext context;

    @BeforeMethod
    public void setUp() {
        context = new TestContext();
        executor = Executors.newCachedThreadPool();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        channels.forEach(ManagedChannel::shutdownNow);
        channels.clear();
        servers.forEach(GrpcServer::stop);
        servers.clear();
        executor.shutdownNow();
    }

    @Test
    public void shouldAnswerUnimplementedForUnconfiguredService() {
        GrpcServer server = startServer(1000, "helloworld.Greeter");
        ManagedChannel channel = channel(server);

        assertThatThrownBy(() -> org.citrusframework.grpc.test.proto.market.PricesGrpc
                .newBlockingStub(channel)
                .subscribe(org.citrusframework.grpc.test.proto.market.PriceSubscription.newBuilder()
                        .setSymbol("ACME")
                        .build())
                .hasNext())
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("UNIMPLEMENTED");
    }

    @Test
    public void shouldAnswerUnaryCall() throws Exception {
        GrpcServer server = startServer(1000);
        ManagedChannel channel = channel(server);

        GreeterGrpc.GreeterFutureStub client = GreeterGrpc.newFutureStub(channel);
        ListenableFuture<HelloReply> response = client.sayHello(HelloRequest.newBuilder().setName("Ada").build());

        GrpcMessage call = (GrpcMessage) server.createConsumer().receive(context, 2000);
        assertThat(call.getKind()).isEqualTo(GrpcMessageHeaders.KIND_CALL);
        assertThat(call.getMethod()).isEqualTo("helloworld.Greeter/SayHello");
        assertThat(call.getMethodType()).isEqualTo(GrpcMessageHeaders.METHOD_TYPE_UNARY);
        assertThat(call.getCallId()).isNotEmpty();
        assertThat(call.getPayload(String.class)).contains("\"name\": \"Ada\"");

        server.createProducer().send(new GrpcMessage("{ \"message\": \"Hello Ada\" }")
                .kind(GrpcMessageHeaders.KIND_RESPONSE)
                .method(call.getMethod())
                .methodType(call.getMethodType())
                .callId(call.getCallId())
                .statusCode("OK"), context);

        assertThat(response.get(2, TimeUnit.SECONDS).getMessage()).isEqualTo("Hello Ada");
    }

    @Test
    public void shouldAnswerWithErrorStatusAndTrailers() throws Exception {
        GrpcServer server = startServer(1000);
        ManagedChannel channel = channel(server);

        Future<?> call = executor.submit(() -> GreeterGrpc.newBlockingStub(channel)
                .getUser(org.citrusframework.grpc.test.proto.helloworld.GetUserRequest.newBuilder()
                        .setId("unknown")
                        .build()));

        GrpcMessage received = (GrpcMessage) server.createConsumer().receive(context, 2000);
        server.createProducer().send(new GrpcMessage()
                .kind(GrpcMessageHeaders.KIND_RESPONSE)
                .method(received.getMethod())
                .methodType(received.getMethodType())
                .callId(received.getCallId())
                .statusCode("NOT_FOUND")
                .statusDescription("unknown user")
                .trailer("x-request-id", "42"), context);

        try {
            call.get(2, TimeUnit.SECONDS);
            fail("Expected the client call to fail");
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(StatusRuntimeException.class);
            StatusRuntimeException cause = (StatusRuntimeException) e.getCause();
            assertThat(cause.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
            assertThat(cause.getStatus().getDescription()).isEqualTo("unknown user");
            assertThat(cause.getTrailers().get(Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER)))
                    .isEqualTo("42");
        }
    }

    @Test
    public void shouldStreamMessagesAndComplete() throws Exception {
        GrpcServer server = startServer(1000);
        ManagedChannel channel = channel(server);

        List<org.citrusframework.grpc.test.proto.market.Price> received = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        org.citrusframework.grpc.test.proto.market.PricesGrpc.newStub(channel)
                .subscribe(org.citrusframework.grpc.test.proto.market.PriceSubscription.newBuilder()
                                .setSymbol("ACME")
                                .build(),
                        new StreamObserver<>() {
                            @Override
                            public void onNext(org.citrusframework.grpc.test.proto.market.Price price) {
                                received.add(price);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                error.set(throwable);
                                completed.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                completed.countDown();
                            }
                        });

        GrpcMessage call = (GrpcMessage) server.createConsumer().receive(context, 2000);
        server.createProducer().send(streamMessage(call, "{\"symbol\": \"ACME\", \"amount\": {\"currency\": \"EUR\", \"amount\": 1}}"), context);
        server.createProducer().send(streamMessage(call, "{\"symbol\": \"ACME\", \"amount\": {\"currency\": \"EUR\", \"amount\": 2}}"), context);
        server.createProducer().send(streamCompleted(call), context);

        assertThat(completed.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(error.get()).isNull();
        assertThat(received).hasSize(2);
        assertThat(received.get(0).getAmount().getAmount()).isEqualTo(1);
        assertThat(received.get(1).getAmount().getAmount()).isEqualTo(2);
    }

    @Test
    public void shouldAnswerInternalWhenNoReplyIsSent() throws Exception {
        GrpcServer server = startServer(200);
        ManagedChannel channel = channel(server);

        Future<?> call = executor.submit(() -> GreeterGrpc.newBlockingStub(channel)
                .sayHello(HelloRequest.newBuilder().setName("Ada").build()));

        server.createConsumer().receive(context, 2000);

        try {
            call.get(2, TimeUnit.SECONDS);
            fail("Expected the client call to fail");
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(StatusRuntimeException.class);
            StatusRuntimeException cause = (StatusRuntimeException) e.getCause();
            assertThat(cause.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
            assertThat(cause.getStatus().getDescription()).contains("200");
        }
    }

    @Test
    public void shouldRestartIdleTimeoutOnEverySend() throws Exception {
        GrpcServer server = startServer(500);
        ManagedChannel channel = channel(server);

        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        org.citrusframework.grpc.test.proto.market.PricesGrpc.newStub(channel)
                .subscribe(org.citrusframework.grpc.test.proto.market.PriceSubscription.newBuilder()
                                .setSymbol("ACME")
                                .build(),
                        new StreamObserver<>() {
                            @Override
                            public void onNext(org.citrusframework.grpc.test.proto.market.Price price) {
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                error.set(throwable);
                                completed.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                completed.countDown();
                            }
                        });

        GrpcMessage call = (GrpcMessage) server.createConsumer().receive(context, 2000);
        Thread.sleep(300);
        server.createProducer().send(streamMessage(call, "{\"symbol\": \"ACME\"}"), context);
        Thread.sleep(300);
        server.createProducer().send(streamCompleted(call), context);

        assertThat(completed.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(error.get()).isNull();
    }

    @Test
    public void shouldFailToSendWhenClientCancelledTheCall() throws Exception {
        GrpcServer server = startServer(1000);
        ManagedChannel channel = channel(server);

        CountDownLatch firstMessage = new CountDownLatch(1);
        AtomicReference<ClientCallStreamObserver<org.citrusframework.grpc.test.proto.market.PriceSubscription>> callStream =
                new AtomicReference<>();

        org.citrusframework.grpc.test.proto.market.PricesGrpc.newStub(channel)
                .subscribe(org.citrusframework.grpc.test.proto.market.PriceSubscription.newBuilder()
                                .setSymbol("ACME")
                                .build(),
                        new ClientResponseObserver<org.citrusframework.grpc.test.proto.market.PriceSubscription,
                                org.citrusframework.grpc.test.proto.market.Price>() {
                            @Override
                            public void beforeStart(ClientCallStreamObserver<org.citrusframework.grpc.test.proto.market.PriceSubscription> requestStream) {
                                callStream.set(requestStream);
                            }

                            @Override
                            public void onNext(org.citrusframework.grpc.test.proto.market.Price price) {
                                firstMessage.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                            }

                            @Override
                            public void onCompleted() {
                            }
                        });

        GrpcMessage call = (GrpcMessage) server.createConsumer().receive(context, 2000);
        server.createProducer().send(streamMessage(call, "{\"symbol\": \"ACME\"}"), context);

        assertThat(firstMessage.await(2, TimeUnit.SECONDS)).isTrue();
        callStream.get().cancel("cancelled by test", null);
        Thread.sleep(300);

        assertThatThrownBy(() -> server.createProducer().send(streamMessage(call, "{\"symbol\": \"ACME\"}"), context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    public void shouldAnswerWithStaticEndpointAdapter() {
        GrpcServerEndpointConfiguration configuration = configuration(1000);
        GrpcServer server = new GrpcServer(configuration);
        server.setEndpointAdapter(new StaticEndpointAdapter(new DefaultMessage("{ \"message\": \"Hi\" }")));
        server.initialize();
        server.start();
        servers.add(server);

        ManagedChannel channel = channel(server);

        HelloReply reply = GreeterGrpc.newBlockingStub(channel)
                .sayHello(HelloRequest.newBuilder().setName("Ada").build());

        assertThat(reply.getMessage()).isEqualTo("Hi");
    }

    private GrpcServer startServer(long timeout, String... services) {
        GrpcServerEndpointConfiguration configuration = configuration(timeout);
        configuration.setServices(List.of(services));

        GrpcServer server = new GrpcServer(configuration);
        server.initialize();
        server.start();
        servers.add(server);
        return server;
    }

    private GrpcServerEndpointConfiguration configuration(long timeout) {
        GrpcServerEndpointConfiguration configuration = new GrpcServerEndpointConfiguration();
        configuration.setPort(findAvailableTcpPort());
        configuration.setDescriptors(List.of(DESCRIPTORS));
        configuration.setTimeout(timeout);
        return configuration;
    }

    private ManagedChannel channel(GrpcServer server) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", ((GrpcServerEndpointConfiguration) server.getEndpointConfiguration()).getPort())
                .usePlaintext()
                .build();
        channels.add(channel);
        return channel;
    }

    private static GrpcMessage streamMessage(GrpcMessage call, String body) {
        return new GrpcMessage(body)
                .kind(GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                .method(call.getMethod())
                .methodType(call.getMethodType())
                .callId(call.getCallId());
    }

    private static GrpcMessage streamCompleted(GrpcMessage call) {
        return new GrpcMessage()
                .kind(GrpcMessageHeaders.KIND_STREAM_END)
                .method(call.getMethod())
                .methodType(call.getMethodType())
                .callId(call.getCallId())
                .statusCode("OK");
    }
}
