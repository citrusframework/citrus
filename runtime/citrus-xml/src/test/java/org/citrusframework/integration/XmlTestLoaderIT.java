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
public class XmlTestLoaderIT {

    @CitrusFramework
    private Citrus citrus;

    @BindToRegistry
    DefaultTextEqualsMessageValidator textEqualsMessageValidator = new DefaultTextEqualsMessageValidator();

    @Test
    @CitrusTestSource(type = TestLoader.XML, name = { "sample.citrus.it" })
    public void XmlTestLoader_1_IT() {}

    @CitrusTestFactory
    public Stream<DynamicTest> XmlTestLoader_2_IT() {
        return Stream.of(
                CitrusTestFactorySupport.xml().dynamicTest("org.citrusframework.xml.actions", "echo.citrus.it.xml"),
                CitrusTestFactorySupport.xml().dynamicTest("org.citrusframework.xml.actions", "sleep.citrus.it.xml")
        );
    }

    @CitrusTestFactory
    public Stream<DynamicTest> XmlTestLoader_3_IT() {
        return CitrusTestFactorySupport.xml().packageScan("org.citrusframework.integration");
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/xml/actions/create-variables.citrus.it.xml" })
    public void XmlTestLoader_4_IT() {}

    @Test
    @CitrusTestSource(type = TestLoader.XML, sources = { "classpath:org/citrusframework/xml/identity.citrus.it.xml" })
    public void XmlTestLoader_5_IT() {}

    /**
     * The test result is reported when the loader stops the test case, which happens after the test
     * method body has run. Assert on the reported result once the test method completed.
     */
    @AfterEach
    public void verifyIdentityTestResult(TestInfo testInfo) {
        if (testInfo.getTestMethod().map(method -> method.getName().equals("XmlTestLoader_5_IT")).orElse(false)) {
            TestResult result = citrus.getCitrusContext().getTestResults().asList().stream()
                    .filter(r -> "IdentityXmlTest".equals(r.getTestName()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Missing test result for IdentityXmlTest"));

            Assertions.assertEquals(XmlTestLoaderIT.class.getName(), result.getClassName());
        }
    }
}
