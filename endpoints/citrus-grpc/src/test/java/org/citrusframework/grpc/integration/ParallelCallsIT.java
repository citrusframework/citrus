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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.endpoint.GrpcEndpoints;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Two calls in flight on one client are kept apart and can be selected by their call id.
 */
@Test
public class ParallelCallsIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private GrpcServer simulator;
    private GrpcClient client;

    @BeforeMethod(alwaysRun = true)
    public void startSimulator() {
        int port = findAvailableTcpPort(18860);
        simulator = GrpcEndpoints.grpc().server()
                .port(port)
                .descriptors("classpath:descriptors/test-protos.binpb")
                .timeout(5000L)
                .autoStart(true)
                .initialize()
                .build();

        client = GrpcEndpoints.grpc().client()
                .target("localhost:" + port)
                .descriptors("classpath:descriptors/test-protos.binpb")
                .timeout(5000L)
                .build();
    }

    @AfterMethod(alwaysRun = true)
    public void stopSimulator() {
        if (client != null) {
            client.destroy();
        }
        if (simulator != null) {
            simulator.stop();
        }
    }

    @CitrusTest
    public void shouldKeepConcurrentCallsApart() {
        List<String> callIds = new ArrayList<>();

        when(grpc().client(client).send()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Ada\" }"));
        run(context -> callIds.add((String) context.getVariable(GrpcMessageHeaders.CALL_ID)));

        when(grpc().client(client).send()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Grace\" }"));
        run(context -> callIds.add((String) context.getVariable(GrpcMessageHeaders.CALL_ID)));

        // the two calls may arrive in any order - echo the name actually received
        then(grpc().server(simulator).receive()
                .call("helloworld.Greeter/SayHello")
                .message()
                .extract(extractor().jsonPath().expression("$.name", "name")));

        then(grpc().server(simulator).send()
                .response()
                .message()
                .body("{ \"message\": \"Hello ${name}\" }"));

        then(grpc().server(simulator).receive()
                .call("helloworld.Greeter/SayHello")
                .message()
                .extract(extractor().jsonPath().expression("$.name", "name")));

        then(grpc().server(simulator).send()
                .response()
                .message()
                .body("{ \"message\": \"Hello ${name}\" }"));

        then(grpc().client(client).receive()
                .response()
                .selector("header:" + GrpcMessageHeaders.CALL_ID + "=" + callIds.get(0))
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));

        then(grpc().client(client).receive()
                .response()
                .selector("header:" + GrpcMessageHeaders.CALL_ID + "=" + callIds.get(1))
                .message()
                .body("{ \"message\": \"Hello Grace\" }"));
    }
}
