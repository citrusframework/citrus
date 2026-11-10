/*
 * Copyright the original author or authors.
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

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.vertx.endpoint.VertxEndpointConfiguration;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * @since 2.0
 */
public class VertxMessageConverterTest extends AbstractUnitTest {

    private io.vertx.core.eventbus.Message vertxMessage = Mockito.mock(io.vertx.core.eventbus.Message.class);

    private VertxMessageConverter messageConverter = new VertxMessageConverter();

    @Test
    public void testConvertInbound() {

        reset(vertxMessage);

        when(vertxMessage.body()).thenReturn("Hello Citrus!");
        when(vertxMessage.address()).thenReturn("hello");
        when(vertxMessage.replyAddress()).thenReturn("answer");

        Message message = messageConverter.convertInbound(vertxMessage, new VertxEndpointConfiguration(), context);

        Assertions.assertEquals("Hello Citrus!", message.getPayload());
        Assertions.assertEquals("hello", message.getHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS));
        Assertions.assertEquals("answer", message.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS));

    }

    @Test
    public void testConvertInboundNullMessage() {
        Assertions.assertNull(messageConverter.convertInbound(null, new VertxEndpointConfiguration(), context));
    }

    @Test
    public void testConvertOutbound() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            messageConverter.convertOutbound(new DefaultMessage("This is a test!"), new VertxEndpointConfiguration(), context);
        });
    }

    @Test
    public void testConvertOutboundOnExternalMessage() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            messageConverter.convertOutbound(vertxMessage, new DefaultMessage("This is a test!"), new VertxEndpointConfiguration(), context);
        });
    }
}
