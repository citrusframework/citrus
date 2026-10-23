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

package org.citrusframework.nosql.yaml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.nosql.actions.NoSqlAction;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * YAML test action representing a NoSQL operation.
 *
 * <pre>{@code
 * - nosql:
 *     client: ordersDatabase
 *     operation: query
 *     target: orders
 *     filter: '{ "orderId": "${orderId}" }'
 *     validate:
 *       - path: $.documents[0].status
 *         value: COMPLETED
 *     extract:
 *       - path: $.documents[0].customerId
 *         variable: customerId
 * }</pre>
 */
public class NoSql implements TestActionBuilder<NoSqlAction>, ReferenceResolverAware {

    private String client;
    private String operation;
    private String target;
    private String command;
    private String filter;
    private String payload;
    private Integer maxResults;
    private Integer retryAttempts;
    private String retryInterval;
    private String description;
    private List<Validate> validate = new ArrayList<>();
    private List<Extract> extract = new ArrayList<>();

    private ReferenceResolver referenceResolver;

    @Override
    public NoSqlAction build() {
        if (filter != null && payload != null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL action must use either a filter or a payload, not both");
        }

        NoSqlAction.Builder builder = new NoSqlAction.Builder()
                .client(client)
                .target(target)
                .operation(operation, command);

        if (description != null) {
            builder.description(description);
        }

        String content = payload != null ? payload : filter;
        if (content != null) {
            builder.payload(content);
        }

        validate.forEach(entry -> builder.validate(entry.path, entry.value));
        extract.forEach(entry -> builder.extract(entry.path, entry.variable));

        if (maxResults != null) {
            builder.maxResults(maxResults);
        }

        if (retryAttempts != null) {
            builder.retry(retryAttempts, retryInterval);
        } else if (retryInterval != null && !retryInterval.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL retry interval requires retry attempts");
        }

        if (referenceResolver != null) {
            builder.withReferenceResolver(referenceResolver);
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public String getClient() {
        return client;
    }

    @SchemaProperty(required = true, description = "Name of the NoSQL database configuration.")
    public void setClient(String client) {
        this.client = client;
    }

    public String getOperation() {
        return operation;
    }

    @SchemaProperty(required = true, description = "Operation to execute: query, insert, update, delete or native.")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTarget() {
        return target;
    }

    @SchemaProperty(required = true, description = "Target collection, table or equivalent database object.")
    public void setTarget(String target) {
        this.target = target;
    }

    public String getCommand() {
        return command;
    }

    @SchemaProperty(description = "Native provider command, required for the native operation.")
    public void setCommand(String command) {
        this.command = command;
    }

    public String getFilter() {
        return filter;
    }

    @SchemaProperty(description = "Query filter or delete selection in provider JSON syntax.")
    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getPayload() {
        return payload;
    }

    @SchemaProperty(description = "Write payload or native command payload in provider JSON syntax.")
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    @SchemaProperty(description = "Maximum number of documents to read.")
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    @SchemaProperty(description = "Maximum read attempts. Writes are never retried.")
    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public String getRetryInterval() {
        return retryInterval;
    }

    @SchemaProperty(description = "Retry interval as ISO-8601 duration or milliseconds.")
    public void setRetryInterval(String retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getDescription() {
        return description;
    }

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String description) {
        this.description = description;
    }

    public List<Validate> getValidate() {
        return validate;
    }

    @SchemaProperty(description = "Result validation entries.")
    public void setValidate(List<Validate> validate) {
        this.validate = validate;
    }

    public List<Extract> getExtract() {
        return extract;
    }

    @SchemaProperty(description = "Result extraction entries.")
    public void setExtract(List<Extract> extract) {
        this.extract = extract;
    }

    public static class Validate {

        private String path;
        private String value;

        public String getPath() {
            return path;
        }

        @SchemaProperty(required = true, description = "JSON path evaluated against the canonical result model.")
        public void setPath(String path) {
            this.path = path;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "Expected value or validation matcher expression.")
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Extract {

        private String path;
        private String variable;

        public String getPath() {
            return path;
        }

        @SchemaProperty(required = true, description = "JSON path evaluated against the canonical result model.")
        public void setPath(String path) {
            this.path = path;
        }

        public String getVariable() {
            return variable;
        }

        @SchemaProperty(required = true, description = "Test variable that receives the extracted value.")
        public void setVariable(String variable) {
            this.variable = variable;
        }
    }
}
