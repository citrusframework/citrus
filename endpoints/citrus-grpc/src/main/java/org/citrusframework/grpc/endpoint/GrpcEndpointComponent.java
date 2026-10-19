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
package org.citrusframework.grpc.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.client.GrpcClientEndpointConfiguration;

/**
 * Creates a gRPC client from an endpoint URI such as
 * {@code grpc://localhost:50051?descriptors=classpath:helloworld.proto}.
 */
public class GrpcEndpointComponent extends AbstractEndpointComponent {

    public GrpcEndpointComponent() {
        super("grpc");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        GrpcClient client = new GrpcClient();
        GrpcClientEndpointConfiguration configuration = client.getEndpointConfiguration();
        configuration.setTarget(resourcePath);

        if (parameters.containsKey("descriptors")) {
            configuration.setDescriptors(java.util.Arrays.asList(parameters.remove("descriptors").split(",")));
        }
        if (parameters.containsKey("protoPaths")) {
            configuration.setProtoPaths(java.util.Arrays.asList(parameters.remove("protoPaths").split(",")));
        }

        enrichEndpointConfiguration(configuration,
                getEndpointConfigurationParameters(parameters, GrpcClientEndpointConfiguration.class), context);

        return client;
    }
}
