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
import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.actions.ReceiveMessageBuilderFactory;
import org.citrusframework.api.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.api.actions.SendMessageBuilderFactory;

/**
 * Action builder interface for gRPC server operations.
 */
public interface GrpcServerActionBuilder<T extends TestAction, B extends TestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Receives a gRPC call.
     * @return
     */
    <M extends ReceiveMessageBuilderFactory<T, M>> GrpcServerRequestActionBuilder<T, M, ?> receive();

    /**
     * Sends a gRPC response message.
     * @return
     */
    <M extends SendMessageBuilderFactory<T, M>> GrpcServerResponseActionBuilder<T, M, ?> send();
}
