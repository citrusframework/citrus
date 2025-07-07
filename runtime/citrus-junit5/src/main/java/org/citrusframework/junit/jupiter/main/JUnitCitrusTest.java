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

package org.citrusframework.junit.jupiter.main;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestSource;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.DynamicTest;

/**
 * JUnit test wrapper to run Citrus polyglot test definitions such as XML, YAML, Groovy.
 * Usually this test is run with TestNG engine in a main CLI.
 * The test is provided with test name and test source file parameters usually specified as part of the JUnit launcher configuration.
 * The test loads the polyglot Citrus test resource via respective test loader implementation.
 *
 * This is not supposed to be a valid base class for arbitrary Citrus Java test cases.
 * For such as use case scenario please refer to the Citrus JUnit extension class instead.
 */
@CitrusSupport
public class JUnitCitrusTest {

    private static final Map<String, TestSource> sources = new LinkedHashMap<>();

    @CitrusFramework
    Citrus citrus;

    @CitrusResource
    TestCaseRunner runner;

    @CitrusResource
    TestContext context;

    @CitrusTestFactory
    public Stream<DynamicTest> execute() {
        return sources.entrySet().stream()
                .map(entry -> {
                    String type;
                    if (entry.getValue() != null) {
                        type = entry.getValue().getType();
                    } else {
                        type = FileUtils.getFileExtension(entry.getKey());
                    }

                    TestLoader testLoader = TestLoader.lookup(type)
                            .orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to resolve test loader for type %s", type)));

                    testLoader.setTestClass(this.getClass());
                    testLoader.setTestName(entry.getKey());

                    if (testLoader instanceof TestSourceAware sourceAwareTestLoader) {
                        sourceAwareTestLoader.setSource(entry.getValue());
                    }

                    CitrusAnnotations.injectAll(testLoader, citrus, context);
                    CitrusAnnotations.injectTestRunner(testLoader, runner);

                    return CitrusTestFactorySupport.factory(type).dynamicTest(entry.getKey(), testLoader);
                });
    }

    public static void addTest(String sourceName, TestSource testSource) {
        sources.put(sourceName, testSource);
    }
}
