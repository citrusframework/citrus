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

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class StopTimeActionTest extends AbstractBaseTest {
	
	@Test
	public void testDefaultTimeline() throws InterruptedException {
		StopTimeAction stopTime = new StopTimeAction();
		
		Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID), false);
		
		stopTime.execute(context);
		Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID), true);
		Thread.sleep(100L);
		stopTime.execute(context);
		Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID), true);
		Thread.sleep(100L);
		stopTime.execute(context);
		Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey(StopTimeAction.DEFAULT_TIMELINE_ID), true);
	}
	
	@Test
    public void testCustomTimeline() throws InterruptedException {
        StopTimeAction stopTime = new StopTimeAction();
        
        stopTime.setId("stopMe");
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopMe"), false);
        
        stopTime.execute(context);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopMe"), true);
        Thread.sleep(100L);
        stopTime.execute(context);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopMe"), true);
        Thread.sleep(100L);
        stopTime.execute(context);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopMe"), true);
    }
	
	@Test
    public void testMultipleTimelines() throws InterruptedException {
	    StopTimeAction stopTime1 = new StopTimeAction();
	    StopTimeAction stopTime2 = new StopTimeAction();
        
        stopTime1.setId("stopThem");
        stopTime2.setId("stopUs");
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopThem"), false);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopUs"), false);
        
        stopTime1.execute(context);
        stopTime2.execute(context);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopThem"), true);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopUs"), true);
        Thread.sleep(100L);
        stopTime1.execute(context);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopThem"), true);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopUs"), true);
        Thread.sleep(100L);
        stopTime1.execute(context);
        stopTime2.execute(context);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopThem"), true);
        Assert.assertEquals(StopTimeAction.getTimeStamps().containsKey("stopUs"), true);
    }
}
