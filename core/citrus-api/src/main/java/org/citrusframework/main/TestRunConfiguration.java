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

package org.citrusframework.main;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestSource;
import org.citrusframework.spi.Resources;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.7.4
 */
public class TestRunConfiguration {

    /** Test engine */
    private String engine = "junit5";

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

    /** Optional working directory for file system read */
    private String workDir;

    /** Should test engine print verbose summary details */
    private boolean verbose = true;

    /** Should test engine reset the suite state and reporters */
    private boolean reset = true;

    /**
     * Gets the engine.
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Sets the engine.
     */
    @SchemaProperty(required = true, description = "The test engine to use when running tests", defaultValue = "junit5")
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Gets the sources.
     */
    public List<TestSource> getTestSources() {
        return sources;
    }

    /**
     * Sets the sources.
     */
    @SchemaProperty(description = "List of test sources to run. " +
            "A test source represents a source code file in one of the supported languages (.java, .xml, .groovy, .yaml, .feature)")
    public void setTestSources(List<TestSource> sources) {
        this.sources.addAll(sources);
    }

    /**
     * Gets the packages.
     */
    public List<String> getPackages() {
        return packages;
    }

    /**
     * Sets the packages.
     */
    @SchemaProperty(description = "List of package names to search for tests. All tests found in these packages are executed.")
    public void setPackages(List<String> packages) {
        this.packages.addAll(packages);
    }

    /**
     * Gets the includes.
     */
    public String[] getIncludes() {
        return includes;
    }

    /**
     * Sets the includes.
     */
    @SchemaProperty(description = "Test name patterns that specify which tests to include in the test run. " +
            "Used when scanning packages for tests.")
    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    /**
     * Gets the testJar.
     */
    public File getTestJar() {
        return testJar;
    }

    /**
     * Sets the testJar.
     */
    public void setTestJar(File testJar) {
        this.testJar = testJar;
    }

    @SchemaProperty(description = "Set the test jar holding tests to execute. " +
            "Jar file is used to perform package scans for test cases to run.")
    public void setTestJar(String testJar) {
        this.testJar = Resources.create(testJar).getFile();
    }

    /**
     * Gets the defaultProperties.
     */
    public Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * Sets the default properties.
     */
    @SchemaProperty(description = "Sets default properties")
    public void setDefaultProperties(Map<String, String> defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    /**
     * Adds default properties.
     */
    public void addDefaultProperties(Map<String, String> defaultProperties) {
        this.defaultProperties.putAll(defaultProperties);
    }

    public boolean hasTests() {
        return !getTestSources().isEmpty() || !getPackages().isEmpty() || getTestJar() != null;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @SchemaProperty(description = "When enabled the test run prints verbose messages.", defaultValue = "true")
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isReset() {
        return reset;
    }

    @SchemaProperty(description = "When enabled the Citrus context instance is reset after the test.", defaultValue = "true")
    public void setReset(boolean reset) {
        this.reset = reset;
    }

    @SchemaProperty(description = "Set custom working directory. " +
            "File system based test engines may use this directory to read files from.")
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getWorkDir() {
        return workDir;
    }
}
