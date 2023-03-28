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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DelegatingMessageSelectorTest extends UnitTestSupport {

    private ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testRootQNameDelegation() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("foo = 'bar' AND root-qname = 'FooTest'", context);

        Message acceptMessage = new DefaultMessage("<FooTest><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("<BarTest><text>foobar</text></BarTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DelegatingMessageSelector("root-qname = 'FooTest'", context);

        acceptMessage = new DefaultMessage("<FooTest><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        declineMessage = new DefaultMessage("<BarTest><text>foobar</text></BarTest>")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testRootQNameDelegationWithNamespace() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("root-qname = '{http://citrusframework.org/fooschema}FooTest'", context);

        Message acceptMessage = new DefaultMessage("<FooTest xmlns=\"http://citrusframework.org/fooschema\"><text>foo</text></FooTest>")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("<FooTest xmlns=\"http://citrusframework.org/barschema\"><text>bar</text></FooTest>")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testXPathEvaluationDelegation() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("foo = 'bar' AND root-qname = 'FooTest' AND xpath://FooTest/text = 'foobar'", context);

        Message acceptMessage = new DefaultMessage("<FooTest><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("<FooTest><text>barfoo</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DelegatingMessageSelector("xpath://FooTest/text = 'foobar'", context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testXPathEvaluationDelegationWithNamespaceBuilder() {
        NamespaceContextBuilder nsContextBuilder = new NamespaceContextBuilder();
        nsContextBuilder.getNamespaceMappings().put("foo", "http://citrusframework.org/foo");

        context.setNamespaceContextBuilder(nsContextBuilder);

        reset(resolver);

        when(resolver.resolve(NamespaceContextBuilder.class)).thenReturn(nsContextBuilder);

        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("foo = 'bar' AND root-qname = 'FooTest' AND xpath://foo:FooTest/foo:text = 'foobar'", context);

        Message acceptMessage = new DefaultMessage("<FooTest xmlns=\"http://citrusframework.org/foo\"><text>foobar</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("<FooTest><text>barfoo</text></FooTest>")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DelegatingMessageSelector("xpath://foo:FooTest/foo:text = 'foobar'", context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

    }
}
