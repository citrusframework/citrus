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

package org.citrusframework.grpc.tls;

import java.io.IOException;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

public class GrpcTlsSupportTest {

    private static final String CA = "classpath:certs/ca.pem";
    private static final String SERVER_CHAIN = "classpath:certs/server1.pem";
    private static final String SERVER_KEY = "classpath:certs/server1.key";

    private int port;
    private Server server;

    @BeforeClass
    public void startServer() throws IOException {
        port = findAvailableTcpPort();
        server = Grpc.newServerBuilderForPort(port, GrpcTlsSupport.serverCredentials(SERVER_CHAIN, SERVER_KEY))
                .addService(new GreeterGrpc.GreeterImplBase() {
                    @Override
                    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
                        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();
    }

    @AfterClass
    public void stopServer() {
        server.shutdownNow();
    }

    @Test
    public void shouldRunTlsCall() {
        ManagedChannel channel = Grpc.newChannelBuilderForAddress("localhost", port, GrpcTlsSupport.clientCredentials(CA))
                .overrideAuthority("foo.test.google.fr")
                .build();

        try {
            HelloReply response = GreeterGrpc.newBlockingStub(channel)
                    .sayHello(HelloRequest.newBuilder().setName("Ada").build());

            assertThat(response.getMessage()).isEqualTo("Hello Ada");
        } finally {
            channel.shutdownNow();
        }
    }

    @Test
    public void shouldFailForPlaintextClientOnTlsServer() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();

        try {
            GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);

            assertThatThrownBy(() -> stub.sayHello(HelloRequest.newBuilder().setName("Ada").build()))
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("UNAVAILABLE");
        } finally {
            channel.shutdownNow();
        }
    }

    @Test
    public void shouldFailForMissingCertificate() {
        assertThatThrownBy(() -> GrpcTlsSupport.clientCredentials("classpath:certs/missing.pem"))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("missing.pem");
    }
}
