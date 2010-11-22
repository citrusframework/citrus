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

import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class SleepActionTest extends AbstractBaseTest {
	
	@Test
	public void testSleep() {
		SleepAction sleep = new SleepAction();
		
		sleep.setDelay("0.1");
		sleep.execute(context);
	}
	
	@Test
    public void testSleepVariablesSupport() {
        SleepAction sleep = new SleepAction();
        
        context.setVariable("time", "0.1");
        sleep.setDelay("${time}");
        
        sleep.execute(context);
    }
}
