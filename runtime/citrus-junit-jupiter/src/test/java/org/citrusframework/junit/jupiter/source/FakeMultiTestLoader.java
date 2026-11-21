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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestSource;
import org.citrusframework.api.common.MultiTestLoader;
import org.citrusframework.api.common.MultiTestRun;
import org.citrusframework.api.common.TestSourceAware;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.common.DefaultMultiTestRun;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.MultiTestDefinition;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Fake multi-test loader registered as loader type "fake-multi". The test name selects the behavior:
 * "multi" names hold the tests first, second and third ("failing" makes second fail, "disabled" disables third,
 * "slow" makes each test take a while), "broken" names cannot be opened, all other names are single-test sources.
 */
public class FakeMultiTestLoader extends DefaultTestLoader implements TestSourceAware, MultiTestLoader {

    /** Recorded events; static so tests can read them after the run. */
    private static final List<String> EVENTS = Collections.synchronizedList(new ArrayList<>());

    public static void reset() {
        EVENTS.clear();
    }

    public static List<String> getEvents() {
        synchronized (EVENTS) {
            return new ArrayList<>(EVENTS);
        }
    }

    @Override
    public void setSource(TestSource source) {
        // the test name selects the behavior
    }

    @Override
    public Optional<MultiTestRun> openMultiTest() {
        initializeTestContext();

        if (testName.contains("broken")) {
            throw new CitrusRuntimeException("Broken multi-test source " + testName);
        }

        if (!testName.contains("multi")) {
            return Optional.empty();
        }

        return Optional.of(createRun());
    }

    @Override
    protected void doLoad() {
        if (testName.contains("broken")) {
            throw new CitrusRuntimeException("Broken multi-test source " + testName);
        }

        if (testName.contains("multi")) {
            runMultiTest(createRun());
            return;
        }

        EVENTS.add("single:" + testName);
        super.doLoad();
    }

    private MultiTestRun createRun() {
        Supplier<MultiTestDefinition> parser = this::definition;
        MultiTestDefinition first = parser.get();
        first.validate(first.getName());

        DefaultMultiTestRun run = createMultiTestRun(first, parser);
        return new MultiTestRun() {
            @Override
            public String getName() {
                return run.getName();
            }

            @Override
            public List<String> getTestNames() {
                return run.getTestNames();
            }

            @Override
            public TestCase runTest(int index) {
                return run.runTest(index);
            }

            @Override
            public List<TestCase> getTestCases() {
                return run.getTestCases();
            }

            @Override
            public void close() {
                EVENTS.add("close:" + testName);
                run.close();
            }
        };
    }

    private MultiTestDefinition definition() {
        DefaultTestCase third = test("third");
        if (testName.contains("disabled")) {
            third.getMetaInfo().setStatus(TestCaseMetaInfo.Status.DISABLED);
        }

        return new MultiTestDefinition.Builder()
                .name(testName)
                .test(test("first"))
                .test(testName.contains("failing") ? failingTest("second") : test("second"))
                .test(third)
                .build();
    }

    private DefaultTestCase test(String name) {
        DefaultTestCase test = new DefaultTestCase();
        test.setName(name);
        test.getActionBuilders().add(record(name));
        return test;
    }

    private DefaultTestCase failingTest(String name) {
        DefaultTestCase test = test(name);
        test.getActionBuilders().add((TestActionBuilder<?>) () -> context -> {
            throw new CitrusRuntimeException(name + " failed");
        });
        return test;
    }

    private TestActionBuilder<?> record(String name) {
        boolean slow = testName.contains("slow");
        return () -> context -> {
            EVENTS.add("start:" + testName + ":" + name);
            if (slow) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            EVENTS.add("end:" + testName + ":" + name);
        };
    }
}
