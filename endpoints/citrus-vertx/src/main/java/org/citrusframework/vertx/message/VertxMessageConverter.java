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

package org.citrusframework.vertx.message;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.*;
import org.citrusframework.message.Message;
import org.citrusframework.vertx.endpoint.VertxEndpointConfiguration;

/**
 * Message converter implementation converts inbound Vert.x messages to internal message representation. Outbound message conversion is not supported
 * as the Vert.x message transport encapsulates message conversion by default.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxMessageConverter implements MessageConverter<io.vertx.core.eventbus.Message, io.vertx.core.eventbus.Message, VertxEndpointConfiguration> {

    @Override
    public Message convertInbound(io.vertx.core.eventbus.Message vertxMessage, VertxEndpointConfiguration endpointConfiguration, TestContext context) {
        if (vertxMessage == null) {
            return null;
        }

        Message message = new DefaultMessage(vertxMessage.body())
                .setHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS, vertxMessage.address())
                .setHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS, vertxMessage.replyAddress());

        return message;
    }

    @Override
    public io.vertx.core.eventbus.Message convertOutbound(Message internalMessage, VertxEndpointConfiguration endpointConfiguration, TestContext context) {
        throw new UnsupportedOperationException("Unable to convert Vert.x outbound message");
    }

    @Override
    public void convertOutbound(io.vertx.core.eventbus.Message externalMessage, Message internalMessage, VertxEndpointConfiguration endpointConfiguration, TestContext context) {
        throw new UnsupportedOperationException("Unable to convert Vert.x outbound message");
    }
}
