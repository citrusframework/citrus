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

package org.citrusframework.yaml.main;

import java.io.IOException;
import java.util.Collections;

import org.citrusframework.TestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.TestNGEngine;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.annotations.Test;

/**
 * @since 2.7.4
 */
public class TestNGEngineTest {

    @Test
    public void testRunTestSource() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource(TestLoader.YAML, "echoTest", "org/citrusframework/yaml/actions/echo-test.yaml")));

        TestNGEngine engine = new TestNGEngine(configuration);
        engine.addTestListener(new ISuiteListener() {
            @Override
            public void onFinish(ISuite suite) {
                Assert.assertEquals(suite.getResults().values().iterator().next().getTestContext().getFailedTests().size(), 0L);
                Assert.assertEquals(suite.getResults().values().iterator().next().getTestContext().getPassedTests().size(), 1L);
            }

            @Override
            public void onStart(ISuite suite) {
            }
        });
        engine.run();
    }

    @Test
    public void testRunTestSourceCode() throws IOException {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource(TestLoader.YAML, "echoTest")
                .sourceCode(FileUtils.readToString(Resources.create("org/citrusframework/yaml/actions/echo-test.yaml")))));

        TestNGEngine engine = new TestNGEngine(configuration);
        engine.addTestListener(new ISuiteListener() {
            @Override
            public void onFinish(ISuite suite) {
                Assert.assertEquals(suite.getResults().values().iterator().next().getTestContext().getFailedTests().size(), 0L);
                Assert.assertEquals(suite.getResults().values().iterator().next().getTestContext().getPassedTests().size(), 1L);
            }

            @Override
            public void onStart(ISuite suite) {
            }
        });
        engine.run();
    }
}
