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
package org.citrusframework.http.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;
import org.springframework.http.HttpStatus;

/**
 * Configures the Vert.x based Http server. The configuration options are the transport
 * neutral options of the Http server. Options that are specific to the Jetty based server
 * (servlet context, servlet handler, filters, connectors) are not supported.
 *
 * @since 5.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "http.vertx-server")
public @interface VertxHttpServerConfig {

    /**
     * Server port.
     * @return
     */
    int port() default 8080;

    /**
     * Secure server port. The secure port is used when a secure connection is configured.
     * @return
     */
    int securePort() default 8443;

    /**
     * Context path of the server. All requests below the context path are handled by the server.
     * @return
     */
    String contextPath() default "";

    /**
     * Server timeout.
     * @return
     */
    long timeout() default 5000L;

    /**
     * Auto start the server.
     * @return
     */
    boolean autoStart() default false;

    /**
     * Reference to a endpoint adapter.
     * @return
     */
    String endpointAdapter() default "";

    /**
     * Debug logging.
     * @return
     */
    boolean debugLogging() default false;

    /**
     * Test actor.
     * @return
     */
    String actor() default "";

    /**
     * Reference to a message converter.
     * @return
     */
    String messageConverter() default "";

    /**
     * Handle cookies.
     * @return
     */
    boolean handleCookies() default false;

    /**
     * Remove semicolon path content (matrix parameters) from the request path.
     * @return
     */
    boolean removeSemicolonPathContent() default true;

    /**
     * Default status code of the response when the server does not set a status code.
     * @return
     */
    HttpStatus defaultStatus() default HttpStatus.OK;

    /**
     * Binary media types.
     * @return
     */
    String[] binaryMediaTypes() default {};

    /**
     * Use the default server filters. On the Vert.x based server this enables the gzip
     * content coding for requests and responses.
     * @return
     */
    boolean useDefaultFilters() default true;

    /**
     * References to server interceptors. Only Citrus server interceptors are supported.
     * @return
     */
    String[] interceptors() default {};

    /**
     * Reference to a server authentication.
     * @return
     */
    String authentication() default "";

    /**
     * Secured path of the server authentication.
     * @return
     */
    String securedPath() default "/*";

    /**
     * Reference to a secure connection.
     * @return
     */
    String secured() default "";

    /**
     * Reference to a Vert.x instance. A supplied instance is never closed by the server.
     * @return
     */
    String vertx() default "";

    /**
     * Size of the worker pool that handles the requests. This matches the default thread
     * pool maximum of the Jetty based server.
     * @return
     */
    int workerPoolSize() default 200;
}
