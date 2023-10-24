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

package org.citrusframework.mail.config.annotation;

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
@CitrusEndpointConfig(qualifier = "mail.server")
public @interface MailServerConfig {

    /**
     * Port.
     * @return
     */
    int port() default 25;

    /**
     * User authentication required.
     * @return
     */
    boolean authRequired() default true;

    /**
     * Auto accept.
     * @return
     */
    boolean autoAccept() default true;

    /**
     * Split multipart messages.
     * @return
     */
    boolean splitMultipart() default false;

    /**
     * Mail marshaller.
     * @return
     */
    String marshaller() default "";

    /**
     * Java mail properties.
     * @return
     */
    String javaMailProperties() default "";

    /**
     * Message converter.
     * @return
     */
    String messageConverter() default  "";

    /**
     * Known users.
     * @return
     */
    String[] knownUsers() default {};

    /**
     * Auto start.
     * @return
     */
    boolean autoStart() default false;

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
