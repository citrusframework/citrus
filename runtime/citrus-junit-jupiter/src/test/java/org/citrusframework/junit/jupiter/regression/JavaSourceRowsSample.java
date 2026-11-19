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

import org.citrusframework.annotations.CitrusParameters;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.context.TestContext;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Sample used by {@link ParameterizedInvocationFailureTest}. Each invocation publishes its row value to the test context,
 * the loaded Java test source fails for the first row only.
 */
@CitrusSupport
class JavaSourceRowsSample {

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third"})
    @CitrusParameters("value")
    @CitrusTestSource(type = TestLoader.JAVA, sources = "classpath:org/citrusframework/junit/jupiter/regression/ParameterizedRowTest.java")
    void row(String value, @CitrusResource TestContext context) {
        context.setVariable("row", value);
    }
}
