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

package org.citrusframework.junit.jupiter;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.testng.UnitTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Abstract base JUnit Jupiter test implementation for Citrus unit testing. Provides access to
 * a test context and injected function registry as well as global variables.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UnitTestConfig.class)
public abstract class AbstractUnitTest {

    @Autowired
    protected ApplicationContext applicationContext;

    /** Test context */
    protected TestContext context;

    /** Factory bean for test context */
    protected TestContextFactory testContextFactory;

    /**
     * Setup test execution.
     */
    @BeforeEach
    public void prepareTest() {
        testContextFactory = createTestContextFactory();
        context = createTestContext();
    }

    /**
     * Creates the test context for this unit test.
     * @return the test context
     */
    protected TestContext createTestContext() {
        return testContextFactory.getObject();
    }

    /**
     * Creates the test context factory for this unit test.
     * @return the test context factory
     */
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }
}
