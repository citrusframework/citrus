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

package com.consol.citrus.camel.message;

import org.apache.camel.Exchange;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

/**
 * Message converter able to read Camel exchange and create proper Spring Integration message
 * for internal use.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelMessageConverter {

    public Message<?> convertMessage(Exchange source) {
        if (source == null) {
            return null;
        }

        org.apache.camel.Message sourceMessage;
        if (source.hasOut()) {
            sourceMessage = source.getOut();
        } else {
            sourceMessage = source.getIn();
        }

        MessageBuilder messageBuilder = MessageBuilder.withPayload(sourceMessage.getBody())
                .copyHeaders(sourceMessage.getHeaders())
                .setHeader(CitrusCamelMessageHeaders.EXCHANGE_ID, source.getExchangeId())
                .setHeader(CitrusCamelMessageHeaders.ROUTE_ID, source.getFromRouteId())
                .setHeader(CitrusCamelMessageHeaders.EXCHANGE_PATTERN, source.getPattern().name())
                .setHeader(CitrusCamelMessageHeaders.EXCHANGE_FAILED, source.isFailed());


        if (source.getException() != null) {
            messageBuilder.setHeader(CitrusCamelMessageHeaders.EXCHANGE_EXCEPTION, source.getException().getClass().getName());
            messageBuilder.setHeader(CitrusCamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE, source.getException().getMessage());
        }

        return messageBuilder.build();

    }
}
