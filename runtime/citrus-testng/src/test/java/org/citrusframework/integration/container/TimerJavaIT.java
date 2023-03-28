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

package org.citrusframework.integration.container;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.actions.StopTimerAction.Builder.stopTimer;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.container.Timer.Builder.timer;

/**
 * @author Martin Maher
 * @since 2.5
 */
@Test
public class TimerJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void timerTest() {
        run(doFinally().actions(
                stopTimer("forkedTimer")
        ));

        run(timer()
            .timerId("forkedTimer")
            .interval(100L)
            .fork(true)
            .actions(
                echo("I'm going to run in the background and let some other test actions run (nested action run ${forkedTimer-index} times)"),
                sleep().milliseconds(50L)
            ));

        run(timer()
            .repeatCount(3)
            .interval(100L)
            .delay(50L)
            .actions(
                sleep().milliseconds(50L),
                echo("I'm going to repeat this message 3 times before the next test actions are executed")
        ));

        run(echo("Test almost complete. Make sure all timers running in the background are stopped"));
    }
}
