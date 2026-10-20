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

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.HandlerRegistry;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.actions.GrpcClientActionBuilder;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;
import org.citrusframework.grpc.endpoint.builder.GrpcEndpoints;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.json.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Client streaming and bidirectional streaming calls, driven through the Citrus DSL against a real
 * grpc-java server that implements both methods with dynamic message handlers.
 */
public class GrpcClientStreamingTest extends AbstractTestNGUnitTest {

    private static final String STORE_USERS = "grpc2grpc.PetStoreService/StoreUsers";
    private static final String BULK_USERS = "grpc2grpc.PetStoreService/BulkUsers";
    private static final String DESCRIPTORS = "classpath:org/citrusframework/grpc/streaming/petstore.proto";

    private GrpcDescriptorRegistry registry;
    private Server server;
    private GrpcClient client;

    private final List<DynamicMessage> storedUsers = new ArrayList<>();
    private final List<DynamicMessage> bulkUsers = new ArrayList<>();
    private DynamicMessage storeUsersResponse;

    @BeforeMethod
    public void setUp() throws Exception {
        storedUsers.clear();
        bulkUsers.clear();
        storeUsersResponse = null;

        registry = new GrpcDescriptorRegistry(List.of(DESCRIPTORS));

        server = ServerBuilder.forPort(findAvailableTcpPort())
                .fallbackHandlerRegistry(new HandlerRegistry() {
                    @Override
                    public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
                        if (STORE_USERS.equals(methodName)) {
                            return definition(registry.findMethod(STORE_USERS), storeUsersHandler());
                        }
                        if (BULK_USERS.equals(methodName)) {
                            return definition(registry.findMethod(BULK_USERS), bulkUsersHandler());
                        }
                        return null;
                    }
                })
                .build()
                .start();

        client = GrpcEndpoints.grpc().client()
                .target("localhost:" + server.getPort())
                .descriptors(DESCRIPTORS)
                .timeout(5000L)
                .build();
    }

    @AfterMethod
    public void tearDown() {
        if (client != null) {
            client.destroy();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    public void shouldSendClientStreamingMessagesAndComplete() {
        GrpcClientActionBuilder clientActions = new GrpcClientActionBuilder(client);

        clientActions.send().call(STORE_USERS).message().body("{ \"id\": 1, \"username\": \"ada\", \"email\": \"ada@example.com\", \"phone\": \"555-1\" }");
        clientActions.build().execute(context);

        clientActions.send().streamMessage().message().body("{ \"id\": 2, \"username\": \"bob\", \"email\": \"bob@example.com\", \"phone\": \"555-2\" }");
        clientActions.build().execute(context);

        clientActions.send().streamMessage().message().body("{ \"id\": 3, \"username\": \"cid\", \"email\": \"cid@example.com\", \"phone\": \"555-3\" }");
        clientActions.build().execute(context);

        clientActions.send().streamCompleted();
        clientActions.build().execute(context);

        clientActions.receive()
                .response()
                .message()
                .validate(jsonPath().expression("$.msg", "@notEmpty()@"));
        clientActions.build().execute(context);

        assertThat(storedUsers).hasSize(3);
        assertThat(username(storedUsers.get(0))).isEqualTo("ada");
        assertThat(username(storedUsers.get(1))).isEqualTo("bob");
        assertThat(username(storedUsers.get(2))).isEqualTo("cid");
        assertThat(storeUsersResponse.getField(messageField("msg"))).isEqualTo("received 3");
    }

    @Test
    public void shouldSendAndReceiveBidirectionalMessages() {
        GrpcClientActionBuilder clientActions = new GrpcClientActionBuilder(client);

        clientActions.send().call(BULK_USERS).message().body("{ \"id\": 1, \"username\": \"ada\", \"email\": \"ada@example.com\", \"phone\": \"555-1\" }");
        clientActions.build().execute(context);

        clientActions.receive()
                .streamMessage()
                .message()
                .validate(jsonPath().expression("$.username", "ada"));
        clientActions.build().execute(context);

        clientActions.send().streamMessage().message().body("{ \"id\": 2, \"username\": \"bob\", \"email\": \"bob@example.com\", \"phone\": \"555-2\" }");
        clientActions.build().execute(context);

        clientActions.receive()
                .streamMessage()
                .message()
                .validate(jsonPath().expression("$.username", "bob"));
        clientActions.build().execute(context);

        clientActions.send().streamCompleted();
        clientActions.build().execute(context);

        clientActions.receive().streamCompleted();
        clientActions.build().execute(context);

        assertThat(bulkUsers).hasSize(2);
        assertThat(username(bulkUsers.get(0))).isEqualTo("ada");
        assertThat(username(bulkUsers.get(1))).isEqualTo("bob");
    }

    @Test
    public void shouldFailToSendOnUnknownCall() {
        GrpcClientActionBuilder clientActions = new GrpcClientActionBuilder(client);

        clientActions.send().streamMessage().message().body("{ \"id\": 1, \"username\": \"ada\", \"email\": \"ada@example.com\", \"phone\": \"555-1\" }");

        assertThatThrownBy(() -> clientActions.build().execute(context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("No open gRPC call available");
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> storeUsersHandler() {
        Descriptors.Descriptor outputType = registry.findMessage("grpc2grpc.EmptyRes");

        return (call, headers) -> {
            call.sendHeaders(new Metadata());
            call.request(1);
            List<DynamicMessage> received = new ArrayList<>();

            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage message) {
                    received.add(message);
                    storedUsers.add(message);
                    call.request(1);
                }

                @Override
                public void onHalfClose() {
                    DynamicMessage response = DynamicMessage.newBuilder(outputType)
                            .setField(outputType.findFieldByName("msg"), "received " + received.size())
                            .build();

                    storeUsersResponse = response;
                    call.sendMessage(response);
                    call.close(Status.OK, new Metadata());
                }
            };
        };
    }

    private ServerCallHandler<DynamicMessage, DynamicMessage> bulkUsersHandler() {
        return (call, headers) -> {
            call.sendHeaders(new Metadata());
            call.request(1);

            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage message) {
                    bulkUsers.add(message);
                    call.sendMessage(message);
                    call.request(1);
                }

                @Override
                public void onHalfClose() {
                    call.close(Status.OK, new Metadata());
                }
            };
        };
    }

    private String username(DynamicMessage message) {
        return (String) message.getField(userField());
    }

    private Descriptors.FieldDescriptor userField() {
        return registry.findMessage("grpc2grpc.User").findFieldByName("username");
    }

    private Descriptors.FieldDescriptor messageField(String name) {
        return registry.findMessage("grpc2grpc.EmptyRes").findFieldByName(name);
    }

    private static ServerMethodDefinition<DynamicMessage, DynamicMessage> definition(
            Descriptors.MethodDescriptor method,
            ServerCallHandler<DynamicMessage, DynamicMessage> handler) {
        return ServerMethodDefinition.create(
                io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                        .setType(grpcMethodType(method))
                        .setFullMethodName(method.getService().getFullName() + "/" + method.getName())
                        .setRequestMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(method.getInputType())))
                        .setResponseMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(method.getOutputType())))
                        .build(),
                handler);
    }

    private static io.grpc.MethodDescriptor.MethodType grpcMethodType(Descriptors.MethodDescriptor method) {
        if (method.isClientStreaming() && method.isServerStreaming()) {
            return io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING;
        }
        if (method.isClientStreaming()) {
            return io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING;
        }
        if (method.isServerStreaming()) {
            return io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING;
        }
        return io.grpc.MethodDescriptor.MethodType.UNARY;
    }
}
