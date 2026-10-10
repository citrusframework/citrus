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
package org.citrusframework.graphql.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.springframework.web.bind.annotation.RequestMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "graphql.client")
public @interface GraphQlClientConfig {

    /** Request URL, e.g. {@code http://localhost:8080}. */
    String requestUrl();

    /** GraphQL path; empty for {@code /graphql} unless the request URL has a path. */
    String path() default "";

    /** SDL schema resources used to validate GraphQL documents. */
    String[] schema() default {};

    /** Default request method, POST or GET. */
    RequestMethod requestMethod() default RequestMethod.POST;

    /** GraphQL document and response checks. */
    boolean strict() default true;

    String restTemplate() default "";

    String requestFactory() default "";

    String endpointResolver() default "";

    String[] interceptors() default {};

    long timeout() default 5000L;

    ErrorHandlingStrategy errorStrategy() default ErrorHandlingStrategy.PROPAGATE;

    String errorHandler() default "";

    boolean handleCookies() default false;

    boolean disableRedirectHandling() default false;

    String charset() default "UTF-8";

    String correlator() default "";

    int pollingInterval() default 500;

    String actor() default "";

    String authentication() default "";

    String secured() default "";
}
