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

package org.citrusframework.groovy.integration.groovy;

import java.util.stream.Stream;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @since 2.1
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class GroovyTestLoaderIT {

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, name = { "sample.citrus.it" })
    public void GroovyTestLoader_1_IT() {}

    @CitrusTestFactory
    public Stream<DynamicTest> GroovyTestLoader_2_IT() {
        return Stream.of(
                CitrusTestFactorySupport.groovy().dynamicTest("org.citrusframework.groovy.integration.actions", "echo.citrus.test.groovy"),
                CitrusTestFactorySupport.groovy().dynamicTest("org.citrusframework.groovy.integration.actions", "delay.citrus.test.groovy")
        );
    }

    @CitrusTestFactory
    public Stream<DynamicTest> GroovyTestLoader_3_IT() {
        return CitrusTestFactorySupport.groovy().packageScan("org.citrusframework.groovy.integration.groovy");
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, sources = { "classpath:org/citrusframework/groovy/integration/actions/createVariables.citrus.test.groovy" })
    public void GroovyTestLoader_4_IT() {}
}
