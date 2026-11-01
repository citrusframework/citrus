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

package org.citrusframework.validation.groovy.actions.dsl;

import java.util.HashMap;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resources;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.citrusframework.validation.groovy.GroovyJsonMessageValidator;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ReceiveMessageActionBuilderTest extends AbstractUnitTest implements TestActionSupport {

    private final Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private final Consumer messageConsumer = Mockito.mock(Consumer.class);
    private final EndpointConfiguration configuration = Mockito.mock(EndpointConfiguration.class);
    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testReceiveBuilderWithValidationScript() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .validate(validation().groovy().script("assert json.message == 'Hello Citrus!'"))
                                .validator("groovyMessageValidator"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(MessageType.JSON.name(), action.getMessageType());
        Assertions.assertEquals(1L, action.getValidators().size());
        Assertions.assertEquals(validator, action.getValidators().get(0));

        Assertions.assertEquals(2L, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultScriptValidationContext.class::isInstance));

        DefaultScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(DefaultScriptValidationContext.class::isInstance).findFirst()
                .map(DefaultScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertEquals(ScriptTypes.GROOVY, validationContext.getScriptType());
        Assertions.assertEquals("assert json.message == 'Hello Citrus!'", validationContext.getValidationScript().trim());
        Assertions.assertNull(validationContext.getValidationScriptResourcePath());

    }

    @Test
    public void testReceiveBuilderWithValidationScriptResourcePath() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .validate(validation().groovy().scriptResource("classpath:org/citrusframework/validation/groovy/actions/dsl/validation.groovy"))
                                .validator("groovyMessageValidator"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(MessageType.JSON.name(), action.getMessageType());
        Assertions.assertEquals(1L, action.getValidators().size());
        Assertions.assertEquals(validator, action.getValidators().get(0));

        Assertions.assertEquals(2L, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultScriptValidationContext.class::isInstance));

        DefaultScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(DefaultScriptValidationContext.class::isInstance).findFirst()
                .map(DefaultScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertEquals(ScriptTypes.GROOVY, validationContext.getScriptType());
        Assertions.assertEquals("", validationContext.getValidationScript());
        Assertions.assertEquals("classpath:org/citrusframework/validation/groovy/actions/dsl/validation.groovy", validationContext.getValidationScriptResourcePath());
    }

    @Test
    public void testReceiveBuilderWithValidationScriptResource() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .validate(validation().groovy().script(Resources.fromClasspath("org/citrusframework/validation/groovy/actions/dsl/validation.groovy")))
                                .validator("groovyMessageValidator"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(MessageType.JSON.name(), action.getMessageType());
        Assertions.assertEquals(1L, action.getValidators().size());
        Assertions.assertEquals(validator, action.getValidators().get(0));

        Assertions.assertEquals(2L, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultScriptValidationContext.class::isInstance));

        DefaultScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(DefaultScriptValidationContext.class::isInstance).findFirst()
                .map(DefaultScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertEquals(ScriptTypes.GROOVY, validationContext.getScriptType());
        Assertions.assertEquals("assert json.message == 'Hello Citrus!'", validationContext.getValidationScript().trim());
        Assertions.assertNull(validationContext.getValidationScriptResourcePath());
    }

    @Test
    public void testReceiveBuilderWithValidationScriptAndHeader() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .type(MessageType.JSON)
                                .validate(validation().groovy().script("assert json.message == 'Hello Citrus!'"))
                                .validator("groovyMessageValidator")
                                .header("operation", "sayHello"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(MessageType.JSON.name(), action.getMessageType());
        Assertions.assertEquals(1L, action.getValidators().size());
        Assertions.assertEquals(validator, action.getValidators().get(0));

        Assertions.assertEquals(2L, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultScriptValidationContext.class::isInstance));

        DefaultScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(DefaultScriptValidationContext.class::isInstance).findFirst()
                .map(DefaultScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertEquals(ScriptTypes.GROOVY, validationContext.getScriptType());
        Assertions.assertEquals("assert json.message == 'Hello Citrus!'", validationContext.getValidationScript().trim());
        Assertions.assertNull(validationContext.getValidationScriptResourcePath());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("", ((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

    }
}
