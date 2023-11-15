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
import org.citrusframework.message.ErrorHandlingStrategy;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "http.client")
public @interface HttpClientConfig {

    /**
     * Request uri.
     * @return
     */
    String requestUrl();

    /**
     * RestTemplate
     * @return
     */
    String restTemplate() default "";

    /**
     * Request factory.
     * @return
     */
    String requestFactory() default "";

    /**
     * Endpoint uri resolver.
     * @return
     */
    String endpointResolver() default "";

    /**
     * Http request method.
     * @return
     */
    RequestMethod requestMethod() default RequestMethod.POST;

    /**
     * Message converter.
     * @return
     */
    String messageConverter() default  "";

    /**
     * Message correlator.
     * @return
     */
    String correlator() default "";

    /**
     * Default charset.
     * @return
     */
    String charset() default "UTF-8";

    /**
     * Default accept header.
     * @return
     */
    boolean defaultAcceptHeader() default true;

    /**
     * Handle cookies.
     * @return
     */
    boolean handleCookies() default false;

    /**
     * Content type.
     * @return
     */
    String contentType() default "text/plain";

    /**
     * Polling interval.
     * @return
     */
    int pollingInterval() default 500;

    /**
     * Error handling strategy.
     * @return
     */
    ErrorHandlingStrategy errorStrategy() default ErrorHandlingStrategy.PROPAGATE;

    /**
     * Error handler.
     * @return
     */
    String errorHandler() default "";

    /**
     * Client interceptors.
     * @return
     */
    String[] interceptors() default {};

    /**
     * Binary media types.
     * @return
     */
    String[] binaryMediaTypes() default {};

    /**
     * Timeout.
     * @return
     */
    long timeout() default 5000L;

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
     * Secured connection.
     * @return
     */
    String secured() default "";
}
