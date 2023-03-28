/*
 * Copyright 2006-2010 the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class StopTimeActionTest extends UnitTestSupport {

	@Test
	public void testDefaultTimeline() throws InterruptedException {
		StopTimeAction stopTime = new StopTimeAction.Builder()
                .build();

        Assert.assertFalse(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assert.assertFalse(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));

		stopTime.execute(context);
        Assert.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assert.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));
		Assert.assertEquals(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class), new Long(0L));
		Thread.sleep(100L);
		stopTime.execute(context);
        Assert.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assert.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));
		Assert.assertTrue(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class) >= 100L);
		Thread.sleep(100L);
		stopTime.execute(context);
        Assert.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID));
        Assert.assertTrue(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX));
		Assert.assertTrue(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class) >= 200L);
	}

	@Test
    public void testCustomTimeline() throws InterruptedException {
        StopTimeAction stopTime = new StopTimeAction.Builder()
                .id("stopMe")
                .suffix("_time")
                .build();

        Assert.assertFalse(context.getVariables().containsKey("stopMe"));
        Assert.assertFalse(context.getVariables().containsKey("stopMe_time"));

        stopTime.execute(context);
        Assert.assertTrue(context.getVariables().containsKey("stopMe"));
        Assert.assertTrue(context.getVariables().containsKey("stopMe_time"));
        Assert.assertEquals(context.getVariable("stopMe_time", Long.class), new Long(0L));

        Thread.sleep(100L);
        stopTime.execute(context);
        Assert.assertTrue(context.getVariables().containsKey("stopMe"));
        Assert.assertTrue(context.getVariables().containsKey("stopMe_time"));
        Assert.assertTrue(context.getVariable("stopMe_time", Long.class) >= 100L);
        Thread.sleep(100L);
        stopTime.execute(context);
        Assert.assertTrue(context.getVariables().containsKey("stopMe"));
        Assert.assertTrue(context.getVariables().containsKey("stopMe_time"));
        Assert.assertTrue(context.getVariable("stopMe_time", Long.class) >= 200L);
    }

	@Test
    public void testMultipleTimelines() throws InterruptedException {
        StopTimeAction stopTime1 = new StopTimeAction.Builder()
                .id("stopThem")
                .build();
        StopTimeAction stopTime2 = new StopTimeAction.Builder()
                .id("stopUs")
                .build();

        Assert.assertFalse(context.getVariables().containsKey("stopThem"));
        Assert.assertFalse(context.getVariables().containsKey("stopThem_VALUE"));
        Assert.assertFalse(context.getVariables().containsKey("stopUs"));
        Assert.assertFalse(context.getVariables().containsKey("stopUs_VALUE"));

        stopTime1.execute(context);
        Assert.assertTrue(context.getVariables().containsKey("stopThem"));
        Assert.assertTrue(context.getVariables().containsKey("stopThem_VALUE"));
        Assert.assertFalse(context.getVariables().containsKey("stopUs"));
        Assert.assertFalse(context.getVariables().containsKey("stopUs_VALUE"));
        Assert.assertEquals(context.getVariable("stopThem_VALUE", Long.class), new Long(0L));

        Thread.sleep(100L);
        stopTime2.execute(context);
        Assert.assertTrue(context.getVariables().containsKey("stopThem"));
        Assert.assertTrue(context.getVariables().containsKey("stopThem_VALUE"));
        Assert.assertTrue(context.getVariables().containsKey("stopUs"));
        Assert.assertTrue(context.getVariables().containsKey("stopUs_VALUE"));
        Assert.assertEquals(context.getVariable("stopThem_VALUE", Long.class), new Long(0L));
        Assert.assertEquals(context.getVariable("stopUs_VALUE", Long.class), new Long(0L));
        Thread.sleep(100L);
        stopTime1.execute(context);
        stopTime2.execute(context);
        Assert.assertTrue(context.getVariables().containsKey("stopThem"));
        Assert.assertTrue(context.getVariables().containsKey("stopThem_VALUE"));
        Assert.assertTrue(context.getVariables().containsKey("stopUs"));
        Assert.assertTrue(context.getVariables().containsKey("stopUs_VALUE"));
        Assert.assertTrue(context.getVariable("stopThem_VALUE", Long.class) >= 200L);
        Assert.assertTrue(context.getVariable("stopUs_VALUE", Long.class) >= 100L);
    }
}
