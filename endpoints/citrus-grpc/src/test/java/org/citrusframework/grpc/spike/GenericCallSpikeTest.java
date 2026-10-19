/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.grpc.spike;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.HandlerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import org.citrusframework.grpc.test.proto.common.Money;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.citrusframework.grpc.test.proto.market.Price;
import org.citrusframework.grpc.test.proto.market.PriceSubscription;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * De-risk spike for T2: proves that grpc-java can serve and call methods with dynamic messages
 * built from runtime descriptors (no generated stubs), against protobuf-java 4.x.
 */
public class GenericCallSpikeTest {

    private static final String SAY_HELLO = "helloworld.Greeter/SayHello";
    private static final String SUBSCRIBE = "market.Prices/Subscribe";
    private static final String WATCH = "market.Prices/Watch";
    private static final String SAY_GOODBYE = "helloworld.Greeter/SayGoodbye";

    private static final Metadata.Key<String> REQUEST_ID = Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> TRAILER_KEY = Metadata.Key.of("x-trailer", Metadata.ASCII_STRING_MARSHALLER);

    private Server server;
    private ManagedChannel channel;

    private final AtomicReference<Metadata> receivedHeaders = new AtomicReference<>();
    private final CountDownLatch watchStarted = new CountDownLatch(1);
    private final CountDownLatch watchCancelled = new CountDownLatch(1);

    @BeforeClass
    public void startServer() throws Exception {
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .fallbackHandlerRegistry(new HandlerRegistry() {
                    @Override
                    public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
                        if (SAY_HELLO.equals(methodName)) {
                            return ServerMethodDefinition.create(sayHelloMethod(), sayHelloHandler());
                        }
                        if (SUBSCRIBE.equals(methodName)) {
                            return ServerMethodDefinition.create(subscribeMethod(), subscribeHandler());
                        }
                        if (WATCH.equals(methodName)) {
                            return ServerMethodDefinition.create(watchMethod(), watchHandler());
                        }
                        return null;
                    }
                })
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() throws Exception {
        channel.shutdownNow();
        server.shutdownNow();
        channel.awaitTermination(5, TimeUnit.SECONDS);
        server.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    public void shouldRunUnaryCallWithoutStubs() throws Exception {
        Metadata requestHeaders = new Metadata();
        requestHeaders.put(REQUEST_ID, "42");

        ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(sayHelloMethod(), CallOptions.DEFAULT);

        List<DynamicMessage> responses = new ArrayList<>();
        AtomicReference<Status> status = new AtomicReference<>();
        AtomicReference<Metadata> trailers = new AtomicReference<>();
        CountDownLatch closed = new CountDownLatch(1);

        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(DynamicMessage message) {
                responses.add(message);
            }

            @Override
            public void onClose(Status result, Metadata metadata) {
                status.set(result);
                trailers.set(metadata);
                closed.countDown();
            }
        }, requestHeaders);

        call.request(2);
        call.sendMessage(helloRequest("Ada"));
        call.halfClose();

        assertTrue(closed.await(5, TimeUnit.SECONDS), "call did not complete");
        assertEquals(status.get().getCode(), Status.Code.OK);
        assertEquals(responses.size(), 1);
        assertEquals(readString(responses.get(0), HelloReply.getDescriptor().findFieldByName("message")), "Hello Ada");
        assertEquals(trailers.get().get(TRAILER_KEY), "trailer-value");
        assertNotNull(receivedHeaders.get(), "server did not see request headers");
        assertEquals(receivedHeaders.get().get(REQUEST_ID), "42");
    }

    @Test
    public void shouldRunServerStreamingCallWithoutStubs() {
        List<DynamicMessage> responses = new ArrayList<>();
        AtomicReference<Status> status = new AtomicReference<>();
        CountDownLatch closed = new CountDownLatch(1);

        ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(subscribeMethod(), CallOptions.DEFAULT);
        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(DynamicMessage message) {
                responses.add(message);
            }

            @Override
            public void onClose(Status result, Metadata metadata) {
                status.set(result);
                closed.countDown();
            }
        }, new Metadata());

        call.request(10);
        call.sendMessage(DynamicMessage.newBuilder(PriceSubscription.getDescriptor())
                .setField(PriceSubscription.getDescriptor().findFieldByName("symbol"), "ACME")
                .build());
        call.halfClose();

        assertTrue(await(closed), "stream did not complete");
        assertEquals(status.get().getCode(), Status.Code.OK);
        assertEquals(responses.size(), 3);
        for (int i = 0; i < responses.size(); i++) {
            assertEquals(readString(responses.get(i), Price.getDescriptor().findFieldByName("symbol")), "ACME");
            assertEquals(readLong(responses.get(i), Price.getDescriptor().findFieldByName("amount"), "amount"), i);
        }
    }

    @Test
    public void shouldAnswerUnimplementedForUnknownMethod() {
        try {
            ClientCalls.blockingUnaryCall(channel, sayGoodbyeMethod(), CallOptions.DEFAULT, helloRequest("Ada"));
            fail("expected UNIMPLEMENTED");
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus().getCode(), Status.Code.UNIMPLEMENTED);
        }
    }

    @Test
    public void shouldSeeCancelServerSide() throws Exception {
        ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(watchMethod(), CallOptions.DEFAULT);
        AtomicReference<Status> status = new AtomicReference<>();
        CountDownLatch closed = new CountDownLatch(1);

        call.start(new ClientCall.Listener<>() {
            @Override
            public void onClose(Status result, Metadata metadata) {
                status.set(result);
                closed.countDown();
            }
        }, new Metadata());

        call.request(1);
        call.sendMessage(DynamicMessage.newBuilder(PriceSubscription.getDescriptor())
                .setField(PriceSubscription.getDescriptor().findFieldByName("symbol"), "ACME")
                .build());
        call.halfClose();

        assertTrue(watchStarted.await(5, TimeUnit.SECONDS), "server did not see the call");
        call.cancel("cancelled by test", null);

        assertTrue(watchCancelled.await(5, TimeUnit.SECONDS), "server did not observe the cancellation");
        assertTrue(closed.await(5, TimeUnit.SECONDS), "client call did not close");
        assertEquals(status.get().getCode(), Status.Code.CANCELLED);
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> sayHelloHandler() {
        return (call, headers) -> {
            receivedHeaders.set(headers);
            call.sendHeaders(new Metadata());
            call.request(2);
            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage request) {
                    Metadata trailers = new Metadata();
                    trailers.put(TRAILER_KEY, "trailer-value");
                    call.sendMessage(DynamicMessage.newBuilder(HelloReply.getDescriptor())
                            .setField(HelloReply.getDescriptor().findFieldByName("message"),
                                    "Hello " + readString(request, HelloRequest.getDescriptor().findFieldByName("name")))
                            .build());
                    call.close(Status.OK, trailers);
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
                    for (int i = 0; i < 3; i++) {
                        DynamicMessage money = DynamicMessage.newBuilder(Money.getDescriptor())
                                .setField(Money.getDescriptor().findFieldByName("currency"), "EUR")
                                .setField(Money.getDescriptor().findFieldByName("amount"), (long) i)
                                .build();
                        call.sendMessage(DynamicMessage.newBuilder(Price.getDescriptor())
                                .setField(Price.getDescriptor().findFieldByName("symbol"), "ACME")
                                .setField(Price.getDescriptor().findFieldByName("amount"), money)
                                .build());
                    }
                    call.close(Status.OK, new Metadata());
                }
            };
        };
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> watchHandler() {
        return (call, headers) -> {
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

    private static DynamicMessage helloRequest(String name) {
        return DynamicMessage.newBuilder(HelloRequest.getDescriptor())
                .setField(HelloRequest.getDescriptor().findFieldByName("name"), name)
                .build();
    }

    private static MethodDescriptor<DynamicMessage, DynamicMessage> sayHelloMethod() {
        return method(SAY_HELLO, MethodDescriptor.MethodType.UNARY,
                HelloRequest.getDescriptor(), HelloReply.getDescriptor());
    }

    private static MethodDescriptor<DynamicMessage, DynamicMessage> sayGoodbyeMethod() {
        return method(SAY_GOODBYE, MethodDescriptor.MethodType.UNARY,
                HelloRequest.getDescriptor(), HelloReply.getDescriptor());
    }

    private static MethodDescriptor<DynamicMessage, DynamicMessage> subscribeMethod() {
        return method(SUBSCRIBE, MethodDescriptor.MethodType.SERVER_STREAMING,
                PriceSubscription.getDescriptor(), Price.getDescriptor());
    }

    private static MethodDescriptor<DynamicMessage, DynamicMessage> watchMethod() {
        return method(WATCH, MethodDescriptor.MethodType.SERVER_STREAMING,
                PriceSubscription.getDescriptor(), Price.getDescriptor());
    }

    private static MethodDescriptor<DynamicMessage, DynamicMessage> method(String fullName,
                                                                           MethodDescriptor.MethodType type,
                                                                           com.google.protobuf.Descriptors.Descriptor request,
                                                                           com.google.protobuf.Descriptors.Descriptor response) {
        return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(type)
                .setFullMethodName(fullName)
                .setRequestMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(request)))
                .setResponseMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(response)))
                .build();
    }

    private static String readString(DynamicMessage message, com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (String) message.getField(field);
    }

    private static long readLong(DynamicMessage message, com.google.protobuf.Descriptors.FieldDescriptor moneyField, String nestedField) {
        DynamicMessage money = (DynamicMessage) message.getField(moneyField);
        return (Long) money.getField(money.getDescriptorForType().findFieldByName(nestedField));
    }

    private static boolean await(CountDownLatch latch) {
        try {
            return latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
