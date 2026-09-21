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

package org.citrusframework.http.actions;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultMessageValidatorRegistry;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidator;
import org.citrusframework.validation.json.JsonTextMessageValidator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Reproduction test for GitHub issue #1734.
 * When receiving an HTTP response with type=plaintext and a validation matcher like @contains(...)@,
 * the validation must correctly fail if the received payload does not match.
 */
public class HttpReceivePlaintextValidationTest extends AbstractTestNGUnitTest {

    @Mock
    private Endpoint endpoint;
    @Mock
    private Consumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;

    @Override
    protected TestContextFactory createTestContextFactory() {
        MockitoAnnotations.openMocks(this);
        return TestContextFactory.newInstance();
    }

    @BeforeMethod
    public void setup() {
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);
        when(endpoint.getActor()).thenReturn(null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void shouldFailPlaintextContainsValidationOnJsonPayload() {
        Message receivedMessage = new HttpMessage("{\"models\":[]}").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.message().type("plaintext").body("@contains('granite4:3b')@");

        ReceiveMessageAction action = builder.build();
        action.execute(context);
    }

    @Test
    public void shouldPassPlaintextContainsValidationWhenMatches() {
        Message receivedMessage = new HttpMessage("{\"models\":[{\"name\":\"granite4:3b\"}]}").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.message().type("plaintext").body("@contains('granite4:3b')@");

        ReceiveMessageAction action = builder.build();

        action.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void shouldFailPlaintextContainsValidationOnJsonPayloadWithContentType() {
        Message receivedMessage = new HttpMessage("{\"models\":[]}").status(HttpStatus.OK)
                .contentType("application/json");
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.message().type("plaintext").body("@contains('granite4:3b')@");

        ReceiveMessageAction action = builder.build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldFailPlaintextValidationWithoutPlaintextValidator() {
        DefaultMessageValidatorRegistry registry = new DefaultMessageValidatorRegistry();
        registry.getMessageValidators().clear();
        registry.addMessageValidator("header", new DefaultMessageHeaderValidator());
        registry.addMessageValidator("json", new JsonTextMessageValidator());
        registry.addMessageValidator("json-path", new JsonPathMessageValidator());
        context.setMessageValidatorRegistry(registry);

        Message receivedMessage = new HttpMessage("{\"models\":[]}").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.message().type("plaintext").body("@contains('granite4:3b')@");

        ReceiveMessageAction action = builder.build();
        action.execute(context);
    }
}
