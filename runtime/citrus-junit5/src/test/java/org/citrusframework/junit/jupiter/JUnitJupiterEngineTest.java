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

import org.citrusframework.TestClass;
import org.citrusframework.TestSource;
import org.citrusframework.junit.jupiter.scan.SampleJUnitJupiterTest;
import org.citrusframework.main.TestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @since 2.7.4
 */
public class JUnitJupiterEngineTest {

    @Test
    public void testRunPackage() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setIncludes(new String[] { ".*Test" });
        configuration.setPackages(Collections.singletonList(SampleJUnitJupiterTest.class.getPackage().getName()));

        runTestEngine(configuration, 1L);
    }

    @Test
    public void testRunClass() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource(SampleJUnitJupiterTest.class)));

        runTestEngine(configuration, 1L);
    }

    @Test
    public void testRunMethod() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(TestClass.fromString(SampleJUnitJupiterTest.class.getName() + "#sampleTest()")));

        runTestEngine(configuration, 1L);
    }

    @Test
    public void testRunNoMatch() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setIncludes(new String[] { ".*Foo" });
        configuration.setPackages(Collections.singletonList(SampleJUnitJupiterTest.class.getPackage().getName()));

        runTestEngine(configuration, 0L);
    }

    @Test
    public void shouldResolveJUnitEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("junit5");
        Assert.assertEquals(TestEngine.lookup(configuration).getClass(), JUnitJupiterEngine.class);
    }

    private void runTestEngine(TestRunConfiguration configuration, long passed) {
        JUnitJupiterEngine engine = new JUnitJupiterEngine(configuration);
        engine.addTestListener(new SummaryGeneratingListener() {
            @Override
            public void testPlanExecutionFinished(TestPlan testPlan) {
                super.testPlanExecutionFinished(testPlan);

                Assert.assertEquals(getSummary().getTestsSucceededCount(), passed);
                Assert.assertEquals(getSummary().getTestsFoundCount(), passed);
            }
        });
        engine.run();
    }
}
