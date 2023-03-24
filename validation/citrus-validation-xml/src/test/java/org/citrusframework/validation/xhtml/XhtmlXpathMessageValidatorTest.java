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

package org.citrusframework.validation.xhtml;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class XhtmlXpathMessageValidatorTest extends UnitTestSupport {
    @Mock
    private Endpoint endpoint;
    @Mock
    private Consumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;

    @Override
    protected TestContextFactory createTestContextFactory() {
        MockitoAnnotations.openMocks(this);
        return super.createTestContextFactory();
    }

    @Test
    public void testXhtmlXpathValidation() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                            + "<head>"
                                + "<title>Sample XHTML content</title>"
                            + "</head>"
                            + "<body>"
                                + "<p>Hello TestFramework!</p>"
                                + "<form action=\"/\">"
                                    + "<input name=\"foo\" type=\"text\" />"
                                + "</form>"
                            + "</body>"
                        + "</html>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.getXpathExpressions().put("/xh:html/xh:head/xh:title", "Sample XHTML content");
        validationContext.getXpathExpressions().put("//xh:p", "Hello TestFramework!");
        validationContext.getNamespaces().put("xh", "http://www.w3.org/1999/xhtml");
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                            + "<head>"
                                + "<title>Sample XHTML content</title>"
                            + "</head>"
                            + "<body>"
                                + "<p>Hello TestFramework!</p>"
                                + "<form action=\"/\">"
                                    + "<input name=\"foo\" type=\"text\" />"
                                + "</form>"
                            + "</body>"
                        + "</html>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .type(MessageType.XHTML)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testXhtmlXpathValidationFailed() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                            + "<head>"
                                + "<title>Sample XHTML content</title>"
                            + "</head>"
                            + "<body>"
                                + "<h1>Hello TestFramework!</h1>"
                            + "</body>"
                        + "</html>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.getXpathExpressions().put("//xh:h1", "Failed!");
        validationContext.getNamespaces().put("xh", "http://www.w3.org/1999/xhtml");
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"org/w3/xhtml/xhtml1-strict.dtd\">"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                            + "<head>"
                                + "<title>Sample XHTML content</title>"
                            + "</head>"
                            + "<body>"
                                + "<h1>Hello TestFramework!</h1>"
                            + "</body>"
                        + "</html>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .type(MessageType.XHTML)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }
}
