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

package org.citrusframework.http.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "http.server")
public @interface HttpServerConfig {

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
     * Filter references.
     * @return
     */
    String[] filters() default {};

    /**
     * Filter mapping references.
     * @return
     */
    String[] filterMappings() default {};

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
     * Handle attribute headers.
     * @return
     */
    boolean handleAttributeHeaders() default false;

    /**
     * Handle cookies.
     * @return
     */
    boolean handleCookies() default false;

    /**
     * Server default response status.
     * @return
     */
    HttpStatus defaultStatus() default HttpStatus.OK;

    /**
     * Server default response cache size.
     * @return
     */
    int responseCacheSize() default 100;

    /**
     * Binary media types.
     * @return
     */
    String[] binaryMediaTypes() default {};

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

    /**
     * User authentication.
     * @return
     */
    String authentication() default "";

    /**
     * Resource path that is secured with user authentication.
     * @return
     */
    String securedPath() default "/*";

    /**
     * Secured connection.
     * @return
     */
    String secured() default "";

    /**
     * Secured server port.
     * @return
     */
    int securePort() default 8443;
}
