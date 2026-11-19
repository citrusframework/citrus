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

package org.citrusframework.junit.jupiter.source;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestSource;
import org.citrusframework.api.common.TestSourceAware;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;

/**
 * Fake test loader that records the test name, source location and test context of every
 * execution instead of reading a test case. Registered as loader type "fake" through
 * META-INF/citrus/test/loader/fake.
 */
public class FakeTestLoader extends DefaultTestLoader implements TestSourceAware {

    /** Recorded executions; static so the engine test can read them after the run. */
    private static final List<Execution> EXECUTIONS = new ArrayList<>();

    private TestSource source;

    public static void reset() {
        EXECUTIONS.clear();
    }

    public static List<Execution> getExecutions() {
        return EXECUTIONS;
    }

    @Override
    public void setSource(TestSource source) {
        this.source = source;
    }

    @Override
    protected void doLoad() {
        String location = source != null ? source.getFilePath() : null;
        synchronized (EXECUTIONS) {
            EXECUTIONS.add(new Execution(testName, location, context));
        }

        DefaultTestCase loadedTestCase = new DefaultTestCase();
        loadedTestCase.setName(testName);
        loadedTestCase.setTestClass(testClass);
        loadedTestCase.setPackageName(packageName);
        testCase = loadedTestCase;
        if (runner instanceof DefaultTestCaseRunner defaultTestCaseRunner) {
            defaultTestCaseRunner.setTestCase(testCase);
        }

        if (location != null && location.contains("fail")) {
            throw new TestCaseFailedException(
                    new CitrusRuntimeException("Failed test source " + location));
        }

        handler.forEach(testCaseHandler -> testCaseHandler.accept(testCase));
    }

    public record Execution(String testName, String source, TestContext context) {
    }
}
