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
package org.citrusframework.grpc.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;

/**
 * Server configuration for a gRPC simulator endpoint injected with {@code @CitrusEndpoint}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "grpc.server")
public @interface GrpcServerConfig {

    int port() default 0;

    String[] services() default {};

    String[] descriptors() default {};

    String[] protoPaths() default {};

    boolean tls() default false;

    String certificateChain() default "";

    String privateKey() default "";

    boolean omitDefaultValues() default false;

    long timeout() default 5000L;

    boolean autoStart() default true;

    String endpointAdapter() default "";

    String actor() default "";
}
