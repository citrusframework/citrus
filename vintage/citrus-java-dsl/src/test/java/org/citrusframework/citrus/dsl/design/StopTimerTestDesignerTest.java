/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.dsl.design;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.StopTimerAction;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class StopTimerTestDesignerTest extends UnitTestSupport {

    @Test
    public void testStopTimerBuilder() {
        final String timerId = "timer1";

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                stopTimer(timerId);
                stopTimers();
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), StopTimerAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), StopTimerAction.class);

        StopTimerAction action = (StopTimerAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "stop-timer");
        Assert.assertEquals(action.getTimerId(), timerId);

        action = (StopTimerAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "stop-timer");
        Assert.assertNull(action.getTimerId());
    }
}
