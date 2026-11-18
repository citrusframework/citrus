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

import java.util.stream.Stream;

import org.citrusframework.Citrus;
import org.citrusframework.TestResult;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @since 2.1
 */
@CitrusSupport
public class YamlTestLoaderIT {

    @CitrusFramework
    private Citrus citrus;

    @BindToRegistry
    DefaultTextEqualsMessageValidator textEqualsMessageValidator = new DefaultTextEqualsMessageValidator();

    @Test
    @CitrusTestSource(type = TestLoader.YAML, name = { "sample.citrus.it" })
    public void YamlTestLoader_1_IT() {}

    @CitrusTestFactory
    public Stream<DynamicTest> YamlTestLoader_2_IT() {
        return Stream.of(
                CitrusTestFactorySupport.factory(TestLoader.YAML).dynamicTest("org.citrusframework.yaml.actions", "echo.citrus.it.yaml"),
                CitrusTestFactorySupport.factory(TestLoader.YAML).dynamicTest("org.citrusframework.yaml.actions", "sleep.citrus.it.yaml")
        );
    }

    @CitrusTestFactory
    public Stream<DynamicTest> YamlTestLoader_3_IT() {
        return CitrusTestFactorySupport.factory(TestLoader.YAML).packageScan("org.citrusframework.integration");
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/yaml/actions/create-variables.citrus.it.yaml" })
    public void YamlTestLoader_4_IT() {}

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/yaml/identity.citrus.it.yaml" })
    public void YamlTestLoader_5_IT() {}

    /**
     * The test result is reported when the loader stops the test case, which happens after the test
     * method body has run. Assert on the reported result once the test method completed.
     */
    @AfterEach
    public void verifyIdentityTestResult(TestInfo testInfo) {
        if (testInfo.getTestMethod().map(method -> method.getName().equals("YamlTestLoader_5_IT")).orElse(false)) {
            TestResult result = citrus.getCitrusContext().getTestResults().asList().stream()
                    .filter(r -> "IdentityYamlTest".equals(r.getTestName()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Missing test result for IdentityYamlTest"));

            Assertions.assertEquals(YamlTestLoaderIT.class.getName(), result.getClassName());
        }
    }
}
