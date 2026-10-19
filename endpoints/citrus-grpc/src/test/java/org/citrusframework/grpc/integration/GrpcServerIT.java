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
import org.citrusframework.grpc.endpoint.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.test.proto.helloworld.GetUserRequest;
import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.citrusframework.grpc.test.proto.helloworld.UserInfo;
import org.citrusframework.grpc.test.proto.market.Price;
import org.citrusframework.grpc.test.proto.market.PriceSubscription;
import org.citrusframework.grpc.test.proto.market.PricesGrpc;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A real grpc-java client (generated stubs) calls the Citrus simulator.
 */
@Test
public class GrpcServerIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final String PRICE_ZERO =
            "{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"0\" } }";
    private static final String PRICE_ONE =
            "{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"1\" } }";

    private GrpcServer simulator;
    private ManagedChannel channel;
    private GreeterGrpc.GreeterBlockingStub greeter;
    private PricesGrpc.PricesBlockingStub prices;

    @BeforeMethod(alwaysRun = true)
    public void startSimulator() {
        simulator = startSimulator(5000L, "helloworld.Greeter", "market.Prices");
        channel = ManagedChannelBuilder.forAddress("localhost", simulator.getEndpointConfiguration().getPort())
                .usePlaintext().build();
        greeter = GreeterGrpc.newBlockingStub(channel);
        prices = PricesGrpc.newBlockingStub(channel);
    }

    @AfterMethod(alwaysRun = true)
    public void stopSimulator() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (simulator != null) {
            simulator.stop();
        }
    }

    @CitrusTest
    public void shouldAnswerUnaryCall() throws Exception {
        CompletableFuture<HelloReply> call = async(() -> greeter.sayHello(
                HelloRequest.newBuilder().setName("Ada").build()));

        when(grpc().server(simulator).receive()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Ada\", \"userId\": 0 }"));

        then(grpc().server(simulator).send()
                .response()
                .trailer("x-trailer", "42")
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));

        assertThat(call.get(5, TimeUnit.SECONDS).getMessage()).isEqualTo("Hello Ada");
    }

    @CitrusTest
    public void shouldAnswerUnimplementedForUnconfiguredService() {
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

    @CitrusTest
    public void shouldAnswerWithErrorStatusAndTrailers() throws Exception {
        CompletableFuture<UserInfo> call = async(() -> greeter.getUser(
                GetUserRequest.newBuilder().setId("unknown").build()));

        when(grpc().server(simulator).receive()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        then(grpc().server(simulator).send()
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

    @CitrusTest
    public void shouldStreamMessagesAndComplete() throws Exception {
        CompletableFuture<List<Price>> call = async(() -> {
            List<Price> received = new ArrayList<>();
            Iterator<Price> messages = prices.subscribe(PriceSubscription.newBuilder().setSymbol("ACME").build());
            messages.forEachRemaining(received::add);
            return received;
        });

        when(grpc().server(simulator).receive()
                .call("market.Prices/Subscribe")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body(PRICE_ZERO));

        then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body(PRICE_ONE));

        then(grpc().server(simulator).send().streamCompleted());

        List<Price> received = call.get(5, TimeUnit.SECONDS);
        assertThat(received).hasSize(2);
        assertThat(received.get(0).getSymbol()).isEqualTo("ACME");
        assertThat(received.get(1).getAmount().getAmount()).isEqualTo(1L);
    }

    @CitrusTest
    public void shouldAnswerInternalWhenNoReplyIsSent() throws Exception {
        GrpcServer idleSimulator = startSimulator(300L, "helloworld.Greeter");
        ManagedChannel idleChannel = ManagedChannelBuilder
                .forAddress("localhost", idleSimulator.getEndpointConfiguration().getPort())
                .usePlaintext().build();

        try {
            GreeterGrpc.GreeterBlockingStub idleGreeter = GreeterGrpc.newBlockingStub(idleChannel);
            CompletableFuture<HelloReply> call = async(() -> idleGreeter.sayHello(
                    HelloRequest.newBuilder().setName("Ada").build()));

            when(grpc().server(idleSimulator).receive()
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

    @CitrusTest
    public void shouldFailSimulatorSendWhenClientCancels() throws Exception {
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

        when(grpc().server(simulator).receive()
                .call("market.Prices/Watch")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body(PRICE_ZERO));

        assertThat(received.await(5, TimeUnit.SECONDS)).isTrue();

        callRef.get().cancel("client cancels", null);
        Thread.sleep(200L);

        assertThatThrownBy(() -> when(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body(PRICE_ONE)))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("cancelled by the client");
    }

    private static GrpcServer startSimulator(long timeout, String... services) {
        return GrpcEndpoints.grpc().server()
                .port(findAvailableTcpPort(18820))
                .descriptors("classpath:descriptors/test-protos.binpb")
                .services(services)
                .timeout(timeout)
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
