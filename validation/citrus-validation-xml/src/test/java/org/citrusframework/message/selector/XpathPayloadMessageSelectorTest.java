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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class XpathPayloadMessageSelectorTest extends UnitTestSupport {

    @Test
    public void testXPathEvaluation() {
        XpathPayloadMessageSelector messageSelector = new XpathPayloadMessageSelector("xpath://Foo/text", "foobar", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Bar><text>foobar</text></Bar>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("This is plain text!")));

        messageSelector = new XpathPayloadMessageSelector("xpath://ns:Foo/ns:text", "foobar", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<ns:Foo xmlns:ns=\"http://citrusframework.org/schema\"><ns:text>foobar</ns:text></ns:Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<ns1:Foo xmlns:ns1=\"http://citrusframework.org/schema\"><ns1:text>foobar</ns1:text></ns1:Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Bar><text>foobar</text></Bar>")));

        messageSelector = new XpathPayloadMessageSelector("xpath://{http://citrusframework.org/schema}Foo/{http://citrusframework.org/schema}text", "foobar", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>")));
        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<ns:Foo xmlns:ns=\"http://citrusframework.org/schema\"><ns:text>foobar</ns:text></ns:Foo>")));
        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<ns1:Foo xmlns:ns1=\"http://citrusframework.org/schema\"><ns1:text>foobar</ns1:text></ns1:Foo>")));

        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/unknown\"><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<ns:Foo xmlns:ns=\"http://citrusframework.org/unknown\"><ns:text>foobar</ns:text></ns:Foo>")));

        messageSelector = new XpathPayloadMessageSelector("xpath://{http://citrusframework.org/schema}Foo/{http://citrusframework.org/schema2}text", "foobar", context);
        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<ns1:Foo xmlns:ns1=\"http://citrusframework.org/schema\" xmlns:ns2=\"http://citrusframework.org/schema2\"><ns2:text>foobar</ns2:text></ns1:Foo>")));

        messageSelector = new XpathPayloadMessageSelector("xpath://ns:Foos/ns:Foo[ns:key='KEY-X']/ns:value", "foo", context);
        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<ns:Foos xmlns:ns=\"http://citrusframework.org/schema\"><ns:Foo><ns:key>KEY-X</ns:key><ns:value>foo</ns:value></ns:Foo><ns:Foo><ns:key>KEY-Y</ns:key><ns:value>bar</ns:value></ns:Foo></ns:Foos>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<ns:Foos xmlns:ns=\"http://citrusframework.org/schema\"><ns:Foo><ns:key>KEY-Z</ns:key><ns:value>foo</ns:value></ns:Foo><ns:Foo><ns:key>KEY-Y</ns:key><ns:value>bar</ns:value></ns:Foo></ns:Foos>")));
    }

    @Test
    public void testXPathEvaluationValidationMatcher() {
        XpathPayloadMessageSelector messageSelector = new XpathPayloadMessageSelector("xpath://Foo/text", "@startsWith(foo)@", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Bar><text>foobar</text></Bar>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("This is plain text!")));
    }

    @Test
    public void testXPathEvaluationWithMessageObjectPayload() {
        XpathPayloadMessageSelector messageSelector = new XpathPayloadMessageSelector("xpath://Foo/text", "foobar", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage(new DefaultMessage("<Foo><text>foobar</text></Foo>"))));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>"))));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage(new DefaultMessage("<Bar><text>foobar</text></Bar>"))));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage(new DefaultMessage("This is plain text!"))));
    }
}
