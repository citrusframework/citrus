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
import org.citrusframework.grpc.endpoint.GrpcEndpoints;
import org.citrusframework.grpc.integration.service.GreeterService;
import org.citrusframework.grpc.integration.service.PricesService;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A Citrus client calls a real grpc-java service implemented with generated stubs.
 */
@Test
public class GrpcClientIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private final int port = findAvailableTcpPort(18810);

    @BindToRegistry
    private final GrpcClient greeter = GrpcEndpoints.grpc().client()
            .target("localhost:" + port)
            .descriptors("classpath:descriptors/test-protos.binpb")
            .timeout(5000L)
            .build();

    private Server server;

    @BeforeClass(alwaysRun = true)
    public void startGrpcService() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new GreeterService())
                .addService(new PricesService())
                .intercept(new GreeterService.MetadataInterceptor())
                .build()
                .start();
    }

    @AfterClass(alwaysRun = true)
    public void stopGrpcService() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    @CitrusTest
    public void shouldCallUnaryMethodWithMetadataAndTrailers() {
        when(grpc().client(greeter).send()
                .call("helloworld.Greeter/SayHello")
                .metadata("x-request-id", "42")
                .message()
                .body("{ \"name\": \"Ada\" }"));

        then(grpc().client(greeter).receive()
                .response()
                .trailer("x-trailer", "trailer-value")
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));

        assertThat(GreeterService.LAST_REQUEST_METADATA.get().get(GreeterService.REQUEST_ID))
                .isEqualTo("42");
    }

    @CitrusTest
    public void shouldExpectErrorStatus() {
        when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        then(grpc().client(greeter).receive()
                .response("NOT_FOUND")
                .statusDescription("@contains('unknown')@"));
    }

    @CitrusTest
    public void shouldFailOnUnexpectedErrorStatus() {
        when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"unknown\" }"));

        assertThatThrownBy(() -> when(grpc().client(greeter).receive().response()))
                .hasMessageContaining("NOT_FOUND")
                .hasMessageContaining("unknown user");
    }

    @CitrusTest
    public void shouldReceiveStreamMessagesInOrder() {
        when(grpc().client(greeter).send()
                .call("market.Prices/Subscribe")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        for (int i = 0; i < 3; i++) {
            int amount = i;
            then(grpc().client(greeter).receive()
                    .streamMessage()
                    .message()
                    .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"%d\" } }"
                            .formatted(amount)));
        }

        then(grpc().client(greeter).receive().streamCompleted());
    }

    @CitrusTest
    public void shouldCancelEndlessSubscription() {
        when(grpc().client(greeter).send()
                .call("market.Prices/Watch")
                .message()
                .body("{ \"symbol\": \"ACME\" }"));

        then(grpc().client(greeter).receive()
                .streamMessage()
                .message()
                .body("{ \"symbol\": \"ACME\", \"amount\": { \"currency\": \"EUR\", \"amount\": \"0\" } }"));

        then(grpc().client(greeter).cancel());
    }

    @CitrusTest
    public void shouldApplyPerCallDeadline() {
        when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .deadline(java.time.Duration.ofMillis(100))
                .message()
                .body("{ \"id\": \"slow\" }"));

        then(grpc().client(greeter).receive()
                .response("DEADLINE_EXCEEDED"));
    }

    @CitrusTest
    public void shouldRaiseReceiveTimeoutInsteadOfDeadline() {
        when(grpc().client(greeter).send()
                .call("helloworld.Greeter/GetUser")
                .message()
                .body("{ \"id\": \"slow\" }"));

        assertThatThrownBy(() -> when(grpc().client(greeter).receive().response().timeout(100L)))
                .hasMessageContaining("Action timeout after 100 milliseconds")
                .hasCauseInstanceOf(MessageTimeoutException.class);
    }
}
