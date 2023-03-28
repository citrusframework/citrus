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

package org.citrusframework.websocket.config.annotation;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public @interface WebSocketConfig {

    /**
     * Id.
     * @return
     */
    String id();

    /**
     * Endpoint path.
     * @return
     */
    String path();

    /**
     * Message converter reference.
     * @return
     */
    String messageConverter() default "";

    /**
     * Timeout.
     * @return
     */
    long timeout() default 5000L;
}
