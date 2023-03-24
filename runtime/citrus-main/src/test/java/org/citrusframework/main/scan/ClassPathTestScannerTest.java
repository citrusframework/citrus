/*
 * Copyright 2006-2018 the original author or authors.
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

import org.citrusframework.TestClass;
import org.citrusframework.junit.scan.SampleJUnit4Test;
import org.citrusframework.testng.scan.SampleTestNGTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ClassPathTestScannerTest {

    @Test(dataProvider = "scannerDataProvider")
    public void testFindTestsInPackage(String pattern, Class<?> testClass, Class<? extends Annotation> annotationType, long expectedFindings) {
        List<TestClass> findings;
        findings = new ClassPathTestScanner(annotationType, pattern).findTestsInPackage(testClass.getPackage().getName());
        Assert.assertEquals(findings.size(), expectedFindings);

        if (expectedFindings > 0) {
            Assert.assertEquals(findings.get(0).getName(), testClass.getName());
        }
    }

    @DataProvider
    public Object[][] scannerDataProvider() {
        return new Object[][] {
                new Object[] { ".*Test", SampleJUnit4Test.class, org.junit.Test.class, 1L},
                new Object[] { ".*Test", SampleJUnit4Test.class, org.testng.annotations.Test.class, 0L},
                new Object[] { ".*Test", SampleTestNGTest.class, org.junit.Test.class, 0L},
                new Object[] { ".*IT", SampleJUnit4Test.class, org.junit.Test.class, 0L},
                new Object[] { ".*Test", SampleTestNGTest.class, org.testng.annotations.Test.class, 1L},
                new Object[] { ".*Test", SampleTestNGTest.class, org.junit.Test.class, 0L},
                new Object[] { ".*Test", SampleJUnit4Test.class, org.testng.annotations.Test.class, 0L},
                new Object[] { ".*IT", SampleTestNGTest.class, org.testng.annotations.Test.class, 0L},
        };
    }
}
