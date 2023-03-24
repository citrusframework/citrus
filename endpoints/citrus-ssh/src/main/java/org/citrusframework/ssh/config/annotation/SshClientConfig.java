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

package org.citrusframework.ssh.config.annotation;

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
@CitrusEndpointConfig(qualifier = "ssh.client")
public @interface SshClientConfig {

    /**
     * Host.
     * @return
     */
    String host() default "localhost";

    /**
     * Server port.
     * @return
     */
    int port() default 2222;

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
     * PrivateKeyPath.
     * @return
     */
    String privateKeyPath() default "";

    /**
     * <privateKeyPassword.
     * @return
     */
    String privateKeyPassword() default "";

    /**
     * StrictHostChecking.
     * @return
     */
    boolean strictHostChecking() default false;

    /**
     * KnownHosts.
     * @return
     */
    String knownHosts() default "";

    /**
     * CommandTimeout.
     * @return
     */
    long commandTimeout() default 1000L * 60L * 5L;

    /**
     * ConnectionTimeout.
     * @return
     */
    int connectionTimeout() default 1000 * 60 * 1;

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
     * Polling interval.
     * @return
     */
    int pollingInterval() default 500;

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
