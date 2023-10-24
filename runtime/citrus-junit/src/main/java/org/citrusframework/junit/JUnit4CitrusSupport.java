/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.junit;

import java.util.Date;

import org.citrusframework.Citrus;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestBehavior;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.runner.RunWith;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@RunWith(CitrusJUnit4Runner.class)
public class JUnit4CitrusSupport implements GherkinTestActionRunner, CitrusFrameworkMethod.Runner {

    /** Citrus instance */
    protected Citrus citrus;

    /** Test builder delegate */
    private TestCaseRunner delegate;

    @Override
    public void run(CitrusFrameworkMethod frameworkMethod) {
        if (frameworkMethod.hasError()) {
            throw frameworkMethod.getError();
        }

        if (citrus == null) {
            citrus = Citrus.newInstance();
        }

        TestContext ctx = prepareTestContext(citrus.getCitrusContext().createTestContext());

        if (frameworkMethod.getMethod().getAnnotation(CitrusTest.class) != null ||
                frameworkMethod.getMethod().getAnnotation(CitrusTestSource.class) != null) {
            TestCaseRunner runner = JUnit4Helper.createTestRunner(frameworkMethod, this.getClass(), ctx);
            frameworkMethod.setAttribute(JUnit4Helper.BUILDER_ATTRIBUTE, runner);

            delegate = runner;

            CitrusAnnotations.injectAll(this, citrus, ctx);

            TestLoader testLoader;
            if (frameworkMethod.getMethod().getAnnotation(CitrusTestSource.class) != null) {
                testLoader = createTestLoader(frameworkMethod.getTestName(), frameworkMethod.getPackageName(),
                        frameworkMethod.getSource(), frameworkMethod.getSourceType());
            } else {
                testLoader = new DefaultTestLoader();
            }

            CitrusAnnotations.injectAll(testLoader, citrus, ctx);
            CitrusAnnotations.injectTestRunner(testLoader, runner);
            testLoader.doWithTestCase(t -> JUnit4Helper.invokeTestMethod(this, frameworkMethod, ctx));
            testLoader.load();
        }
    }

    /**
     * Prepares the test context.
     *
     * Provides a hook for test context modifications before the test gets executed.
     *
     * @param testContext the test context.
     * @return the (prepared) test context.
     */
    protected TestContext prepareTestContext(final TestContext testContext) {
        return testContext;
    }

    /**
     * Creates new test loader which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test loader with custom test annotations set.
     * @param testName
     * @param packageName
     * @param source
     * @param type
     * @return
     */
    protected TestLoader createTestLoader(String testName, String packageName, String source, String type) {
        TestLoader testLoader = TestLoader.lookup(type)
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Missing test loader for type '%s'", type)));

        testLoader.setTestClass(getClass());
        testLoader.setTestName(testName);
        testLoader.setPackageName(packageName);

        if (testLoader instanceof TestSourceAware) {
            ((TestSourceAware) testLoader).setSource(source);
        }

        return testLoader;
    }

    @Override
    public <T extends TestAction> T run(TestActionBuilder<T> builder) {
        return delegate.run(builder);
    }

    @Override
    public <T extends TestAction> TestActionBuilder<T> applyBehavior(TestBehavior behavior) {
        return delegate.applyBehavior(behavior);
    }

    public <T> T variable(String name, T value) {
        return delegate.variable(name, value);
    }

    public void name(String name) {
        delegate.name(name);
    }

    public void description(String description) {
        delegate.description(description);
    }

    public void author(String author) {
        delegate.author(author);
    }

    public void status(TestCaseMetaInfo.Status status) {
        delegate.status(status);
    }

    public void creationDate(Date date) {
        delegate.creationDate(date);
    }
}
