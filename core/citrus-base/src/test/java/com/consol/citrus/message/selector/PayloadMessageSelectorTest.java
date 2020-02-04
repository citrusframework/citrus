/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.message.selector;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.validation.matcher.ValidationMatcherLibrary;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PayloadMessageSelectorTest {

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testPayloadEvaluation() {
        PayloadMatchingMessageSelector messageSelector = new PayloadMatchingMessageSelector("payload", "foobar", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage("foobar")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("barfoo")));
    }

    @Test
    public void testPayloadEvaluationValidationMatcher() {
        PayloadMatchingMessageSelector messageSelector = new PayloadMatchingMessageSelector("payload", "@startsWith(foo)@", context);

        ValidationMatcherLibrary library = new ValidationMatcherLibrary();
        library.getMembers().put("startsWith", (fieldName, value, controlParameters, context) -> {
            if (!value.startsWith(controlParameters.get(0))) {
                throw new ValidationException("Not starting with " + controlParameters.get(0));
            }
        });
        context.getValidationMatcherRegistry().getValidationMatcherLibraries().add(library);
        Assert.assertTrue(messageSelector.accept(new DefaultMessage("foobar")));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage("barfoo")));
    }

    @Test
    public void testPayloadEvaluationWithMessageObjectPayload() {
        PayloadMatchingMessageSelector messageSelector = new PayloadMatchingMessageSelector("payload", "foobar", context);

        Assert.assertTrue(messageSelector.accept(new DefaultMessage(new DefaultMessage("foobar"))));
        Assert.assertFalse(messageSelector.accept(new DefaultMessage(new DefaultMessage("barfoo"))));
    }
}
