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

package com.consol.citrus.config.annotation;

import com.consol.citrus.annotations.CitrusEndpointConfig;

import java.lang.annotation.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "endpoint.parser.channel")
public @interface ChannelEndpointConfig {

    /**
     * Destination name.
     * @return
     */
    String channelName() default "";

    /**
     * Destination reference.
     * @return
     */
    String channel() default "";

    /**
     * Messaging template.
     * @return
     */
    String messagingTemplate() default "";

    /**
     * Message converter reference.
     * @return
     */
    String messageConverter() default "";

    /**
     * Destination resolver.
     * @return
     */
    String channelResolver() default "";

    /**
     * Should use object messages.
     * @return
     */
    boolean useObjectMessages() default false;

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
