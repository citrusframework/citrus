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

package org.citrusframework.actions;

import org.citrusframework.base.UnitTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StopTimeActionTest extends UnitTestSupport {

	@Test
	public void testDefaultTimeline() throws InterruptedException {
		StopTimeAction stopTime = new StopTimeAction.Builder()
                .build();

        Assertions.assertFalse(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assertions.assertFalse(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));

		stopTime.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assertions.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));
		Assertions.assertEquals(0L, context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class));
		Thread.sleep(100L);
		stopTime.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assertions.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));
		Assertions.assertTrue(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class) >= 100L);
		Thread.sleep(100L);
		stopTime.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assertions.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));
		Assertions.assertTrue(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class) >= 200L);
	}

	@Test
    public void testCustomTimeline() throws InterruptedException {
        StopTimeAction stopTime = new StopTimeAction.Builder()
                .id("stopMe")
                .suffix("_time")
                .build();

        Assertions.assertFalse(context.getVariables().containsKey("stopMe"));
        Assertions.assertFalse(context.getVariables().containsKey("stopMe_time"));

        stopTime.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey("stopMe"));
        Assertions.assertTrue(context.getVariables().containsKey("stopMe_time"));
        Assertions.assertEquals(0L, context.getVariable("stopMe_time", Long.class));

        Thread.sleep(100L);
        stopTime.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey("stopMe"));
        Assertions.assertTrue(context.getVariables().containsKey("stopMe_time"));
        Assertions.assertTrue(context.getVariable("stopMe_time", Long.class) >= 100L);
        Thread.sleep(100L);
        stopTime.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey("stopMe"));
        Assertions.assertTrue(context.getVariables().containsKey("stopMe_time"));
        Assertions.assertTrue(context.getVariable("stopMe_time", Long.class) >= 200L);
    }

	@Test
    public void testMultipleTimelines() throws InterruptedException {
        StopTimeAction stopTime1 = new StopTimeAction.Builder()
                .id("stopThem")
                .build();
        StopTimeAction stopTime2 = new StopTimeAction.Builder()
                .id("stopUs")
                .build();

        Assertions.assertFalse(context.getVariables().containsKey("stopThem"));
        Assertions.assertFalse(context.getVariables().containsKey("stopThem_VALUE"));
        Assertions.assertFalse(context.getVariables().containsKey("stopUs"));
        Assertions.assertFalse(context.getVariables().containsKey("stopUs_VALUE"));

        stopTime1.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey("stopThem"));
        Assertions.assertTrue(context.getVariables().containsKey("stopThem_VALUE"));
        Assertions.assertFalse(context.getVariables().containsKey("stopUs"));
        Assertions.assertFalse(context.getVariables().containsKey("stopUs_VALUE"));
        Assertions.assertEquals(0L, context.getVariable("stopThem_VALUE", Long.class));

        Thread.sleep(100L);
        stopTime2.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey("stopThem"));
        Assertions.assertTrue(context.getVariables().containsKey("stopThem_VALUE"));
        Assertions.assertTrue(context.getVariables().containsKey("stopUs"));
        Assertions.assertTrue(context.getVariables().containsKey("stopUs_VALUE"));
        Assertions.assertEquals(0L, context.getVariable("stopThem_VALUE", Long.class));
        Assertions.assertEquals(0L, context.getVariable("stopUs_VALUE", Long.class));
        Thread.sleep(100L);
        stopTime1.execute(context);
        stopTime2.execute(context);
        Assertions.assertTrue(context.getVariables().containsKey("stopThem"));
        Assertions.assertTrue(context.getVariables().containsKey("stopThem_VALUE"));
        Assertions.assertTrue(context.getVariables().containsKey("stopUs"));
        Assertions.assertTrue(context.getVariables().containsKey("stopUs_VALUE"));
        Assertions.assertTrue(context.getVariable("stopThem_VALUE", Long.class) >= 200L);
        Assertions.assertTrue(context.getVariable("stopUs_VALUE", Long.class) >= 100L);
    }
}
