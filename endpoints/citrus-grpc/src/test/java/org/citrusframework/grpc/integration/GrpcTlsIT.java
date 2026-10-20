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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import io.grpc.Grpc;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.endpoint.builder.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.citrusframework.grpc.tls.GrpcTlsSupport;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Verifies that the gRPC client and the gRPC simulator endpoint establish a TLS connection.
 *
 * The certificate is generated at runtime so that no private key has to be committed to the
 * repository.
 */
@Test
public class GrpcTlsIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final String KEYSTORE_PASSWORD = "citrus";

    private Path certificateFile;
    private Path privateKeyFile;
    private String certificateChain;
    private String privateKey;
    private String trustCertificate;

    private GrpcServer simulator;
    private GrpcClient client;
    private Server service;

    @BeforeClass(alwaysRun = true)
    public void setupTls() throws Exception {
        Path keystore = Files.createTempDirectory("citrus-grpc-tls").resolve("tls-test.p12");
        generateKeyPair(keystore);

        certificateFile = keystore.getParent().resolve("localhost.crt");
        privateKeyFile = keystore.getParent().resolve("localhost.key");
        writeCertificateChain(keystore, certificateFile);
        writePrivateKey(keystore, privateKeyFile);

        certificateChain = "file:" + certificateFile.toAbsolutePath();
        privateKey = "file:" + privateKeyFile.toAbsolutePath();
        trustCertificate = "file:" + certificateFile.toAbsolutePath();
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTls() throws IOException {
        if (certificateFile != null) {
            Files.deleteIfExists(certificateFile);
        }
        if (privateKeyFile != null) {
            Files.deleteIfExists(privateKeyFile);
        }
        if (certificateFile != null && certificateFile.getParent() != null) {
            Files.deleteIfExists(certificateFile.getParent().resolve("tls-test.p12"));
            Files.deleteIfExists(certificateFile.getParent());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void stopEndpoints() {
        if (client != null) {
            client.destroy();
            client = null;
        }
        if (simulator != null) {
            simulator.stop();
            simulator = null;
        }
        if (service != null) {
            service.shutdownNow();
            service = null;
        }
    }

    @CitrusTest
    public void shouldCallTlsService() throws IOException {
        int port = findAvailableTcpPort(18895);
        service = Grpc.newServerBuilderForPort(port, GrpcTlsSupport.serverCredentials(certificateChain, privateKey))
                .addService(new GreeterGrpc.GreeterImplBase() {
                    @Override
                    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
                        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        client = GrpcEndpoints.grpc().client()
                .target("localhost:" + port)
                .descriptors("classpath:descriptors/test-protos.binpb")
                .tls(true)
                .trustCertificate(trustCertificate)
                .timeout(5000L)
                .build();

        when(grpc().client(client).send()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Ada\" }"));

        then(grpc().client(client).receive()
                .response()
                .message()
                .body("{ \"message\": \"Hello Ada\" }"));
    }

    @CitrusTest
    public void shouldCallTlsSimulator() {
        int port = findAvailableTcpPort(18896);
        simulator = GrpcEndpoints.grpc().server()
                .port(port)
                .descriptors("classpath:descriptors/test-protos.binpb")
                .tls(true)
                .certificateChain(certificateChain)
                .privateKey(privateKey)
                .timeout(5000L)
                .autoStart(true)
                .initialize()
                .build();

        client = GrpcEndpoints.grpc().client()
                .target("localhost:" + port)
                .descriptors("classpath:descriptors/test-protos.binpb")
                .tls(true)
                .trustCertificate(trustCertificate)
                .timeout(5000L)
                .build();

        when(grpc().client(client).send()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Bob\" }"));

        then(grpc().server(simulator).receive()
                .call("helloworld.Greeter/SayHello")
                .message()
                .body("{ \"name\": \"Bob\", \"userId\": 0 }"));

        then(grpc().server(simulator).send()
                .response()
                .message()
                .body("{ \"message\": \"Hello Bob\" }"));

        then(grpc().client(client).receive()
                .response()
                .message()
                .body("{ \"message\": \"Hello Bob\" }"));
    }

    private static void generateKeyPair(Path keystore) throws IOException, InterruptedException {
        Path keytool = Path.of(System.getProperty("java.home"), "bin", "keytool");

        Process process = new ProcessBuilder(keytool.toString(),
                "-genkeypair",
                "-alias", "localhost",
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "1",
                "-dname", "CN=localhost, O=Citrus",
                "-ext", "SAN=dns:localhost,ip:127.0.0.1",
                "-storetype", "PKCS12",
                "-keystore", keystore.toAbsolutePath().toString(),
                "-storepass", KEYSTORE_PASSWORD,
                "-keypass", KEYSTORE_PASSWORD,
                "-noprompt")
                .redirectErrorStream(true)
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(60, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("Failed to generate the test certificate: " + output);
        }
    }

    private static void writeCertificateChain(Path keystore, Path target) throws Exception {
        KeyStore keyStore = loadKeyStore(keystore);
        StringBuilder pem = new StringBuilder();

        for (Certificate certificate : keyStore.getCertificateChain("localhost")) {
            pem.append("-----BEGIN CERTIFICATE-----\n")
                    .append(Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(certificate.getEncoded()))
                    .append("\n-----END CERTIFICATE-----\n");
        }

        Files.write(target, pem.toString().getBytes(StandardCharsets.UTF_8), CREATE, TRUNCATE_EXISTING);
    }

    private static void writePrivateKey(Path keystore, Path target) throws Exception {
        KeyStore keyStore = loadKeyStore(keystore);
        PrivateKey key = (PrivateKey) keyStore.getKey("localhost", KEYSTORE_PASSWORD.toCharArray());

        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(key.getEncoded())
                + "\n-----END PRIVATE KEY-----\n";

        Files.write(target, pem.getBytes(StandardCharsets.UTF_8), CREATE, TRUNCATE_EXISTING);
    }

    private static KeyStore loadKeyStore(Path keystore) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream stream = Files.newInputStream(keystore)) {
            keyStore.load(stream, KEYSTORE_PASSWORD.toCharArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        assertThat(keyStore.containsAlias("localhost")).isTrue();
        return keyStore;
    }
}
