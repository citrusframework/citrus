/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.http.server.vertx;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the Http exchange of a Vert.x request. The request body is received on the event loop, the
 * endpoint adapter is invoked on a worker thread and the response is written back on the event loop.
 */
class VertxExchangeHandler {

    private static final Logger logger = LoggerFactory.getLogger(VertxExchangeHandler.class);

    private final WorkerExecutor workerExecutor;
    private final VertxRequestMapper requestMapper;
    private final VertxResponseWriter responseWriter;
    private final EndpointAdapter endpointAdapter;

    VertxExchangeHandler(WorkerExecutor workerExecutor, VertxRequestMapper requestMapper,
                         VertxResponseWriter responseWriter, EndpointAdapter endpointAdapter) {
        this.workerExecutor = workerExecutor;
        this.requestMapper = requestMapper;
        this.responseWriter = responseWriter;
        this.endpointAdapter = endpointAdapter;
    }

    /**
     * Handles the given request and writes the response.
     *
     * @param context the routing context of the request.
     */
    void handle(RoutingContext context) {
        HttpServerRequest request = context.request();

        request.body()
                .onSuccess(buffer -> {
                    VertxRequestSnapshot snapshot = createSnapshot(request, buffer);

                    workerExecutor.<VertxResponseSnapshot>executeBlocking(() -> handleOnWorker(snapshot), false)
                            .onSuccess(response -> writeResponse(context, request, response))
                            .onFailure(t -> handleFailure(context, t));
                })
                .onFailure(t -> handleFailure(context, t));
    }

    /**
     * Maps the request, invokes the endpoint adapter and creates the response. Runs on a worker thread.
     */
    private VertxResponseSnapshot handleOnWorker(VertxRequestSnapshot snapshot) {
        HttpMessage request = requestMapper.map(snapshot);
        Message reply = endpointAdapter.handleMessage(request);

        return responseWriter.write(reply, snapshot);
    }

    /**
     * Writes the response on the event loop.
     */
    private void writeResponse(RoutingContext context, HttpServerRequest request, VertxResponseSnapshot response) {
        HttpServerResponse httpResponse = context.response();

        if (httpResponse.closed() || httpResponse.ended()) {
            return;
        }

        httpResponse.setStatusCode(response.status());

        for (Map.Entry<String, List<String>> header : response.headers().entrySet()) {
            for (String value : header.getValue()) {
                httpResponse.putHeader(header.getKey(), value);
            }
        }

        if (HttpMethod.HEAD.equals(request.method())) {
            httpResponse.end();
        } else {
            httpResponse.end(Buffer.buffer(response.body()));
        }
    }

    /**
     * Answers with an internal server error when the request could not be handled.
     */
    private void handleFailure(RoutingContext context, Throwable t) {
        logger.error("Error while handling Http request: {}", t.getMessage(), t);

        HttpServerResponse httpResponse = context.response();

        if (httpResponse.closed() || httpResponse.ended()) {
            return;
        }

        httpResponse.setStatusCode(500).end();
    }

    /**
     * Creates the request snapshot of the given request.
     */
    private VertxRequestSnapshot createSnapshot(HttpServerRequest request, Buffer buffer) {
        return new VertxRequestSnapshot(
                request.method().name(),
                request.uri(),
                request.path(),
                request.query(),
                getVersionString(request.version()),
                request.headers(),
                createCookies(request.cookies()),
                buffer != null ? buffer.getBytes() : new byte[0]);
    }

    /**
     * Gets the protocol version of the request.
     */
    private String getVersionString(HttpVersion version) {
        if (version == null) {
            return "HTTP/1.1";
        }

        return switch (version) {
            case HTTP_1_0 -> "HTTP/1.0";
            case HTTP_2 -> "HTTP/2";
            default -> "HTTP/1.1";
        };
    }

    /**
     * Converts the cookies of the request to servlet cookies.
     */
    private List<jakarta.servlet.http.Cookie> createCookies(Set<io.vertx.core.http.Cookie> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return List.of();
        }

        return cookies.stream()
                .map(cookie -> {
                    jakarta.servlet.http.Cookie servletCookie = new jakarta.servlet.http.Cookie(cookie.getName(), cookie.getValue());
                    servletCookie.setPath(cookie.getPath());
                    servletCookie.setDomain(cookie.getDomain());
                    servletCookie.setSecure(cookie.isSecure());
                    servletCookie.setHttpOnly(cookie.isHttpOnly());
                    return servletCookie;
                })
                .toList();
    }
}
