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
package org.citrusframework.graphql.message;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import graphql.language.Document;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.graphql.document.GraphQlDocuments;
import org.citrusframework.graphql.document.GraphQlOperation;
import org.citrusframework.graphql.validation.GraphQlEndpointSettings;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.MediaTypes;
import org.springframework.graphql.support.DefaultGraphQlRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Builds the HTTP request of a GraphQL client send action at send time: resolves Citrus expressions in
 * document, operation name and variables, runs the strict document checks (parse, operation selection,
 * GET only for queries, schema) and writes a POST body or a form-encoded GET query string. The GraphQL
 * body is written after the generic message building, so resolved values are never resolved twice.
 */
public class GraphQlRequestMessageBuilder extends HttpMessageBuilder {

    public static final String ACCEPT = MediaTypes.APPLICATION_GRAPHQL_RESPONSE + ", " + MediaType.APPLICATION_JSON_VALUE;

    private String query;
    private String queryResourcePath;
    private Resource queryResource;
    private String operationName;
    private final Map<String, Object> variables = new LinkedHashMap<>();
    private String variablesJson;
    private HttpMethod method;
    private Boolean strict;

    private Endpoint endpoint;
    private String endpointUri;

    public GraphQlRequestMessageBuilder(HttpMessage message) {
        super(message);
    }

    @Override
    public Message build(TestContext context, String messageType) {
        HttpMessage message = (HttpMessage) super.build(context, messageType);
        GraphQlEndpointSettings settings = GraphQlEndpointSettings.resolve(endpoint, endpointUri, context).withStrict(strict);

        String document = resolveDocument(context);
        String resolvedOperationName = operationName == null ? null : context.replaceDynamicContentInString(operationName);
        Map<String, Object> resolvedVariables = GraphQlVariables.resolve(variables, variablesJson, context);
        HttpMethod httpMethod = method != null ? method : HttpMethod.valueOf(settings.requestMethod().name());
        boolean get = HttpMethod.GET.equals(httpMethod);

        if (settings.strict()) {
            Document parsed = GraphQlDocuments.parse(document);
            GraphQlOperation operation = GraphQlDocuments.selectOperation(parsed, resolvedOperationName);
            if (get) {
                GraphQlDocuments.checkGetAllowed(operation);
            }
            if (settings.schemaLoader() != null) {
                GraphQlDocuments.validate(settings.schemaLoader().getSchema(), parsed);
            }
            if (operation.name() != null) {
                message.setHeader(GraphQlMessageHeaders.OPERATION_NAME, operation.name());
            }
            message.setHeader(GraphQlMessageHeaders.OPERATION_TYPE, operation.typeName());
        } else if (resolvedOperationName != null) {
            message.setHeader(GraphQlMessageHeaders.OPERATION_NAME, resolvedOperationName);
        }

        GraphQlRequest request = new DefaultGraphQlRequest(document, resolvedOperationName, resolvedVariables, Map.of());
        String path = getMessage().getPath() != null ? message.getPath() : settings.path();

        message.method(httpMethod);
        if (message.getAccept() == null) {
            message.accept(ACCEPT);
        }
        if (get) {
            message.setPayload("");
            target(message, path, "?" + GraphQlMessages.toQueryString(request), settings);
        } else {
            message.setPayload(GraphQlMessages.toRequestBody(request));
            message.contentType(MediaType.APPLICATION_JSON_VALUE);
            target(message, path, "", settings);
        }

        return message;
    }

    /**
     * Points the request at the GraphQL path. Without a path the request URL is used as is; a GET
     * query string then goes into the endpoint URI header, as a request path would add a slash.
     */
    private static void target(HttpMessage message, String path, String queryString, GraphQlEndpointSettings settings) {
        if (path != null && !path.isEmpty()) {
            message.path(path + queryString);
        } else if (!queryString.isEmpty() && settings.requestUrl() != null) {
            message.setHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, settings.requestUrl() + queryString);
        }
    }

    private String resolveDocument(TestContext context) {
        if (query != null) {
            return context.replaceDynamicContentInString(query);
        }

        Resource resource = queryResource != null
                ? queryResource
                : queryResourcePath != null ? Resources.create(context.replaceDynamicContentInString(queryResourcePath)) : null;
        if (resource == null) {
            throw new CitrusRuntimeException("Missing GraphQL query - set query(...) or queryResource(...)");
        }

        try {
            return context.replaceDynamicContentInString(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read GraphQL query resource '%s'".formatted(resource), e);
        }
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setQueryResource(String resourcePath) {
        this.queryResourcePath = resourcePath;
    }

    public void setQueryResource(Resource resource) {
        this.queryResource = resource;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void addVariable(String name, Object value) {
        this.variables.put(name, value);
    }

    public void setVariables(String json) {
        this.variablesJson = json;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    /**
     * Sets the endpoint whose GraphQL settings apply, given as instance or as name/URI.
     */
    public void setEndpoint(Endpoint endpoint, String endpointUri) {
        this.endpoint = endpoint;
        this.endpointUri = endpointUri;
    }
}
