/*
 * Copyright the original author or authors.
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

package org.citrusframework.container;

import org.citrusframework.TestAction;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.container.HamcrestConditionExpression.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RepeatUntilTrueTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testRepeatHamcrestConditionExpression() {
        reset(action);

        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue.Builder()
                .condition(assertThat(is(5)))
                .index("i")
                .actions(() -> action)
                .build();
        repeatUntilTrue.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "4");

        verify(action, times(4)).execute(context);
    }
}
