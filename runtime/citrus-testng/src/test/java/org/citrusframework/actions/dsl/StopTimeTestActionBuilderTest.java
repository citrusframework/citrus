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
import org.citrusframework.actions.StopTimeAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.actions.StopTimeAction.Builder.stopTime;

public class StopTimeTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testStopTimeBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(stopTime());
        builder.$(stopTime("timerId"));

        builder.$(sleep().milliseconds(200));

        builder.$(stopTime());
        builder.$(stopTime("timerId"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 5);
        Assert.assertEquals(test.getActions().get(0).getClass(), StopTimeAction.class);

        StopTimeAction action = (StopTimeAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "stop-time");
        Assert.assertEquals(action.getId(), "CITRUS_TIMELINE");

        action = (StopTimeAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "stop-time");
        Assert.assertEquals(action.getId(), "timerId");
    }
}
