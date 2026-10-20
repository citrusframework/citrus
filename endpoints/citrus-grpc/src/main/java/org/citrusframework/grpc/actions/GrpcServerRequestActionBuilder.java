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

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.base.message.builder.ReceiveMessageBuilderSupport;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.MessageType;

/**
 * Receives a call on a gRPC simulator endpoint and validates its method, metadata and request body.
 */
public class GrpcServerRequestActionBuilder
        extends ReceiveMessageAction.ReceiveMessageActionBuilder<ReceiveMessageAction, GrpcServerRequestActionBuilder.GrpcServerRequestActionBuilderSupport, GrpcServerRequestActionBuilder>
        implements org.citrusframework.api.actions.grpc.GrpcServerRequestActionBuilder<ReceiveMessageAction, GrpcServerRequestActionBuilder.GrpcServerRequestActionBuilderSupport, GrpcServerRequestActionBuilder> {

    public GrpcServerRequestActionBuilder() {
        message().type(MessageType.JSON);
    }

    @Override
    public GrpcServerRequestActionBuilder call(String method) {
        message().header(GrpcMessageHeaders.METHOD, method);
        return this;
    }

    @Override
    public GrpcServerRequestActionBuilder metadata(String name, Object value) {
        message().header(name, value);
        return this;
    }

    @Override
    public GrpcServerRequestActionBuilder streamMessage() {
        message().header(GrpcMessageHeaders.KIND, GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        return this;
    }

    @Override
    public GrpcServerRequestActionBuilder streamCompleted() {
        message().header(GrpcMessageHeaders.KIND, GrpcMessageHeaders.KIND_STREAM_END);
        return this;
    }

    @Override
    public GrpcServerRequestActionBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new GrpcServerRequestActionBuilderSupport(this);
        }
        return super.getMessageBuilderSupport();
    }

    @Override
    protected ReceiveMessageAction doBuild() {
        return new ReceiveMessageAction(this);
    }

    public static class GrpcServerRequestActionBuilderSupport
            extends ReceiveMessageBuilderSupport<ReceiveMessageAction, GrpcServerRequestActionBuilder, GrpcServerRequestActionBuilderSupport> {

        public GrpcServerRequestActionBuilderSupport(GrpcServerRequestActionBuilder delegate) {
            super(delegate);
        }
    }
}
