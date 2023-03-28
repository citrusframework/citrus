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

package org.citrusframework.condition;

import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.context.TestContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ActionConditionTest {

    private TestContext context = Mockito.mock(TestContext.class);

    @Test
    public void isSatisfiedShouldSucceed() {
        ActionCondition testling = new ActionCondition(new EchoAction.Builder().build());

        Assert.assertTrue(testling.isSatisfied(context));
    }

    @Test
    public void isSatisfiedShouldFail() {
        ActionCondition testling = new ActionCondition(new FailAction.Builder().message("Fail!").build());

        when(context.replaceDynamicContentInString("Fail!")).thenReturn("Fail!");

        Assert.assertFalse(testling.isSatisfied(context));
    }
}
