/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.main;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestSource;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class TestRunConfiguration {

    /** Test engine */
    private String engine = "junit4";

    /** Test to execute at runtime */
    private final List<TestSource> sources = new ArrayList<>();

    /** Package to execute at runtime */
    private final List<String> packages = new ArrayList<>();

    /** Include tests based on these test name pattern */
    private String[] includes = new String[] { "^.*IT$", "^.*ITCase$", "^IT.*$" };

    /** Default properties set as system properties */
    private Map<String, String> defaultProperties = new LinkedHashMap<>();

    /** Optional test jar artifact holding tests */
    private File testJar;

    /**
     * Gets the engine.
     * @return
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Sets the engine.
     * @param engine
     */
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Gets the sources.
     *
     * @return
     */
    public List<TestSource> getTestSources() {
        return sources;
    }

    /**
     * Sets the sources.
     *
     * @param sources
     */
    public void setTestSources(List<TestSource> sources) {
        this.sources.addAll(sources);
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
        this.packages.addAll(packages);
    }

    /**
     * Gets the includes.
     *
     * @return
     */
    public String[] getIncludes() {
        return includes;
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
     * Gets the testJar.
     *
     * @return
     */
    public File getTestJar() {
        return testJar;
    }

    /**
     * Sets the testJar.
     *
     * @param testJar
     */
    public void setTestJar(File testJar) {
        this.testJar = testJar;
    }

    /**
     * Gets the defaultProperties.
     *
     * @return
     */
    public Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * Adds default properties.
     *
     * @param defaultProperties
     */
    public void addDefaultProperties(Map<String, String> defaultProperties) {
        this.defaultProperties = defaultProperties;
    }
}
