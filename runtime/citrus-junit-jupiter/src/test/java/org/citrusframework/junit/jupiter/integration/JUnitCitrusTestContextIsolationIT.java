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

import java.util.stream.Stream;

import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.junit.jupiter.api.DynamicTest;

/**
 * Verifies that dynamic tests produced by a {@link CitrusTestFactory} get isolated
 * {@link org.citrusframework.context.TestContext} instances so that state (variables,
 * final actions, exceptions, etc.) does not leak between tests.
 *
 * <p>The first test sets a context variable; the second asserts the variable is absent.
 * If contexts are shared, the second test fails with a {@code CitrusRuntimeException}.</p>
 *
 * @see <a href="https://github.com/citrusframework/citrus/issues/1816">#1816</a>
 */
@CitrusSupport
public class JUnitCitrusTestContextIsolationIT {

    @CitrusTestFactory
    public Stream<DynamicTest> shouldIsolateContextBetweenDynamicTests() {
        return Stream.of(
                CitrusTestFactorySupport.java().dynamicTest(
                        "org.citrusframework.junit.jupiter.integration.isolation", "SetVariableTest.java"),
                CitrusTestFactorySupport.java().dynamicTest(
                        "org.citrusframework.junit.jupiter.integration.isolation", "CheckIsolationTest.java"));
    }
}
