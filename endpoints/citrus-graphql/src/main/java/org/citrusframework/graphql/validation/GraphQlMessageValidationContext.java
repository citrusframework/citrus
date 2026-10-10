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
package org.citrusframework.graphql.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.api.graphql.GraphQlError;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Expectations on a GraphQL response (client side) or on an incoming GraphQL request (simulator side),
 * validated by {@link GraphQlMessageValidator}. Deliberately not a message validation context, so the
 * generic message validators never pick it up.
 */
public class GraphQlMessageValidationContext extends DefaultValidationContext {

    public enum Kind { REQUEST, RESPONSE }

    private final Kind kind;
    private final Boolean strict;
    private final Endpoint endpoint;
    private final String endpointUri;

    private final boolean errorsExpected;
    private final List<GraphQlError> expectedErrors;
    private final Map<String, Object> dataExpressions;
    private final String data;

    private final String operationName;
    private final String query;
    private final Map<String, Object> variables;
    private final String variablesJson;

    private GraphQlMessageValidationContext(Builder builder) {
        this.kind = builder.kind;
        this.strict = builder.strict;
        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
        this.errorsExpected = builder.errorsExpected;
        this.expectedErrors = List.copyOf(builder.expectedErrors);
        this.dataExpressions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.dataExpressions));
        this.data = builder.data;
        this.operationName = builder.operationName;
        this.query = builder.query;
        this.variables = Collections.unmodifiableMap(new LinkedHashMap<>(builder.variables));
        this.variablesJson = builder.variablesJson;
    }

    /**
     * Effective GraphQL settings: those of the action's endpoint, with the action's {@code strict}
     * override applied.
     */
    public GraphQlEndpointSettings getSettings(TestContext context) {
        return GraphQlEndpointSettings.resolve(endpoint, endpointUri, context).withStrict(strict);
    }

    @Override
    public boolean requiresValidator() {
        return true;
    }

    @Override
    public Optional<String> getCorrespondingValidationModule() {
        return Optional.of("org.citrusframework:citrus-graphql");
    }

    public Kind getKind() {
        return kind;
    }

    public Boolean getStrict() {
        return strict;
    }

    /**
     * Whether errors are expected: {@code expectErrors()} or at least one expected error.
     */
    public boolean isErrorsExpected() {
        return errorsExpected || !expectedErrors.isEmpty();
    }

    public List<GraphQlError> getExpectedErrors() {
        return expectedErrors;
    }

    public Map<String, Object> getDataExpressions() {
        return dataExpressions;
    }

    public String getData() {
        return data;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public static final class Builder implements ValidationContext.Builder<GraphQlMessageValidationContext, Builder> {

        private Kind kind = Kind.RESPONSE;
        private Boolean strict;
        private Endpoint endpoint;
        private String endpointUri;
        private boolean errorsExpected;
        private final List<GraphQlError> expectedErrors = new ArrayList<>();
        private final Map<String, Object> dataExpressions = new LinkedHashMap<>();
        private String data;
        private String operationName;
        private String query;
        private final Map<String, Object> variables = new LinkedHashMap<>();
        private String variablesJson;

        /**
         * Expectations on a GraphQL response.
         */
        public static Builder response() {
            return new Builder().kind(Kind.RESPONSE);
        }

        /**
         * Expectations on an incoming GraphQL request.
         */
        public static Builder request() {
            return new Builder().kind(Kind.REQUEST);
        }

        public Builder kind(Kind kind) {
            this.kind = kind;
            return this;
        }

        public Builder strict(Boolean strict) {
            this.strict = strict;
            return this;
        }

        public Builder endpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder endpointUri(String endpointUri) {
            this.endpointUri = endpointUri;
            return this;
        }

        public Builder expectErrors() {
            this.errorsExpected = true;
            return this;
        }

        public Builder expectError(GraphQlError error) {
            this.expectedErrors.add(error);
            return this;
        }

        /**
         * Expects a value in the response data; the JSONPath is relative to {@code $.data}.
         */
        public Builder data(String path, Object expected) {
            this.dataExpressions.put(path, expected);
            return this;
        }

        /**
         * Expects the response data object, compared as JSON.
         */
        public Builder data(String json) {
            this.data = json;
            return this;
        }

        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder variable(String name, Object expected) {
            this.variables.put(name, expected);
            return this;
        }

        public Builder variables(String json) {
            this.variablesJson = json;
            return this;
        }

        /**
         * Whether any expectation or the strict switch has been set.
         */
        public boolean hasExpectations() {
            return strict != null || errorsExpected || !expectedErrors.isEmpty() || !dataExpressions.isEmpty()
                    || data != null || operationName != null || query != null || !variables.isEmpty()
                    || variablesJson != null;
        }

        @Override
        public GraphQlMessageValidationContext build() {
            return new GraphQlMessageValidationContext(this);
        }
    }
}
