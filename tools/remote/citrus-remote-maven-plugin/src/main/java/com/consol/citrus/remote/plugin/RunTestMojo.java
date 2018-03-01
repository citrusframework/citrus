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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.remote.model.RemoteResult;
import com.consol.citrus.remote.plugin.config.RunConfiguration;
import com.consol.citrus.report.*;
import com.consol.citrus.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo(name = "test", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class RunTestMojo extends AbstractCitrusRemoteMojo {

    /** Global url encoding */
    private static final String ENCODING = "UTF-8";

    @Parameter(property = "citrus.remote.skip.test", defaultValue = "false")
    protected boolean skipRun;

    /**
     * Run configuration for test execution on remote server.
     */
    @Parameter
    private RunConfiguration run;

    /**
     * Object mapper for JSON response to object conversion.
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (skipRun) {
            return;
        }

        if (run == null) {
            run = new RunConfiguration();
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
                RequestBuilder requestBuilder = RequestBuilder.get(getServer().getUrl() + "/run")
                                                            .addHeader(new BasicHeader(HttpHeaders.ACCEPT, "application/json"))
                                                            .addParameter("package", URLEncoder.encode(testPackage, ENCODING));

                if (run.getIncludes() != null) {
                    requestBuilder.addParameter("includes", URLEncoder.encode(run.getIncludes().stream().collect(Collectors.joining(",")), ENCODING));
                }

                response = getHttpClient().execute(requestBuilder.build());

                if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                    throw new MojoExecutionException("Failed to run tests on remote server" + EntityUtils.toString(response.getEntity()));
                }

                handleTestResults(response);
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
                        .addHeader(new BasicHeader(HttpHeaders.ACCEPT, "application/json"))
                        .addParameter("class", URLEncoder.encode(testClass, ENCODING))
                        .build());

                if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                    throw new MojoExecutionException("Failed to run tests on remote server" + EntityUtils.toString(response.getEntity()));
                }

                handleTestResults(response);
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
            RequestBuilder requestBuilder = RequestBuilder.get(getServer().getUrl() + "/run")
                                                        .addHeader(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));

            if (run.getIncludes() != null) {
                requestBuilder.addParameter("includes", URLEncoder.encode(run.getIncludes().stream().collect(Collectors.joining(",")), ENCODING));
            }

            response = getHttpClient().execute(requestBuilder.build());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new MojoExecutionException("Failed to run tests on remote server" + EntityUtils.toString(response.getEntity()));
            }

            handleTestResults(response);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to run tests on remote server", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    /**
     * Check test results for failures.
     * @param response
     * @throws IOException
     */
    private void handleTestResults(HttpResponse response) throws IOException {
        RemoteResult[] results = objectMapper.readValue(response.getEntity().getContent(), RemoteResult[].class);

        StringWriter resultWriter = new StringWriter();
        resultWriter.append(String.format("%n"));
        OutputStreamReporter reporter = new OutputStreamReporter(resultWriter);
        Stream.of(results).forEach(result -> reporter.getTestResults().addResult(RemoteResult.toTestResult(result)));
        reporter.generateTestResults();
        getLog().info(resultWriter.toString());

        if (getReport().isHtmlReport()) {
            HtmlReporter htmlReporter = new HtmlReporter();
            htmlReporter.setReportDirectory(getOutputDirectory().getPath() + File.separator + getReport().getDirectory());
            Stream.of(results).forEach(result -> htmlReporter.getTestResults().addResult(RemoteResult.toTestResult(result)));
            htmlReporter.generateTestResults();
        }

        SummaryReporter summaryReporter = new SummaryReporter();
        Stream.of(results).forEach(result -> summaryReporter.getTestResults().addResult(RemoteResult.toTestResult(result)));
        summaryReporter.setReportDirectory(getOutputDirectory().getPath() + File.separator + getReport().getDirectory());
        summaryReporter.setReportFileName(getReport().getSummaryFile());
        summaryReporter.generateTestResults();

        getAndSaveReports();
    }

    private void getAndSaveReports() {
        if (!getReport().isSaveReportFiles()) {
            return;
        }

        HttpResponse response = null;
        String[] reportFiles = {};
        try {
            response = getHttpClient().execute(RequestBuilder.get(getServer().getUrl() + "/results/files")
                    .addHeader(new BasicHeader(HttpHeaders.ACCEPT, "application/xml"))
                    .build());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                getLog().warn("Failed to get test reports from remote server");
            }

            reportFiles = objectMapper.readValue(response.getEntity().getContent(), String[].class);
        } catch (IOException e) {
            getLog().warn("Failed to get test reports from remote server", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        File junitReportsDirectory = new File(getOutputDirectory() + File.separator + getReport().getDirectory(), "junitreports");

        if (!junitReportsDirectory.exists()) {
            if (!junitReportsDirectory.mkdirs()) {
                throw new CitrusRuntimeException("Unable to create message JUnit reports output directory: " + junitReportsDirectory.getPath());
            }
        }

        Stream.of(reportFiles)
            .map(reportFile -> new File(junitReportsDirectory, reportFile))
            .forEach(reportFile -> {
                HttpResponse fileResponse = null;
                try {
                    fileResponse = getHttpClient().execute(RequestBuilder.get(getServer().getUrl() + "/results/file/" + URLEncoder.encode(reportFile.getName(), ENCODING))
                            .addHeader(new BasicHeader(HttpHeaders.ACCEPT, "application/xml"))
                            .build());

                    if (HttpStatus.SC_OK != fileResponse.getStatusLine().getStatusCode()) {
                        getLog().warn("Failed to get report file for test: " + reportFile.getName());
                    }

                    getLog().info("Writing report file: " + reportFile);
                    FileUtils.writeToFile(fileResponse.getEntity().getContent(), reportFile);
                } catch (IOException e) {
                    getLog().warn("Failed to get report file for test: " + reportFile.getName(), e);
                } finally {
                    HttpClientUtils.closeQuietly(fileResponse);
                }
            });
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
