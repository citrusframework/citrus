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
package org.citrusframework.grpc.integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.endpoint.builder.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.test.proto.helloworld.GetUserRequest;
import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.citrusframework.grpc.test.proto.helloworld.UserInfo;
import org.citrusframework.grpc.test.proto.market.Price;
import org.citrusframework.grpc.test.proto.market.PriceSubscription;
import org.citrusframework.grpc.test.proto.market.PricesGrpc;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A real grpc-java client (generated stubs) calls the Citrus simulator.
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class GrpcServerIT implements TestActionSupport {

    private static final String PRICE_ZERO =
            "{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"0\" } }";
    private static final String PRICE_ONE =
            "{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"1\" } }";

    private GrpcServer simulator;
    private ManagedChannel channel;
    private GreeterGrpc.GreeterBlockingStub greeter;
    private PricesGrpc.PricesBlockingStub prices;

    @BeforeEach
    public void startSimulator() {
        simulator = startSimulator(5000L, "helloworld.Greeter", "market.Prices");
        channel = ManagedChannelBuilder.forAddress("localhost", simulator.getEndpointConfiguration().getPort())
                .usePlaintext().build();
        greeter = GreeterGrpc.newBlockingStub(channel);
        prices = PricesGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void stopSimulator() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (simulator != null) {
            simulator.stop();
        }
    }

    @Test
    @CitrusTest
    public void shouldAnswerUnaryCall(@CitrusResource GherkinTestActionRunner runner) throws Exception {
        CompletableFuture<HelloReply> call = async(() -> greeter.sayHello(
                HelloRequest.newBuilder().setName("Ada").build()));

        runner.when(grpc().server(simulator).receive()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Ada\", \"userId\": 0 }"));

        runner.then(grpc().server(simulator).send()
                .response()
                .trailer("x-trailer", "42")
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));

        assertThat(call.get(5, TimeUnit.SECONDS).getMessage()).isEqualTo("Hello Ada");
    }

    @Test
    @CitrusTest
    public void shouldAnswerUnimplementedForUnconfiguredService(@CitrusResource GherkinTestActionRunner runner) {
        GrpcServer greeterOnly = startSimulator(5000L, "helloworld.Greeter");
        ManagedChannel greeterChannel = ManagedChannelBuilder
                .forAddress("localhost", greeterOnly.getEndpointConfiguration().getPort())
                .usePlaintext().build();

        try {
            PricesGrpc.PricesBlockingStub pricesStub = PricesGrpc.newBlockingStub(greeterChannel);
            assertThatThrownBy(() -> pricesStub.subscribe(PriceSubscription.newBuilder().setSymbol("ACME").build()).hasNext())
                    .isInstanceOf(StatusRuntimeException.class)
                    .satisfies(error -> assertThat(((StatusRuntimeException) error).getStatus().getCode())
                            .isEqualTo(Status.Code.UNIMPLEMENTED));
        } finally {
            greeterChannel.shutdownNow();
            greeterOnly.stop();
        }
    }

    @Test
    @CitrusTest
    public void shouldRestartSimulatorAfterStop(@CitrusResource GherkinTestActionRunner runner) throws Exception {
        GrpcServer restartable = startSimulator(5000L, "helloworld.Greeter");
        ManagedChannel restartChannel = ManagedChannelBuilder
                .forAddress("localhost", restartable.getEndpointConfiguration().getPort())
                .usePlaintext().build();

        try {
            GreeterGrpc.GreeterBlockingStub restartGreeter = GreeterGrpc.newBlockingStub(restartChannel);

            restartable.stop();
            assertThat(restartable.isRunning()).isFalse();

            restartable.start();
            assertThat(restartable.isRunning()).isTrue();

            CompletableFuture<HelloReply> call = async(() -> restartGreeter.sayHello(
                    HelloRequest.newBuilder().setName("Ada").build()));

            runner.when(grpc().server(restartable).receive()
                    .call("helloworld.Greeter/SayHello")
                    .message()
                    .body("{ \"name\": \"Ada\", \"userId\": 0 }"));

            runner.then(grpc().server(restartable).send()
                    .response()
                    .message()
                    .body("{ \"message\": \"Hello Ada\" }"));

            assertThat(call.get(5, TimeUnit.SECONDS).getMessage()).isEqualTo("Hello Ada");
        } finally {
            restartChannel.shutdownNow();
            restartable.stop();
        }
    }

    @Test
    @CitrusTest
    public void shouldAnswerWithErrorStatusAndTrailers(@CitrusResource GherkinTestActionRunner runner) throws Exception {
        CompletableFuture<UserInfo> call = async(() -> greeter.getUser(
                GetUserRequest.newBuilder().setId("unknown").build()));

        runner.when(grpc().server(simulator).receive()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        runner.then(grpc().server(simulator).send()
                .response("NOT_FOUND")
                .statusDescription("unknown user")
                .trailer("x-trailer", "not-found"));

        ExecutionException error = expectFailure(call);
        StatusRuntimeException status = (StatusRuntimeException) error.getCause();
        assertThat(status.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(status.getStatus().getDescription()).isEqualTo("unknown user");
        assertThat(status.getTrailers().get(Metadata.Key.of("x-trailer", Metadata.ASCII_STRING_MARSHALLER)))
                .isEqualTo("not-found");
    }

    @Test
    @CitrusTest
    public void shouldStreamMessagesAndComplete(@CitrusResource GherkinTestActionRunner runner) throws Exception {
        CompletableFuture<List<Price>> call = async(() -> {
            List<Price> received = new ArrayList<>();
            Iterator<Price> messages = prices.subscribe(PriceSubscription.newBuilder().setSymbol("ACME").build());
            messages.forEachRemaining(received::add);
            return received;
        });

        runner.when(grpc().server(simulator).receive()
                .call("market.Prices/Subscribe")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body(PRICE_ZERO));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body(PRICE_ONE));

        runner.then(grpc().server(simulator).send().streamCompleted());

        List<Price> received = call.get(5, TimeUnit.SECONDS);
        assertThat(received).hasSize(2);
        assertThat(received.get(0).getSymbol()).isEqualTo("ACME");
        assertThat(received.get(1).getAmount().getAmount()).isEqualTo(1L);
    }

    @Test
    @CitrusTest
    public void shouldAnswerInternalWhenNoReplyIsSent(@CitrusResource GherkinTestActionRunner runner) throws Exception {
        GrpcServer idleSimulator = startSimulator(300L, "helloworld.Greeter");
        ManagedChannel idleChannel = ManagedChannelBuilder
                .forAddress("localhost", idleSimulator.getEndpointConfiguration().getPort())
                .usePlaintext().build();

        try {
            GreeterGrpc.GreeterBlockingStub idleGreeter = GreeterGrpc.newBlockingStub(idleChannel);
            CompletableFuture<HelloReply> call = async(() -> idleGreeter.sayHello(
                    HelloRequest.newBuilder().setName("Ada").build()));

            runner.when(grpc().server(idleSimulator).receive()
                    .call("helloworld.Greeter/SayHello")
                    .message()
                    .body("{ \"name\": \"Ada\", \"userId\": 0 }"));

            ExecutionException error = expectFailure(call);
            StatusRuntimeException status = (StatusRuntimeException) error.getCause();
            assertThat(status.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
            assertThat(status.getStatus().getDescription()).contains("no response within 300 ms");
        } finally {
            idleChannel.shutdownNow();
            idleSimulator.stop();
        }
    }

    @Test
    @CitrusTest
    public void shouldFailSimulatorSendWhenClientCancels(@CitrusResource GherkinTestActionRunner runner) throws Exception {
        AtomicReference<ClientCallStreamObserver<PriceSubscription>> callRef = new AtomicReference<>();
        CountDownLatch received = new CountDownLatch(1);

        PricesGrpc.PricesStub asyncStub = PricesGrpc.newStub(channel);
        asyncStub.watch(PriceSubscription.newBuilder().setSymbol("ACME").build(),
                new ClientResponseObserver<PriceSubscription, Price>() {
                    @Override
                    public void beforeStart(ClientCallStreamObserver<PriceSubscription> requestStream) {
                        callRef.set(requestStream);
                    }

                    @Override
                    public void onNext(Price value) {
                        received.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onCompleted() {
                    }
                });

        runner.when(grpc().server(simulator).receive()
                .call("market.Prices/Watch")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body(PRICE_ZERO));

        assertThat(received.await(5, TimeUnit.SECONDS)).isTrue();

        callRef.get().cancel("client cancels", null);

        CitrusRuntimeException failure = awaitCancelledCallFailure(runner);
        assertThat(failure).isNotNull();
        assertThat(failure.getMessage()).contains("cancelled by the client");
    }

    /**
     * Sends a stream message until the simulator reports the cancelled call.
     *
     * @return the failure raised for the cancelled call or {@code null} if it was not raised
     */
    private CitrusRuntimeException awaitCancelledCallFailure(GherkinTestActionRunner runner) {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            try {
                runner.when(grpc().server(simulator).send()
                        .streamMessage()
                        .message()
                        .body(PRICE_ONE));
            } catch (CitrusRuntimeException e) {
                return e;
            } catch (IllegalStateException e) {
                // the call has been closed by the client in the meantime - retry until the simulator
                // has processed the cancellation
            }
        }

        return null;
    }

    private static GrpcServer startSimulator(long timeout, String... services) {
        return GrpcEndpoints.grpc().server()
                .port(findAvailableTcpPort(18820))
                .descriptors("classpath:descriptors/test-protos.binpb")
                .services(services)
                .timeout(timeout)
                .responseTimeout(timeout)
                .autoStart(true)
                .initialize()
                .build();
    }

    private static <T> CompletableFuture<T> async(Supplier<T> call) {
        return CompletableFuture.supplyAsync(call);
    }

    private static ExecutionException expectFailure(CompletableFuture<?> call) throws Exception {
        try {
            call.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            return e;
        }
        org.assertj.core.api.Assertions.fail("Expected the gRPC call to fail");
        return null;
    }
}
