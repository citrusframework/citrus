/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.jmx.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.rmi.registry.Registry;

import org.citrusframework.annotations.CitrusEndpointConfig;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "jmx.server")
public @interface JmxServerConfig {

    /**
     * Server uri.
     * @return
     */
    String serverUrl() default "";

    /**
     * Host.
     * @return
     */
    String host() default "localhost";

    /**
     * Server port.
     * @return
     */
    int port() default Registry.REGISTRY_PORT;

    /**
     * Binding.
     * @return
     */
    String binding() default "";

    /**
     * Protocol.
     * @return
     */
    String protocol() default "rmi";

    /**
     * Create registry.
     * @return
     */
    boolean createRegistry() default false;

    /**
     * Environment properties.
     * @return
     */
    String environmentProperties() default  "";

    /**
     * Mbeans.
     * @return
     */
    MbeanConfig[] mbeans() default {};

    /**
     * Auto start.
     * @return
     */
    boolean autoStart() default false;

    /**
     * Message converter.
     * @return
     */
    String messageConverter() default  "";

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
