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

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageCodec;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.message.GrpcMetadataMapper;
import org.citrusframework.grpc.tls.GrpcTlsSupport;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC client endpoint. Calls any gRPC method by its full name using dynamically built messages
 * from the configured Protobuf descriptors - no generated stubs are needed.
 *
 * @since 5.1
 */
public class GrpcClient extends AbstractEndpoint implements Producer, SelectiveConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

    private final ClientCallRegistry calls = new ClientCallRegistry();

    private ManagedChannel channel;
    private GrpcDescriptorRegistry descriptorRegistry;
    private GrpcMessageCodec codec;

    public GrpcClient() {
        this(new GrpcClientEndpointConfiguration());
    }

    public GrpcClient(GrpcClientEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public GrpcClientEndpointConfiguration getEndpointConfiguration() {
        return (GrpcClientEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        GrpcMessage grpcMessage = new GrpcMessage(message);

        if (GrpcMessageHeaders.KIND_STREAM_END.equals(grpcMessage.getKind())) {
            calls.complete(currentCallId(context));
            return;
        }

        if (GrpcMessageHeaders.KIND_STREAM_MESSAGE.equals(grpcMessage.getKind())) {
            String callId = currentCallId(context);
            ClientCallRegistry.LiveCall liveCall = calls.getLiveCall(callId);
            if (liveCall == null) {
                throw new CitrusRuntimeException("Unknown gRPC call '%s' - open a call first".formatted(callId));
            }

            calls.sendMessage(callId, codec().encode(liveCall.method().getInputType(), message.getPayload()));
            return;
        }

        String methodName = grpcMessage.getMethod();

        if (methodName == null || methodName.isBlank()) {
            throw new CitrusRuntimeException("Missing gRPC method name - set the '%s' header".formatted(GrpcMessageHeaders.METHOD));
        }

        Descriptors.MethodDescriptor method = descriptorRegistry().findMethod(methodName);
        DynamicMessage request = codec().encode(method.getInputType(), message.getPayload());

        String callId = UUID.randomUUID().toString();
        ClientCallQueue queue = new ClientCallQueue(callId, getEndpointConfiguration().getMaxBufferedMessages());
        ClientCall<DynamicMessage, DynamicMessage> call = newCall(method, grpcMessage);

        calls.register(callId, queue, call, method, () -> call.cancel("cancelled by Citrus test", null));
        registerCleanup(context);

        call.start(new ClientCallListener(queue, method, codec()), GrpcMetadataMapper.toMetadata(grpcMessage.getHeaders()));
        call.request(Integer.MAX_VALUE);
        call.sendMessage(request);

        if (!method.isClientStreaming()) {
            call.halfClose();
        }

        context.getVariables().put(GrpcMessageHeaders.CALL_ID, callId);
        context.getVariables().put(correlationKeyName(), callId);
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(null, context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        String callId = resolveCallId(selector, context);
        ClientCallQueue queue = calls.get(callId);

        if (queue == null) {
            throw new CitrusRuntimeException("Unknown gRPC call '%s'".formatted(callId));
        }

        if (queue.isOverflowed()) {
            calls.cancel(callId);
            throw new CitrusRuntimeException("gRPC call '%s' exceeded the maximum of %d buffered messages"
                    .formatted(callId, queue.getMaxBufferedMessages()));
        }

        GrpcMessage message = queue.poll(timeout);

        if (message == null) {
            throw new MessageTimeoutException(timeout, getName());
        }

        if (GrpcMessageHeaders.KIND_STREAM_END.equals(message.getKind())) {
            calls.remove(callId);
        }

        return message;
    }

    /**
     * Cancels the call started by the preceding send in this test.
     *
     * @param context the test context
     */
    public void cancel(TestContext context) {
        calls.cancel(currentCallId(context));
    }

    private String currentCallId(TestContext context) {
        Object callId = context.getVariables().get(correlationKeyName());
        if (callId == null) {
            throw new CitrusRuntimeException("No open gRPC call available - send a request first");
        }

        return callId.toString();
    }

    @Override
    public Producer createProducer() {
        return this;
    }

    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }

    /**
     * Shuts down the underlying channel.
     */
    public void destroy() {
        calls.cancelAll();

        if (channel != null) {
            channel.shutdownNow();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            channel = null;
        }
    }

    private String resolveCallId(String selector, TestContext context) {
        if (selector != null && !selector.isBlank()) {
            String key = "header:" + GrpcMessageHeaders.CALL_ID + "=";
            if (!selector.startsWith(key)) {
                throw new CitrusRuntimeException("Unsupported gRPC message selector '%s' - use '%s<callId>'".formatted(selector, key));
            }
            return selector.substring(key.length());
        }

        Object callId = context.getVariables().get(correlationKeyName());
        if (callId == null) {
            throw new CitrusRuntimeException("No gRPC call available - send a request first");
        }

        return callId.toString();
    }

    private String correlationKeyName() {
        return getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
    }

    private void registerCleanup(TestContext context) {
        String cleanupKey = "citrus_grpc_cleanup_registered_" + getName();
        if (context.getVariables().containsKey(cleanupKey)) {
            return;
        }

        context.getVariables().put(cleanupKey, Boolean.TRUE);
        context.doFinally(() -> testContext -> calls.cancelAll());
    }

    private ClientCall<DynamicMessage, DynamicMessage> newCall(Descriptors.MethodDescriptor method, GrpcMessage message) {
        Duration deadline = message.getDeadline() != null
                ? message.getDeadline()
                : getEndpointConfiguration().getDeadline();

        CallOptions callOptions = deadline != null
                ? CallOptions.DEFAULT.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                : CallOptions.DEFAULT;

        io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> descriptor = io.grpc.MethodDescriptor
                .<DynamicMessage, DynamicMessage>newBuilder()
                .setType(grpcMethodType(method))
                .setFullMethodName(method.getService().getFullName() + "/" + method.getName())
                .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(method.getInputType())))
                .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(method.getOutputType())))
                .build();

        return channel().newCall(descriptor, callOptions);
    }

    private synchronized ManagedChannel channel() {
        if (channel == null) {
            GrpcClientEndpointConfiguration configuration = getEndpointConfiguration();
            if (configuration.getTarget() == null || configuration.getTarget().isBlank()) {
                throw new CitrusRuntimeException("Missing gRPC client target - set the endpoint target (host:port)");
            }

            if (configuration.isTls()) {
                channel = io.grpc.Grpc.newChannelBuilder(configuration.getTarget(),
                                GrpcTlsSupport.clientCredentials(configuration.getTrustCertificate()))
                        .build();
            } else {
                channel = io.grpc.ManagedChannelBuilder.forTarget(configuration.getTarget()).usePlaintext().build();
            }
        }

        return channel;
    }

    private synchronized GrpcDescriptorRegistry descriptorRegistry() {
        if (descriptorRegistry == null) {
            GrpcClientEndpointConfiguration configuration = getEndpointConfiguration();
            descriptorRegistry = new GrpcDescriptorRegistry(configuration.getDescriptors(), configuration.getProtoPaths());
        }

        return descriptorRegistry;
    }

    private synchronized GrpcMessageCodec codec() {
        if (codec == null) {
            codec = new GrpcMessageCodec(descriptorRegistry(), getEndpointConfiguration().isOmitDefaultValues());
        }

        return codec;
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

    private static String methodType(Descriptors.MethodDescriptor method) {
        if (method.isClientStreaming() && method.isServerStreaming()) {
            return GrpcMessageHeaders.METHOD_TYPE_BIDI_STREAMING;
        }
        if (method.isClientStreaming()) {
            return GrpcMessageHeaders.METHOD_TYPE_CLIENT_STREAMING;
        }
        if (method.isServerStreaming()) {
            return GrpcMessageHeaders.METHOD_TYPE_SERVER_STREAMING;
        }
        return GrpcMessageHeaders.METHOD_TYPE_UNARY;
    }

    /**
     * Listener translating gRPC callbacks into Citrus messages.
     */
    private static final class ClientCallListener extends ClientCall.Listener<DynamicMessage> {

        private final ClientCallQueue queue;
        private final Descriptors.MethodDescriptor method;
        private final GrpcMessageCodec codec;
        private int sequence;
        private DynamicMessage response;

        private ClientCallListener(ClientCallQueue queue, Descriptors.MethodDescriptor method, GrpcMessageCodec codec) {
            this.queue = queue;
            this.method = method;
            this.codec = codec;
        }

        @Override
        public void onHeaders(Metadata headers) {
            Map<String, Object> mapped = GrpcMetadataMapper.fromMetadata(headers);
            if (!mapped.isEmpty()) {
                logger.debug("Received gRPC response headers for call '{}': {}", queue.getCallId(), mapped.keySet());
            }
        }

        @Override
        public void onMessage(DynamicMessage message) {
            if (!method.isServerStreaming()) {
                this.response = message;
                return;
            }

            GrpcMessage grpcMessage = new GrpcMessage(codec.decode(message))
                    .method(method.getService().getFullName() + "/" + method.getName())
                    .methodType(methodType(method))
                    .callId(queue.getCallId())
                    .kind(GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                    .streamSequence(sequence++);

            queue.offer(grpcMessage);
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            GrpcMessage grpcMessage = new GrpcMessage(response != null ? codec.decode(response) : "")
                    .method(method.getService().getFullName() + "/" + method.getName())
                    .methodType(methodType(method))
                    .callId(queue.getCallId())
                    .kind(method.isServerStreaming()
                            ? GrpcMessageHeaders.KIND_STREAM_END
                            : GrpcMessageHeaders.KIND_RESPONSE)
                    .statusCode(status.getCode().name())
                    .endOfStream(true);

            if (status.getDescription() != null) {
                grpcMessage.statusDescription(status.getDescription());
            }

            grpcMessage.getHeaders().putAll(GrpcMetadataMapper.fromTrailers(trailers));
            queue.offer(grpcMessage);
        }
    }
}
