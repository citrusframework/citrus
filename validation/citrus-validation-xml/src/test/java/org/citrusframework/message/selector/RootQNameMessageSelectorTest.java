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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class RootQNameMessageSelectorTest extends UnitTestSupport {

    @Test
    public void testQNameSelector() {
        RootQNameMessageSelector messageSelector = new RootQNameMessageSelector(RootQNameMessageSelector.SELECTOR_ID, "Foo", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo><text>foobar</text></Foo>")));
        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Bar><text>foobar</text></Bar>")));

        messageSelector = new RootQNameMessageSelector(RootQNameMessageSelector.SELECTOR_ID,"{}Foo", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo><text>foobar</text></Foo>")));
        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Bar><text>foobar</text></Bar>")));
    }

    @Test
    public void testQNameSelectorWithMessageObjectPayload() {
        RootQNameMessageSelector messageSelector = new RootQNameMessageSelector(RootQNameMessageSelector.SELECTOR_ID,"Foo", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage(new DefaultMessage("<Foo><text>foobar</text></Foo>"))));
        Assert.assertTrue(messageSelector.accept(new DefaultMessage(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>"))));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage(new DefaultMessage("<Bar><text>foobar</text></Bar>"))));
    }

    @Test
    public void testQNameWithNamespaceSelector() {
        RootQNameMessageSelector messageSelector = new RootQNameMessageSelector(RootQNameMessageSelector.SELECTOR_ID,"{http://citrusframework.org/schema}Foo", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Foo><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema/foo\"><text>foobar</text></Foo>")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("<Bar xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Bar>")));
    }

    @Test
    public void testNonXmlPayload() {
        RootQNameMessageSelector messageSelector = new RootQNameMessageSelector(RootQNameMessageSelector.SELECTOR_ID,"{http://citrusframework.org/schema}Foo", context);

        Assert.assertFalse(messageSelector.accept(new DefaultMessage("PLAINTEXT")));
    }

    @Test
    public void testInvalidQName() {
        try {
            new RootQNameMessageSelector(RootQNameMessageSelector.SELECTOR_ID,"{http://citrusframework.org/schemaFoo", context);
            Assert.fail("Missing exception due to invalid QName");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid root QName"));
        }
    }

}
