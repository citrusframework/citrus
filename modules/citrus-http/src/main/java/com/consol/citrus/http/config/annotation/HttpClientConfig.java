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

package com.consol.citrus.http.config.annotation;

import com.consol.citrus.annotations.CitrusEndpointConfig;
import com.consol.citrus.message.ErrorHandlingStrategy;
import org.springframework.http.HttpMethod;

import java.lang.annotation.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "endpoint.parser.http.client")
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
    HttpMethod requestMethod() default HttpMethod.POST;

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
     * Client interceptors.
     * @return
     */
    String[] interceptors() default {};

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
}
