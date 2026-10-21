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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.server.AbstractCommonHttpServerBuilder;

import io.vertx.core.Vertx;

/**
 * Builder for the Vert.x based Http server. In addition to all transport neutral options the
 * builder supports the Vert.x specific options such as the Vert.x instance to use and the size of
 * the worker pool running the endpoint adapter.
 *
 * @since 5.2
 */
@SchemaType(module = "citrus-http")
@XmlType(name = "", propOrder = {})
public class VertxHttpServerBuilder extends AbstractCommonHttpServerBuilder<VertxHttpServer, VertxHttpServerBuilder> {

    /**
     * Vert.x instance reference
     */
    private String vertx;

    /**
     * Interceptor references
     */
    private final List<String> interceptors = new ArrayList<>();

    public VertxHttpServerBuilder() {
        this(new VertxHttpServer());
    }

    protected VertxHttpServerBuilder(VertxHttpServer server) {
        super(server);
    }

    /**
     * Sets the Vert.x instance to use. A Vert.x instance supplied by the user is never closed by
     * the server.
     */
    public VertxHttpServerBuilder vertx(Vertx vertx) {
        endpoint.setVertx(vertx);
        return self;
    }

    @SchemaProperty(description = "The Vert.x instance used by the Http server. A supplied instance is never closed by the server.")
    @XmlAttribute(name = "vertx")
    public void setVertx(String vertx) {
        this.vertx = vertx;
    }

    /**
     * Sets the size of the worker pool running the endpoint adapter.
     */
    public VertxHttpServerBuilder workerPoolSize(int workerPoolSize) {
        endpoint.setWorkerPoolSize(workerPoolSize);
        return self;
    }

    @SchemaProperty(description = "The size of the worker pool running the endpoint adapter.")
    @XmlAttribute(name = "worker-pool-size")
    public void setWorkerPoolSize(int workerPoolSize) {
        workerPoolSize(workerPoolSize);
    }

    /**
     * Sets the interceptors running around the request handling. Only Citrus server interceptors
     * are supported by the Vert.x based server.
     */
    public VertxHttpServerBuilder interceptors(List<Object> interceptors) {
        List<Object> configuredInterceptors = new ArrayList<>(interceptors);

        endpoint.setConfiguredInterceptors(configuredInterceptors);
        endpoint.setServerInterceptors(configuredInterceptors.stream()
                .filter(HttpServerInterceptor.class::isInstance)
                .map(HttpServerInterceptor.class::cast)
                .toList());

        return self;
    }

    @XmlAttribute(name = "interceptors")
    public void setInterceptors(String interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors.split(",")));
    }

    @Override
    public VertxHttpServer build() {
        if (referenceResolver != null) {
            if (vertx != null && !vertx.isBlank()) {
                endpoint.setVertx(referenceResolver.resolve(vertx, Vertx.class));
            }

            if (!interceptors.isEmpty()) {
                interceptors(referenceResolver.resolve(interceptors.toArray(new String[0]), Object.class));
            }
        }

        return super.build();
    }
}
