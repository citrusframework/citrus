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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Local HTTP proxy for browser and API integration tests, listening on {@code 127.0.0.1} with a
 * random port and forwarding traffic to a local {@link FixtureServer} port.
 *
 * <p>Supports both HTTP {@code CONNECT} tunneling and forward-proxy HTTP requests, with optional
 * {@code Basic} {@code Proxy-Authorization} enforcement ({@code 407 Proxy Authentication Required}).</p>
 */
public final class LocalHttpProxy implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final String expectedProxyAuth;
    private final int upstreamPort;
    private final AtomicInteger authenticatedCount = new AtomicInteger();

    private LocalHttpProxy(ServerSocket serverSocket, String expectedProxyAuth, int upstreamPort) {
        this.serverSocket = serverSocket;
        this.expectedProxyAuth = expectedProxyAuth;
        this.upstreamPort = upstreamPort;
    }

    public static LocalHttpProxy start(int upstreamPort) throws IOException {
        ServerSocket socket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        LocalHttpProxy proxy = new LocalHttpProxy(socket, null, upstreamPort);
        Thread.ofVirtual().start(proxy::acceptLoop);
        return proxy;
    }

    public static LocalHttpProxy start(String username, String password, int upstreamPort) throws IOException {
        ServerSocket socket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        LocalHttpProxy proxy = new LocalHttpProxy(socket, auth, upstreamPort);
        Thread.ofVirtual().start(proxy::acceptLoop);
        return proxy;
    }

    public String url() {
        return "http://127.0.0.1:" + serverSocket.getLocalPort();
    }

    public int authenticatedCount() {
        return authenticatedCount.get();
    }

    private void acceptLoop() {
        while (!serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClient(client));
            } catch (IOException ignored) {
                return;
            }
        }
    }

    private void handleClient(Socket client) {
        try (client) {
            client.setSoTimeout(5000);
            InputStream clientIn = client.getInputStream();
            OutputStream clientOut = client.getOutputStream();

            String head = readHeaderBlock(clientIn);
            if (head.isEmpty()) {
                return;
            }
            String[] lines = head.split("\r\n");
            String requestLine = lines[0];
            String proxyAuth = null;
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                int colon = line.indexOf(':');
                if (colon > 0 && "proxy-authorization".equals(line.substring(0, colon).trim().toLowerCase(Locale.ROOT))) {
                    proxyAuth = line.substring(colon + 1).trim();
                }
            }

            if (expectedProxyAuth != null && !expectedProxyAuth.equals(proxyAuth)) {
                byte[] challenge = ("HTTP/1.1 407 Proxy Authentication Required\r\n"
                        + "Proxy-Authenticate: Basic realm=\"proxy\"\r\n"
                        + "Content-Length: 0\r\n"
                        + "Connection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1);
                clientOut.write(challenge);
                clientOut.flush();
                return;
            }

            if (expectedProxyAuth != null) {
                authenticatedCount.incrementAndGet();
            }
            try (Socket upstream = new Socket(InetAddress.getLoopbackAddress(), upstreamPort)) {
                upstream.setSoTimeout(5000);
                InputStream upstreamIn = upstream.getInputStream();
                OutputStream upstreamOut = upstream.getOutputStream();

                if (requestLine.startsWith("CONNECT ")) {
                    clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
                    clientOut.flush();

                    Thread reverse = Thread.ofVirtual().start(() -> {
                        try {
                            upstreamIn.transferTo(clientOut);
                            clientOut.flush();
                            client.shutdownOutput();
                        } catch (IOException ignored) {
                        }
                    });
                    try {
                        clientIn.transferTo(upstreamOut);
                        upstreamOut.flush();
                        upstream.shutdownOutput();
                    } catch (IOException ignored) {
                    }
                    reverse.join(5000);
                } else {
                    String[] parts = requestLine.split(" ");
                    URI uri = URI.create(parts[1]);
                    String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
                    if (uri.getRawQuery() != null) {
                        path = path + "?" + uri.getRawQuery();
                    }
                    StringBuilder forwarded = new StringBuilder()
                            .append(parts[0]).append(' ').append(path).append(' ').append(parts[2]).append("\r\n");
                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i];
                        String lower = line.toLowerCase(Locale.ROOT);
                        if (!lower.startsWith("proxy-authorization:")
                                && !lower.startsWith("proxy-connection:")
                                && !lower.startsWith("connection:")) {
                            forwarded.append(line).append("\r\n");
                        }
                    }
                    forwarded.append("Connection: close\r\n\r\n");
                    upstreamOut.write(forwarded.toString().getBytes(StandardCharsets.ISO_8859_1));
                    upstreamOut.flush();
                    upstreamIn.transferTo(clientOut);
                    clientOut.flush();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException ignored) {
        }
    }

    private static String readHeaderBlock(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
        int matched = 0;
        while (buffer.size() < 16_384) {
            int b = in.read();
            if (b == -1) {
                break;
            }
            buffer.write(b);
            if ((matched == 0 && b == '\r')
                    || (matched == 1 && b == '\n')
                    || (matched == 2 && b == '\r')
                    || (matched == 3 && b == '\n')) {
                matched++;
                if (matched == 4) {
                    break;
                }
            } else {
                matched = b == '\r' ? 1 : 0;
            }
        }
        String raw = buffer.toString(StandardCharsets.ISO_8859_1);
        return raw.endsWith("\r\n\r\n") ? raw.substring(0, raw.length() - 4) : raw;
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
