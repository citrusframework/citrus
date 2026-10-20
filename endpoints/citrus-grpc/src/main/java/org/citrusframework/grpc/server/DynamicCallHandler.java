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

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.HandlerRegistry;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.descriptor.DescriptorRegistryException;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageCodec;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.message.GrpcMetadataMapper;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fallback handler registry of the gRPC simulator. It resolves every call against the loaded
 * descriptors: methods outside the configured services answer {@code UNIMPLEMENTED}, known methods
 * are turned into Citrus call messages.
 */
public class DynamicCallHandler extends HandlerRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicCallHandler.class);

    private final GrpcServerEndpointConfiguration configuration;
    private final GrpcDescriptorRegistry descriptorRegistry;
    private final GrpcMessageCodec codec;
    private final ServerCallRegistry calls;
    private final MessageQueue inboundQueue;
    private final EndpointAdapter endpointAdapter;
    private final boolean testDriven;
    private final ScheduledExecutorService scheduler;

    public DynamicCallHandler(GrpcServerEndpointConfiguration configuration,
                              GrpcDescriptorRegistry descriptorRegistry,
                              GrpcMessageCodec codec,
                              ServerCallRegistry calls,
                              MessageQueue inboundQueue,
                              EndpointAdapter endpointAdapter,
                              boolean testDriven,
                              ScheduledExecutorService scheduler) {
        this.configuration = configuration;
        this.descriptorRegistry = descriptorRegistry;
        this.codec = codec;
        this.calls = calls;
        this.inboundQueue = inboundQueue;
        this.endpointAdapter = endpointAdapter;
        this.testDriven = testDriven;
        this.scheduler = scheduler;
    }

    @Override
    public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
        String serviceName = serviceName(methodName);
        if (serviceName == null || !isConfigured(serviceName)) {
            return null;
        }

        Descriptors.MethodDescriptor method;
        try {
            method = descriptorRegistry.findMethod(methodName);
        } catch (DescriptorRegistryException e) {
            return null;
        }

        return ServerMethodDefinition.create(methodDescriptor(method), new DynamicServerCallHandler(method));
    }

    private boolean isConfigured(String serviceName) {
        return configuration.getServices().isEmpty() || configuration.getServices().contains(serviceName);
    }

    private static String serviceName(String methodName) {
        int separator = methodName.lastIndexOf('/');
        return separator > 0 ? methodName.substring(0, separator) : null;
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

    private static String fullMethodName(Descriptors.MethodDescriptor method) {
        return method.getService().getFullName() + "/" + method.getName();
    }

    private static MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor(Descriptors.MethodDescriptor method) {
        return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(method.isServerStreaming()
                        ? MethodDescriptor.MethodType.SERVER_STREAMING
                        : MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(fullMethodName(method))
                .setRequestMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(method.getInputType())))
                .setResponseMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(method.getOutputType())))
                .build();
    }

    private void handleCall(ServerCall<DynamicMessage, DynamicMessage> call,
                            Metadata headers,
                            Descriptors.MethodDescriptor method,
                            DynamicMessage request,
                            AtomicReference<ServerCallRegistry.ActiveCall> activeCall) {
        String callId = UUID.randomUUID().toString();
        ServerCallRegistry.ActiveCall active = calls.register(callId, call, method, headers,
                configuration.getTimeout(), scheduler);
        activeCall.set(active);
        active.startTimeout();

        GrpcMessage callMessage = new GrpcMessage(codec.decode(request))
                .kind(GrpcMessageHeaders.KIND_CALL)
                .method(fullMethodName(method))
                .methodType(methodType(method))
                .callId(callId);
        GrpcMetadataMapper.fromMetadata(headers).forEach(callMessage::setHeader);

        if (testDriven) {
            inboundQueue.send(callMessage);
        } else if (endpointAdapter != null) {
            scheduler.execute(() -> handleWithAdapter(active, callMessage));
        } else {
            closeInternal(active, "Citrus simulator: no endpoint adapter available");
        }
    }

    private void handleStreamMessage(ServerCallRegistry.ActiveCall active,
                                     Descriptors.MethodDescriptor method,
                                     DynamicMessage message,
                                     int sequence) {
        if (!testDriven) {
            LOG.debug("Ignoring additional request message on gRPC call '{}' handled by an endpoint adapter",
                    active.getCallId());
            return;
        }

        GrpcMessage streamMessage = new GrpcMessage(codec.decode(message))
                .kind(GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                .method(fullMethodName(method))
                .methodType(methodType(method))
                .callId(active.getCallId())
                .streamSequence(sequence);

        active.restartTimeout();
        inboundQueue.send(streamMessage);
    }

    private void handleRequestCompleted(ServerCallRegistry.ActiveCall active, Descriptors.MethodDescriptor method) {
        if (!testDriven) {
            return;
        }

        GrpcMessage completed = new GrpcMessage("")
                .kind(GrpcMessageHeaders.KIND_STREAM_END)
                .method(fullMethodName(method))
                .methodType(methodType(method))
                .callId(active.getCallId())
                .statusCode(Status.Code.OK.name())
                .endOfStream(true);

        active.restartTimeout();
        inboundQueue.send(completed);
    }

    private void handleWithAdapter(ServerCallRegistry.ActiveCall active, GrpcMessage callMessage) {
        try {
            Message reply = endpointAdapter.handleMessage(callMessage);
            if (reply == null) {
                closeInternal(active, "Citrus simulator: no response from endpoint adapter");
                return;
            }

            GrpcMessage grpcReply = new GrpcMessage(reply);
            Status status = statusOf(grpcReply);
            Metadata trailers = GrpcMetadataMapper.toTrailers(grpcReply.getHeaders());
            Metadata headers = GrpcMetadataMapper.toMetadata(grpcReply.getHeaders());
            Descriptors.Descriptor outputType = active.getMethod().getOutputType();

            if (active.getMethod().isServerStreaming()) {
                if (status.isOk()) {
                    active.sendHeaders(headers);
                    active.sendMessage(responseMessage(grpcReply, outputType));
                }
            } else if (status.isOk()) {
                active.sendHeaders(headers);
                active.sendMessage(responseMessage(grpcReply, outputType));
            }

            active.close(status, trailers);
            calls.remove(active.getCallId());
        } catch (RuntimeException e) {
            LOG.warn("Failed to handle gRPC call '{}' with endpoint adapter", callMessage.getMethod(), e);
            closeInternal(active, "Citrus simulator: failed to handle call - " + e.getMessage());
        }
    }

    private DynamicMessage responseMessage(GrpcMessage reply, Descriptors.Descriptor outputType) {
        Object payload = reply.getPayload();
        if (payload == null || (payload instanceof String value && value.isBlank())) {
            return DynamicMessage.getDefaultInstance(outputType);
        }
        return codec.encode(outputType, payload);
    }

    static Status statusOf(GrpcMessage message) {
        String code = message.getStatusCode();
        if (code == null || code.isBlank()) {
            return Status.OK;
        }
        try {
            Status status = Status.fromCode(Status.Code.valueOf(code.toUpperCase(Locale.ROOT)));
            String description = message.getStatusDescription();
            if (description != null && !description.isBlank()) {
                status = status.withDescription(description);
            }
            return status;
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException(String.format("Unknown gRPC status code '%s'", code));
        }
    }

    private void closeInternal(ServerCallRegistry.ActiveCall active, String description) {
        active.close(Status.INTERNAL.withDescription(description), new Metadata());
        calls.remove(active.getCallId());
    }

    private final class DynamicServerCallHandler implements ServerCallHandler<DynamicMessage, DynamicMessage> {

        private final Descriptors.MethodDescriptor method;

        private DynamicServerCallHandler(Descriptors.MethodDescriptor method) {
            this.method = method;
        }

        @Override
        public ServerCall.Listener<DynamicMessage> startCall(ServerCall<DynamicMessage, DynamicMessage> call,
                                                             Metadata headers) {
            call.request(2);
            AtomicReference<ServerCallRegistry.ActiveCall> activeCall = new AtomicReference<>();
            AtomicBoolean requestCompleted = new AtomicBoolean(false);
            AtomicInteger sequence = new AtomicInteger();

            return new ServerCall.Listener<>() {
                @Override
                public void onMessage(DynamicMessage message) {
                    ServerCallRegistry.ActiveCall active = activeCall.get();
                    if (active == null) {
                        handleCall(call, headers, method, message, activeCall);
                    } else if (!requestCompleted.get()) {
                        handleStreamMessage(active, method, message, sequence.incrementAndGet());
                    }

                    if (method.isClientStreaming() && !requestCompleted.get()) {
                        call.request(1);
                    }
                }

                @Override
                public void onHalfClose() {
                    ServerCallRegistry.ActiveCall active = activeCall.get();
                    if (active != null && method.isClientStreaming()
                            && requestCompleted.compareAndSet(false, true)) {
                        handleRequestCompleted(active, method);
                    }
                }

                @Override
                public void onCancel() {
                    ServerCallRegistry.ActiveCall active = activeCall.get();
                    if (active != null) {
                        LOG.debug("gRPC call '{}' was cancelled by the client", active.getCallId());
                        active.markCancelled();
                    }
                }
            };
        }
    }
}
