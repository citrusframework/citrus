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

package org.citrusframework.testng.main;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.TestNGCitrusSupport;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * TestNG test wrapper to run Citrus polyglot test definitions such as XML, YAML, Groovy.
 * Usually this test is run with TestNG engine in a main CLI.
 * The test is provided with test name and test source file parameters usually specified as part of the TestNG XML suite configuration.
 * The test loads the polyglot Citrus test resource via respective test loader implementation.
 *
 * This is not supposed to be a valid base class for arbitrary Citrus Java test cases.
 * For such as use case scenario please refer to the Citrus TestNG support classes instead.
 */
public class TestNGCitrusTest extends TestNGCitrusSupport {

    public static final String TEST_NAME_PARAM = "name";
    public static final String TEST_SOURCE_PARAM = "source";

    private String name;
    private String source;

    @Parameters({ TEST_NAME_PARAM, TEST_SOURCE_PARAM })
    @BeforeMethod
    public void beforeTest(String name, String source) {
        this.name = name;
        this.source = source;
    }

    @Test
    @CitrusTest
    public void execute() {
    }

    @Override
    protected TestLoader createTestLoader() {
        String type;
        if (StringUtils.hasText(source)) {
            type = FileUtils.getFileExtension(source);
        } else {
            type = FileUtils.getFileExtension(name);
        }

        name(name);

        TestLoader testLoader = TestLoader.lookup(type)
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to resolve test loader for type %s", type)));

        testLoader.setTestClass(this.getClass());
        testLoader.setTestName(name);

        if (testLoader instanceof TestSourceAware sourceAwareTestLoader) {
            sourceAwareTestLoader.setSource(source);
        }

        return testLoader;
    }
}
