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

package com.consol.citrus.mvn.plugin;

import com.consol.citrus.creator.UnitFramework;
import com.consol.citrus.mvn.plugin.config.TestConfiguration;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractCitrusMojo extends AbstractMojo {

    @Parameter(property = "citrus.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Which unit test framework to use for test execution (default: testng; options: testng, junit4, junit5)
     */
    @Parameter(defaultValue = "testng")
    private String framework = "testng";

    /**
     * Test configurations configured directly.
     */
    @Parameter
    private List<TestConfiguration> tests;

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
     * Gets the framework.
     *
     * @return
     */
    public UnitFramework getFramework() {
        return UnitFramework.fromString(framework);
    }
}
