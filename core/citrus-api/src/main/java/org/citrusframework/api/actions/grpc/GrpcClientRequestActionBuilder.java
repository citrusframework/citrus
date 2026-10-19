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

import java.time.Duration;

import org.citrusframework.TestAction;
import org.citrusframework.api.actions.SendActionBuilder;
import org.citrusframework.api.actions.SendMessageBuilderFactory;

/**
 * Action builder interface for sending a gRPC request message.
 */
public interface GrpcClientRequestActionBuilder<T extends TestAction, M extends SendMessageBuilderFactory<T, M>, B extends GrpcClientRequestActionBuilder<T, M, B>>
        extends SendActionBuilder<T, M, B> {

    /**
     * Sets the gRPC method to call. The method name is supposed to use the
     * {@code package.Service/Method} syntax.
     * @param method
     * @return
     */
    B call(String method);

    /**
     * Adds request metadata to the gRPC call.
     * @param name
     * @param value
     * @return
     */
    B metadata(String name, Object value);

    /**
     * Sets the gRPC deadline for this call. Overrides the endpoint default deadline.
     * @param deadline
     * @return
     */
    B deadline(Duration deadline);
}
