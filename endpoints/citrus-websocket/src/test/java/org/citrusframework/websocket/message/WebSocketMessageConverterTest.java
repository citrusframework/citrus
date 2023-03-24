/*
 * Copyright 2006-2015 the original author or authors.
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
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.websocket.client.WebSocketClientEndpointConfiguration;
import org.citrusframework.websocket.endpoint.WebSocketEndpointConfiguration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class WebSocketMessageConverterTest extends AbstractTestNGUnitTest {

    private WebSocketMessageConverter messageConverter = new WebSocketMessageConverter();

    @Test
    public void testConvertTextMessageOutbound() throws Exception {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();

        WebSocketMessage message = new WebSocketMessage("Hello WebSocket!");
        org.springframework.web.socket.WebSocketMessage result = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assert.assertTrue(TextMessage.class.isInstance(result));
        Assert.assertEquals(((TextMessage) result).getPayload(), "Hello WebSocket!");
        Assert.assertEquals(result.isLast(), true);

        message = new WebSocketMessage("Hello WebSocket - stay tuned!").last(false);
        result = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assert.assertTrue(TextMessage.class.isInstance(result));
        Assert.assertEquals(((TextMessage) result).getPayload(), "Hello WebSocket - stay tuned!");
        Assert.assertEquals(result.isLast(), false);
    }

    @Test
    public void testConvertBinaryMessageOutbound() throws Exception {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();

        WebSocketMessage message = new WebSocketMessage("Hello WebSocket!".getBytes(Charset.forName("UTF-8")));
        org.springframework.web.socket.WebSocketMessage result = messageConverter.convertOutbound(message, endpointConfiguration, context);

        Assert.assertTrue(BinaryMessage.class.isInstance(result));
        Assert.assertEquals(((BinaryMessage) result).getPayload().array(), "Hello WebSocket!".getBytes(Charset.forName("UTF-8")));
        Assert.assertEquals(result.isLast(), true);
    }

    @Test
    public void testConvertTextMessageInbound() throws Exception {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();
        org.springframework.web.socket.WebSocketMessage externalMessage = new TextMessage("Hello WebSocket!");

        Message internal = messageConverter.convertInbound(externalMessage, endpointConfiguration, context);

        Assert.assertTrue(WebSocketMessage.class.isInstance(internal));
        Assert.assertEquals(internal.getPayload(String.class), "Hello WebSocket!");
        Assert.assertEquals(((WebSocketMessage) internal).isLast(), true);

        externalMessage = new TextMessage("Hello WebSocket - stay tuned!", false);
        internal = messageConverter.convertInbound(externalMessage, endpointConfiguration, context);

        Assert.assertTrue(WebSocketMessage.class.isInstance(internal));
        Assert.assertEquals(internal.getPayload(String.class), "Hello WebSocket - stay tuned!");
        Assert.assertEquals(((WebSocketMessage) internal).isLast(), false);
    }

    @Test
    public void testConvertBinaryMessageInbound() throws Exception {
        WebSocketEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();
        org.springframework.web.socket.WebSocketMessage externalMessage = new BinaryMessage("Hello WebSocket!".getBytes(Charset.forName("UTF-8")));

        Message internal = messageConverter.convertInbound(externalMessage, endpointConfiguration, context);

        Assert.assertTrue(WebSocketMessage.class.isInstance(internal));
        Assert.assertEquals(internal.getPayload(ByteBuffer.class).array(), "Hello WebSocket!".getBytes(Charset.forName("UTF-8")));
        Assert.assertEquals(((WebSocketMessage) internal).isLast(), true);
    }
}
