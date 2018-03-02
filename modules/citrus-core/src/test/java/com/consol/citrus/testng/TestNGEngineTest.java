/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.testng;

import com.consol.citrus.TestClass;
import com.consol.citrus.main.TestRunConfiguration;
import com.consol.citrus.testng.scan.SampleTestNGTest;
import org.testng.*;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class TestNGEngineTest {

    @Test
    public void testRunPackage() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setIncludes(new String[] { ".*Test" });
        configuration.setPackages(Collections.singletonList(SampleTestNGTest.class.getPackage().getName()));

        runTestEngine(configuration, 0L, 1L);
    }

    @Test
    public void testRunClass() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestClasses(Collections.singletonList(new TestClass(SampleTestNGTest.class.getName())));

        runTestEngine(configuration, 0L, 1L);
    }

    @Test
    public void testRunNoMatch() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setIncludes(new String[] { ".*Foo" });
        configuration.setPackages(Collections.singletonList(SampleTestNGTest.class.getPackage().getName()));

        runTestEngine(configuration, 0L, 0L);
    }

    private void runTestEngine(TestRunConfiguration configuration, long failure, long passed) {
        TestNGEngine engine = new TestNGEngine(configuration);
        engine.addTestListener(new ISuiteListener() {
            @Override
            public void onFinish(ISuite suite) {
                Assert.assertEquals(suite.getResults().values().iterator().next().getTestContext().getFailedTests().size(), failure);
                Assert.assertEquals(suite.getResults().values().iterator().next().getTestContext().getPassedTests().size(), passed);
            }

            @Override
            public void onStart(ISuite suite) {
            }
        });
        engine.run();
    }
}