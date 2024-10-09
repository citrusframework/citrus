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

package org.citrusframework;

/**
 * Test group to mark a test should fail on purpose. Group is executed during Maven integration-test phase in a separate
 * execution in order to separate those tests form normal success test cases.
 *
 * This group is used in both JUnit and TestNG test suites where JUnit uses the class itself as a {@link org.junit.experimental.categories.Category} and
 * TestNG uses the class name as test group name in {@link org.testng.annotations.Test}.
 *
 */
public final class ShouldFailGroup {

    /**
     * Prevent instantiation
     */
    private ShouldFailGroup() {
        super();
    }
}
