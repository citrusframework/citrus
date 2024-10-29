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

package org.citrusframework.container;

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class RepeatOnErrorUntilTrueTest extends UnitTestSupport {

    @Mock
    private TestAction action;

    private AutoCloseable openedMocks;

    @BeforeMethod
    public void beforeMethodSetup() {
        openedMocks = openMocks(this);
    }

    @AfterMethod
    public void afterMethodTearDown() throws Exception {
        openedMocks.close();
    }

    @Test
    public void buildWithLongApi() {
        var autoSleepMillis = 5L;

        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .autoSleep(autoSleepMillis)
                .build();

        assertThat(repeat)
                .satisfies(
                        r -> assertThat(r.getAutoSleep()).isEqualTo(autoSleepMillis),
                        r -> assertThat(r.getAutoSleepDuration()).isEqualTo(Duration.ofMillis(autoSleepMillis))
                );
    }

    @Test
    public void buildWithDurationApi() {
        var autoSleepMillis = 120_000L;
        var autoSleepDuration = Duration.ofMinutes(2);

        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .autoSleep(autoSleepDuration)
                .build();

        assertThat(repeat)
                .satisfies(
                        r -> assertThat(r.getAutoSleep()).isEqualTo(autoSleepMillis),
                        r -> assertThat(r.getAutoSleepDuration()).isEqualTo(autoSleepDuration)
                );
    }

    @DataProvider
    public Object[][] expressionProvider() {
        return new Object[][]{
                new Object[]{"i = 5"},
                new Object[]{"@greaterThan(4)@"}
        };
    }

    @Test(dataProvider = "expressionProvider")
    public void testSuccessOnFirstIteration(String expression) {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .condition(expression)
                .index("i")
                .actions(() -> action)
                .build();

        repeat.execute(context);

        verify(action).execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccess() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .condition("i = 5")
                .index("i")
                .autoSleep(Duration.ofMillis(0))
                .actions(() -> action, new FailAction.Builder())
                .build();

        repeat.execute(context);

        verify(action, times(4)).execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccessConditionExpression() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .condition((index, context) -> index == 5)
                .index("i")
                .autoSleep(Duration.ofMillis(0))
                .actions(() -> action, new FailAction.Builder())
                .build();

        repeat.execute(context);

        verify(action, times(4)).execute(context);
    }
}
