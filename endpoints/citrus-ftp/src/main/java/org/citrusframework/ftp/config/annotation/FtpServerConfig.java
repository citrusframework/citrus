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

package org.citrusframework.ftp.config.annotation;

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
@CitrusEndpointConfig(qualifier = "ftp.server")
public @interface FtpServerConfig {

    /**
     * Server port.
     * @return
     */
    int port() default 22222;

    /**
     * Ftp server.
     * @return
     */
    String server() default "";

    /**
     * User manager.
     * @return
     */
    String userManager() default "";

    /**
     * User manager properties.
     * @return
     */
    String userManagerProperties() default "";

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
     * Auto start.
     * @return
     */
    boolean autoStart() default false;

    /**
     * Auto connect.
     * @return
     */
    boolean autoConnect() default true;

    /**
     * Auto login.
     * @return
     */
    boolean autoLogin() default true;

    /**
     * Auto handle commands.
     * @return
     */
    String autoHandleCommands() default "PORT,TYPE";

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
