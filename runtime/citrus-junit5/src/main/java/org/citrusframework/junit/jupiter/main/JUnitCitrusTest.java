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

import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestSource;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.Test;

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

    private static String sourceName;
    private static TestSource source;

    @CitrusFramework
    Citrus citrus;

    @CitrusResource
    TestCaseRunner runner;

    @CitrusResource
    TestContext context;

    @Test
    @CitrusTest
    public void execute() {
        String type;
        if (source != null) {
            type = source.getType();
        } else {
            type = FileUtils.getFileExtension(sourceName);
        }

        TestLoader testLoader = TestLoader.lookup(type)
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to resolve test loader for type %s", type)));

        testLoader.setTestClass(this.getClass());
        testLoader.setTestName(sourceName);

        if (testLoader instanceof TestSourceAware sourceAwareTestLoader) {
            sourceAwareTestLoader.setSource(source);
        }

        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, runner);
        testLoader.load();
    }

    public static void setSourceName(String name) {
        sourceName = name;
    }

    public static void setSource(TestSource testSource) {
        source = testSource;
    }
}
