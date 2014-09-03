/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.vertx.message;

import org.springframework.messaging.Message;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxMessageConverter {

    /**
     * Converts Vert.x message to Citrus internal message representation. Adds default headers
     * for Vert.x event bus address.
     * @param source
     * @return
     */
    public Message<?> convertMessage(org.vertx.java.core.eventbus.Message source) {
        if (source == null) {
            return null;
        }

        MessageBuilder builder = MessageBuilder.withPayload(source.body());
        builder.setHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS, source.address());
        builder.setHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS, source.replyAddress());

        return builder.build();
    }
}
