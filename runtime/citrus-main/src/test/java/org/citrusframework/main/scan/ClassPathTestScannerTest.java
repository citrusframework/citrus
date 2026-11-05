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

package org.citrusframework.main.scan;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

import org.citrusframework.TestClass;
import org.citrusframework.base.main.scan.ClassPathTestScanner;
import org.citrusframework.junit.scan.SampleJUnit4Test;
import org.citrusframework.junit.scan.SampleJUnitTest;
import org.citrusframework.testng.scan.SampleTestNGTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @since 2.7.4
 */
public class ClassPathTestScannerTest {

    @ParameterizedTest
    @MethodSource("scannerDataProvider")
    public void testFindTestsInPackage(String pattern, Class<?> testClass, Class<? extends Annotation> annotationType, long expectedFindings) {
        List<TestClass> findings;
        findings = new ClassPathTestScanner(annotationType, pattern).findTestsInPackage(testClass.getPackage().getName());
        Assertions.assertEquals(expectedFindings, findings.size());

        if (expectedFindings > 0) {
            Assertions.assertEquals(testClass.getName(), findings.get(0).getName());
        }
    }

    static Stream<Arguments> scannerDataProvider() {
        return Stream.of(
                Arguments.of(".*Test", SampleJUnit4Test.class, org.junit.Test.class, 1L),
                Arguments.of(".*Test", SampleTestNGTest.class, org.junit.Test.class, 0L),
                Arguments.of(".*IT", SampleJUnit4Test.class, org.junit.Test.class, 0L),
                Arguments.of(".*Test", SampleJUnitTest.class, org.junit.jupiter.api.Test.class, 1L),
                Arguments.of(".*Test", SampleJUnitTest.class, org.testng.annotations.Test.class, 0L),
                Arguments.of(".*Test", SampleTestNGTest.class, org.junit.jupiter.api.Test.class, 0L),
                Arguments.of(".*IT", SampleJUnitTest.class, org.junit.jupiter.api.Test.class, 0L),
                Arguments.of(".*Test", SampleTestNGTest.class, org.testng.annotations.Test.class, 1L),
                Arguments.of(".*Test", SampleTestNGTest.class, org.junit.jupiter.api.Test.class, 0L),
                Arguments.of(".*Test", SampleJUnitTest.class, org.testng.annotations.Test.class, 0L),
                Arguments.of(".*IT", SampleTestNGTest.class, org.testng.annotations.Test.class, 0L)
        );
    }
}
