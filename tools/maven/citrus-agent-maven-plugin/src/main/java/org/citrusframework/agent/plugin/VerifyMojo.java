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

package org.citrusframework.agent.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.citrusframework.util.FileUtils;

/**
 * Verify the test results created from test run.
 */
@Mojo( name = "verify", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class VerifyMojo extends AbstractAgentMojo {

    @Parameter(property = "citrus.agent.skip.test", defaultValue = "false")
    protected boolean skipRun;

    /**
     * Fail build if not tests were executed.
     */
    @Parameter(property = "citrus.agent.failIfNoTests", defaultValue = "true")
    private boolean failIfNoTests = true;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (skipRun) {
            getLog().info("Citrus agent verify is skipped.");
            return;
        }

        try {
            String summary = FileUtils.readToString(new File(getOutputDirectory().getPath() + File.separator +
                    getReport().getDirectory() + File.separator + getReport().getSummaryFile()));

            if (failIfNoTests && summary.contains("<completed>0</completed>")) {
                throw new MojoFailureException("No tests were executed! In case you want to allow empty test runs - " +
                        "please set citrus.agent.failIfNoTests property to 'false'.");
            }

            if (!summary.contains("<failures>0</failures>")) {
                throw new MojoFailureException("There are test failures!");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read test report", e);
        }
    }

    /**
     * Gets the failIfNoTests.
     *
     * @return
     */
    public boolean isFailIfNoTests() {
        return failIfNoTests;
    }

    /**
     * Sets the failIfNoTests.
     *
     * @param failIfNoTests
     */
    public void setFailIfNoTests(boolean failIfNoTests) {
        this.failIfNoTests = failIfNoTests;
    }
}
