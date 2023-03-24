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
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "mail.client")
public @interface MailClientConfig {

    /**
     * Host.
     * @return
     */
    String host() default "";

    /**
     * Port.
     * @return
     */
    int port() default JavaMailSenderImpl.DEFAULT_PORT;

    /**
     * protocol.
     * @return
     */
    String protocol() default JavaMailSenderImpl.DEFAULT_PROTOCOL;

    /**
     * Message converter.
     * @return
     */
    String messageConverter() default  "";

    /**
     * Java mail sender.
     * @return
     */
    String javaMailSender() default "";

    /**
     * Default username.
     * @return
     */
    String username() default "";

    /**
     * Content password.
     * @return
     */
    String password() default "";

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
