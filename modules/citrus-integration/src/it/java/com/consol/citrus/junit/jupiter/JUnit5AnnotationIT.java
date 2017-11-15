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

package com.consol.citrus.junit.jupiter;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 */
@ExtendWith(CitrusExtension.class)
public class JUnit5AnnotationIT {

    @Test
    @DisplayName("JUnit5AnnotationIT")
    @CitrusXmlTest(name = "JUnit5AnnotationIT")
    public void JUnit5Annotation_0_IT() {
    }

    @Test
    @CitrusXmlTest(name = "SampleIT")
    public void JUnit5Annotation_1_IT() {
    }

    @TestFactory
    public Stream<DynamicTest> JUnit5Annotation_2_IT() {
        return Stream.of(
                CitrusBaseExtension.dynamicTest("com.consol.citrus.actions", "EchoActionIT"),
                CitrusBaseExtension.dynamicTest("com.consol.citrus.actions", "FailActionIT"),
                CitrusBaseExtension.dynamicTest("com.consol.citrus.actions", "CreateVariablesIT")
        );
    }

    @TestFactory
    public Stream<DynamicTest> JUnit5Annotation_3_IT() {
        return CitrusBaseExtension.packageScan("com.consol.citrus.functions");
    }
}
