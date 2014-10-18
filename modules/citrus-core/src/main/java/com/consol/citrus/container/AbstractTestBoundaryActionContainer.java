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

package com.consol.citrus.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract test action container describes methods to enable/disable container execution based on given test name, package
 * and test groups. This action container is typically used by before and after test action containers.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class AbstractTestBoundaryActionContainer extends AbstractActionContainer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SequenceBeforeTest.class);

    /** Test case name pattern that this sequence container matches with */
    private String namePattern;

    /** Test package name pattern that this sequence container matches with */
    private String packageNamePattern;

    /** List of test group names that match for this container */
    private List<String> testGroups = new ArrayList<String>();

    /**
     * Checks if this suite actions should execute according to suite name and included test groups.
     * @param testName
     * @param packageName
     * @param includedGroups
     * @return
     */
    public boolean shouldExecute(String testName, String packageName, String[] includedGroups) {
        String baseErrorMessage = "Before test container restrictions did not match given %s - do not execute container '%s'";

        if (StringUtils.hasText(packageNamePattern)) {
            if (packageName == null || !PatternMatchUtils.simpleMatch(packageNamePattern, packageName)) {
                if (log.isDebugEnabled())  {
                    log.debug(String.format(baseErrorMessage, "test package", getName()));
                }
                return false;
            }
        }

        if (StringUtils.hasText(namePattern)) {
            if (testName == null || !PatternMatchUtils.simpleMatch(namePattern, testName)) {
                if (log.isDebugEnabled())  {
                    log.debug(String.format(baseErrorMessage, "test name", getName()));
                }
                return false;
            }
        }

        if (!checkTestGroups(includedGroups)) {
            if (log.isDebugEnabled())  {
                log.debug(String.format(baseErrorMessage, "test groups", getName()));
            }
            return false;
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
}
