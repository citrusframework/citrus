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

package org.citrusframework.groovy.dsl.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.container.Timer;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.dsl.AbstractGroovyActionDslTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimerTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadTimer() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/container/timer.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("TimerTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());

        Assertions.assertEquals(Timer.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        Timer action = (Timer) result.getTestAction(actionIndex++);
        Assertions.assertEquals("timer1", action.getTimerId());
        Assertions.assertEquals(5000L, action.getDelay());
        Assertions.assertEquals(1, action.getRepeatCount());
        Assertions.assertEquals(2000L, action.getInterval());
        Assertions.assertEquals(1, action.getActionCount());

        action = (Timer) result.getTestAction(actionIndex++);
        Assertions.assertEquals("timer2", action.getTimerId());
        Assertions.assertEquals(500L, action.getDelay());
        Assertions.assertEquals(2, action.getRepeatCount());
        Assertions.assertEquals(200L, action.getInterval());
        Assertions.assertEquals(2, action.getActionCount());

        long defaultDelay = 0L;
        int defaultRepeat = Integer.MAX_VALUE;
        long defaultInterval = 1000L;

        action = (Timer) result.getTestAction(actionIndex);
        Assertions.assertNotNull(action.getTimerId());
        Assertions.assertEquals(defaultDelay, action.getDelay());
        Assertions.assertEquals(defaultRepeat, action.getRepeatCount());
        Assertions.assertEquals(defaultInterval, action.getInterval());
        Assertions.assertEquals(1, action.getActionCount());
    }
}
