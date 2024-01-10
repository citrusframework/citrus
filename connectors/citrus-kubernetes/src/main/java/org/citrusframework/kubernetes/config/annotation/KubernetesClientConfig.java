/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.kubernetes.config.annotation;

import org.citrusframework.annotations.CitrusEndpointConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "k8s.client")
public @interface KubernetesClientConfig {

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
     * Authentication Token
     * @return
     */
    String oauthToken() default "";

    /**
     * Namespace
     * @return
     */
    String namespace() default "";

    /**
     * Certificate file
     * @return
     */
    String certFile() default "";

    /**
     * Message converter.
     * @return
     */
    String messageConverter() default  "";

    /**
     * Object mapper.
     * @return
     */
    String objectMapper() default  "";

}
