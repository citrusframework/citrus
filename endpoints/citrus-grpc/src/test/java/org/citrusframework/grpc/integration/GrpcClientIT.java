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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.endpoint.builder.GrpcEndpoints;
import org.citrusframework.grpc.integration.service.GreeterService;
import org.citrusframework.grpc.integration.service.PricesService;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A Citrus client calls a real grpc-java service implemented with generated stubs.
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class GrpcClientIT implements TestActionSupport {

    private static final int port = findAvailableTcpPort(18810);

    @BindToRegistry
    private final GrpcClient greeter = GrpcEndpoints.grpc().client()
            .target("localhost:" + port)
            .descriptors("classpath:descriptors/test-protos.binpb")
            .timeout(5000L)
            .build();

    private static Server server;

    @BeforeAll
    public static void startGrpcService() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new GreeterService())
                .addService(new PricesService())
                .intercept(new GreeterService.MetadataInterceptor())
                .build()
                .start();
    }

    @AfterAll
    public static void stopGrpcService() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    @CitrusTest
    public void shouldCallUnaryMethodWithMetadataAndTrailers(@CitrusResource GherkinTestActionRunner runner) {
        GreeterService.reset();

        runner.when(grpc().client(greeter).send()
                .call("helloworld.Greeter/SayHello")
                .metadata("x-request-id", "42")
                .message()
                .body("{ \"name\": \"Ada\" }"));

        runner.then(grpc().client(greeter).receive()
                .response()
                .trailer("x-trailer", "trailer-value")
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));

        assertThat(GreeterService.LAST_REQUEST_METADATA.get().get(GreeterService.REQUEST_ID))
                .isEqualTo("42");
    }

    @Test
    @CitrusTest
    public void shouldExpectErrorStatus(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        runner.then(grpc().client(greeter).receive()
                .response("NOT_FOUND")
                .statusDescription("@contains('unknown')@"));
    }

    @Test
    @CitrusTest
    public void shouldFailOnUnexpectedErrorStatus(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        assertThatThrownBy(() -> runner.when(grpc().client(greeter).receive().response()))
                .hasMessageContaining("NOT_FOUND")
                .hasMessageContaining("unknown user");
    }

    @Test
    @CitrusTest
    public void shouldReceiveStreamMessagesInOrder(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(greeter).send()
                .call("market.Prices/Subscribe")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        for (int i = 0; i < 3; i++) {
            int amount = i;
            runner.then(grpc().client(greeter).receive()
                    .streamMessage()
                    .message()
                    .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"%d\" } }"
                            .formatted(amount)));
        }

        runner.then(grpc().client(greeter).receive().streamCompleted());
    }

    @Test
    @CitrusTest
    public void shouldCancelEndlessSubscription(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(greeter).send()
                .call("market.Prices/Watch")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        runner.then(grpc().client(greeter).receive()
                .streamMessage()
                .message()
                .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"0\" } }"));

        runner.then(grpc().client(greeter).cancel());
    }

    @Test
    @CitrusTest
    public void shouldApplyPerCallDeadline(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .deadline(java.time.Duration.ofMillis(100))
                .message()
                .body("{ \"id\": \"slow\" }"));

        runner.then(grpc().client(greeter).receive()
                .response("DEADLINE_EXCEEDED"));
    }

    @Test
    @CitrusTest
    public void shouldRaiseReceiveTimeoutInsteadOfDeadline(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"slow\" }"));

        assertThatThrownBy(() -> runner.when(grpc().client(greeter).receive().response().timeout(100L)))
                .hasMessageContaining("Action timeout after 100 milliseconds")
                .hasCauseInstanceOf(MessageTimeoutException.class);
    }
}
