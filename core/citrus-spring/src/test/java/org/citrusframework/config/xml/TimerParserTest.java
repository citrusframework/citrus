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

package org.citrusframework.config.xml;

import org.citrusframework.container.Timer;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class TimerParserTest extends AbstractActionParserTest<Timer> {

    @Test
    public void testParse() throws Exception {
        assertActionCount(3);
        assertActionClassAndName(Timer.class, "timer");

        Timer action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTimerId(), "timer1");
        Assert.assertEquals(action.getDelay(), 5000L);
        Assert.assertEquals(action.getRepeatCount(), 1);
        Assert.assertEquals(action.getInterval(), 2000L);
        Assert.assertEquals(action.getActionCount(), 1);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTimerId(), "timer2");
        Assert.assertEquals(action.getDelay(), 500L);
        Assert.assertEquals(action.getRepeatCount(), 2);
        Assert.assertEquals(action.getInterval(), 200L);
        Assert.assertEquals(action.getActionCount(), 2);

        long defaultDelay = 0L;
        int defaultRepeat = Integer.MAX_VALUE;
        long defaultInterval = 1000L;

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getTimerId());
        Assert.assertEquals(action.getDelay(), defaultDelay);
        Assert.assertEquals(action.getRepeatCount(), defaultRepeat);
        Assert.assertEquals(action.getInterval(), defaultInterval);
        Assert.assertEquals(action.getActionCount(), 1);
    }
}
