/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;

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
