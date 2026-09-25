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

package org.citrusframework.playwright.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Local HTTP server for browser integration tests, on {@code 127.0.0.1} with a random port.
 *
 * <p>Serves the static files under {@code /fixtures} from the test classpath, plus any routes a
 * test registers. Every request that reaches a route is recorded, so tests can assert what the
 * browser or an API client actually sent.</p>
 *
 * <p>{@link #startHttps()} serves the same over TLS with the self-signed, expired certificate in
 * {@code keystore/fixture-server.jks} ({@code CN=Citrus}), which every client rejects unless it
 * ignores HTTPS errors.</p>
 */
public final class FixtureServer implements AutoCloseable {

    private static final String KEYSTORE = "/keystore/fixture-server.jks";
    private static final char[] KEYSTORE_PASSWORD = "secret".toCharArray();

    private final HttpServer server;
    private final String scheme;
    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private final List<RecordedRequest> requests = new CopyOnWriteArrayList<>();

    private FixtureServer(HttpServer server, String scheme) {
        this.server = server;
        this.scheme = scheme;
    }

    public static FixtureServer start() throws IOException {
        return start(HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0), "http");
    }

    public static FixtureServer startHttps() throws IOException {
        HttpsServer server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext()));
        return start(server, "https");
    }

    private static FixtureServer start(HttpServer server, String scheme) {
        FixtureServer fixture = new FixtureServer(server, scheme);
        server.createContext("/", fixture::handle);
        server.start();
        return fixture;
    }

    private static SSLContext sslContext() throws IOException {
        try (InputStream stream = FixtureServer.class.getResourceAsStream(KEYSTORE)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(stream, KEYSTORE_PASSWORD);
            KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagers.init(keyStore, KEYSTORE_PASSWORD);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagers.getKeyManagers(), null, null);
            return context;
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to load the fixture server keystore " + KEYSTORE, e);
        }
    }

    public String url(String path) {
        return "%s://127.0.0.1:%d%s".formatted(scheme, server.getAddress().getPort(), path);
    }

    /**
     * Registers a dynamic route for an exact path; it takes precedence over a static file.
     *
     * @param path request path
     * @param route response producer
     * @return this server
     */
    public FixtureServer route(String path, Route route) {
        routes.put(path, route);
        return this;
    }

    /**
     * Requests that reached a registered route, oldest first.
     *
     * @return recorded requests
     */
    public List<RecordedRequest> requests() {
        return List.copyOf(requests);
    }

    /**
     * The most recent request to a path.
     *
     * @param path request path
     * @return last recorded request for the path
     */
    public Optional<RecordedRequest> lastRequest(String path) {
        for (int i = requests.size() - 1; i >= 0; i--) {
            if (requests.get(i).path().equals(path)) {
                return Optional.of(requests.get(i));
            }
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            Route route = routes.get(path);
            if (route != null) {
                RecordedRequest request = record(exchange);
                requests.add(request);
                route.respond(request, new Response(exchange));
                return;
            }
            serveStatic(exchange, path);
        } finally {
            exchange.close();
        }
    }

    private static RecordedRequest record(HttpExchange exchange) throws IOException {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        exchange.getRequestHeaders().forEach((name, values) -> headers.put(name, List.copyOf(values)));
        return new RecordedRequest(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                exchange.getRequestURI().getRawQuery(), headers,
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    private static void serveStatic(HttpExchange exchange, String path) throws IOException {
        try (InputStream stream = FixtureServer.class.getResourceAsStream("/fixtures" + path)) {
            if (stream == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            byte[] body = stream.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
        }
    }

    /**
     * Produces the response for a registered route.
     */
    @FunctionalInterface
    public interface Route {
        void respond(RecordedRequest request, Response response) throws IOException;
    }

    /**
     * A request as the server received it.
     *
     * @param method request method
     * @param path request path
     * @param query raw query string, or null
     * @param headers request headers, case-insensitive
     * @param body request body as UTF-8 text
     */
    public record RecordedRequest(String method, String path, String query, Map<String, List<String>> headers, String body) {

        /**
         * First value of a request header.
         *
         * @param name header name, any case
         * @return header value, or null when absent
         */
        public String header(String name) {
            List<String> values = headers.get(name);
            return values == null || values.isEmpty() ? null : values.get(0);
        }

        /**
         * Reports whether the {@code Cookie} header carries a cookie with this name.
         *
         * @param name cookie name
         * @return true when the cookie was sent
         */
        public boolean hasCookie(String name) {
            String cookies = header("Cookie");
            if (cookies == null) {
                return false;
            }
            for (String pair : cookies.split(";")) {
                if (pair.trim().toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT) + "=")) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Response writer for a route.
     */
    public static final class Response {

        private final HttpExchange exchange;

        private Response(HttpExchange exchange) {
            this.exchange = exchange;
        }

        public Response header(String name, String value) {
            exchange.getResponseHeaders().add(name, value);
            return this;
        }

        public void send(int status, String contentType, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            if (contentType != null) {
                exchange.getResponseHeaders().set("Content-Type", contentType);
            }
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                exchange.getResponseBody().write(bytes);
            }
        }
    }
}
