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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DelegatingMessageSelectorTest extends UnitTestSupport {

    @Test
    public void testJsonPathEvaluationDelegation() {
        DelegatingMessageSelector messageSelector = new DelegatingMessageSelector("foo = 'bar' AND jsonPath:$.foo.text = 'foobar'", context);

        Message acceptMessage = new DefaultMessage("{ \"foo\": { \"text\": \"foobar\"} }")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("{ \"foo\": { \"text\": \"barfoo\"} }")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new DelegatingMessageSelector("jsonPath:$.foo.text = 'foobar'", context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }
}
