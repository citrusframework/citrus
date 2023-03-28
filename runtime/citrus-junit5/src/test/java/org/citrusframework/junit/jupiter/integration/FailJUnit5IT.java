/*
 * Copyright 2006-2017 the original author or authors.
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
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
@ExtendWith({CitrusExtension.class, FailJUnit5IT.ShouldFailExtension.class})
public class FailJUnit5IT {

    @Test
    @CitrusTest
    public void failTest(@CitrusResource TestActionRunner runner) {
        ShouldFailExtension.message = "Unknown variable 'foo'";
        runner.run(echo("This test should fail because of unknown variable ${foo}"));
    }

    @Test
    @CitrusTest
    public void failRuntimeTest(@CitrusResource TestActionRunner runner) {
        ShouldFailExtension.message = "This test should fail because of runtime exception";
        throw new RuntimeException("This test should fail because of runtime exception");
    }

    /**
     * Handle exception thrown by tests.
     */
    public static class ShouldFailExtension implements Extension, TestExecutionExceptionHandler {
        static String message = "";

        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            Assertions.assertEquals(message, throwable.getMessage());
        }
    }
}
