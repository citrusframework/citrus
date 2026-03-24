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

package org.citrusframework.junit.jupiter.integration.java;

import java.util.stream.Stream;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

@CitrusSupport
public class JavaTestLoader_IT {

    @Test
    @DisplayName("JavaTestLoader_IT")
    @CitrusTestSource(type = TestLoader.JAVA)
    public void JavaTest() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.JAVA, name = "JavaTest")
    public void JavaTestLoader_1_IT() {
    }

    @CitrusTestFactory
    public Stream<DynamicTest> JavaTestLoader_2_IT() {
        return Stream.of(
                CitrusTestFactorySupport.java().dynamicTest("org.citrusframework.junit.jupiter.simple", "EchoTest.java"),
                CitrusTestFactorySupport.java().dynamicTest("org.citrusframework.junit.jupiter.simple", "DelayTest.java")
        );
    }

    @CitrusTestFactory
    public Stream<DynamicTest> JavaTestLoader_3_IT() {
        return CitrusTestFactorySupport.java().packageScan("org.citrusframework.junit.jupiter.simple");
    }

    @Test
    @CitrusTestSource(type = TestLoader.JAVA, sources = "classpath:org/citrusframework/junit/jupiter/integration/java/JavaTest.java")
    public void JavaTestLoader_4_IT() {
    }
}
