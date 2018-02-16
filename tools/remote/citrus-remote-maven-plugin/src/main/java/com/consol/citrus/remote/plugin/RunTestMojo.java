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

package com.consol.citrus.remote.plugin;

import com.consol.citrus.remote.plugin.config.RunConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunTestMojo extends AbstractCitrusRemoteMojo {

    @Parameter(property = "citrus.skip.run", defaultValue = "false")
    protected boolean skipRun;

    /**
     * Run configuration for test execution on remote server.
     */
    @Parameter
    private RunConfiguration run;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (skipRun || run == null) {
            return;
        }

        if (!run.hasClasses() && !run.hasPackages()) {
            runAllTests();
        }

        if (run.hasClasses()) {
            runClasses(run.getClasses());
        }

        if (run.hasPackages()) {
            runPackages(run.getPackages());
        }
    }

    private void runPackages(List<String> packages) throws MojoExecutionException {
        for (String testPackage : packages) {
            HttpResponse response = null;
            try {
                response = getHttpClient().execute(RequestBuilder.get(getServer().getUrl() + "/run")
                        .addParameter("package", URLEncoder.encode(testPackage, "UTF-8"))
                        .build());

                if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                    throw new MojoExecutionException("Failed to run tests on remote server" + EntityUtils.toString(response.getEntity()));
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to run tests on remote server", e);
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        }
    }

    private void runClasses(List<String> classes) throws MojoExecutionException {
        for (String testClass : classes) {
            HttpResponse response = null;
            try {
                response = getHttpClient().execute(RequestBuilder.get(getServer().getUrl() + "/run")
                        .addParameter("class", URLEncoder.encode(testClass, "UTF-8"))
                        .build());

                if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                    throw new MojoExecutionException("Failed to run tests on remote server" + EntityUtils.toString(response.getEntity()));
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to run tests on remote server", e);
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        }
    }

    private void runAllTests() throws MojoExecutionException {
        HttpResponse response = null;
        try {
            response = getHttpClient().execute(RequestBuilder.get(getServer().getUrl() + "/run")
                    .addParameter("package", URLEncoder.encode(project.getGroupId() + ".*", "UTF-8"))
                    .build());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new MojoExecutionException("Failed to run tests on remote server" + EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to run tests on remote server", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    /**
     * Sets the tests.
     *
     * @param tests
     */
    public void setTests(RunConfiguration tests) {
        this.run = tests;
    }
}
