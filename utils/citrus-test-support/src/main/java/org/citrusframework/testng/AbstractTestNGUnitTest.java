/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.testng;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.util.ObjectHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * Abstract base testng test implementation for Citrus unit testing. Provides access to
 * a test context and injected function registry as well as global variables.
 *
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = UnitTestConfig.class)
public abstract class AbstractTestNGUnitTest extends AbstractTestNGSpringContextTests {

    /** Test context */
    protected TestContext context;

    /** Factory bean for test context */
    protected TestContextFactory testContextFactory;

    /**
     * Runs tasks before test suite.
     * @throws Exception on error.
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() throws Exception {
        springTestContextPrepareTestInstance();
        ObjectHelper.assertNotNull(applicationContext, "Missing proper application context in before suite initialization");
    }

    /**
     * Setup test execution.
     */
    @BeforeMethod
    public void prepareTest() {
        testContextFactory = createTestContextFactory();
        context = createTestContext();
    }

    /**
     * Creates the test context for this unit test.
     * @return
     */
    protected TestContext createTestContext() {
        return testContextFactory.getObject();
    }

    /**
     * Creates the test context factory for this unit test.
     * @return
     */
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }
}
