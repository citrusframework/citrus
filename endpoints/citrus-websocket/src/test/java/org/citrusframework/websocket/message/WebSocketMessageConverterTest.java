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

package org.citrusframework.websocket.message;

import org.citrusframework.message.Message;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.websocket.client.WebSocketClientEndpointConfiguration;
import org.citrusframework.websocket.endpoint.WebSocketEndpointConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @since 2.3
 */
public class WebSocketMessageConverterTest extends AbstractUnitTest {

    private WebSocketMessageConverter messageConverter = new WebSocketMessageConverter();

    @Test
    public void testConvertTextMessageOutbound() {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();

        WebSocketMessage message = new WebSocketMessage("Hello WebSocket!");
        org.springframework.web.socket.WebSocketMessage result = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assertions.assertTrue(TextMessage.class.isInstance(result));
        Assertions.assertEquals("Hello WebSocket!", ((TextMessage) result).getPayload());
        Assertions.assertEquals(true, result.isLast());

        message = new WebSocketMessage("Hello WebSocket - stay tuned!").last(false);
        result = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assertions.assertTrue(TextMessage.class.isInstance(result));
        Assertions.assertEquals("Hello WebSocket - stay tuned!", ((TextMessage) result).getPayload());
        Assertions.assertEquals(false, result.isLast());
    }

    @Test
    public void testConvertBinaryMessageOutbound() {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();

        WebSocketMessage message = new WebSocketMessage("Hello WebSocket!".getBytes(StandardCharsets.UTF_8));
        org.springframework.web.socket.WebSocketMessage result = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assertions.assertTrue(BinaryMessage.class.isInstance(result));
        Assertions.assertArrayEquals("Hello WebSocket!".getBytes(StandardCharsets.UTF_8), ((BinaryMessage) result).getPayload().array());
        Assertions.assertEquals(true, result.isLast());
    }

    @Test
    public void testConvertTextMessageInbound() {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();
        org.springframework.web.socket.WebSocketMessage externalMessage = new TextMessage("Hello WebSocket!");

        Message internal = messageConverter.convertInbound(externalMessage, endpointConfiguration, context);

        Assertions.assertTrue(WebSocketMessage.class.isInstance(internal));
        Assertions.assertEquals("Hello WebSocket!", internal.getPayload(String.class));
        Assertions.assertEquals(true, ((WebSocketMessage) internal).isLast());

        externalMessage = new TextMessage("Hello WebSocket - stay tuned!", false);
        internal = messageConverter.convertInbound(externalMessage, endpointConfiguration, context);

        Assertions.assertTrue(WebSocketMessage.class.isInstance(internal));
        Assertions.assertEquals("Hello WebSocket - stay tuned!", internal.getPayload(String.class));
        Assertions.assertEquals(false, ((WebSocketMessage) internal).isLast());
    }

    @Test
    public void testConvertBinaryMessageInbound() {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();
        org.springframework.web.socket.WebSocketMessage externalMessage = new BinaryMessage("Hello WebSocket!".getBytes(StandardCharsets.UTF_8));

        Message internal = messageConverter.convertInbound(externalMessage, endpointConfiguration, context);

        Assertions.assertTrue(WebSocketMessage.class.isInstance(internal));
        Assertions.assertArrayEquals("Hello WebSocket!".getBytes(StandardCharsets.UTF_8), internal.getPayload(ByteBuffer.class).array());
        Assertions.assertEquals(true, ((WebSocketMessage) internal).isLast());
    }
}
