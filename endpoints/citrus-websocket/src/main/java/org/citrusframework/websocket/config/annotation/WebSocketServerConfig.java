/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.websocket.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "websocket.server")
public @interface WebSocketServerConfig {

    /**
     * WebSocket endpoints.
     * @return
     */
    WebSocketConfig[] webSockets() default {};

    /**
     * Server port.
     * @return
     */
    int port() default 8080;

    /**
     * Context config location.
     * @return
     */
    String contextConfigLocation() default "";

    /**
     * Resource base.
     * @return
     */
    String resourceBase() default "";

    /**
     * Root parent context.
     * @return
     */
    boolean rootParentContext() default false;

    /**
     * Connector references.
     * @return
     */
    String[] connectors() default {};

    /**
     * Connector reference.
     * @return
     */
    String connector() default "";

    /**
     * Servlet name.
     * @return
     */
    String servletName() default "";

    /**
     * Servlet mapping path.
     * @return
     */
    String servletMappingPath() default "";

    /**
     * Context path.
     * @return
     */
    String contextPath() default "";

    /**
     * Servlet handler reference.
     * @return
     */
    String servletHandler() default "";

    /**
     * Security handler reference.
     * @return
     */
    String securityHandler() default "";

    /**
     * Message converter reference.
     * @return
     */
    String messageConverter() default "";

    /**
     * Auto start.
     * @return
     */
    boolean autoStart() default false;

    /**
     * Timeout.
     * @return
     */
    long timeout() default 5000L;

    /**
     * Endpoint adapter reference.
     * @return
     */
    String endpointAdapter() default "";

    /**
     * Debug logging enabled.
     * @return
     */
    boolean debugLogging() default false;

    /**
     * Interceptor references.
     * @return
     */
    String[] interceptors() default {};

    /**
     * Test actor.
     * @return
     */
    String actor() default "";
}
