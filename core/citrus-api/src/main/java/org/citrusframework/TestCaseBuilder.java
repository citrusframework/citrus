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

import java.util.Date;

public interface TestCaseBuilder {

    /**
     * Builds the test case.
     * @return
     */
    TestCase getTestCase();

    /**
     * Set test class.
     * @param type
     */
    void testClass(Class<?> type);

    /**
     * Set custom test case name.
     * @param name
     */
    void name(String name);

    /**
     * Adds description to the test case.
     *
     * @param description
     */
    void description(String description);

    /**
     * Adds author to the test case.
     *
     * @param author
     */
    void author(String author);

    /**
     * Sets custom package name for this test case.
     * @param packageName
     */
    void packageName(String packageName);

    /**
     * Sets test case status.
     *
     * @param status
     */
    void status(TestCaseMetaInfo.Status status);

    /**
     * Sets the creation date.
     *
     * @param date
     */
    void creationDate(Date date);

    /**
     * Sets the test group names for this test.
     */
    void groups(String[] groups);

    /**
     * Adds a new variable definition to the set of test variables
     * for this test case and return its value.
     *
     * @param name
     * @param value
     * @return
     */
    <T> T variable(String name, T value);
}
