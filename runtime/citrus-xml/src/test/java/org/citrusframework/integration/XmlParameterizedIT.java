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

import org.citrusframework.annotations.CitrusParameters;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.context.TestContext;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Each data row of a parameterized test reaches the loaded XML test case as test variable.
 */
@CitrusSupport
public class XmlParameterizedIT {

    @CitrusResource
    private TestContext context;

    @ParameterizedTest(name = "[{index}] greeting={0}")
    @CitrusTestSource(type = TestLoader.XML,
            sources = { "classpath:org/citrusframework/parameterized/parameterized.citrus.it.xml" })
    @CitrusParameters({ "greeting" })
    @CsvSource({ "Hello", "Hallo" })
    void parameterizedXmlIT(String greeting) {
        Assertions.assertEquals(greeting, context.getVariable("greeting"));
    }

    @ParameterizedTest(name = "[{index}] fileValueWins")
    @CitrusTestSource(type = TestLoader.XML,
            sources = { "classpath:org/citrusframework/parameterized/parameterized-override.citrus.it.xml" })
    @CitrusParameters({ "greeting" })
    @CsvSource({ "Hello", "Hallo" })
    void fileValueWinsXmlIT(String greeting) {
        Assertions.assertEquals("from-file", context.getVariable("greeting"));
    }
}
