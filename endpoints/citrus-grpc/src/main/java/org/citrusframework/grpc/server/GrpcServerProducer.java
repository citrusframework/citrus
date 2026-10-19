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

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.Metadata;
import io.grpc.Status;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageCodec;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.message.GrpcMetadataMapper;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.messaging.Producer;

/**
 * Producer of the gRPC simulator. It writes response messages, stream messages and the final status
 * to the {@link io.grpc.ServerCall} that the preceding receive took.
 */
public class GrpcServerProducer implements Producer {

    private final ServerCallRegistry calls;
    private final GrpcMessageCodec codec;
    private final MessageCorrelator correlator;
    private final String correlationKeyName;
    private final String name;

    public GrpcServerProducer(ServerCallRegistry calls,
                              GrpcMessageCodec codec,
                              MessageCorrelator correlator,
                              String correlationKeyName,
                              String name) {
        this.calls = calls;
        this.codec = codec;
        this.correlator = correlator;
        this.correlationKeyName = correlationKeyName;
        this.name = name;
    }

    @Override
    public void send(Message message, TestContext context) {
        GrpcMessage grpcMessage = new GrpcMessage(message);
        String callId = grpcMessage.getCallId();
        if (callId == null) {
            Object correlationKey = context.getVariables().get(correlationKeyName);
            callId = correlationKey != null ? correlationKey.toString() : null;
        }

        if (callId == null) {
            throw new CitrusRuntimeException("No gRPC call available - receive a call first");
        }

        ServerCallRegistry.ActiveCall call = calls.get(callId);
        if (call == null) {
            throw new CitrusRuntimeException(String.format("Unknown gRPC call '%s'", callId));
        }

        if (call.isCancelled()) {
            throw new CitrusRuntimeException(String.format("gRPC call '%s' was cancelled by the client", callId));
        }

        Status status = DynamicCallHandler.statusOf(grpcMessage);
        Metadata headers = GrpcMetadataMapper.toMetadata(grpcMessage.getHeaders());
        Metadata trailers = GrpcMetadataMapper.toTrailers(grpcMessage.getHeaders());

        if (GrpcMessageHeaders.KIND_STREAM_END.equals(grpcMessage.getKind())) {
            call.close(status, trailers);
            calls.remove(callId);
            return;
        }

        if (GrpcMessageHeaders.KIND_STREAM_MESSAGE.equals(grpcMessage.getKind())) {
            call.sendHeaders(headers);
            call.sendMessage(encode(grpcMessage, call.getMethod().getOutputType()));
            call.restartTimeout();
            return;
        }

        if (status.isOk()) {
            call.sendHeaders(headers);
            call.sendMessage(encode(grpcMessage, call.getMethod().getOutputType()));
            call.close(Status.OK, trailers);
        } else {
            call.close(status, trailers);
        }

        calls.remove(callId);
    }

    private DynamicMessage encode(GrpcMessage message, Descriptors.Descriptor outputType) {
        Object payload = message.getPayload();
        if (payload == null || (payload instanceof String value && value.isBlank())) {
            return DynamicMessage.getDefaultInstance(outputType);
        }
        return codec.encode(outputType, payload);
    }

    @Override
    public String getName() {
        return name;
    }
}
