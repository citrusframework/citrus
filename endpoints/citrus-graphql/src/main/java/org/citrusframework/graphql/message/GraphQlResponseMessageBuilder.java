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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.api.graphql.GraphQlError;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.validation.GraphQlEndpointSettings;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;

/**
 * Builds the reply of a GraphQL simulator send action: {@code data} and/or {@code errors}. When
 * neither is set the message body set on the action is sent. When strict, the reply must be a
 * GraphQL response (data and/or errors, every error with a message).
 */
public class GraphQlResponseMessageBuilder extends HttpMessageBuilder {

    private String data;
    private final List<GraphQlError> errors = new ArrayList<>();
    private Boolean strict;

    private Endpoint endpoint;
    private String endpointUri;

    public GraphQlResponseMessageBuilder(HttpMessage message) {
        super(message);
    }

    @Override
    public Message build(TestContext context, String messageType) {
        HttpMessage message = (HttpMessage) super.build(context, messageType);
        GraphQlEndpointSettings settings = GraphQlEndpointSettings.resolve(endpoint, endpointUri, context).withStrict(strict);

        if (data != null || !errors.isEmpty()) {
            Map<String, Object> body = new LinkedHashMap<>();
            if (data != null) {
                body.put("data", resolveData(context, settings.strict()));
            }
            if (!errors.isEmpty()) {
                body.put("errors", errors.stream().map(error -> toMap(error, context)).toList());
            }
            message.setPayload(GraphQlMessages.toJson(body));
        }

        if (settings.strict()) {
            String violation = GraphQlMessages.shapeViolation(message.getPayload(String.class));
            if (violation != null) {
                throw new ValidationException("Invalid GraphQL reply: %s".formatted(violation));
            }
        }

        return message;
    }

    private Object resolveData(TestContext context, boolean strict) {
        String json = context.replaceDynamicContentInString(data);
        if ("null".equals(json.trim())) {
            return null;
        }

        Object parsed = GraphQlMessages.parseOrNull(json);
        if (parsed == null) {
            if (strict) {
                throw new ValidationException("Invalid GraphQL reply: data is not valid JSON: %s".formatted(json));
            }
            return json;
        }

        return parsed;
    }

    private static Map<String, Object> toMap(GraphQlError error, TestContext context) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (error.getMessage() != null) {
            map.put("message", context.replaceDynamicContentInString(error.getMessage()));
        }
        if (!error.getPath().isEmpty()) {
            map.put("path", error.getPath());
        }
        if (error.getCode() != null) {
            map.put("extensions", Map.of("code", context.replaceDynamicContentInString(error.getCode())));
        }
        return map;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void addError(GraphQlError error) {
        this.errors.add(error);
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    /**
     * Sets the endpoint whose GraphQL settings apply, given as instance or as name.
     */
    public void setEndpoint(Endpoint endpoint, String endpointUri) {
        this.endpoint = endpoint;
        this.endpointUri = endpointUri;
    }
}
