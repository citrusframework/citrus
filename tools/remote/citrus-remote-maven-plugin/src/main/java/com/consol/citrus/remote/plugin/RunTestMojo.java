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

import com.consol.citrus.TestClass;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.main.TestRunConfiguration;
import com.consol.citrus.remote.model.RemoteResult;
import com.consol.citrus.remote.plugin.config.RunConfiguration;
import com.consol.citrus.report.*;
import com.consol.citrus.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
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
        TestRunConfiguration runConfiguration = new TestRunConfiguration();

        runConfiguration.setPackages(packages);

        if (run.getIncludes() != null) {
            runConfiguration.setIncludes(run.getIncludes().toArray(new String[run.getIncludes().size()]));
        }

        if (run.getSystemProperties() != null) {
            runConfiguration.addDefaultProperties(run.getSystemProperties());
        }

        runTests(runConfiguration);
    }

    private void runClasses(List<String> classes) throws MojoExecutionException {
        TestRunConfiguration runConfiguration = new TestRunConfiguration();

        runConfiguration.setTestClasses(classes.stream()
                                                .map(TestClass::fromString)
                                                .collect(Collectors.toList()));

        if (run.getSystemProperties() != null) {
            runConfiguration.addDefaultProperties(run.getSystemProperties());
        }

        runTests(runConfiguration);
    }

    private void runAllTests() throws MojoExecutionException {
        TestRunConfiguration runConfiguration = new TestRunConfiguration();

        if (run.getIncludes() != null) {
            runConfiguration.setIncludes(run.getIncludes().toArray(new String[run.getIncludes().size()]));
        }

        if (run.getSystemProperties() != null) {
            runConfiguration.addDefaultProperties(run.getSystemProperties());
        }

        runTests(runConfiguration);
    }

    /**
     * Invokes run tests remote service and provide response message. If async mode is used the service is called with request method PUT
     * that creates a new run job on the server. The test results are then polled with multiple requests instead of processing the single synchronous response.
     *
     * @param runConfiguration
     * @return
     * @throws MojoExecutionException
     */
    private void runTests(TestRunConfiguration runConfiguration) throws MojoExecutionException {
        HttpResponse response = null;

        try {
            RequestBuilder requestBuilder;

            if (run.isAsync()) {
                requestBuilder = RequestBuilder.put(getServer().getUrl() + "/run");
            } else {
                requestBuilder = RequestBuilder.post(getServer().getUrl() + "/run");
            }

            requestBuilder.addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()));

            StringEntity body = new StringEntity(new ObjectMapper().writeValueAsString(runConfiguration), ContentType.APPLICATION_JSON);
            requestBuilder.setEntity(body);

            response = getHttpClient().execute(requestBuilder.build());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new MojoExecutionException("Failed to run tests on remote server: " + EntityUtils.toString(response.getEntity()));
            }

            if (run.isAsync()) {
                HttpClientUtils.closeQuietly(response);
                handleTestResults(pollTestResults());
            } else {
                handleTestResults(objectMapper.readValue(response.getEntity().getContent(), RemoteResult[].class));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to run tests on remote server", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    /**
     * When using async test execution mode the client does not synchronously wait for test results as it might lead to read timeouts. Instead
     * this method polls for test results and waits for the test execution to completely finish.
     *
     * @return
     * @throws MojoExecutionException
     */
    private RemoteResult[] pollTestResults() throws MojoExecutionException {
        HttpResponse response = null;
        try {
            do {
                HttpClientUtils.closeQuietly(response);
                response = getHttpClient().execute(RequestBuilder.get(getServer().getUrl() + "/results")
                        .addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                        .addParameter("timeout", String.valueOf(run.getPollingInterval()))
                        .build());

                if (HttpStatus.SC_PARTIAL_CONTENT == response.getStatusLine().getStatusCode()) {
                    getLog().info("Waiting for remote tests to finish ...");
                    getLog().info(Stream.of(objectMapper.readValue(response.getEntity().getContent(), RemoteResult[].class))
                            .map(RemoteResult::toTestResult).map(result -> result.isSkipped() ? "x" : (result.isSuccess() ? "+" : "-")).collect(Collectors.joining()));
                }
            } while (HttpStatus.SC_PARTIAL_CONTENT == response.getStatusLine().getStatusCode());

            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new MojoExecutionException("Failed to get test results from remote server: " + EntityUtils.toString(response.getEntity()));
            }

            return objectMapper.readValue(response.getEntity().getContent(), RemoteResult[].class);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to get test results from remote server", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    /**
     * Check test results for failures.
     * @param results
     * @throws IOException
     */
    private void handleTestResults(RemoteResult[] results) {
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
                    .addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType()))
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

        File citrusReportsDirectory = new File(getOutputDirectory() + File.separator + getReport().getDirectory());
        if (!citrusReportsDirectory.exists()) {
            if (!citrusReportsDirectory.mkdirs()) {
                throw new CitrusRuntimeException("Unable to create reports output directory: " + citrusReportsDirectory.getPath());
            }
        }

        File junitReportsDirectory = new File(citrusReportsDirectory, "junitreports");
        if (!junitReportsDirectory.exists()) {
            if (!junitReportsDirectory.mkdirs()) {
                throw new CitrusRuntimeException("Unable to create JUnit reports directory: " + junitReportsDirectory.getPath());
            }
        }

        JUnitReporter jUnitReporter = new JUnitReporter();
        loadAndSaveReportFile(new File(citrusReportsDirectory, String.format(jUnitReporter.getReportFileNamePattern(), jUnitReporter.getSuiteName())), getServer().getUrl() + "/results/suite", ContentType.APPLICATION_XML.getMimeType());

        Stream.of(reportFiles)
            .map(reportFile -> new File(junitReportsDirectory, reportFile))
            .forEach(reportFile -> {
                try {
                    loadAndSaveReportFile(reportFile, getServer().getUrl() + "/results/file/" + URLEncoder.encode(reportFile.getName(), ENCODING), ContentType.APPLICATION_XML.getMimeType());
                } catch (IOException e) {
                    getLog().warn("Failed to get report file: " + reportFile.getName(), e);
                }
            });
    }

    /**
     * Get report file content from server and save content to given file on local file system.
     * @param reportFile
     * @param serverUrl
     * @param contentType
     */
    private void loadAndSaveReportFile(File reportFile, String serverUrl, String contentType) {
        HttpResponse fileResponse = null;
        try {
            fileResponse = getHttpClient().execute(RequestBuilder.get(serverUrl)
                    .addHeader(new BasicHeader(HttpHeaders.ACCEPT, contentType))
                    .build());

            if (HttpStatus.SC_OK != fileResponse.getStatusLine().getStatusCode()) {
                getLog().warn("Failed to get report file: " + reportFile.getName());
                return;
            }

            getLog().info("Writing report file: " + reportFile);
            FileUtils.writeToFile(fileResponse.getEntity().getContent(), reportFile);
        } catch (IOException e) {
            getLog().warn("Failed to get report file: " + reportFile.getName(), e);
        } finally {
            HttpClientUtils.closeQuietly(fileResponse);
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
