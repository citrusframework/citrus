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
package org.citrusframework.grpc.actions;

import java.util.LinkedHashMap;
import java.util.Map;

import io.grpc.Status;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.base.message.builder.ReceiveMessageBuilderSupport;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.validation.GrpcStatusValidationContext;
import org.citrusframework.message.MessageType;

/**
 * Receives a response, a stream message or the completion of a gRPC call.
 */
public class GrpcClientResponseActionBuilder
        extends ReceiveMessageAction.ReceiveMessageActionBuilder<GrpcClientReceiveAction, GrpcClientResponseActionBuilder.GrpcClientResponseActionBuilderSupport, GrpcClientResponseActionBuilder>
        implements org.citrusframework.api.actions.grpc.GrpcClientResponseActionBuilder<GrpcClientReceiveAction, GrpcClientResponseActionBuilder.GrpcClientResponseActionBuilderSupport, GrpcClientResponseActionBuilder> {

    private String kind;
    private String statusCode = "OK";
    private String statusDescription;
    private final Map<String, Object> trailers = new LinkedHashMap<>();

    public GrpcClientResponseActionBuilder() {
        message().type(MessageType.JSON);
    }

    @Override
    public GrpcClientResponseActionBuilder response() {
        return response("OK");
    }

    @Override
    public GrpcClientResponseActionBuilder response(String statusCode) {
        this.kind = GrpcMessageHeaders.KIND_RESPONSE;
        this.statusCode = statusCode;
        return this;
    }

    public GrpcClientResponseActionBuilder response(Status.Code statusCode) {
        return response(statusCode.name());
    }

    @Override
    public GrpcClientResponseActionBuilder streamMessage() {
        this.kind = GrpcMessageHeaders.KIND_STREAM_MESSAGE;
        return this;
    }

    @Override
    public GrpcClientResponseActionBuilder streamCompleted() {
        return streamCompleted("OK");
    }

    @Override
    public GrpcClientResponseActionBuilder streamCompleted(String statusCode) {
        this.kind = GrpcMessageHeaders.KIND_STREAM_END;
        this.statusCode = statusCode;
        return this;
    }

    public GrpcClientResponseActionBuilder streamCompleted(Status.Code statusCode) {
        return streamCompleted(statusCode.name());
    }

    @Override
    public GrpcClientResponseActionBuilder statusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
        return this;
    }

    @Override
    public GrpcClientResponseActionBuilder trailer(String name, Object value) {
        this.trailers.put(name, value);
        return this;
    }

    @Override
    public GrpcClientResponseActionBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new GrpcClientResponseActionBuilderSupport(this);
        }
        return super.getMessageBuilderSupport();
    }

    @Override
    protected GrpcClientReceiveAction doBuild() {
        if (GrpcMessageHeaders.KIND_RESPONSE.equals(kind) || GrpcMessageHeaders.KIND_STREAM_END.equals(kind)) {
            GrpcStatusValidationContext validationContext = new GrpcStatusValidationContext();
            validationContext.setStatusCode(statusCode);
            if (statusDescription != null) {
                validationContext.setStatusDescription(statusDescription);
            }
            trailers.forEach(validationContext::trailer);
            validate(validationContext);
        }

        return new GrpcClientReceiveAction(this, kind);
    }

    public static class GrpcClientResponseActionBuilderSupport
            extends ReceiveMessageBuilderSupport<GrpcClientReceiveAction, GrpcClientResponseActionBuilder, GrpcClientResponseActionBuilderSupport> {

        public GrpcClientResponseActionBuilderSupport(GrpcClientResponseActionBuilder delegate) {
            super(delegate);
        }
    }
}
