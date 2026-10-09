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
package org.citrusframework.graphql.server;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.message.GraphQlMessages;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.MediaTypes;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

/**
 * Decorates the endpoint adapter of a {@link GraphQlServer}. For requests on the GraphQL path it
 * presents GET requests as the equivalent JSON request body, so tests validate POST and GET requests
 * the same way (method and raw query string stay available as headers), and it negotiates the reply
 * content type from the request's {@code Accept} header when the reply does not set one.
 */
public class GraphQlEndpointAdapter implements EndpointAdapter {

    private final EndpointAdapter delegate;
    private final String path;

    public GraphQlEndpointAdapter(EndpointAdapter delegate, String path) {
        this.delegate = delegate;
        this.path = path;
    }

    @Override
    public Message handleMessage(Message request) {
        if (!isGraphQlPath(request)) {
            return delegate.handleMessage(request);
        }

        if (GraphQlMessages.isGet(request)) {
            presentAsJsonBody(request);
        }

        Message reply = delegate.handleMessage(request);
        if (reply != null && reply.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE) == null) {
            reply.setHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE, negotiateContentType(request));
        }

        return reply;
    }

    public EndpointAdapter getDelegate() {
        return delegate;
    }

    @Override
    public Endpoint getEndpoint() {
        return delegate.getEndpoint();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return delegate.getEndpointConfiguration();
    }

    private boolean isGraphQlPath(Message request) {
        Object requestPath = request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI);
        if (requestPath == null) {
            return false;
        }

        String requestUri = normalize(requestPath.toString());
        Object contextPath = request.getHeader(HttpMessageHeaders.HTTP_CONTEXT_PATH);
        String context = contextPath == null ? "" : normalize(contextPath.toString());
        if (!"/".equals(context) && requestUri.startsWith(context + "/")) {
            requestUri = requestUri.substring(context.length());
        }

        return requestUri.equals(normalize(path));
    }

    /**
     * A GET request that is not a valid GraphQL request is left as it is; the GraphQL validation of
     * the receive action reports it.
     */
    private static void presentAsJsonBody(Message request) {
        try {
            GraphQlRequest graphQlRequest = GraphQlMessages.toRequest(request);
            request.setPayload(GraphQlMessages.toRequestBody(graphQlRequest));
        } catch (ValidationException e) {
            // keep the request unchanged
        }
    }

    private static String negotiateContentType(Message request) {
        String accept = GraphQlMessages.header(request, HttpMessageHeaders.HTTP_ACCEPT);
        if (accept != null && acceptsGraphQlResponse(accept)) {
            return MediaTypes.APPLICATION_GRAPHQL_RESPONSE.toString();
        }

        return MediaType.APPLICATION_JSON_VALUE;
    }

    private static boolean acceptsGraphQlResponse(String accept) {
        try {
            return MediaType.parseMediaTypes(accept).stream()
                    .anyMatch(mediaType -> mediaType.getQualityValue() > 0
                            && mediaType.equalsTypeAndSubtype(MediaTypes.APPLICATION_GRAPHQL_RESPONSE));
        } catch (InvalidMediaTypeException e) {
            return false;
        }
    }

    private static String normalize(String urlPath) {
        String normalized = urlPath;
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized.isEmpty() ? "/" : normalized;
    }
}
