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

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "graphql.server")
public @interface GraphQlServerConfig {

    int port() default 8080;

    /** GraphQL path. */
    String path() default "/graphql";

    /** SDL schema resources used to validate incoming GraphQL documents. */
    String[] schema() default {};

    /** GraphQL document and reply checks. */
    boolean strict() default true;

    boolean autoStart() default false;

    long timeout() default 5000L;

    String contextPath() default "";

    String endpointAdapter() default "";

    String[] interceptors() default {};

    boolean debugLogging() default false;

    String actor() default "";

    String authentication() default "";

    String securedPath() default "/*";

    String secured() default "";

    int securePort() default 8443;
}
