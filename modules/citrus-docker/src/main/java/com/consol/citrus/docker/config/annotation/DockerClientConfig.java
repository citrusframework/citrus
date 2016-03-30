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

package com.consol.citrus.docker.config.annotation;

import com.consol.citrus.annotations.CitrusEndpointConfig;

import java.lang.annotation.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "endpoint.parser.docker.client")
public @interface DockerClientConfig {

    /**
     * Client url
     * @return
     */
    String url() default "";

    /**
     * Version.
     * @return
     */
    String version() default "";

    /**
     * Username
     * @return
     */
    String username() default "";

    /**
     * Password
     * @return
     */
    String password() default "";

    /**
     * Email
     * @return
     */
    String email() default "";

    /**
     * Server address
     * @return
     */
    String serverAddress() default "";

    /**
     * Certificate path
     * @return
     */
    String certPath() default "";

    /**
     * Configuration path
     * @return
     */
    String configPath() default "";
}
