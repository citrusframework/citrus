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

package org.citrusframework.junit.jupiter.regression;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.FailAction.Builder.fail;

/**
 * Sample used by {@link ParameterizedInvocationFailureTest}. The first invocation fails on purpose, the others must still run.
 */
@CitrusSupport
class ValueSourceRowsSample {

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third"})
    @CitrusTest
    void row(String value, @CitrusResource TestCaseRunner runner) {
        runner.$(echo("row: " + value));
        if (value.equals("first")) {
            runner.$(fail("deliberate failure in the first row"));
        }
    }
}
