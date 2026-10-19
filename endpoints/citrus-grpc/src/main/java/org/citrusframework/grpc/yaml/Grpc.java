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
package org.citrusframework.grpc.yaml;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.actions.GrpcActionBuilder;
import org.citrusframework.grpc.actions.GrpcClientActionBuilder;
import org.citrusframework.grpc.actions.GrpcClientRequestActionBuilder;
import org.citrusframework.grpc.actions.GrpcClientResponseActionBuilder;
import org.citrusframework.grpc.actions.GrpcServerActionBuilder;
import org.citrusframework.grpc.actions.GrpcServerRequestActionBuilder;
import org.citrusframework.grpc.actions.GrpcServerResponseActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.actions.Message;
import org.citrusframework.yaml.actions.Receive;
import org.citrusframework.yaml.actions.Send;

import static org.citrusframework.api.yaml.SchemaProperty.Kind.ACTION;

/**
 * YAML DSL of the gRPC test actions. Properties may come in any order; the action is built once the
 * client or server is known.
 */
public class Grpc implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private static final String GRPC_GROUP = "grpc";
    private static final String GRPC_MODULE = "citrus-grpc";

    private String client;
    private String server;
    private String description;
    private String actor;

    private SendElement send;
    private ReceiveElement receive;
    private Boolean cancel;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true)
    public void setActor(String actor) {
        this.actor = actor;
    }

    @SchemaProperty(description = "The gRPC client calling the service. Uses an endpoint URI or references an endpoint name.")
    public void setClient(String client) {
        this.client = client;
    }

    @SchemaProperty(description = "The gRPC server simulating the service. References an endpoint name.")
    public void setServer(String server) {
        this.server = server;
    }

    @SchemaProperty(kind = ACTION, group = GRPC_GROUP, module = GRPC_MODULE,
            description = "Sends a gRPC request as client or a response as server.")
    public void setSend(SendElement send) {
        this.send = send;
    }

    @SchemaProperty(kind = ACTION, group = GRPC_GROUP, module = GRPC_MODULE,
            description = "Receives a gRPC response as client or a call as server.")
    public void setReceive(ReceiveElement receive) {
        this.receive = receive;
    }

    @SchemaProperty(kind = ACTION, group = GRPC_GROUP, module = GRPC_MODULE,
            description = "Cancels the current gRPC call on a client endpoint.")
    public void setCancel(Boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public TestAction build() {
        GrpcActionBuilder actions = new GrpcActionBuilder().withReferenceResolver(referenceResolver);

        if (send != null) {
            return buildSend(actions);
        }
        if (receive != null) {
            return buildReceive(actions);
        }
        if (cancel != null && cancel) {
            GrpcClientActionBuilder clientBuilder = actions.client(requireClient()).withReferenceResolver(referenceResolver);
            clientBuilder.cancel().withReferenceResolver(referenceResolver);
            return clientBuilder.build();
        }

        throw new CitrusRuntimeException("Missing gRPC action - please provide send, receive or cancel");
    }

    private TestAction buildSend(GrpcActionBuilder actions) {
        Send sendAction;
        if (client != null) {
            GrpcClientRequestActionBuilder builder = actions.client(client)
                    .withReferenceResolver(referenceResolver)
                    .send();
            builder.description(description);

            if (send.call != null) {
                builder.call(send.call);
            }
            send.metadata.forEach(metadata -> builder.metadata(metadata.name, metadata.value));
            if (send.deadline != null) {
                builder.deadline(Duration.ofMillis(send.deadline));
            }

            sendAction = new Send(builder) {
                @Override
                protected SendMessageAction doBuild() {
                    return null;
                }
            };
            applyMessage(sendAction, send);
            return builder.build();
        }

        if (server != null) {
            GrpcServerResponseActionBuilder builder = actions.server(server)
                    .withReferenceResolver(referenceResolver)
                    .send();
            builder.description(description);
            applyResponse(builder, send);

            sendAction = new Send(builder) {
                @Override
                protected SendMessageAction doBuild() {
                    return null;
                }
            };
            applyMessage(sendAction, send);
            return builder.build();
        }

        throw new CitrusRuntimeException("Missing gRPC client or server - set the 'client' or 'server' property");
    }

    private TestAction buildReceive(GrpcActionBuilder actions) {
        Receive receiveAction;
        if (client != null) {
            GrpcClientResponseActionBuilder builder = actions.client(client)
                    .withReferenceResolver(referenceResolver)
                    .receive();
            builder.description(description);
            applyResponse(builder, receive);

            if (receive.statusDescription != null) {
                builder.statusDescription(receive.statusDescription);
            }
            receive.trailers.forEach(trailer -> builder.trailer(trailer.name, trailer.value));

            receiveAction = receive(builder);
            return builder.build();
        }

        if (server != null) {
            GrpcServerRequestActionBuilder builder = actions.server(server)
                    .withReferenceResolver(referenceResolver)
                    .receive();
            builder.description(description);

            if (receive.call != null) {
                builder.call(receive.call);
            }
            receive.metadata.forEach(metadata -> builder.metadata(metadata.name, metadata.value));

            receiveAction = receive(builder);
            return builder.build();
        }

        throw new CitrusRuntimeException("Missing gRPC client or server - set the 'client' or 'server' property");
    }

    private Receive receive(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder) {
        Receive receiveAction = new Receive(builder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                return null;
            }
        };

        if (receive.message != null) {
            receiveAction.setMessage(receive.message);
        }
        if (receive.timeout != null) {
            receiveAction.setTimeout(receive.timeout);
        }
        if (receive.selector != null) {
            receiveAction.setSelect(receive.selector);
        }
        if (receive.extract != null) {
            receiveAction.setExtract(receive.extract);
        }

        return receiveAction;
    }

    private static void applyMessage(Send sendAction, SendElement element) {
        if (element.message != null) {
            sendAction.setMessage(element.message);
        }
        if (element.fork != null) {
            sendAction.setFork(element.fork);
        }
        if (element.extract != null) {
            sendAction.setExtract(element.extract);
        }
    }

    private static void applyResponse(GrpcClientResponseActionBuilder builder, SendElement element) {
        if (element.response != null) {
            builder.response(status(element.response.status));
        } else if (Boolean.TRUE.equals(element.streamMessage)) {
            builder.streamMessage();
        } else if (element.streamCompleted != null) {
            builder.streamCompleted(status(element.streamCompleted.status));
        }
    }

    private static void applyResponse(GrpcServerResponseActionBuilder builder, SendElement element) {
        if (element.response != null) {
            builder.response(status(element.response.status));
        } else if (Boolean.TRUE.equals(element.streamMessage)) {
            builder.streamMessage();
        } else if (element.streamCompleted != null) {
            builder.streamCompleted(status(element.streamCompleted.status));
        }

        if (element.statusDescription != null) {
            builder.statusDescription(element.statusDescription);
        }
        element.trailers.forEach(trailer -> builder.trailer(trailer.name, trailer.value));
    }

    private static void applyResponse(GrpcClientResponseActionBuilder builder, ReceiveElement element) {
        if (element.response != null) {
            builder.response(status(element.response.status));
        } else if (Boolean.TRUE.equals(element.streamMessage)) {
            builder.streamMessage();
        } else if (element.streamCompleted != null) {
            builder.streamCompleted(status(element.streamCompleted.status));
        }
    }

    private static String status(String status) {
        return status == null || status.isBlank() ? "OK" : status;
    }

    private String requireClient() {
        if (client == null) {
            throw new CitrusRuntimeException("Missing gRPC client - set the 'client' property");
        }
        return client;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public static class SendElement {
        private String call;
        private List<Metadata> metadata = new ArrayList<>();
        private Long deadline;
        private Status response;
        private Boolean streamMessage;
        private Status streamCompleted;
        private String statusDescription;
        private List<Metadata> trailers = new ArrayList<>();
        private Message message;
        private Message.Extract extract;
        private Boolean fork;

        @SchemaProperty(description = "The gRPC method to call, e.g. package.Service/Method.")
        public void setCall(String call) {
            this.call = call;
        }

        @SchemaProperty(description = "Request metadata entries.")
        public void setMetadata(List<Metadata> metadata) {
            this.metadata = metadata;
        }

        @SchemaProperty(description = "Call deadline in milliseconds.")
        public void setDeadline(Long deadline) {
            this.deadline = deadline;
        }

        @SchemaProperty(description = "Sends a unary response with an optional status code.")
        public void setResponse(Status response) {
            this.response = response;
        }

        @SchemaProperty(description = "Sends a stream message.")
        public void setStreamMessage(Boolean streamMessage) {
            this.streamMessage = streamMessage;
        }

        @SchemaProperty(description = "Completes the stream with an optional status code.")
        public void setStreamCompleted(Status streamCompleted) {
            this.streamCompleted = streamCompleted;
        }

        @SchemaProperty(description = "The status description.")
        public void setStatusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
        }

        @SchemaProperty(description = "Response trailer entries.")
        public void setTrailer(List<Metadata> trailers) {
            this.trailers = trailers;
        }

        @SchemaProperty(description = "The message payload and headers.")
        public void setMessage(Message message) {
            this.message = message;
        }

        @SchemaProperty(description = "Extracts variables from the message.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }

        @SchemaProperty(description = "Runs the send action asynchronously.")
        public void setFork(Boolean fork) {
            this.fork = fork;
        }
    }

    public static class ReceiveElement {
        private String call;
        private List<Metadata> metadata = new ArrayList<>();
        private Status response;
        private Boolean streamMessage;
        private Status streamCompleted;
        private String statusDescription;
        private List<Metadata> trailers = new ArrayList<>();
        private Message message;
        private Message.Extract extract;
        private Integer timeout;
        private String selector;

        @SchemaProperty(description = "The expected gRPC method, e.g. package.Service/Method.")
        public void setCall(String call) {
            this.call = call;
        }

        @SchemaProperty(description = "Expected metadata entries.")
        public void setMetadata(List<Metadata> metadata) {
            this.metadata = metadata;
        }

        @SchemaProperty(description = "Expects a unary response with an optional status code.")
        public void setResponse(Status response) {
            this.response = response;
        }

        @SchemaProperty(description = "Receives the next stream message.")
        public void setStreamMessage(Boolean streamMessage) {
            this.streamMessage = streamMessage;
        }

        @SchemaProperty(description = "Receives the stream completion with an optional status code.")
        public void setStreamCompleted(Status streamCompleted) {
            this.streamCompleted = streamCompleted;
        }

        @SchemaProperty(description = "The expected status description.")
        public void setStatusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
        }

        @SchemaProperty(description = "Expected trailer entries.")
        public void setTrailer(List<Metadata> trailers) {
            this.trailers = trailers;
        }

        @SchemaProperty(description = "The expected message payload and headers.")
        public void setMessage(Message message) {
            this.message = message;
        }

        @SchemaProperty(description = "Extracts variables from the message.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }

        @SchemaProperty(description = "The receive timeout in milliseconds.")
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        @SchemaProperty(description = "Message selector expression.")
        public void setSelector(String selector) {
            this.selector = selector;
        }
    }

    public static class Metadata {
        private String name;
        private String value;

        @SchemaProperty(required = true)
        public void setName(String name) {
            this.name = name;
        }

        @SchemaProperty(required = true)
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Status {
        private String status;

        @SchemaProperty(description = "The gRPC status code, defaults to OK.")
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
