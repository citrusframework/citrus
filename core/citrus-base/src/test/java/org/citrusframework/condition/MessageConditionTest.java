/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.condition;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageStore;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Martin Maher
 * @since 2.6.2
 */
public class MessageConditionTest {

    private TestContext context = Mockito.mock(TestContext.class);
    private MessageStore messageStore = Mockito.mock(MessageStore.class);

    @Test
    public void isSatisfiedShouldSucceed() {
        String messageName = "request";

        MessageCondition testling = new MessageCondition();
        testling.setMessageName(messageName);

        reset(context);
        when(context.replaceDynamicContentInString(messageName)).thenReturn(messageName);
        when(context.getMessageStore()).thenReturn(messageStore);
        when(messageStore.getMessage(messageName)).thenReturn(new DefaultMessage("OK"));
        Assert.assertTrue(testling.isSatisfied(context));
    }

    @Test
    public void isSatisfiedShouldFail() {
        String messageName = "request";

        MessageCondition testling = new MessageCondition();
        testling.setMessageName(messageName);

        reset(context);
        when(context.replaceDynamicContentInString(messageName)).thenReturn(messageName);
        when(context.getMessageStore()).thenReturn(messageStore);
        when(messageStore.getMessage(messageName)).thenReturn(null);
        Assert.assertFalse(testling.isSatisfied(context));
    }
}
