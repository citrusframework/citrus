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

package org.citrusframework.api.container;

import java.util.List;
import java.util.Map;

public interface TestBoundaryActionContainer extends TestActionContainer {

    /**
     * Checks if this container actions should execute according to given test name, package name and included groups.
     */
    boolean shouldExecute(String testName, String packageName, String[] includedGroups);

    /**
     * Gets the test groups that restrict the container execution.
     */
    List<String> getTestGroups();

    /**
     * Sets the test groups that restrict the container execution.
     */
    void setTestGroups(List<String> testGroups);

    /**
     * Gets the name pattern.
     */
    String getNamePattern();

    /**
     * Sets the name pattern.
     */
    void setNamePattern(String namePattern);

    /**
     * Gets the package name pattern.
     */
    String getPackageNamePattern();

    /**
     * Sets the package name pattern.
     */
    void setPackageNamePattern(String packageNamePattern);

    /**
     * Gets the env.
     */
    Map<String, String> getEnv();

    /**
     * Sets the env.
     */
    void setEnv(Map<String, String> env);

    /**
     * Gets the systemProperties.
     */
    Map<String, String> getSystemProperties();

    /**
     * Sets the systemProperties.
     */
    void setSystemProperties(Map<String, String> systemProperties);
}
