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

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

/**
 * @since 2.1
 */
@CitrusSupport
public class XmlTestLoaderIT {

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
}
