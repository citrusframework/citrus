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

package com.consol.citrus.main;

import com.consol.citrus.TestClass;
import com.consol.citrus.config.CitrusSpringConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusAppConfiguration {

    /** Server time to live in milliseconds */
    private long timeToLive = 0;

    /** Optional custom configuration class for Spring application context */
    private Class<? extends CitrusSpringConfig> configClass;

    /** Test to execute at runtime */
    private List<TestClass> testClasses = new ArrayList<>();

    /** Package to execute at runtime */
    private List<String> packages = new ArrayList<>();

    /** Skip test execution at runtime */
    private boolean skipTests;

    /** Force system exit when application is finished using {@code System.exit()} */
    private boolean systemExit = false;

    /** Test name pattern */
    private String[] testNamePatterns = new String[] { "^.*IT$", "^.*ITCase$", "^IT.*$" };

    /**
     * Gets the timeToLive.
     *
     * @return
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     * Sets the timeToLive.
     *
     * @param timeToLive
     */
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * Gets the configClass.
     *
     * @return
     */
    public Class<? extends CitrusSpringConfig> getConfigClass() {
        return configClass;
    }

    /**
     * Sets the configClass.
     *
     * @param configClass
     */
    public void setConfigClass(Class<? extends CitrusSpringConfig> configClass) {
        this.configClass = configClass;
    }

    /**
     * Gets the testClasses.
     *
     * @return
     */
    public List<TestClass> getTestClasses() {
        return testClasses;
    }

    /**
     * Sets the testClasses.
     *
     * @param testClasses
     */
    public void setTestClasses(List<TestClass> testClasses) {
        this.testClasses = testClasses;
    }

    /**
     * Gets the packages.
     *
     * @return
     */
    public List<String> getPackages() {
        return packages;
    }

    /**
     * Sets the packages.
     *
     * @param packages
     */
    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    /**
     * Gets the skipTests.
     *
     * @return
     */
    public boolean isSkipTests() {
        return skipTests;
    }

    /**
     * Sets the skipTests.
     *
     * @param skipTests
     */
    public void setSkipTests(boolean skipTests) {
        this.skipTests = skipTests;
    }

    /**
     * Gets the systemExit.
     *
     * @return
     */
    public boolean isSystemExit() {
        return systemExit;
    }

    /**
     * Sets the systemExit.
     *
     * @param systemExit
     */
    public void setSystemExit(boolean systemExit) {
        this.systemExit = systemExit;
    }

    /**
     * Gets the testNamePatterns.
     *
     * @return
     */
    public String[] getTestNamePatterns() {
        return testNamePatterns;
    }

    /**
     * Sets the testNamePatterns.
     *
     * @param testNamePatterns
     */
    public void setTestNamePatterns(String[] testNamePatterns) {
        this.testNamePatterns = testNamePatterns;
    }
}
