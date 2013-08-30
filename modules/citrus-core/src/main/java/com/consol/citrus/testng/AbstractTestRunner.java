/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.testng;

import com.consol.citrus.context.TestContext;

/**
 * Abstract test runner executes Citrus test case instance at runtime.
 * @author Christoph Deppisch
 */
public abstract class AbstractTestRunner implements TestRunner {

    /** This runners test case and context */
    private final TestContext testContext;

    /**
     * Default constructor using the test context field.
     * @param testContext
     */
    public AbstractTestRunner(TestContext testContext) {
        this.testContext = testContext;
    }

    public void run() {
        getTestCase().execute(testContext);
    }

    /**
     * Gets the test context.
     * @return
     */
    public TestContext getTestContext() {
        return testContext;
    }
}
