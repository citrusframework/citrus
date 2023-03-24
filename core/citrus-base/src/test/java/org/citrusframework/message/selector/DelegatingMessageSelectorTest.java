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
package org.citrusframework.message.selector;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageSelector;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DelegatingMessageSelectorTest extends UnitTestSupport {

    private ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testHeaderMatchingSelector() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("operation = 'foo'", context);

        Message acceptMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "foobar");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorAndOperation() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("foo = 'bar' AND operation = 'foo'", context);

        Message acceptMessage = new DefaultMessage("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testPayloadMatchingDelegation() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("foo = 'bar' AND payload = 'FooTest'", context);

        Message acceptMessage = new DefaultMessage("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("BarTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DelegatingMessageSelector("payload = 'FooTest'", context);

        acceptMessage = new DefaultMessage("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        declineMessage = new DefaultMessage("BarTest")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testPayloadAndHeaderMatchingDelegation() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("header:payload = 'foo' AND payload = 'foo'", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("foo")
                .setHeader("payload", "foo")));

        Assert.assertFalse(messageSelector.accept(new DefaultMessage("foo")
                .setHeader("payload", "bar")));

        Assert.assertFalse(messageSelector.accept(new DefaultMessage("bar")
                .setHeader("payload", "foo")));
    }

    @Test
    public void testCustomMessageSelectorDelegation() {
        Map<String, MessageSelector.MessageSelectorFactory> factories = new HashMap<>();
        factories.put("customSelectorFactory", new MessageSelector.MessageSelectorFactory() {
            @Override
            public boolean supports(String key) {
                return key.startsWith("x:");
            }

            @Override
            public MessageSelector create(String key, String value, TestContext context) {
                return message -> message.getHeaders().get("foo").equals(value);
            }
        });

        when(resolver.resolveAll(MessageSelector.MessageSelectorFactory.class)).thenReturn(factories);

        context.setReferenceResolver(resolver);
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("x:foo = 'bar'", context);

        Message acceptMessage = new DefaultMessage("FooBar")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("FooBar")
                .setHeader("foo", "bars")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }
}
