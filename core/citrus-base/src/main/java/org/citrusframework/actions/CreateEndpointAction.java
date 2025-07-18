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

package org.citrusframework.actions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action creates a new endpoint as part of the test.
 * Upcoming test actions may use this endpoint to send and receive messages.
 */
public class CreateEndpointAction extends AbstractTestAction {

    /** The endpoint uri that defines the type and properties */
    private final String endpointUri;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateEndpointAction.class);

    /**
     * Default constructor.
     */
    private CreateEndpointAction(Builder builder) {
        super("create-endpoint", builder);

        this.endpointUri = builder.endpointUri;
    }

    @Override
    public void doExecute(TestContext context) {
        logger.info("Creating endpoint '{}'", endpointUri);
        context.getEndpointFactory().create(endpointUri, context);
    }

    public String getEndpointUri() {
        return endpointUri;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<CreateEndpointAction, Builder> {

        private String endpointUri;
        private String type;
        private final Map<String, String> properties = new LinkedHashMap<>();

        public static Builder createEndpoint(String type, Map<String, String> properties) {
            Builder builder = new Builder();
            builder.type(type);
            builder.properties(properties);
            return builder;
        }

        public static Builder createEndpoint(String endpointUri) {
            return new Builder().uri(endpointUri);
        }

        public static Builder createEndpoint() {
            return new Builder();
        }

        public Builder uri(String endpointUri) {
            this.endpointUri = endpointUri;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder endpointName(String name) {
            return property(EndpointComponent.ENDPOINT_NAME, name);
        }

        public Builder property(String name, String value) {
            this.properties.put(name, value);
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        @Override
        public CreateEndpointAction build() {
            if (endpointUri == null && type == null) {
                throw new CitrusRuntimeException("Failed to build endpoint specification - " +
                        "please specify an endpoint URI or a type");
            }

            if (endpointUri == null) {
                if (properties.isEmpty()) {
                    endpointUri = type;
                } else {
                    endpointUri = "%s?%s".formatted(type,
                            properties.entrySet()
                                    .stream()
                                    .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
                                    .collect(Collectors.joining("&")));
                }
            }

            return new CreateEndpointAction(this);
        }
    }
}
