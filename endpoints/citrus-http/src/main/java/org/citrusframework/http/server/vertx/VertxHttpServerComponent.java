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

package org.citrusframework.http.server.vertx;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.TypeConversionUtils;

/**
 * Component creates a Vert.x based Http server endpoint from endpoint uri resource and parameters.
 * <p>
 * The endpoint uri uses the type {@code http.vertx-server} and supports the parameters
 * {@code port}, {@code contextPath}, {@code autoStart}, {@code timeout} and {@code name}.
 *
 * @since 5.2
 */
public class VertxHttpServerComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public VertxHttpServerComponent() {
        this("http.vertx-server");
    }

    /**
     * Constructor using the name for this component.
     * @param name
     */
    public VertxHttpServerComponent(String name) {
        super(name);
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        VertxHttpServer server = new VertxHttpServer();

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            applyParameter(server, parameter.getKey(), parameter.getValue(), context);
        }

        return server;
    }

    private void applyParameter(VertxHttpServer server, String name, String value, TestContext context) {
        if (value == null) {
            throw new CitrusRuntimeException("Unable to apply parameter '%s' without a value to the Vert.x Http server".formatted(name));
        }

        switch (name) {
            case "port" -> server.setPort(TypeConversionUtils.convertStringToType(value, Integer.class, context));
            case "contextPath" -> server.setContextPath(value);
            case "autoStart" -> server.setAutoStart(TypeConversionUtils.convertStringToType(value, Boolean.class, context));
            case "timeout" -> server.setDefaultTimeout(TypeConversionUtils.convertStringToType(value, Long.class, context));
            case "name" -> server.setName(value);
            default -> throw new CitrusRuntimeException("Unsupported parameter '%s' for the Vert.x Http server".formatted(name));
        }
    }
}
