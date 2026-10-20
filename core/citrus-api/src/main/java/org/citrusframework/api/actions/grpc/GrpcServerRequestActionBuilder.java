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
 * Action builder interface for receiving a gRPC call on a simulator.
 */
public interface GrpcServerRequestActionBuilder<T extends TestAction, M extends ReceiveMessageBuilderFactory<T, M>, B extends GrpcServerRequestActionBuilder<T, M, B>>
        extends ReceiveActionBuilder<T, M, B> {

    /**
     * Expects the gRPC method name in the {@code package.Service/Method} syntax.
     * @param method
     * @return
     */
    B call(String method);

    /**
     * Expects the given request metadata.
     * @param name
     * @param value
     * @return
     */
    B metadata(String name, Object value);

    /**
     * Receives another request message of the current client streaming call.
     * @return
     */
    B streamMessage();

    /**
     * Receives the completion of the request side of the current client streaming call.
     * @return
     */
    B streamCompleted();
}
