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

package org.citrusframework.mvn.plugin;

import org.citrusframework.generate.UnitFramework;
import org.citrusframework.mvn.plugin.config.docs.DocsConfiguration;
import org.citrusframework.mvn.plugin.config.tests.TestConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractCitrusMojo extends AbstractMojo {

    @Parameter(property = "citrus.plugin.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Mojo looks in this directory for test files that are included in this report. Defaults to "src/test/"
     */
    @Parameter(property = "citrus.test.src.directory", defaultValue = "src/test/")
    private String testSrcDirectory = "src/test/";

    /**
     * Which target code base type to use for test execution (default: java; options: java, xml)
     */
    @Parameter(property = "citrus.test.type", defaultValue = "java")
    private String type = "java";

    /**
     * Which unit test framework to use for test execution (default: testng; options: testng, junit4, junit5)
     */
    @Parameter(property = "citrus.test.framework", defaultValue = "testng")
    private String framework = "testng";

    /**
     * Test configurations configured directly.
     */
    @Parameter
    private List<TestConfiguration> tests;

    /**
     * Test configurations configured directly.
     */
    @Parameter
    private DocsConfiguration docs;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            doExecute();
        }
    }

    /**
     * Subclass execution logic.
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    /**
     * Gets the tests.
     *
     * @return
     */
    public List<TestConfiguration> getTests() {
        return tests;
    }

    /**
     * Sets the tests.
     *
     * @param tests
     */
    public void setTests(List<TestConfiguration> tests) {
        this.tests = tests;
    }

    /**
     * Gets the docs.
     *
     * @return
     */
    public DocsConfiguration getDocs() {
        return docs;
    }

    /**
     * Sets the docs.
     *
     * @param docs
     */
    public void setDocs(DocsConfiguration docs) {
        this.docs = docs;
    }

    /**
     * Gets the framework.
     *
     * @return
     */
    public UnitFramework getFramework() {
        return UnitFramework.fromString(framework);
    }

    /**
     * Gets the type.
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the testSrcDirectory.
     *
     * @return
     */
    public String getTestSrcDirectory() {
        return testSrcDirectory;
    }
}
