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

import java.time.Duration;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.base.message.builder.SendMessageBuilderSupport;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.MessageType;

/**
 * Sends a request message to a gRPC service.
 */
public class GrpcClientRequestActionBuilder
        extends SendMessageAction.SendMessageActionBuilder<SendMessageAction, GrpcClientRequestActionBuilder.GrpcClientRequestActionBuilderSupport, GrpcClientRequestActionBuilder>
        implements org.citrusframework.api.actions.grpc.GrpcClientRequestActionBuilder<SendMessageAction, GrpcClientRequestActionBuilder.GrpcClientRequestActionBuilderSupport, GrpcClientRequestActionBuilder> {

    public GrpcClientRequestActionBuilder() {
        message().type(MessageType.JSON);
    }

    @Override
    public GrpcClientRequestActionBuilder call(String method) {
        message().header(GrpcMessageHeaders.METHOD, method);
        return this;
    }

    @Override
    public GrpcClientRequestActionBuilder metadata(String name, Object value) {
        message().header(name, value);
        return this;
    }

    @Override
    public GrpcClientRequestActionBuilder deadline(Duration deadline) {
        message().header(GrpcMessageHeaders.DEADLINE, deadline);
        return this;
    }

    @Override
    public GrpcClientRequestActionBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new GrpcClientRequestActionBuilderSupport(this);
        }
        return super.getMessageBuilderSupport();
    }

    @Override
    protected SendMessageAction doBuild() {
        return new SendMessageAction(this);
    }

    public static class GrpcClientRequestActionBuilderSupport
            extends SendMessageBuilderSupport<SendMessageAction, GrpcClientRequestActionBuilder, GrpcClientRequestActionBuilderSupport> {

        public GrpcClientRequestActionBuilderSupport(GrpcClientRequestActionBuilder delegate) {
            super(delegate);
        }
    }
}
