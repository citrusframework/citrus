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
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.Message;

/**
 * Receives a gRPC message and enforces that the received message matches the expected call kind.
 */
public class GrpcClientReceiveAction extends ReceiveMessageAction {

    private final String expectedKind;

    public GrpcClientReceiveAction(ReceiveMessageActionBuilder<?, ?, ?> builder, String expectedKind) {
        super(builder);
        this.expectedKind = expectedKind;
    }

    @Override
    protected void validateMessage(Message message, TestContext context) {
        GrpcMessage grpcMessage = message instanceof GrpcMessage ? (GrpcMessage) message : new GrpcMessage(message);
        String actualKind = grpcMessage.getKind();
        String method = grpcMessage.getMethod();

        if (GrpcMessageHeaders.KIND_RESPONSE.equals(expectedKind)
                && !GrpcMessageHeaders.KIND_RESPONSE.equals(actualKind)) {
            throw new CitrusRuntimeException(String.format(
                    "gRPC method '%s' is server-streaming - use streamMessage() to receive stream messages",
                    method));
        }

        if (GrpcMessageHeaders.KIND_STREAM_MESSAGE.equals(expectedKind)) {
            if (GrpcMessageHeaders.KIND_RESPONSE.equals(actualKind)) {
                throw new CitrusRuntimeException(String.format(
                        "gRPC method '%s' is unary - use response() to receive the response", method));
            }
            if (GrpcMessageHeaders.KIND_STREAM_END.equals(actualKind)) {
                throw new CitrusRuntimeException(String.format(
                        "gRPC stream '%s' already ended with status %s", method, grpcMessage.getStatusCode()));
            }
        }

        if (GrpcMessageHeaders.KIND_STREAM_END.equals(expectedKind)
                && GrpcMessageHeaders.KIND_STREAM_MESSAGE.equals(actualKind)) {
            throw new CitrusRuntimeException(String.format(
                    "gRPC stream '%s' has unread messages - receive them with streamMessage() first", method));
        }

        super.validateMessage(message, context);
    }
}
