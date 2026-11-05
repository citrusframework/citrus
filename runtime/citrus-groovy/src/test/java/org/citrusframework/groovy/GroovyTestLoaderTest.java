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

package org.citrusframework.groovy;

import org.citrusframework.Citrus;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.base.DefaultCitrusContext;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroovyTestLoaderTest extends UnitTestSupport {

    private Citrus citrus;

    @BeforeEach
    public void setup() {
        citrus = Citrus.newInstance(() -> DefaultCitrusContext.Builder.defaultContext().referenceResolver(context.getReferenceResolver()).build());
    }

    @Test
    public void shouldLoadGroovyTest() {
        TestLoader testLoader = getTestLoader("sample.citrus.test");
        testLoader.load();
        TestCase test = testLoader.getTestCase();

        Assertions.assertEquals("sample.citrus.test", test.getName());
        Assertions.assertEquals(this.getClass().getPackageName() + ".dsl", test.getPackageName());
        Assertions.assertEquals(getClass(), test.getTestClass());
        Assertions.assertEquals(6L, test.getActionCount());
        Assertions.assertEquals(2L, ((DefaultTestCase)test).getFinalActions().size());
    }

    @Test
    public void shouldSupportJsonBuilderTest() {
        TestLoader testLoader = getTestLoader("json.citrus.test");
        testLoader.load();
        TestCase test = testLoader.getTestCase();

        Assertions.assertEquals("json.citrus.test", test.getName());
        Assertions.assertEquals(this.getClass().getPackageName() + ".dsl", test.getPackageName());
        Assertions.assertEquals(getClass(), test.getTestClass());
        Assertions.assertEquals(3L, test.getActionCount());
    }

    @Test
    public void shouldSupportXmlMarkupBuilderTest() {
        TestLoader testLoader = getTestLoader("xml.citrus.test");
        testLoader.load();
        TestCase test = testLoader.getTestCase();

        Assertions.assertEquals("xml.citrus.test", test.getName());
        Assertions.assertEquals(this.getClass().getPackageName() + ".dsl", test.getPackageName());
        Assertions.assertEquals(getClass(), test.getTestClass());
        Assertions.assertEquals(3L, test.getActionCount());
    }

    @Test
    public void shouldLookupTestLoader() {
        Assertions.assertTrue(TestLoader.lookup().containsKey(TestLoader.GROOVY));
        Assertions.assertTrue(TestLoader.lookup(TestLoader.GROOVY).isPresent());
        Assertions.assertEquals(GroovyTestLoader.class, TestLoader.lookup(TestLoader.GROOVY).get().getClass());
    }

    private TestLoader getTestLoader(String testName) {
        TestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.testClass(getClass());
        runner.name(testName);
        runner.packageName(this.getClass().getPackageName() + ".dsl");

        TestLoader testLoader = TestLoader.lookup(TestLoader.GROOVY).orElseThrow(() -> new CitrusRuntimeException("Missing Groovy test loader implementation"));
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, runner);
        return testLoader;
    }
}
