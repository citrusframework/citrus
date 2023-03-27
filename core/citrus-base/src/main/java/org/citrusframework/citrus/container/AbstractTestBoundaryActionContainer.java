/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.citrus.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract test action container describes methods to enable/disable container execution based on given test name, package
 * and test groups. This action container is typically used by before and after test action containers.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class AbstractTestBoundaryActionContainer extends AbstractActionContainer {

    /** Test case name pattern that this sequence container matches with */
    private String namePattern;

    /** Test package name pattern that this sequence container matches with */
    private String packageNamePattern;

    /** List of test group names that match for this container */
    private List<String> testGroups = new ArrayList<>();

    /** Optional env parameters */
    private Map<String, String> env = new HashMap<>();

    /** Optional system properties */
    private Map<String, String> systemProperties = new HashMap<>();

    /**
     * Checks if this suite actions should execute according to suite name and included test groups.
     * @param testName
     * @param packageName
     * @param includedGroups
     * @return
     */
    public boolean shouldExecute(String testName, String packageName, String[] includedGroups) {
        String baseErrorMessage = "Skip before test container because of %s restrictions - do not execute container '%s'";

        if (StringUtils.hasText(packageNamePattern)) {
            if (!PatternMatchUtils.simpleMatch(packageNamePattern, packageName)) {
                log.warn(String.format(baseErrorMessage, "test package", getName()));
                return false;
            }
        }

        if (StringUtils.hasText(namePattern)) {
            if (!PatternMatchUtils.simpleMatch(namePattern, testName)) {
                log.warn(String.format(baseErrorMessage, "test name", getName()));
                return false;
            }
        }

        if (!checkTestGroups(includedGroups)) {
            log.warn(String.format(baseErrorMessage, "test groups", getName()));
            return false;
        }

        for (Map.Entry<String, String> envEntry : env.entrySet()) {
            if (!System.getenv().containsKey(envEntry.getKey()) ||
                    (StringUtils.hasText(envEntry.getValue()) && !System.getenv().get(envEntry.getKey()).equals(envEntry.getValue()))) {
                log.warn(String.format(baseErrorMessage, "env properties", getName()));
                return false;
            }
        }

        for (Map.Entry<String, String> systemProperty : systemProperties.entrySet()) {
            if (!System.getProperties().containsKey(systemProperty.getKey()) ||
                    (StringUtils.hasText(systemProperty.getValue()) && !System.getProperties().get(systemProperty.getKey()).equals(systemProperty.getValue()))) {
                log.warn(String.format(baseErrorMessage, "system properties", getName()));
                return false;
            }
        }

        return true;
    }



    /**
     * Checks on included test groups if we should execute sequence. Included group list should have
     * at least one entry matching the sequence test groups restriction.
     *
     * @param includedGroups
     * @return
     */
    private boolean checkTestGroups(String[] includedGroups) {
        if (testGroups.isEmpty()) {
            return true;
        }

        if (includedGroups != null) {
            for (String includedGroup : includedGroups) {
                if (testGroups.contains(includedGroup)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the test groups that restrict the container execution.
     * @return
     */
    public List<String> getTestGroups() {
        return testGroups;
    }

    /**
     * Sets the test groups that restrict the container execution.
     * @param testGroups
     */
    public void setTestGroups(List<String> testGroups) {
        this.testGroups = testGroups;
    }

    /**
     * Gets the name pattern.
     * @return
     */
    public String getNamePattern() {
        return namePattern;
    }

    /**
     * Sets the name pattern.
     * @param namePattern
     */
    public void setNamePattern(String namePattern) {
        this.namePattern = namePattern;
    }

    /**
     * Gets the package name pattern.
     * @return
     */
    public String getPackageNamePattern() {
        return packageNamePattern;
    }

    /**
     * Sets the package name pattern.
     * @param packageNamePattern
     */
    public void setPackageNamePattern(String packageNamePattern) {
        this.packageNamePattern = packageNamePattern;
    }

    /**
     * Gets the env.
     *
     * @return
     */
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * Sets the env.
     *
     * @param env
     */
    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    /**
     * Gets the systemProperties.
     *
     * @return
     */
    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    /**
     * Sets the systemProperties.
     *
     * @param systemProperties
     */
    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }
}
