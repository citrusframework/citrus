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

package org.citrusframework.http.server.contract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal wire level HTTP/1.1 client used by the HTTP server contract test suite. The client writes
 * a plain request to a socket and parses the raw response. This way the tests are able to assert on
 * details that a higher level client would hide, such as redirect responses, the content length
 * header of a HEAD response or the raw TRACE echo.
 */
public final class ContractClient {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 15000;

    private ContractClient() {
    }

    /**
     * Sends a request without headers and body.
     *
     * @param port the target port.
     * @param method the HTTP method.
     * @param target the request target.
     * @return the parsed response.
     */
    public static Response send(int port, String method, String target) {
        return send(port, method, target, Map.of(), new byte[0]);
    }

    /**
     * Sends a request with the given body.
     *
     * @param port the target port.
     * @param method the HTTP method.
     * @param target the request target.
     * @param body the request body.
     * @return the parsed response.
     */
    public static Response send(int port, String method, String target, byte[] body) {
        return send(port, method, target, Map.of(), body);
    }

    /**
     * Sends a request with the given headers and body.
     *
     * @param port the target port.
     * @param method the HTTP method.
     * @param target the request target.
     * @param headers the additional request headers.
     * @param body the request body.
     * @return the parsed response.
     */
    public static Response send(int port, String method, String target, Map<String, String> headers, byte[] body) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), CONNECT_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(buildRequest(port, method, target, headers, body));
            outputStream.flush();

            return readResponse(socket.getInputStream(), method);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to send %s request to port %d".formatted(method, port), e);
        }
    }

    private static byte[] buildRequest(int port, String method, String target, Map<String, String> headers, byte[] body) {
        StringBuilder request = new StringBuilder();
        request.append(method).append(' ').append(target).append(" HTTP/1.1\r\n");
        request.append("Host: localhost:").append(port).append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        request.append("Content-Length: ").append(body.length).append("\r\n");
        request.append("Connection: close\r\n");
        request.append("\r\n");

        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        requestBytes.writeBytes(request.toString().getBytes(StandardCharsets.ISO_8859_1));
        requestBytes.writeBytes(body);
        return requestBytes.toByteArray();
    }

    private static Response readResponse(InputStream inputStream, String method) throws IOException {
        String statusLine = readLine(inputStream);
        if (statusLine == null || statusLine.isEmpty()) {
            throw new IOException("Received an empty response");
        }

        String[] statusParts = statusLine.split(" ", 3);
        int statusCode = Integer.parseInt(statusParts[1]);
        String reasonPhrase = statusParts.length > 2 ? statusParts[2] : "";

        Map<String, List<String>> headers = new LinkedHashMap<>();
        String headerLine;
        while ((headerLine = readLine(inputStream)) != null && !headerLine.isEmpty()) {
            int separator = headerLine.indexOf(':');
            if (separator < 0) {
                continue;
            }

            String name = headerLine.substring(0, separator).trim().toLowerCase(Locale.ROOT);
            String value = headerLine.substring(separator + 1).trim();
            headers.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
        }

        byte[] body = readBody(inputStream, headers, statusCode, method);
        return new Response(statusCode, reasonPhrase, headers, body);
    }

    private static byte[] readBody(InputStream inputStream, Map<String, List<String>> headers, int statusCode, String method) throws IOException {
        if ("HEAD".equalsIgnoreCase(method) || statusCode == 204 || statusCode == 304) {
            return new byte[0];
        }

        List<String> transferEncoding = headers.get("transfer-encoding");
        if (transferEncoding != null && transferEncoding.stream().anyMatch(value -> value.toLowerCase(Locale.ROOT).contains("chunked"))) {
            return readChunkedBody(inputStream);
        }

        List<String> contentLength = headers.get("content-length");
        if (contentLength != null) {
            return inputStream.readNBytes(Integer.parseInt(contentLength.get(0).trim()));
        }

        return inputStream.readAllBytes();
    }

    private static byte[] readChunkedBody(InputStream inputStream) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();

        while (true) {
            String sizeLine = readLine(inputStream);
            if (sizeLine == null) {
                break;
            }

            int extension = sizeLine.indexOf(';');
            if (extension >= 0) {
                sizeLine = sizeLine.substring(0, extension);
            }

            sizeLine = sizeLine.trim();
            if (sizeLine.isEmpty()) {
                continue;
            }

            int chunkSize = Integer.parseInt(sizeLine, 16);
            if (chunkSize == 0) {
                String trailer;
                while ((trailer = readLine(inputStream)) != null && !trailer.isEmpty()) {
                    // consume optional trailer headers
                }
                break;
            }

            body.writeBytes(inputStream.readNBytes(chunkSize));
            readLine(inputStream);
        }

        return body.toByteArray();
    }

    private static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();

        int character;
        while ((character = inputStream.read()) != -1) {
            if (character == '\n') {
                break;
            }

            if (character != '\r') {
                line.write(character);
            }
        }

        if (character == -1 && line.size() == 0) {
            return null;
        }

        return line.toString(StandardCharsets.ISO_8859_1);
    }

    /**
     * The raw response of the contract client.
     */
    public static final class Response {

        private final int statusCode;
        private final String reasonPhrase;
        private final Map<String, List<String>> headers;
        private final byte[] body;

        Response(int statusCode, String reasonPhrase, Map<String, List<String>> headers, byte[] body) {
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
            this.headers = headers;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public List<String> getHeaders(String name) {
            return headers.getOrDefault(name.toLowerCase(Locale.ROOT), List.of());
        }

        public String getHeader(String name) {
            List<String> values = getHeaders(name);
            return values.isEmpty() ? null : values.get(0);
        }

        public byte[] getBody() {
            return body;
        }

        public String getBodyAsString() {
            return new String(body, StandardCharsets.UTF_8);
        }
    }
}
