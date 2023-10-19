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
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestCaseRunnerFactoryTest {

    @Test
    public void testDefaultRunnerWithGivenContext()  {
        TestContext testContext = new TestContext();
        TestCaseRunner runner = TestCaseRunnerFactory.createRunner(testContext);
        assertEquals(runner.getClass(), DefaultTestCaseRunner.class);

        DefaultTestCaseRunner defaultTestCaseRunner = (DefaultTestCaseRunner) runner;
        assertEquals(defaultTestCaseRunner.getContext(), testContext);
        assertTrue(defaultTestCaseRunner.getTestCase() instanceof  DefaultTestCase);
    }

    @Test
    public void testDefaultRunnerWithGivenTestCaseAndContext()  {
        TestContext testContext = new TestContext();
        TestCase testCase = new DefaultTestCase();

        TestCaseRunner runner = TestCaseRunnerFactory.createRunner(testCase, testContext);
        assertEquals(runner.getClass(), DefaultTestCaseRunner.class);

        DefaultTestCaseRunner defaultTestCaseRunner = (DefaultTestCaseRunner) runner;
        assertEquals(defaultTestCaseRunner.getContext(), testContext);
        assertEquals(defaultTestCaseRunner.getTestCase(), testCase);
    }

    @Test
    public void testCustomRunnerGivenContext() {
        ResourcePathTypeResolver resolverMock = Mockito.mock(ResourcePathTypeResolver.class);

        Mockito.doReturn(new CustomTestCaseRunnerProvider()).when(resolverMock).resolve("custom");
        TestCaseRunnerFactory instance = (TestCaseRunnerFactory) ReflectionTestUtils.getField(
            TestCaseRunnerFactory.class,"INSTANCE");
        Assert.assertNotNull(instance);

        TestContext testContext = new TestContext();

        Object currentResolver = ReflectionTestUtils.getField(instance, "typeResolver");
        try {
            ReflectionTestUtils.setField(instance, "typeResolver", resolverMock);
            TestCaseRunner runner = TestCaseRunnerFactory.createRunner(testContext);

            assertEquals(runner.getClass(), CustomTestCaseRunner.class);

            CustomTestCaseRunner defaultTestCaseRunner = (CustomTestCaseRunner) runner;
            assertEquals(defaultTestCaseRunner.getContext(), testContext);

        } finally {
            ReflectionTestUtils.setField(instance, "typeResolver", currentResolver);
        }

    }

    @Test
    public void testCustomRunnerGivenTestCaseAndContext() {
        ResourcePathTypeResolver resolverMock = Mockito.mock(ResourcePathTypeResolver.class);

        Mockito.doReturn(new CustomTestCaseRunnerProvider()).when(resolverMock).resolve("custom");
        TestCaseRunnerFactory instance = (TestCaseRunnerFactory) ReflectionTestUtils.getField(
            TestCaseRunnerFactory.class,"INSTANCE");
        Assert.assertNotNull(instance);

        TestContext testContext = new TestContext();
        TestCase testCase = new DefaultTestCase();

        Object currentResolver = ReflectionTestUtils.getField(instance, "typeResolver");
        try {
            ReflectionTestUtils.setField(instance, "typeResolver", resolverMock);
            TestCaseRunner runner = TestCaseRunnerFactory.createRunner(testCase, testContext);

            assertEquals(runner.getClass(), CustomTestCaseRunner.class);

            CustomTestCaseRunner defaultTestCaseRunner = (CustomTestCaseRunner) runner;
            assertEquals(defaultTestCaseRunner.getContext(), testContext);
            assertEquals(defaultTestCaseRunner.getTestCase(), testCase);

        } finally {
            ReflectionTestUtils.setField(instance, "typeResolver", currentResolver);
        }

    }

    private static class CustomTestCaseRunnerProvider implements TestCaseRunnerProvider {

        @Override
        public TestCaseRunner createTestCaseRunner(TestCase testCase, TestContext context) {
            return new CustomTestCaseRunner(testCase, context);
        }

        @Override
        public TestCaseRunner createTestCaseRunner(TestContext context) {
            return new CustomTestCaseRunner(context);
        }
    }

    private static class CustomTestCaseRunner extends DefaultTestCaseRunner {

        public CustomTestCaseRunner(TestContext context) {
            super(context);
        }

        public CustomTestCaseRunner(TestCase testCase, TestContext context) {
            super(testCase, context);
        }
    }
}
