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

package org.citrusframework.dsl.grpc;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.actions.grpc.GrpcActionBuilder;
import org.citrusframework.api.actions.grpc.GrpcTestActions;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Adds the gRPC test action support to the standard test action support interface. The gRPC actions
 * are provided by the optional citrus-grpc module that has to be added to the project.
 */
public interface GrpcTestActionSupport extends GrpcTestActions {

    /**
     * Provides the gRPC test action builder. Requires the citrus-grpc module on the classpath.
     * @return
     */
    @Override
    default GrpcActionBuilder<?, ?> grpc() {
        return (GrpcActionBuilder<?, ?>) TestActionBuilder.lookup("grpc")
                .orElseThrow(() -> new CitrusRuntimeException("Missing Citrus module 'citrus-grpc' for action 'grpc' - " +
                        "please add org.citrusframework:citrus-grpc to your project"));
    }
}
