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

import io.grpc.Status;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.base.message.builder.SendMessageBuilderSupport;
import org.citrusframework.grpc.message.GrpcMessageHeaders;

/**
 * Sends a response, stream message or final status on a call received by a gRPC simulator endpoint.
 */
public class GrpcServerResponseActionBuilder
        extends SendMessageAction.SendMessageActionBuilder<SendMessageAction, GrpcServerResponseActionBuilder.GrpcServerResponseActionBuilderSupport, GrpcServerResponseActionBuilder>
        implements org.citrusframework.api.actions.grpc.GrpcServerResponseActionBuilder<SendMessageAction, GrpcServerResponseActionBuilder.GrpcServerResponseActionBuilderSupport, GrpcServerResponseActionBuilder> {

    @Override
    public GrpcServerResponseActionBuilder response() {
        return response("OK");
    }

    @Override
    public GrpcServerResponseActionBuilder response(String statusCode) {
        message().header(GrpcMessageHeaders.KIND, GrpcMessageHeaders.KIND_RESPONSE);
        message().header(GrpcMessageHeaders.STATUS_CODE, statusCode);
        return this;
    }

    public GrpcServerResponseActionBuilder response(Status.Code statusCode) {
        return response(statusCode.name());
    }

    @Override
    public GrpcServerResponseActionBuilder streamMessage() {
        message().header(GrpcMessageHeaders.KIND, GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        return this;
    }

    @Override
    public GrpcServerResponseActionBuilder streamCompleted() {
        return streamCompleted("OK");
    }

    @Override
    public GrpcServerResponseActionBuilder streamCompleted(String statusCode) {
        message().header(GrpcMessageHeaders.KIND, GrpcMessageHeaders.KIND_STREAM_END);
        message().header(GrpcMessageHeaders.STATUS_CODE, statusCode);
        return this;
    }

    public GrpcServerResponseActionBuilder streamCompleted(Status.Code statusCode) {
        return streamCompleted(statusCode.name());
    }

    @Override
    public GrpcServerResponseActionBuilder statusDescription(String description) {
        message().header(GrpcMessageHeaders.STATUS_DESCRIPTION, description);
        return this;
    }

    @Override
    public GrpcServerResponseActionBuilder trailer(String name, Object value) {
        message().header(GrpcMessageHeaders.TRAILER_PREFIX + name, value);
        return this;
    }

    @Override
    public GrpcServerResponseActionBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new GrpcServerResponseActionBuilderSupport(this);
        }
        return super.getMessageBuilderSupport();
    }

    @Override
    protected SendMessageAction doBuild() {
        return new SendMessageAction(this);
    }

    public static class GrpcServerResponseActionBuilderSupport
            extends SendMessageBuilderSupport<SendMessageAction, GrpcServerResponseActionBuilder, GrpcServerResponseActionBuilderSupport> {

        public GrpcServerResponseActionBuilderSupport(GrpcServerResponseActionBuilder delegate) {
            super(delegate);
        }
    }
}
