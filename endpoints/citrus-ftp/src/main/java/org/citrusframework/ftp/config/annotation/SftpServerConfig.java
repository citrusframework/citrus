/*
 * Copyright 2006-2018 the original author or authors.
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
 * @since 2.7.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "sftp.server")
public @interface SftpServerConfig {

    /**
     * Server port.
     * @return
     */
    int port() default 22;

    /**
     * User.
     * @return
     */
    String user() default "";

    /**
     * Password.
     * @return
     */
    String password() default "";

    /**
     * HostKeyPath.
     * @return
     */
    String hostKeyPath() default "";

    /**
     * UserHomeDir.
     * @return
     */
    String userHomePath() default "";

    /**
     * AllowedKeyPath.
     * @return
     */
    String allowedKeyPath() default "";

    /**
     * Message converter.
     * @return
     */
    String messageConverter() default  "";

    /**
     * Polling interval.
     * @return
     */
    int pollingInterval() default 500;

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
