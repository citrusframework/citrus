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

package com.consol.citrus.remote.controller;

import com.consol.citrus.TestClass;
import com.consol.citrus.main.CitrusApp;
import com.consol.citrus.main.CitrusAppConfiguration;
import com.consol.citrus.remote.CitrusRemoteConfiguration;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RunController {

    /** Include tests based on these test names patterns */
    private String[] includes;

    /** Default properties set as system properties */
    private Map<String, String> defaultProperties = new LinkedHashMap<>();

    private final CitrusRemoteConfiguration configuration;

    /**
     * Constructor with given configuration.
     * @param configuration
     */
    public RunController(CitrusRemoteConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Run all tests found in classpath.
     */
    public void runAll() {
        runPackages(Collections.singletonList(""));
    }

    /**
     * Run Citrus application with given test package names.
     * @param packages
     */
    public void runPackages(List<String> packages) {
        CitrusAppConfiguration citrusAppConfiguration = new CitrusAppConfiguration();
        citrusAppConfiguration.setIncludes(Optional.ofNullable(includes).orElse(configuration.getIncludes()));
        citrusAppConfiguration.setPackages(packages);
        citrusAppConfiguration.setConfigClass(configuration.getConfigClass());
        citrusAppConfiguration.addDefaultProperties(configuration.getDefaultProperties());
        citrusAppConfiguration.addDefaultProperties(defaultProperties);
        run(citrusAppConfiguration);
    }

    /**
     * Run Citrus application with given test class names.
     * @param testClasses
     */
    public void runClasses(List<TestClass> testClasses) {
        CitrusAppConfiguration citrusAppConfiguration = new CitrusAppConfiguration();

        citrusAppConfiguration.setTestClasses(testClasses);
        citrusAppConfiguration.setConfigClass(configuration.getConfigClass());
        citrusAppConfiguration.addDefaultProperties(configuration.getDefaultProperties());
        citrusAppConfiguration.addDefaultProperties(defaultProperties);

        run(citrusAppConfiguration);
    }

    /**
     * Run tests with default configuration.
     */
    public void run() {
        this.run(configuration);
    }

    /**
     * Run Citrus application with given configuration and cached Citrus instance.
     * @param citrusAppConfiguration
     */
    private void run(CitrusAppConfiguration citrusAppConfiguration) {
        CitrusApp citrusApp = new CitrusApp(citrusAppConfiguration);
        citrusApp.run();
    }

    /**
     * Sets the includes.
     *
     * @param includes
     */
    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    /**
     * Sets the defaultProperties.
     *
     * @param defaultProperties
     */
    public void addDefaultProperties(Map<String, String> defaultProperties) {
        this.defaultProperties.putAll(defaultProperties);
    }
}
