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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.consol.citrus.UnitTestSupport;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SleepActionTest extends UnitTestSupport {

	@Test
	public void testSleepDuration() {
		SleepAction sleep = new SleepAction.Builder()
		        .time(Duration.ofMillis(200))
                .build();

		sleep.execute(context);
	}

	@Test
	public void testSleep() {
		SleepAction sleep = new SleepAction.Builder()
		        .milliseconds(100L)
                .build();

		sleep.execute(context);
	}

	@Test
    public void testSleepVariablesSupport() {
        SleepAction sleep = new SleepAction.Builder()
                .milliseconds("${time}")
                .build();

        context.setVariable("time", "100");

        sleep.execute(context);
    }

    @Test
    public void testSleepDecimalValueSupport() {
        SleepAction sleep = new SleepAction.Builder()
                .time("500.0", TimeUnit.MILLISECONDS)
                .build();

        sleep.execute(context);

        sleep = new SleepAction.Builder()
                .time("0.5", TimeUnit.SECONDS)
                .build();

        sleep.execute(context);

        sleep = new SleepAction.Builder()
                .time("0.01", TimeUnit.MINUTES)
                .build();

        sleep.execute(context);
    }

    @Test
    public void testSleepLegacy() {
        SleepAction sleep = new SleepAction.Builder()
                .seconds(0.1)
                .build();

        sleep.execute(context);
    }

    @Test
    public void testSleepLegacyVariablesSupport() {
        SleepAction sleep = new SleepAction.Builder()
                .time("${time}", TimeUnit.SECONDS)
                .build();

        context.setVariable("time", "1");

        sleep.execute(context);
    }
}
