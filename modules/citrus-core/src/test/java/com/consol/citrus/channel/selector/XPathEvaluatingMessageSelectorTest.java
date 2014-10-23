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

package com.consol.citrus.channel.selector;

import com.consol.citrus.message.DefaultMessage;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.xml.namespace.NamespaceContextBuilder;

/**
 * @author Christoph Deppisch
 */
public class XPathEvaluatingMessageSelectorTest {

    private NamespaceContextBuilder nsContextBuilder = new NamespaceContextBuilder();
    
    @Test
    public void testXPathEvaluation() {
        XPathEvaluatingMessageSelector messageSelector = new XPathEvaluatingMessageSelector("xpath://Foo/text", "foobar", nsContextBuilder);
        
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<Foo><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Bar><text>foobar</text></Bar>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("This is plain text!").build()));
        
        messageSelector = new XPathEvaluatingMessageSelector("xpath://ns:Foo/ns:text", "foobar", nsContextBuilder);
        
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<ns:Foo xmlns:ns=\"http://citrusframework.org/schema\"><ns:text>foobar</ns:text></ns:Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<ns1:Foo xmlns:ns1=\"http://citrusframework.org/schema\"><ns1:text>foobar</ns1:text></ns1:Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Bar><text>foobar</text></Bar>").build()));
        
        messageSelector = new XPathEvaluatingMessageSelector("xpath://{http://citrusframework.org/schema}Foo/{http://citrusframework.org/schema}text", "foobar", nsContextBuilder);
        
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>").build()));
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<ns:Foo xmlns:ns=\"http://citrusframework.org/schema\"><ns:text>foobar</ns:text></ns:Foo>").build()));
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<ns1:Foo xmlns:ns1=\"http://citrusframework.org/schema\"><ns1:text>foobar</ns1:text></ns1:Foo>").build()));
        
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Foo xmlns=\"http://citrusframework.org/unknown\"><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<ns:Foo xmlns:ns=\"http://citrusframework.org/unknown\"><ns:text>foobar</ns:text></ns:Foo>").build()));
        
        messageSelector = new XPathEvaluatingMessageSelector("xpath://{http://citrusframework.org/schema}Foo/{http://citrusframework.org/schema2}text", "foobar", nsContextBuilder);
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<ns1:Foo xmlns:ns1=\"http://citrusframework.org/schema\" xmlns:ns2=\"http://citrusframework.org/schema2\"><ns2:text>foobar</ns2:text></ns1:Foo>").build()));
        
        messageSelector = new XPathEvaluatingMessageSelector("xpath://ns:Foos/ns:Foo[ns:key='KEY-X']/ns:value", "foo", nsContextBuilder);
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<ns:Foos xmlns:ns=\"http://citrusframework.org/schema\"><ns:Foo><ns:key>KEY-X</ns:key><ns:value>foo</ns:value></ns:Foo><ns:Foo><ns:key>KEY-Y</ns:key><ns:value>bar</ns:value></ns:Foo></ns:Foos>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<ns:Foos xmlns:ns=\"http://citrusframework.org/schema\"><ns:Foo><ns:key>KEY-Z</ns:key><ns:value>foo</ns:value></ns:Foo><ns:Foo><ns:key>KEY-Y</ns:key><ns:value>bar</ns:value></ns:Foo></ns:Foos>").build()));
    }

    @Test
    public void testXPathEvaluationWithMessageObjectPayload() {
        XPathEvaluatingMessageSelector messageSelector = new XPathEvaluatingMessageSelector("xpath://Foo/text", "foobar", nsContextBuilder);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("<Foo><text>foobar</text></Foo>")).build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>")).build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("<Bar><text>foobar</text></Bar>")).build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("This is plain text!")).build()));
    }
}
