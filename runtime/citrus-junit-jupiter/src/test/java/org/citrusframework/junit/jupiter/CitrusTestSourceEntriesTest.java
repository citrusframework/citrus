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

package org.citrusframework.junit.jupiter;

import java.util.Collections;
import java.util.List;

import org.citrusframework.TestSource;
import org.citrusframework.api.main.TestRunConfiguration;
import org.citrusframework.junit.jupiter.source.FakeTestLoader;
import org.citrusframework.junit.jupiter.source.FailingSourceFixture;
import org.citrusframework.junit.jupiter.source.SeveralSourcesFixture;
import org.citrusframework.junit.jupiter.source.SingleSourceFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CitrusTestSourceEntriesTest {

    @BeforeEach
    public void resetFakeLoader() {
        FakeTestLoader.reset();
    }

    @Test
    public void shouldRunSingleSourceTest() {
        runTestEngine(SingleSourceFixture.class, 1L, 0L);

        List<FakeTestLoader.Execution> executions = FakeTestLoader.getExecutions();
        assertEquals(1, executions.size());
        assertEquals("first.citrus.it", executions.get(0).testName());
    }

    @Test
    public void shouldRunSeveralSourcesInOneTest() {
        runTestEngine(SeveralSourcesFixture.class, 1L, 0L);

        List<FakeTestLoader.Execution> executions = FakeTestLoader.getExecutions();
        assertEquals(2, executions.size());
        assertEquals("first.citrus.it", executions.get(0).testName());
        assertEquals("second.citrus.it", executions.get(1).testName());
        Assertions.assertNotSame(executions.get(0).context(), executions.get(1).context(),
                "Each test source must run with its own test context");
    }

    @Test
    public void shouldFailTestWhenAnySourceFails() {
        runTestEngine(FailingSourceFixture.class, 0L, 1L);

        List<FakeTestLoader.Execution> executions = FakeTestLoader.getExecutions();
        assertEquals(2, executions.size());
        assertEquals("first.citrus.it", executions.get(0).testName());
        assertEquals("failing.citrus.it", executions.get(1).testName());
    }

    @Test
    public void shouldProvideYamlTestFactorySupport() throws Exception {
        CitrusTestFactorySupport support = CitrusTestFactorySupport.yaml();

        java.lang.reflect.Field type = CitrusTestFactorySupport.class.getDeclaredField("type");
        type.setAccessible(true);
        assertEquals("yaml", type.get(support));
    }

    private void runTestEngine(Class<?> testClass, long passed, long failed) {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource(testClass)));

        JUnitJupiterEngine engine = new JUnitJupiterEngine(configuration);
        engine.addTestListener(new SummaryGeneratingListener() {
            @Override
            public void testPlanExecutionFinished(TestPlan testPlan) {
                super.testPlanExecutionFinished(testPlan);

                assertEquals(passed, getSummary().getTestsSucceededCount());
                assertEquals(failed, getSummary().getTestsFailedCount());
            }
        });
        engine.run();
    }
}
