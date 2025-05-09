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

package org.citrusframework.agent.connector;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.testng.annotations.BeforeMethod;

public abstract class UnitTestSupport {

    protected TestContextFactory testContextFactory;
    protected TestContext context;

    /**
     * Setup test execution.
     */
    @BeforeMethod
    public void prepareTest() {
        testContextFactory = createTestContextFactory();
        context = testContextFactory.getObject();
    }

    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }
}
