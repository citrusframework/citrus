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
package org.citrusframework.api.actions.grpc;

import org.citrusframework.TestAction;
import org.citrusframework.api.actions.ReceiveActionBuilder;
import org.citrusframework.api.actions.ReceiveMessageBuilderFactory;

/**
 * Action builder interface for receiving a gRPC response message.
 */
public interface GrpcClientResponseActionBuilder<T extends TestAction, M extends ReceiveMessageBuilderFactory<T, M>, B extends GrpcClientResponseActionBuilder<T, M, B>>
        extends ReceiveActionBuilder<T, M, B> {

    /**
     * Expects the response of a unary call with status {@code OK}.
     * @return
     */
    B response();

    /**
     * Expects the response of a unary call with the given status code (e.g. {@code OK}, {@code NOT_FOUND}).
     * @param statusCode
     * @return
     */
    B response(String statusCode);

    /**
     * Receives the next message of a server streaming call.
     * @return
     */
    B streamMessage();

    /**
     * Receives the end of a server streaming call with status {@code OK}.
     * @return
     */
    B streamCompleted();

    /**
     * Receives the end of a server streaming call with the given status code.
     * @param statusCode
     * @return
     */
    B streamCompleted(String statusCode);

    /**
     * Expects the given status description.
     * @param statusDescription
     * @return
     */
    B statusDescription(String statusDescription);

    /**
     * Expects the given response trailer.
     * @param name
     * @param value
     * @return
     */
    B trailer(String name, Object value);
}
