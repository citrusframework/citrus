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

import static org.citrusframework.json.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * The {@code grpc2grpc.PetStoreService} (the Beeceptor demo service) simulated with the Citrus gRPC
 * simulator, the same way the GraphQL client/server integration tests simulate their API. Covers
 * all five RPC kinds: unary (twice), server streaming, client streaming and bidirectional
 * streaming.
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class PetStoreSimulationIT implements TestActionSupport {

    private static final String PET_STORE = "grpc2grpc.PetStoreService";
    private static final String DESCRIPTORS = "classpath:org/citrusframework/grpc/streaming/petstore.proto";

    private GrpcServer simulator;
    private GrpcClient client;

    @BeforeEach
    public void startSimulator() {
        int port = findAvailableTcpPort(18890);

        simulator = GrpcEndpoints.grpc().server()
                .port(port)
                .descriptors(DESCRIPTORS)
                .timeout(5000L)
                .autoStart(true)
                .initialize()
                .build();

        client = GrpcEndpoints.grpc().client()
                .target("localhost:" + port)
                .descriptors(DESCRIPTORS)
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
    public void shouldCallPetById(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call(PET_STORE + "/PetById")
                .message()
                .body("{ \"id\": 123 }"));

        runner.then(grpc().server(simulator).receive()
                .call(PET_STORE + "/PetById")
                .message()
                .body("{ \"id\": 123 }"));

        runner.then(grpc().server(simulator).send()
                .response()
                .message()
                .body("{ \"pet\": { \"id\": 304104, \"name\": \"Jellybean\" } }"));

        runner.then(grpc().client(client).receive()
                .response()
                .message()
                .validate(jsonPath().expression("$.pet.id", "@isNumber()@"))
                .validate(jsonPath().expression("$.pet.name", "@notEmpty()@")));
    }

    @Test
    @CitrusTest
    public void shouldCallUserByName(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call(PET_STORE + "/UserByName")
                .message()
                .body("{ \"username\": \"ada\" }"));

        runner.then(grpc().server(simulator).receive()
                .call(PET_STORE + "/UserByName")
                .message()
                .body("{ \"username\": \"ada\" }"));

        runner.then(grpc().server(simulator).send()
                .response()
                .message()
                .body("{ \"user\": { \"id\": 166755, \"username\": \"Nienow8333\", \"email\": \"eldorazulauf@schroeder.io\", \"phone\": \"4132863446\" } }"));

        runner.then(grpc().client(client).receive()
                .response()
                .message()
                .validate(jsonPath().expression("$.user.username", "@notEmpty()@"))
                .validate(jsonPath().expression("$.user.email", "@contains('@')@")));
    }

    @Test
    @CitrusTest
    public void shouldStreamUsersAndComplete(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call(PET_STORE + "/ListUsers")
                .message()
                .body("{ \"msg\": \"hello\" }"));

        runner.then(grpc().server(simulator).receive()
                .call(PET_STORE + "/ListUsers")
                .message()
                .body("{ \"msg\": \"hello\" }"));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body("{ \"id\": 168414, \"username\": \"Pacocha4001\", \"email\": \"christianahilll@schiller.com\", \"phone\": \"2746917635\" }"));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body("{ \"id\": 945780, \"username\": \"Schroeder1676\", \"email\": \"marcellemertz@schaden.net\", \"phone\": \"8822339849\" }"));

        runner.then(grpc().server(simulator).send().streamCompleted());

        for (int i = 0; i < 2; i++) {
            runner.then(grpc().client(client).receive()
                    .streamMessage()
                    .message()
                    .validate(jsonPath().expression("$.username", "@notEmpty()@")));
        }

        runner.then(grpc().client(client).receive().streamCompleted());
    }

    @Test
    @CitrusTest
    public void shouldCallClientStreamingMethod(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call(PET_STORE + "/StoreUsers")
                .message()
                .body("{ \"id\": 1, \"username\": \"ada\", \"email\": \"ada@example.com\", \"phone\": \"555-1\" }"));

        runner.then(grpc().server(simulator).receive()
                .call(PET_STORE + "/StoreUsers")
                .message()
                .body("{ \"id\": 1, \"username\": \"ada\", \"email\": \"ada@example.com\", \"phone\": \"555-1\" }"));

        runner.when(grpc().client(client).send()
                .streamMessage()
                .message()
                .body("{ \"id\": 2, \"username\": \"bob\", \"email\": \"bob@example.com\", \"phone\": \"555-2\" }"));

        runner.then(grpc().server(simulator).receive()
                .streamMessage()
                .message()
                .body("{ \"id\": 2, \"username\": \"bob\", \"email\": \"bob@example.com\", \"phone\": \"555-2\" }"));

        runner.when(grpc().client(client).send()
                .streamMessage()
                .message()
                .body("{ \"id\": 3, \"username\": \"cid\", \"email\": \"cid@example.com\", \"phone\": \"555-3\" }"));

        runner.then(grpc().server(simulator).receive()
                .streamMessage()
                .message()
                .body("{ \"id\": 3, \"username\": \"cid\", \"email\": \"cid@example.com\", \"phone\": \"555-3\" }"));

        runner.when(grpc().client(client).send().streamCompleted());

        runner.then(grpc().server(simulator).receive().streamCompleted());

        runner.then(grpc().server(simulator).send()
                .response()
                .message()
                .body("{ \"msg\": \"received 3\" }"));

        runner.then(grpc().client(client).receive()
                .response()
                .message()
                .validate(jsonPath().expression("$.msg", "@notEmpty()@")));
    }

    @Test
    @CitrusTest
    public void shouldCallBidirectionalMethod(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(client).send()
                .call(PET_STORE + "/BulkUsers")
                .message()
                .body("{ \"id\": 1, \"username\": \"ada\", \"email\": \"ada@example.com\", \"phone\": \"555-1\" }"));

        runner.then(grpc().server(simulator).receive()
                .call(PET_STORE + "/BulkUsers")
                .message()
                .body("{ \"id\": 1, \"username\": \"ada\", \"email\": \"ada@example.com\", \"phone\": \"555-1\" }"));

        runner.then(grpc().server(simulator).send()
                .streamMessage()
                .message()
                .body("{ \"id\": 10, \"username\": \"echo\", \"email\": \"echo@example.com\", \"phone\": \"555-10\" }"));

        runner.then(grpc().client(client).receive()
                .streamMessage()
                .message()
                .validate(jsonPath().expression("$.username", "echo")));

        runner.when(grpc().client(client).send()
                .streamMessage()
                .message()
                .body("{ \"id\": 2, \"username\": \"bob\", \"email\": \"bob@example.com\", \"phone\": \"555-2\" }"));

        runner.then(grpc().server(simulator).receive()
                .streamMessage()
                .message()
                .body("{ \"id\": 2, \"username\": \"bob\", \"email\": \"bob@example.com\", \"phone\": \"555-2\" }"));

        runner.when(grpc().client(client).send().streamCompleted());

        runner.then(grpc().server(simulator).receive().streamCompleted());

        runner.then(grpc().server(simulator).send().streamCompleted());

        runner.then(grpc().client(client).receive().streamCompleted());
    }
}
