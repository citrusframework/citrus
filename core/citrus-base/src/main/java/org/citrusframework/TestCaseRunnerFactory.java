/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;

/**
 * Factory for creating {@link TestCaseRunner} instances. By default, it uses
 * Citrus' built-in runner, but it also offers the flexibility to replace the default runner with a
 * custom implementation. To do this, it leverages the Citrus {@link ResourcePathTypeResolver}
 * mechanism.
 *
 * To provide a custom runner, the following file needs to be added to the classpath:
 * <p>
 * <code>
 * 'META-INF/citrus/test/runner/custom'
 * </code>
 * </p>
 * The specified file must define the type of {@link TestCaseRunnerProvider} responsible for
 * delivering the custom test case runner.
 *
 * @author Thorsten Schlathoelter
 * @since 4.0
 * @see TestCaseRunnerProvider
 */
public class TestCaseRunnerFactory {

    /** The key for the default Citrus test case runner provider */
    private static final String DEFAULT = "default";

    /** The key for a custom test case runner provider */
    private static final String CUSTOM = "custom";

    /** Test runner resource lookup path */
    private static final String RESOURCE_PATH = "META-INF/citrus/test/runner";

    /** Default Citrus test runner from classpath resource properties. */
    private final ResourcePathTypeResolver typeResolver = new ResourcePathTypeResolver(RESOURCE_PATH);

    private static final TestCaseRunnerFactory INSTANCE = new TestCaseRunnerFactory();

    private TestCaseRunnerFactory() {
        // Singleton
    }

    /**
     * @return the Citrus default test case runner.
     */
    private TestCaseRunnerProvider lookupDefault() {
        return typeResolver.resolve(DEFAULT);
    }

    /**
     * @return a custom test case runner provider or the default, if no custom runner provider exists.
     */
    private TestCaseRunnerProvider lookupCustomOrDefault() {
        try {
            return typeResolver.resolve(CUSTOM);
        } catch (CitrusRuntimeException e) {
            return lookupDefault();
        }
    }

    /**
     * Create a runner.
     * @param context
     * @return
     */
    public static TestCaseRunner createRunner(TestContext context) {
        TestCaseRunnerProvider testCaseRunnerProvider = INSTANCE.lookupCustomOrDefault();
        return testCaseRunnerProvider.createTestCaseRunner(context);
    }

    /**
     * Create a runner.
     * @param testCase
     * @param context
     * @return
     */
    public static TestCaseRunner createRunner(TestCase testCase, TestContext context) {
        TestCaseRunnerProvider testCaseRunnerProvider = INSTANCE.lookupCustomOrDefault();
        return testCaseRunnerProvider.createTestCaseRunner(testCase, context);
    }
}
