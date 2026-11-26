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

package org.citrusframework.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.citrusframework.CitrusContext;
import org.citrusframework.TestAction;
import org.citrusframework.annotations.CitrusParameters;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.context.TestContext;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Each data row of a parameterized test runs the whole multi-test file, and the row reaches beforeAll, every test
 * and afterAll as test variable.
 */
@CitrusSupport
@Execution(ExecutionMode.CONCURRENT)
public class YamlMultiTestParameterizedIT implements CitrusExtension.TestListener {

    private static CountDownLatch overlappingFileHooks;

    @BeforeAll
    static void resetOverlapCheck() {
        overlappingFileHooks = new CountDownLatch(2);
    }

    @Override
    public void before(CitrusContext citrusContext) {
        citrusContext.getReferenceResolver().bind("awaitBothParameterizedRows", (TestAction) context -> {
            if (!Boolean.parseBoolean(System.getProperty("junit.jupiter.execution.parallel.enabled", "true"))) {
                return;
            }
            overlappingFileHooks.countDown();
            try {
                Assertions.assertTrue(overlappingFileHooks.await(5, TimeUnit.SECONDS),
                        "Both parameterized invocations must reach beforeAll");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for the other invocation", e);
            }
        });
    }

    @CitrusResource
    private TestContext context;

    @ParameterizedTest(name = "[{index}] env={0}")
    @CitrusTestSource(type = TestLoader.YAML,
            sources = { "classpath:org/citrusframework/parameterized/multi-parameterized.citrus.it.yaml" })
    @CitrusParameters({ "env" })
    @CsvSource({ "dev", "staging" })
    void multiTestFileYamlIT(String env) {
        Assertions.assertEquals(env, context.getVariable("env"));
    }
}
