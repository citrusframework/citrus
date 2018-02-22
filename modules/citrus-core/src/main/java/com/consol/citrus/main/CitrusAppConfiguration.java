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

import com.consol.citrus.config.CitrusSpringConfig;

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
    private Class<?> testClass;

    /** Test method to execute at runtime */
    private String testMethod;

    /** Package to execute at runtime */
    private String packageName;

    /** Skip test execution at runtime */
    private boolean skipTests;

    /** Force system exit when application is finished using {@code System.exit()} */
    private boolean systemExit = false;

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
     * Gets the testClass.
     *
     * @return
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Sets the testClass.
     *
     * @param testClass
     */
    public void setTestClass(Class<?> testClass) {
        this.testClass = testClass;
    }

    /**
     * Gets the testMethod.
     *
     * @return
     */
    public String getTestMethod() {
        return testMethod;
    }

    /**
     * Sets the testMethod.
     *
     * @param testMethod
     */
    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    /**
     * Gets the packageName.
     *
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the packageName.
     *
     * @param packageName
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
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
}
