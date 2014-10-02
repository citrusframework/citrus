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

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.vertx.endpoint.VertxEndpointConfiguration;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class VertxMessageConverterTest {

    private org.vertx.java.core.eventbus.Message vertxMessage = EasyMock.createMock(org.vertx.java.core.eventbus.Message.class);

    private VertxMessageConverter messageConverter = new VertxMessageConverter();

    @Test
    public void testConvertInbound() {

        reset(vertxMessage);

        expect(vertxMessage.body()).andReturn("Hello Citrus!").once();
        expect(vertxMessage.address()).andReturn("hello").once();
        expect(vertxMessage.replyAddress()).andReturn("answer").once();

        replay(vertxMessage);

        Message message = messageConverter.convertInbound(vertxMessage, new VertxEndpointConfiguration());

        Assert.assertEquals(message.getPayload(), "Hello Citrus!");
        Assert.assertEquals(message.getHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS), "hello");
        Assert.assertEquals(message.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS), "answer");

        verify(vertxMessage);
    }

    @Test
    public void testConvertInboundNullMessage() {
        Assert.assertNull(messageConverter.convertInbound(null, new VertxEndpointConfiguration()));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConvertOutbound() {
        messageConverter.convertOutbound(new DefaultMessage("This is a test!"), new VertxEndpointConfiguration());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConvertOutboundOnExternalMessage() {
        messageConverter.convertOutbound(vertxMessage, new DefaultMessage("This is a test!"), new VertxEndpointConfiguration());
    }
}
