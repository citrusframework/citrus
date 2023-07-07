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

import java.util.concurrent.TimeUnit;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.SleepAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.SleepAction.Builder.sleep;

/**
 * @author Christoph Deppisch
 */
public class SleepTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testSleepBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(sleep().milliseconds(200));
        builder.$(sleep().milliseconds(150));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SleepAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SleepAction.class);

        SleepAction action = (SleepAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "sleep");
        Assert.assertEquals(action.getTime(), "200");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.MILLISECONDS);

        action = (SleepAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "sleep");
        Assert.assertEquals(action.getTime(), "150");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.MILLISECONDS);
    }
}
