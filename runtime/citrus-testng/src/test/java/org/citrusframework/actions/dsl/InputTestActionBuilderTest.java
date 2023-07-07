/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.InputAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.InputAction.Builder.input;

public class InputTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void TestInputBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("answer", "yes");

        builder.$(input().message("Want to test me?")
                .result("answer")
                .answers("yes", "no", "maybe"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), InputAction.class);

        InputAction action = (InputAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "input");
        Assert.assertEquals(action.getMessage(), "Want to test me?");
        Assert.assertEquals(action.getValidAnswers(), "yes/no/maybe");
        Assert.assertEquals(action.getVariable(), "answer");
    }
}
