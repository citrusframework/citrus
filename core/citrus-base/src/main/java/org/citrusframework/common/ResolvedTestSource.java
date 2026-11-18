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

import java.util.Objects;

/**
 * Single entry resolved from a {@link org.citrusframework.annotations.CitrusTestSource} annotation. The entry
 * describes which test to run: either by test name, or by pointing to a test source file.
 *
 * @since 5.3
 */
public final class ResolvedTestSource {

    private final String testName;
    private final String packageName;
    private final String source;

    /**
     * Constructor with test name, package name and optional source file path.
     * @param testName the test name.
     * @param packageName the package name.
     * @param source the source file path or null for name based tests.
     */
    public ResolvedTestSource(String testName, String packageName, String source) {
        this.testName = testName;
        this.packageName = packageName;
        this.source = source;
    }

    /**
     * Gets the test name.
     * @return the test name.
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Gets the package name.
     * @return the package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets the source file path. Null for name based tests.
     * @return the source file path or null.
     */
    public String getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResolvedTestSource that = (ResolvedTestSource) o;
        return Objects.equals(testName, that.testName)
                && Objects.equals(packageName, that.packageName)
                && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testName, packageName, source);
    }

    @Override
    public String toString() {
        return "ResolvedTestSource{" +
                "testName='" + testName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
