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

package org.citrusframework.spring.config.xml;

import org.citrusframework.container.Timer;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.5
 */
public class TimerParserTest extends AbstractActionParserTest<Timer> {

    @Test
    public void testParse() {
        assertActionCount(3);
        assertActionClassAndName(Timer.class, "timer");

        Timer action = getNextTestActionFromTest();
        Assertions.assertEquals("timer1", action.getTimerId());
        Assertions.assertEquals(5000L, action.getDelay());
        Assertions.assertEquals(1, action.getRepeatCount());
        Assertions.assertEquals(2000L, action.getInterval());
        Assertions.assertEquals(1, action.getActionCount());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("timer2", action.getTimerId());
        Assertions.assertEquals(500L, action.getDelay());
        Assertions.assertEquals(2, action.getRepeatCount());
        Assertions.assertEquals(200L, action.getInterval());
        Assertions.assertEquals(2, action.getActionCount());

        long defaultDelay = 0L;
        int defaultRepeat = Integer.MAX_VALUE;
        long defaultInterval = 1000L;

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getTimerId());
        Assertions.assertEquals(defaultDelay, action.getDelay());
        Assertions.assertEquals(defaultRepeat, action.getRepeatCount());
        Assertions.assertEquals(defaultInterval, action.getInterval());
        Assertions.assertEquals(1, action.getActionCount());
    }
}
