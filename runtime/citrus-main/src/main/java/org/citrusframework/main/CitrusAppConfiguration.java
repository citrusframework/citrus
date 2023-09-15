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

package org.citrusframework.main;

import java.util.Map;
import java.util.Optional;

import org.citrusframework.CitrusSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusAppConfiguration extends TestRunConfiguration {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusAppConfiguration.class);

    /** Server time to live in milliseconds */
    private long timeToLive = 0;

    /** Optional custom configuration class name for Spring application context */
    private String configClass;

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
    public String getConfigClass() {
        return configClass;
    }

    /**
     * Sets the configClass.
     *
     * @param configClass
     */
    public void setConfigClass(String configClass) {
        this.configClass = configClass;
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
     * Reads default properties in configuration and sets them as system properties.
     */
    public void setDefaultProperties() {
        for (Map.Entry<String, String> entry : getDefaultProperties().entrySet()) {
            logger.debug(String.format("Setting application property %s=%s", entry.getKey(), entry.getValue()));
            System.setProperty(entry.getKey(), Optional.ofNullable(entry.getValue()).orElse(""));
        }

        if (getConfigClass() != null) {
            System.setProperty(CitrusSettings.DEFAULT_CONFIG_CLASS_PROPERTY, getConfigClass());
        }
    }
}
