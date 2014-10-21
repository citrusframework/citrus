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

import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContextFactory;

/**
 * Simple test runner to be executed by TestNG {@link org.testng.annotations.Factory} factory methods that create Citrus tests
 * on the fly. Runner has {@link org.testng.annotations.Test} annotated run method which is executed by TestNG.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
*/
public class CitrusTestRunner extends AbstractTestRunner {

    private final TestCase testCase;

    /**
     * Constructor using final fields for testCase and testContext.
     * @param testCase
     * @param testContextFactory
     */
    public CitrusTestRunner(TestCase testCase, TestContextFactory testContextFactory) {
        super(testContextFactory);
        this.testCase = testCase;
    }

    @Override
    public TestCase getTestCase() {
        return testCase;
    }
}
