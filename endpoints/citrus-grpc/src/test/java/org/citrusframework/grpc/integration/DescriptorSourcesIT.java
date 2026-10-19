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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.endpoint.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * The same call works with descriptors from a .proto file, a descriptor set and a generated class.
 */
@Test
public class DescriptorSourcesIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private GrpcServer simulator;
    private int port;

    @BeforeMethod(alwaysRun = true)
    public void startSimulator() {
        port = findAvailableTcpPort(18850);
        simulator = GrpcEndpoints.grpc().server()
                .port(port)
                .descriptors("classpath:descriptors/test-protos.binpb")
                .timeout(5000L)
                .autoStart(true)
                .initialize()
                .build();
    }

    @AfterMethod(alwaysRun = true)
    public void stopSimulator() {
        if (simulator != null) {
            simulator.stop();
        }
    }

    @CitrusTest
    public void shouldCallWithProtoFileSource() {
        verifyCall(clientWith("classpath:helloworld/greeter.proto"));
    }

    @CitrusTest
    public void shouldCallWithDescriptorSetSource() {
        verifyCall(clientWith("classpath:descriptors/test-protos.binpb"));
    }

    @CitrusTest
    public void shouldCallWithGeneratedClassSource() {
        verifyCall(clientWith(GreeterGrpc.class.getName()));
    }

    private GrpcClient clientWith(String descriptorSource) {
        return GrpcEndpoints.grpc().client()
                .target("localhost:" + port)
                .descriptors(descriptorSource)
                .timeout(5000L)
                .build();
    }

    private void verifyCall(GrpcClient client) {
        try {
            when(grpc().client(client).send()
                    .call("helloworld.Greeter/SayHello")
                    .message()
                    .body("{ \"name\": \"Ada\" }"));

            then(grpc().server(simulator).receive()
                    .call("helloworld.Greeter/SayHello")
                    .message()
                    .body("{ \"name\": \"Ada\", \"userId\": 0 }"));

            then(grpc().server(simulator).send()
                    .response()
                    .message()
                    .body("{ \"message\": \"Hello Ada\" }"));

            then(grpc().client(client).receive()
                    .response()
                    .message()
                    .body("{ \"message\": \"Hello Ada\" }"));
        } finally {
            client.destroy();
        }
    }
}
