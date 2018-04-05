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

package com.consol.citrus.remote.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RunConfiguration {

    @Parameter
    private List<String> classes;

    @Parameter
    private List<String> packages;

    @Parameter
    private List<String> includes;

    @Parameter
    private Map<String, String> systemProperties;

    @Parameter(property = "citrus.remote.run.async", defaultValue = "false")
    private boolean async;

    @Parameter(property = "citrus.remote.run.polling.interval", defaultValue = "10000")
    private long pollingInterval = 10000L;

    /**
     * Gets the classes.
     *
     * @return
     */
    public List<String> getClasses() {
        return classes;
    }

    /**
     * Sets the classes.
     *
     * @param classes
     */
    public void setClasses(List<String> classes) {
        this.classes = classes;
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
     * Checks existence of classes.
     * @return
     */
    public boolean hasClasses() {
        return getClasses() != null && !getClasses().isEmpty();
    }

    /**
     * Checks existence of packages.
     * @return
     */
    public boolean hasPackages() {
        return getPackages() != null && !getPackages().isEmpty();
    }

    /**
     * Gets the includes.
     *
     * @return
     */
    public List<String> getIncludes() {
        return includes;
    }

    /**
     * Sets the includes.
     *
     * @param includes
     */
    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    /**
     * Gets the system properties.
     *
     * @return
     */
    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    /**
     * Sets the system properties.
     *
     * @param properties
     */
    public void setSystemProperties(Map<String, String> properties) {
        this.systemProperties = properties;
    }

    /**
     * Gets the async.
     *
     * @return
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Sets the async.
     *
     * @param async
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Gets the pollingInterval.
     *
     * @return
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval.
     *
     * @param pollingInterval
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
