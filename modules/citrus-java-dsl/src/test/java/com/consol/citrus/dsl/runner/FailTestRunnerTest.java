/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class FailTestRunnerTest extends AbstractTestNGUnitTest {

    @Test()
    public void testFailBuilder() {
        MockTestRunner builder = null;

        try {
            builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
                @Override
                public void execute() {
                    fail("This test will fail.");
                }
            };
            Assert.fail("Missing test failure exception");
        } catch (CitrusRuntimeException e) {
            if (builder != null) {
                TestCase test = builder.getTestCase();
                Assert.assertEquals(test.getActionCount(), 1);
                Assert.assertEquals(test.getActions().get(0).getClass(), FailAction.class);
                Assert.assertEquals(test.getLastExecutedAction().getClass(), FailAction.class);

                FailAction action = (FailAction) test.getActions().get(0);
                Assert.assertEquals(action.getName(), "fail");
                Assert.assertEquals(action.getMessage(), "This test will fail.");
            }
        }
    }
}