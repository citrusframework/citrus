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

package org.citrusframework.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSourceResolverTest {

    private final Method method = findMethod("resolveMethod");

    private static Method findMethod(String name) {
        for (Method declared : TestSourceResolverTest.class.getDeclaredMethods()) {
            if (declared.getName().equals(name)) {
                return declared;
            }
        }

        throw new AssertionError("Missing method " + name);
    }

    /**
     * Placeholder method used as fallback test name donor.
     */
    void resolveMethod() {
    }

    @Test
    public void resolvesTestNames() {
        Stub testSource = new Stub();
        testSource.name = new String[]{ "firstTest", "secondTest" };

        List<ResolvedTestSource> entries = TestSourceResolver.resolve(testSource, method);

        Assertions.assertEquals(2, entries.size());
        Assertions.assertEquals("firstTest", entries.get(0).getTestName());
        Assertions.assertEquals("secondTest", entries.get(1).getTestName());
        for (ResolvedTestSource entry : entries) {
            Assertions.assertNull(entry.getSource());
            Assertions.assertEquals(TestSourceResolverTest.class.getPackageName(), entry.getPackageName());
        }
    }

    @Test
    public void resolvesTestNamesWithCustomPackageName() {
        Stub testSource = new Stub();
        testSource.name = new String[]{ "firstTest" };
        testSource.packageName = "org.example.tests";

        List<ResolvedTestSource> entries = TestSourceResolver.resolve(testSource, method);

        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals("org.example.tests", entries.get(0).getPackageName());
    }

    @Test
    public void resolvesSourcesInGivenOrder() {
        Stub testSource = new Stub();
        testSource.sources = new String[] {
                "classpath:org/citrusframework/common/scan/first.citrus.it.xml",
                "classpath:org/citrusframework/common/scan/second.citrus.it.xml"
        };

        List<ResolvedTestSource> entries = TestSourceResolver.resolve(testSource, method);

        Assertions.assertEquals(2, entries.size());
        Assertions.assertEquals("first.citrus.it", entries.get(0).getTestName());
        Assertions.assertEquals("second.citrus.it", entries.get(1).getTestName());
        Assertions.assertEquals("org.citrusframework.common.scan", entries.get(0).getPackageName());
        Assertions.assertEquals("org.citrusframework.common.scan", entries.get(1).getPackageName());
        Assertions.assertEquals(testSource.sources[0], entries.get(0).getSource());
        Assertions.assertEquals(testSource.sources[1], entries.get(1).getSource());
    }

    @Test
    public void resolvesPackageScanSortedByPath() {
        Stub testSource = new Stub();
        testSource.packageScan = new String[]{ "org.citrusframework.common.scan" };

        List<ResolvedTestSource> entries = TestSourceResolver.resolve(testSource, method);

        Assertions.assertEquals(3, entries.size());
        Assertions.assertEquals("first.citrus.it", entries.get(0).getTestName());
        Assertions.assertEquals("second.citrus.it", entries.get(1).getTestName());
        Assertions.assertEquals("third.citrus.it", entries.get(2).getTestName());
        for (ResolvedTestSource entry : entries) {
            Assertions.assertEquals("org/citrusframework/common/scan", entry.getPackageName());
            Assertions.assertNull(entry.getSource());
        }
    }

    @Test
    public void resolvesFallbackEntry() {
        List<ResolvedTestSource> entries = TestSourceResolver.resolve(new Stub(), method);

        Assertions.assertEquals(1, entries.size());
        Assertions.assertEquals("resolveMethod", entries.get(0).getTestName());
        Assertions.assertEquals(TestSourceResolverTest.class.getPackageName(), entries.get(0).getPackageName());
        Assertions.assertNull(entries.get(0).getSource());
    }

    @Test
    public void resolvesNamesThenSourcesThenPackageScan() {
        Stub testSource = new Stub();
        testSource.name = new String[]{ "namedTest" };
        testSource.sources = new String[]{ "classpath:org/citrusframework/common/scan/first.citrus.it.xml" };
        testSource.packageScan = new String[]{ "org.citrusframework.common.scan" };

        List<ResolvedTestSource> entries = TestSourceResolver.resolve(testSource, method);

        Assertions.assertEquals(5, entries.size());
        Assertions.assertEquals("namedTest", entries.get(0).getTestName());
        Assertions.assertEquals("first.citrus.it", entries.get(1).getTestName());
        Assertions.assertEquals("first.citrus.it", entries.get(2).getTestName());
        Assertions.assertEquals("second.citrus.it", entries.get(3).getTestName());
        Assertions.assertEquals("third.citrus.it", entries.get(4).getTestName());
    }

    /**
     * Stub annotation instance used to feed the resolver with test source settings.
     */
    private static class Stub implements CitrusTestSource {

        String[] name = {};
        String packageName = "";
        String[] packageScan = {};
        String[] sources = {};
        String type = TestLoader.XML;

        @Override
        public Class<? extends Annotation> annotationType() {
            return CitrusTestSource.class;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public String[] name() {
            return name;
        }

        @Override
        public String packageName() {
            return packageName;
        }

        @Override
        public String[] packageScan() {
            return packageScan;
        }

        @Override
        public String[] sources() {
            return sources;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public String toString() {
            return "@" + CitrusTestSource.class.getName();
        }
    }
}
