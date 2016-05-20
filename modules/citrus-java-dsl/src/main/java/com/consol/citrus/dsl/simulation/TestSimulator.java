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

package com.consol.citrus.dsl.simulation;

import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * Test simulator interface defines simulation method that executes test methods in simulation mode.
 * @author Christoph Deppisch
 * @since 2.6
 */
public interface TestSimulator {

    /**
     * Simulates test method execution.
     * @param method
     * @param context
     * @param applicationContext
     */
    void simulate(Method method, TestContext context, ApplicationContext applicationContext);

    /**
     * Gets the test case.
     * @return
     */
    TestCase getTestCase();
}
