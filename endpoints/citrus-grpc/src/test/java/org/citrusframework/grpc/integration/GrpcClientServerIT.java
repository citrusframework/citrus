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
import org.citrusframework.grpc.endpoint.builder.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A Citrus client calls a Citrus simulator.
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class GrpcClientServerIT implements TestActionSupport {

    private GrpcServer simulator;
    private GrpcClient client;

    @BeforeEach
    public void startSimulator() {
        int port = findAvailableTcpPort(18840);
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

    @AfterEach
    public void stopSimulator() {
        if (client != null) {
            client.destroy();
        }
        if (simulator != null) {
            simulator.stop();
        }
    }

    @Test
    @CitrusTest
    public void shouldCallUnaryMethod(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Ada\" }"));

        runner.then(grpc().server(simulator).receive()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Ada\", \"userId\": 0 }"));

        runner.then(grpc().server(simulator).send()
                .response()
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));

        runner.then(grpc().client(client).receive()
                .response()
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));
    }

    @Test
    @CitrusTest
    public void shouldStreamMessagesAndComplete(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call("market.Prices/Subscribe")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        runner.then(grpc().server(simulator).receive()
                .call("market.Prices/Subscribe")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"0\" } }"));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"1\" } }"));

        runner.then(grpc().server(simulator).send().streamCompleted());

        runner.then(grpc().client(client).receive()
                .streamMessage()
                .message()
                .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"0\" } }"));

        runner.then(grpc().client(client).receive()
                .streamMessage()
                .message()
                .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"1\" } }"));

        runner.then(grpc().client(client).receive().streamCompleted());
    }

    @Test
    @CitrusTest
    public void shouldExpectErrorStatus(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        runner.then(grpc().server(simulator).receive()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        runner.then(grpc().server(simulator).send()
                .response("NOT_FOUND")
                .statusDescription("unknown user"));

        runner.then(grpc().client(client).receive()
                .response("NOT_FOUND")
                .statusDescription("@contains('unknown')@"));
    }
}
