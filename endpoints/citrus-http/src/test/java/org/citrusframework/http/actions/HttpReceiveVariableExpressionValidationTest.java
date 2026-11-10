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
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.PathExpressionValidationContext;
import org.citrusframework.validation.context.path.DynamicPathExpressionValidationContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Reproduction test for GitHub issue #676.
 * <p>
 * A path expression that references a test variable (e.g. {@code ${VERSION_PATH}}) must be resolved
 * at runtime and afterwards be classified as JSONPath or XPath expression. Otherwise the validation
 * gets silently bypassed because the expression is misinterpreted as JSONPath due to the leading
 * dollar sign.
 */
@ExtendWith(MockitoExtension.class)
public class HttpReceiveVariableExpressionValidationTest extends AbstractUnitTest {

    @Mock
    private Endpoint endpoint;
    @Mock
    private Consumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;

    @Override
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }

    @BeforeEach
    public void setup() {
        lenient().when(endpoint.createConsumer()).thenReturn(consumer);
        lenient().when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        lenient().when(endpointConfiguration.getTimeout()).thenReturn(5000L);
        lenient().when(endpoint.getActor()).thenReturn(null);
    }

    @Test
    public void shouldResolveVariableExpressionToDynamicValidationContext() {
        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.validate(PathExpressionValidationContext.Builder.pathExpression()
                .expression("${VERSION_PATH}", "1.0")
                .build());

        ReceiveMessageAction action = builder.build();

        Assertions.assertTrue(action.getValidationContexts()
                .stream()
                .anyMatch(DynamicPathExpressionValidationContext.class::isInstance));
    }

    @Test
    public void shouldValidateXpathExpressionFromVariable() {
        context.setVariable("VERSION_PATH", "/root/version/@number");

        Message receivedMessage = new HttpMessage("<root><version number=\"1.0\"/></root>").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.validate(PathExpressionValidationContext.Builder.pathExpression()
                .expression("${VERSION_PATH}", "1.0")
                .build());

        ReceiveMessageAction action = builder.build();
        action.execute(context);
    }

    @Test
    public void shouldFailXpathValidationFromVariableWithWrongValue() {
        context.setVariable("VERSION_PATH", "/root/version/@number");

        Message receivedMessage = new HttpMessage("<root><version number=\"1.0\"/></root>").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.validate(PathExpressionValidationContext.Builder.pathExpression()
                .expression("${VERSION_PATH}", "2.0")
                .build());

        ReceiveMessageAction action = builder.build();
        Assertions.assertThrows(ValidationException.class, () -> action.execute(context));
    }

    @Test
    public void shouldValidateJsonPathExpressionFromVariable() {
        context.setVariable("VERSION_PATH", "$.version.number");

        Message receivedMessage = new HttpMessage("{\"version\":{\"number\":\"1.0\"}}").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.validate(PathExpressionValidationContext.Builder.pathExpression()
                .expression("${VERSION_PATH}", "1.0")
                .build());

        ReceiveMessageAction action = builder.build();
        action.execute(context);
    }

    @Test
    public void shouldFailJsonPathValidationFromVariableWithWrongValue() {
        context.setVariable("VERSION_PATH", "$.version.number");

        Message receivedMessage = new HttpMessage("{\"version\":{\"number\":\"1.0\"}}").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.validate(PathExpressionValidationContext.Builder.pathExpression()
                .expression("${VERSION_PATH}", "2.0")
                .build());

        ReceiveMessageAction action = builder.build();
        Assertions.assertThrows(ValidationException.class, () -> action.execute(context));
    }

    @Test
    public void shouldFailControlMessageBodyComparisonWithJsonPayloadFromVariable() {
        context.setVariable("VERSION_PATH", "$.version.number");

        Message receivedMessage = new HttpMessage("{\"version\":{\"number\":\"1.0\"}}").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.message().type("json").body("{\"version\":{\"number\":\"2.0\"}}");
        builder.validate(PathExpressionValidationContext.Builder.pathExpression()
                .expression("${VERSION_PATH}", "1.0")
                .build());

        ReceiveMessageAction action = builder.build();
        Assertions.assertThrows(ValidationException.class, () -> action.execute(context));
    }

    @Test
    public void shouldFailControlMessageBodyComparisonWithXmlPayloadFromVariable() {
        context.setVariable("VERSION_PATH", "/root/version/@number");

        Message receivedMessage = new HttpMessage("<root><version number=\"1.0\"/></root>").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);
        builder.message().body("<root><version number=\"2.0\"/></root>");
        builder.validate(PathExpressionValidationContext.Builder.pathExpression()
                .expression("${VERSION_PATH}", "1.0")
                .build());

        ReceiveMessageAction action = builder.build();
        Assertions.assertThrows(ValidationException.class, () -> action.execute(context));
    }

    @Test
    public void shouldValidateXpathExpressionFromVariableUsingXmlDsl() {
        context.setVariable("VERSION_PATH", "/root/version/@number");

        Message receivedMessage = new HttpMessage("<root><version number=\"1.0\"/></root>").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);

        org.citrusframework.xml.actions.Receive receive = new org.citrusframework.xml.actions.Receive(builder);
        org.citrusframework.xml.actions.Receive.Validate validate = new org.citrusframework.xml.actions.Receive.Validate();
        validate.setPath("${VERSION_PATH}");
        validate.setValue("1.0");
        receive.getValidates().add(validate);

        ReceiveMessageAction action = receive.build();
        action.execute(context);
    }

    @Test
    public void shouldFailXpathValidationFromVariableUsingXmlDslWithWrongValue() {
        context.setVariable("VERSION_PATH", "/root/version/@number");

        Message receivedMessage = new HttpMessage("<root><version number=\"1.0\"/></root>").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);

        org.citrusframework.xml.actions.Receive receive = new org.citrusframework.xml.actions.Receive(builder);
        org.citrusframework.xml.actions.Receive.Validate validate = new org.citrusframework.xml.actions.Receive.Validate();
        validate.setPath("${VERSION_PATH}");
        validate.setValue("2.0");
        receive.getValidates().add(validate);

        ReceiveMessageAction action = receive.build();
        Assertions.assertThrows(ValidationException.class, () -> action.execute(context));
    }

    @Test
    public void shouldValidateXpathExpressionFromVariableUsingYamlDsl() {
        context.setVariable("VERSION_PATH", "/root/version/@number");

        Message receivedMessage = new HttpMessage("<root><version number=\"1.0\"/></root>").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);

        org.citrusframework.yaml.actions.Receive receive = new org.citrusframework.yaml.actions.Receive(builder);
        org.citrusframework.yaml.actions.Receive.Validate validate = new org.citrusframework.yaml.actions.Receive.Validate();
        validate.setPath("${VERSION_PATH}");
        validate.setValue("1.0");
        receive.getValidate().add(validate);

        ReceiveMessageAction action = receive.build();
        action.execute(context);
    }

    @Test
    public void shouldFailXpathValidationFromVariableUsingYamlDslWithWrongValue() {
        context.setVariable("VERSION_PATH", "/root/version/@number");

        Message receivedMessage = new HttpMessage("<root><version number=\"1.0\"/></root>").status(HttpStatus.OK);
        when(consumer.receive(any(), anyLong())).thenReturn(receivedMessage);

        HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
        builder.endpoint(endpoint);

        org.citrusframework.yaml.actions.Receive receive = new org.citrusframework.yaml.actions.Receive(builder);
        org.citrusframework.yaml.actions.Receive.Validate validate = new org.citrusframework.yaml.actions.Receive.Validate();
        validate.setPath("${VERSION_PATH}");
        validate.setValue("2.0");
        receive.getValidate().add(validate);

        ReceiveMessageAction action = receive.build();
        Assertions.assertThrows(ValidationException.class, () -> action.execute(context));
    }
}
