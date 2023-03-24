/*
 * Copyright 2006-2012 the original author or authors.
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
package org.citrusframework.channel.selector;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class DispatchingMessageSelectorTest extends UnitTestSupport {

    private BeanFactory beanFactory = Mockito.mock(BeanFactory.class);

    @BeforeMethod
    public void setupMock() {
        reset(beanFactory);
        doThrow(new NoSuchBeanDefinitionException(NamespaceContextBuilder.class)).when(beanFactory).getBean(NamespaceContextBuilder.class);
    }

    @Test
    public void testHeaderMatchingSelector() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("operation = 'foo'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "foobar")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorAndOperation() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("foo = 'bar' AND operation = 'foo'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testPayloadMatchingDelegation() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("foo = 'bar' AND payload = 'FooTest'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("BarTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DispatchingMessageSelector("payload = 'FooTest'", beanFactory, context);

        acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        declineMessage = MessageBuilder.withPayload("BarTest")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testPayloadAndHeaderMatchingDelegation() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("header:payload = 'foo' AND payload = 'foo'", beanFactory, context);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("foo")
                .setHeader("payload", "foo")
                .build()));

        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("foo")
                .setHeader("payload", "bar")
                .build()));

        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("bar")
                .setHeader("payload", "foo")
                .build()));
    }

    @Test
    public void testRootQNameDelegation() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("foo = 'bar' AND root-qname = 'FooTest'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("<FooTest><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("<BarTest><text>foobar</text></BarTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DispatchingMessageSelector("root-qname = 'FooTest'", beanFactory, context);

        acceptMessage = MessageBuilder.withPayload("<FooTest><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        declineMessage = MessageBuilder.withPayload("<BarTest><text>foobar</text></BarTest>")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testRootQNameDelegationWithNamespace() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("root-qname = '{http://citrusframework.org/fooschema}FooTest'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("<FooTest xmlns=\"http://citrusframework.org/fooschema\"><text>foo</text></FooTest>")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("<FooTest xmlns=\"http://citrusframework.org/barschema\"><text>bar</text></FooTest>")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testXPathEvaluationDelegation() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("foo = 'bar' AND root-qname = 'FooTest' AND xpath://FooTest/text = 'foobar'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("<FooTest><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("<FooTest><text>barfoo</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DispatchingMessageSelector("xpath://FooTest/text = 'foobar'", beanFactory, context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testXPathEvaluationDelegationWithNamespaceBuilder() {
        NamespaceContextBuilder nsContextBuilder = new NamespaceContextBuilder();
        nsContextBuilder.getNamespaceMappings().put("foo", "http://citrusframework.org/foo");

        context.setNamespaceContextBuilder(nsContextBuilder);

        reset(beanFactory);

        when(beanFactory.getBean(NamespaceContextBuilder.class)).thenReturn(nsContextBuilder);

        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("foo = 'bar' AND root-qname = 'FooTest' AND xpath://foo:FooTest/foo:text = 'foobar'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("<FooTest xmlns=\"http://citrusframework.org/foo\"><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("<FooTest><text>barfoo</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DispatchingMessageSelector("xpath://foo:FooTest/foo:text = 'foobar'", beanFactory, context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

    }

    @Test
    public void testJsonPathEvaluationDelegation() {
        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("foo = 'bar' AND jsonPath:$.foo.text = 'foobar'", beanFactory, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("{ \"foo\": { \"text\": \"foobar\"} }")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("{ \"foo\": { \"text\": \"barfoo\"} }")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DispatchingMessageSelector("jsonPath:$.foo.text = 'foobar'", beanFactory, context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testCustomMessageSelectorDelegation() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

        Map<String, MessageSelectorFactory> factories = new HashMap<>();
        factories.put("customSelectorFactory", new MessageSelectorFactory() {
            @Override
            public boolean supports(String key) {
                return key.startsWith("x:");
            }

            @Override
            public MessageSelector create(String key, String value, TestContext context) {
                return message -> message.getHeaders().get("foo").equals(value);
            }
        });

        when(applicationContext.getBeansOfType(MessageSelectorFactory.class)).thenReturn(factories);

        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("x:foo = 'bar'", applicationContext, context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooBar")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("FooBar")
                .setHeader("foo", "bars")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }
}
