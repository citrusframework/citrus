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

package org.citrusframework.junit.jupiter.integration;

import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Integration test verifying method-level parallel execution of {@code @CitrusTest} methods,
 * container behaviors (with and without explicit runner), overloaded methods, and parameterized tests.
 */
@CitrusSupport
@Execution(ExecutionMode.CONCURRENT)
class ParallelApplyTestBehaviorIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void shouldApplyInContainer(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        runner.run(sequential()
                .actions(
                        createVariable("greeting", "Hallo"),
                        echo("Apply in container with explicit runner"),
                        apply().behavior(new SayParallelHelloBehavior("Hallo")).on(runner),
                        runner.applyBehavior(new SayParallelHelloBehavior("Hallo"))
                ));
        Assertions.assertEquals("Hallo", context.getVariable("greeting"));
    }

    @Test
    @CitrusTest
    public void shouldApplyInContainerWithoutRunner(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        runner.run(sequential()
                .actions(
                        createVariable("greeting", "Hola"),
                        echo("Apply in container without explicit runner"),
                        apply(new SayParallelHelloBehavior("Hola")),
                        apply().behavior(new SayParallelHelloBehavior("Hola"))
                ));
        Assertions.assertEquals("Hola", context.getVariable("greeting"));
    }

    @Test
    @CitrusTest
    public void shouldIsolateOverloadedMethod(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        runner.run(sequential()
                .actions(
                        createVariable("greeting", "Bonjour"),
                        apply(new SayParallelHelloBehavior("Bonjour"))
                ));
        Assertions.assertEquals("Bonjour", context.getVariable("greeting"));
    }

    @Test
    @CitrusTest
    public void shouldIsolateOverloadedMethod(TestInfo testInfo, @CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        Assertions.assertNotNull(testInfo);
        runner.run(sequential()
                .actions(
                        createVariable("greeting", "Ciao"),
                        apply(new SayParallelHelloBehavior("Ciao"))
                ));
        Assertions.assertEquals("Ciao", context.getVariable("greeting"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Servus", "Salut", "Hej", "Ola"})
    @CitrusTest
    public void shouldIsolateConcurrentParameterizedBehaviors(String expectedGreeting,
                                                              @CitrusResource TestActionRunner runner,
                                                              @CitrusResource TestContext context) {
        runner.run(sequential()
                .actions(
                        createVariable("greeting", expectedGreeting),
                        apply(new SayParallelHelloBehavior(expectedGreeting))
                ));
        Assertions.assertEquals(expectedGreeting, context.getVariable("greeting"));
    }

    private static class SayParallelHelloBehavior implements TestBehavior, TestActionSupport {

        private final String expectedGreeting;

        SayParallelHelloBehavior(String expectedGreeting) {
            this.expectedGreeting = expectedGreeting;
        }

        @Override
        public void apply(TestActionRunner runner) {
            runner.run(ctx -> Assertions.assertEquals(expectedGreeting, ctx.getVariable("greeting")));
            runner.run(echo("${greeting} Citrus!"));
        }
    }
}
