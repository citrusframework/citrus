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

package com.consol.citrus.actions;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class StopTimeActionTest extends AbstractTestNGUnitTest {
	
	@Test
	public void testDefaultTimeline() throws InterruptedException {
		StopTimeAction stopTime = new StopTimeAction();
		
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID), false);
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX), false);

		stopTime.execute(context);
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID ), true);
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX), true);
		Assert.assertEquals(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class), new Long(0L));
		Thread.sleep(100L);
		stopTime.execute(context);
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID), true);
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX), true);
		Assert.assertTrue(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class) >= 100L);
		Thread.sleep(100L);
		stopTime.execute(context);
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID), true);
		Assert.assertEquals(context.getVariables().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX), true);
		Assert.assertTrue(context.getVariable(StopTimeAction.DEFAULT_TIMELINE_ID + StopTimeAction.DEFAULT_TIMELINE_VALUE_SUFFIX, Long.class) >= 200L);
	}
	
	@Test
    public void testCustomTimeline() throws InterruptedException {
        StopTimeAction stopTime = new StopTimeAction();
        
        stopTime.setId("stopMe");
        stopTime.setSuffix("_time");
        Assert.assertEquals(context.getVariables().containsKey("stopMe"), false);
        Assert.assertEquals(context.getVariables().containsKey("stopMe_time"), false);

        stopTime.execute(context);
        Assert.assertEquals(context.getVariables().containsKey("stopMe"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopMe_time"), true);
        Assert.assertEquals(context.getVariable("stopMe_time", Long.class), new Long(0L));

        Thread.sleep(100L);
        stopTime.execute(context);
        Assert.assertEquals(context.getVariables().containsKey("stopMe"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopMe_time"), true);
        Assert.assertTrue(context.getVariable("stopMe_time", Long.class) >= 100L);
        Thread.sleep(100L);
        stopTime.execute(context);
        Assert.assertEquals(context.getVariables().containsKey("stopMe"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopMe_time"), true);
        Assert.assertTrue(context.getVariable("stopMe_time", Long.class) >= 200L);

    }
	
	@Test
    public void testMultipleTimelines() throws InterruptedException {
	    StopTimeAction stopTime1 = new StopTimeAction();
	    StopTimeAction stopTime2 = new StopTimeAction();
        
        stopTime1.setId("stopThem");
        stopTime2.setId("stopUs");
        Assert.assertEquals(context.getVariables().containsKey("stopThem"), false);
        Assert.assertEquals(context.getVariables().containsKey("stopThem_VALUE"), false);
        Assert.assertEquals(context.getVariables().containsKey("stopUs"), false);
        Assert.assertEquals(context.getVariables().containsKey("stopUs_VALUE"), false);

        stopTime1.execute(context);
        Assert.assertEquals(context.getVariables().containsKey("stopThem"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopThem_VALUE"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopUs"), false);
        Assert.assertEquals(context.getVariables().containsKey("stopUs_VALUE"), false);
        Assert.assertEquals(context.getVariable("stopThem_VALUE", Long.class), new Long(0L));

        Thread.sleep(100L);
        stopTime2.execute(context);
        Assert.assertEquals(context.getVariables().containsKey("stopThem"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopThem_VALUE"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopUs"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopUs_VALUE"), true);
        Assert.assertEquals(context.getVariable("stopThem_VALUE", Long.class), new Long(0L));
        Assert.assertEquals(context.getVariable("stopUs_VALUE", Long.class), new Long(0L));
        Thread.sleep(100L);
        stopTime1.execute(context);
        stopTime2.execute(context);
        Assert.assertEquals(context.getVariables().containsKey("stopThem"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopThem_VALUE"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopUs"), true);
        Assert.assertEquals(context.getVariables().containsKey("stopUs_VALUE"), true);
        Assert.assertTrue(context.getVariable("stopThem_VALUE", Long.class) >= 200L);
        Assert.assertTrue(context.getVariable("stopUs_VALUE", Long.class) >= 100L);

    }
}
