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

package org.citrusframework.http.vertx;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.citrusframework.http.message.HttpMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpTlsIT {
    public static HttpClient client(SSLContext context, String protocol) {
        var builder = HttpClient.newBuilder().sslContext(context).version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3));
        if (protocol != null) builder.sslParameters(new SSLParameters(null, new String[] {protocol}));
        return builder.build();
    }
    static HttpResponse<String> send(VertxHttpServer server, HttpClient client) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("https://localhost:" + server.getActualPort() + "/tls"))
                .timeout(Duration.ofSeconds(4)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test public void negotiatesTls12And13WithTrustedHostname() throws Exception {
        try (var certificates = new VertxTlsTestSupport()) {
            var server = new VertxHttpServer(); server.setPort(0);
            server.setTls(new VertxHttpTls(certificates.server, VertxTlsTestSupport.PASSWORD));
            server.setEndpointAdapter(VertxPrototypeAdapters.fixedReply(new HttpMessage("secure")));
            server.initialize(); server.start();
            try {
                for (String protocol : new String[] {"TLSv1.2", "TLSv1.3"}) {
                    var response = send(server, client(certificates.context(certificates.server, null), protocol));
                    assertEquals(response.statusCode(), 200); assertEquals(response.body(), "secure");
                    assertEquals(response.sslSession().orElseThrow().getProtocol(), protocol);
                }
            } finally { server.stop(); server.destroy(); }
        }
    }
    @Test public void rejectsUntrustedCertificatesAndWrongHostnames() throws Exception {
        try (var certificates = new VertxTlsTestSupport()) {
            var server = new VertxHttpServer(); server.setPort(0);
            server.setTls(new VertxHttpTls(certificates.server, VertxTlsTestSupport.PASSWORD));
            var calls = new java.util.concurrent.atomic.AtomicInteger();
            server.setEndpointAdapter(VertxPrototypeAdapters.delegate(request -> { calls.incrementAndGet(); return new HttpMessage("secure"); }));
            server.initialize(); server.start();
            try {
                var untrusted = client(certificates.context(certificates.stranger, null), null);
                expectThrows(javax.net.ssl.SSLHandshakeException.class, () -> send(server, untrusted));
                assertEquals(calls.get(), 0);
                server.stop();
                server.setTls(new VertxHttpTls(certificates.stranger, VertxTlsTestSupport.PASSWORD));
                server.start();
                var wrongHostname = client(certificates.context(certificates.stranger, null), null);
                expectThrows(javax.net.ssl.SSLHandshakeException.class, () -> send(server, wrongHostname));
                assertEquals(calls.get(), 0);
            } finally { server.stop(); server.destroy(); }
        }
    }

    @Test public void requiresTrustedClientIdentityWhenConfigured() throws Exception {
        try (var certificates = new VertxTlsTestSupport()) {
            var server = new VertxHttpServer(); server.setPort(0);
            server.setTls(new VertxHttpTls(certificates.server, VertxTlsTestSupport.PASSWORD)
                    .withClientAuthentication(certificates.client, VertxTlsTestSupport.PASSWORD));
            var calls = new java.util.concurrent.atomic.AtomicInteger();
            server.setEndpointAdapter(VertxPrototypeAdapters.delegate(request -> { calls.incrementAndGet(); return new HttpMessage("mutual"); }));
            server.initialize(); server.start();
            try {
                var anonymous = client(certificates.context(certificates.server, null), "TLSv1.2");
                expectThrows(java.io.IOException.class, () -> send(server, anonymous));
                var stranger = client(certificates.context(certificates.server, certificates.stranger), "TLSv1.2");
                expectThrows(java.io.IOException.class, () -> send(server, stranger));
                assertEquals(calls.get(), 0);
                for (String protocol : new String[] {"TLSv1.2", "TLSv1.3"}) {
                    var trusted = send(server, client(certificates.context(certificates.server, certificates.client), protocol));
                    assertEquals(trusted.statusCode(), 200); assertEquals(trusted.body(), "mutual");
                    assertEquals(trusted.sslSession().orElseThrow().getProtocol(), protocol);
                }
                assertEquals(calls.get(), 2);
            } finally { server.stop(); server.destroy(); }
        }
    }

    @Test public void appliesProtocolChangesAfterRestartAndRecoversFromBadStores() throws Exception {
        try (var certificates = new VertxTlsTestSupport()) {
            var server = new VertxHttpServer(); server.setPort(0);
            server.setEndpointAdapter(VertxPrototypeAdapters.fixedReply(new HttpMessage("secure")));
            server.setTls(new VertxHttpTls(certificates.server, "wrong-password"));
            server.initialize();
            try {
                expectThrows(RuntimeException.class, server::start);
                assertFalse(server.isRunning()); assertEquals(server.getActualPort(), -1);
                var configuration = new VertxHttpTls(certificates.server, VertxTlsTestSupport.PASSWORD)
                        .withProtocols(java.util.Set.of("TLSv1.2"));
                server.setTls(configuration); server.start();
                server.setTls(configuration.withProtocols(java.util.Set.of("TLSv1.3")));
                assertEquals(send(server, client(certificates.context(certificates.server, null), "TLSv1.2")).statusCode(), 200);
                var tls13 = client(certificates.context(certificates.server, null), "TLSv1.3");
                expectThrows(javax.net.ssl.SSLHandshakeException.class, () -> send(server, tls13));
                server.stop(); server.start();
                assertEquals(send(server, client(certificates.context(certificates.server, null), "TLSv1.3")).statusCode(), 200);
                var tls12 = client(certificates.context(certificates.server, null), "TLSv1.2");
                expectThrows(javax.net.ssl.SSLHandshakeException.class, () -> send(server, tls12));
                server.stop(); server.setTls(null); server.start();
                assertEquals(VertxHttpGzipIT.send(server.getActualPort(), new byte[0], "text/plain", null, null).statusCode(), 200);
            } finally { server.stop(); server.destroy(); }
        }
    }

}
