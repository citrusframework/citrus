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

package org.citrusframework.citrus.actions.dsl;

import java.io.IOException;
import java.util.HashMap;

import org.citrusframework.citrus.DefaultTestCaseRunner;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.ReceiveMessageAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.EndpointConfiguration;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.messaging.Consumer;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.script.ScriptTypes;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.testng.AbstractTestNGUnitTest;
import org.citrusframework.citrus.validation.builder.DefaultMessageBuilder;
import org.citrusframework.citrus.validation.context.HeaderValidationContext;
import org.citrusframework.citrus.validation.script.GroovyJsonMessageValidator;
import org.citrusframework.citrus.validation.script.ScriptValidationContext;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.citrus.validation.script.ScriptValidationContext.Builder.groovy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionBuilderTest extends AbstractTestNGUnitTest {

    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Consumer messageConsumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration configuration = Mockito.mock(EndpointConfiguration.class);
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

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
                                .validate(groovy().script("assert json.message == 'Hello Citrus!'"))
                                .validator("groovyMessageValidator"));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 2L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript().trim(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());

    }

    @Test
    public void testReceiveBuilderWithValidationScriptResourcePath() throws IOException {
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
                                .validate(groovy().scriptResource("classpath:org/citrusframework/citrus/actions/dsl/validation.groovy"))
                                .validator("groovyMessageValidator"));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 2L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "");
        Assert.assertEquals(validationContext.getValidationScriptResourcePath(), "classpath:org/citrusframework/citrus/actions/dsl/validation.groovy");
    }

    @Test
    public void testReceiveBuilderWithValidationScriptResource() throws IOException {
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
                                .validate(groovy().script(new ClassPathResource("org/citrusframework/citrus/actions/dsl/validation.groovy")))
                                .validator("groovyMessageValidator"));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 2L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript().trim(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());
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
                                .validate(groovy().script("assert json.message == 'Hello Citrus!'"))
                                .validator("groovyMessageValidator")
                                .header("operation", "sayHello"));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 2L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript().trim(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

    }
}