/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.validation;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DTDValidationTest extends UnitTestSupport {
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Consumer consumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public void testInlineDTD() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<!DOCTYPE root [ "
                + "<!ELEMENT root (message)>"
                + "<!ELEMENT message (text)>"
                + "<!ELEMENT text (#PCDATA)>"
                + " ]>"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<!DOCTYPE root [ "
                + "<!ELEMENT root (message)>"
                + "<!ELEMENT message (text)>"
                + "<!ELEMENT text (#PCDATA)>"
                + " ]>"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSystemId() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<!DOCTYPE root SYSTEM \"org/citrusframework/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<!DOCTYPE root SYSTEM \"org/citrusframework/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testPublicId() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<!DOCTYPE root PUBLIC \"example\" \"org/citrusframework/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<!DOCTYPE root PUBLIC \"example\" \"org/citrusframework/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testPublicIdError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<!DOCTYPE root PUBLIC \"example\" \"org/citrusframework/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<!DOCTYPE root PUBLIC \"foo\" \"org/citrusframework/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>"));


        try {
            ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                    .endpoint(endpoint)
                    .message(controlMessageBuilder)
                    .build();
            receiveAction.execute(context);
            Assert.fail("Missing validation exception due to mismatch in public id");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'foo' but was 'example'"));
        }
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSystemIdError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<!DOCTYPE root PUBLIC \"example\" \"org/citrusframework/validation/example.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<!DOCTYPE root PUBLIC \"example\" \"org/w3/xhtml/xhtml1-transitional.dtd\">"
                        + "<root>"
                            + "<message>"
                                + "<text>Hello TestFramework!</text>"
                            + "</message>"
                        + "</root>"));


        try {
            ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                    .endpoint(endpoint)
                    .message(controlMessageBuilder)
                    .build();
            receiveAction.execute(context);
            Assert.fail("Missing validation exception due to mismatch in public id");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'org/w3/xhtml/xhtml1-transitional.dtd' but was 'org/citrusframework/validation/example.dtd'"));
        }
    }
}
