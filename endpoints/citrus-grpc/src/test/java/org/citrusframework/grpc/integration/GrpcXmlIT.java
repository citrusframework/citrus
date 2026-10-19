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
package org.citrusframework.grpc.integration;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.endpoint.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * Runs gRPC client and simulator from an XML test.
 */
public class GrpcXmlIT extends TestNGCitrusSpringSupport {

    @BindToRegistry
    private final GrpcServer simulator = GrpcEndpoints.grpc().server()
            .port(18870)
            .descriptors("classpath:descriptors/test-protos.binpb")
            .timeout(5000L)
            .autoStart(true)
            .initialize()
            .build();

    @BindToRegistry
    private final GrpcClient client = GrpcEndpoints.grpc().client()
            .target("localhost:18870")
            .descriptors("classpath:descriptors/test-protos.binpb")
            .timeout(5000L)
            .build();

    @Test
    @CitrusTestSource(type = TestLoader.XML, name = "grpc-xml.citrus.it",
            packageName = "org.citrusframework.grpc.integration")
    public void grpcXml() {
    }
}
