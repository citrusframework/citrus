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
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.citrusframework.TestClass;
import org.citrusframework.TestResult;
import org.citrusframework.TestSource;
import org.citrusframework.agent.plugin.config.KubernetesConfiguration;
import org.citrusframework.agent.plugin.config.RunConfiguration;
import org.citrusframework.agent.plugin.model.RemoteResult;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporterSettings;
import org.citrusframework.report.OutputStreamReporter;
import org.citrusframework.report.SummaryReporter;
import org.citrusframework.report.TestResults;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.StringUtils;

import static java.util.stream.Collectors.joining;

/**
 * Run tests on the connected Citrus agent server.
 */
@Mojo(name = "test", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class RunTestMojo extends AbstractAgentMojo {

    /** Global url encoding */
    private static final String ENCODING = "UTF-8";

    @Parameter(property = "citrus.agent.skip.test", defaultValue = "false")
    protected boolean skipRun;

    /**
     * Run configuration for test execution via agent service.
     */
    @Parameter
    private RunConfiguration run;

    @Parameter
    private KubernetesConfiguration kubernetes;

    private LocalPortForward portForward;

    /**
     * Object mapper for JSON response to object conversion.
     */
    private ObjectMapper mapper;

    @Override
    public void doExecute() throws MojoExecutionException {
        if (skipRun) {
            getLog().info("Citrus agent tests are skipped.");
            return;
        }

        try {
            if (isConnected(getServer().getConnectTimeout() / 10, TimeUnit.MILLISECONDS, false)) {
                getLog().info("Using Citrus agent service connection: %s".formatted(getServer().getUrl()));
            } else if (connect()) {
                getLog().info("Connected to Citrus agent service: %s".formatted(getServer().getUrl()));
            } else {
                throw new MojoExecutionException("Not connected to Citrus agent service");
            }

            if (!getRunConfig().hasClasses() && !getRunConfig().hasPackages()) {
                runAllTests();
            }

            if (getRunConfig().hasClasses()) {
                runClasses(getRunConfig().getClasses());
            }

            if (getRunConfig().hasPackages()) {
                runPackages(getRunConfig().getPackages());
            }
        } catch (Exception e) {
            // Do not throw any exception so following post-integration-phase is run
            // Verify goal will fail the Maven build in case of errors
            getLog().error("Failed to run tests via Citrus agent server", e);
        } finally {
            disconnect();
        }
    }

    private boolean connect() {
        if (!getServer().isAutoConnect()) {
            return false;
        }

        if (getKubernetes().isEnabled()) {
            KubernetesClient k8s = getKubernetes().getKubernetesClient();
            String ns = getKubernetes().getNamespace(k8s, getLog());

            if (StringUtils.hasText(getServer().getLocalPort())) {
                portForward = k8s.services()
                        .inNamespace(ns)
                        .withName(getServer().getName())
                        .portForward(8080, Integer.parseInt(getServer().getLocalPort()));
            } else {
                portForward = k8s.services()
                        .inNamespace(ns)
                        .withName(getServer().getName())
                        .portForward(8080);
            }

            getServer().setUrl("http://localhost:%d".formatted(portForward.getLocalPort()));
        }

        return isConnected(getServer().getConnectTimeout(), TimeUnit.MILLISECONDS, true);
    }

    private void disconnect() {
        if (getKubernetes().isEnabled()) {
            if (portForward != null && portForward.isAlive()) {
                try {
                    portForward.close();
                } catch (IOException e) {
                    getLog().warn("Error closing local port forward to Kubernetes Citrus agent service");
                }
            }
            getKubernetes().getKubernetesClient().close();
        }
    }

    private boolean isConnected(long timeout, TimeUnit unit, boolean verbose) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> verified = executorService.submit(() -> {
                while (!doHealthCheck(verbose)) {
                    try {
                        if (verbose) {
                            getLog().info("Citrus agent server health check - retrying ...");
                        }
                        Thread.sleep(unit.toMillis(timeout / 5));
                    } catch (InterruptedException e) {
                        getLog().warn("Interrupted while waiting for next health check attempt");
                    }
                }

                return true;
            });

            return verified.get(timeout, unit);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            getLog().warn("Citrus agent server health check failed - %s %s".formatted(e.getClass().getName(), Optional.ofNullable(e.getMessage()).orElse("")));
            return false;
        } finally {
            executorService.shutdownNow();
        }
    }

    private boolean doHealthCheck(boolean verbose) {
        String url = getServer().getUrl();
        if (url != null) {
            ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(url + "/health").build();
            try (var response = getHttpClient().executeOpen(null, httpRequest, null)) {
                return HttpStatus.SC_OK == response.getCode();
            } catch (IOException e) {
                if (verbose) {
                    getLog().info("Health check failed for Citrus agent server url '%s/health'".formatted(url));
                }
            }
        }

        return false;
    }

    private void runPackages(List<String> packages) throws MojoExecutionException {
        TestRunConfiguration options = new TestRunConfiguration();

        options.setEngine(getRunConfig().getEngine());
        options.setPackages(packages);

        if (getRunConfig().getIncludes() != null) {
            options.setIncludes(getRunConfig().getIncludes().toArray(new String[0]));
        }

        if (getRunConfig().getSystemProperties() != null) {
            options.addDefaultProperties(getRunConfig().getSystemProperties());
        }

        if (getRunConfig().getSources() != null) {
            options.setTestSources(getRunConfig().getSources().stream().map(TestSourceHelper::create).toList());
        }

        runTests(options);
    }

    private void runClasses(List<String> classes) throws MojoExecutionException {
        TestRunConfiguration options = new TestRunConfiguration();

        options.setEngine(getRunConfig().getEngine());

        List<TestSource> testSources = classes.stream()
                .map(TestClass::fromString)
                .map(testClass -> (TestSource) testClass)
                .toList();
        options.setTestSources(testSources);

        if (getRunConfig().getSystemProperties() != null) {
            options.addDefaultProperties(getRunConfig().getSystemProperties());
        }

        if (getRunConfig().getSources() != null) {
            options.setTestSources(getRunConfig().getSources().stream().map(TestSourceHelper::create).toList());
        }

        runTests(options);
    }

    private void runAllTests() throws MojoExecutionException {
        TestRunConfiguration options = new TestRunConfiguration();

        options.setEngine(getRunConfig().getEngine());
        if (getRunConfig().getIncludes() != null) {
            options.setIncludes(getRunConfig().getIncludes().toArray(new String[0]));
        }

        if (getRunConfig().getSystemProperties() != null) {
            options.addDefaultProperties(getRunConfig().getSystemProperties());
        }

        if (getRunConfig().getSources() != null) {
            options.setTestSources(getRunConfig().getSources().stream().map(TestSourceHelper::create).toList());
        }

        runTests(options);
    }

    /**
     * Invokes run tests Citrus agent service and provide response message. If async mode is used the service is called with request method PUT
     * that creates a new run job on the server. The test results are then polled with multiple requests instead of processing the single synchronous response.
     *
     * @param options
     * @throws MojoExecutionException
     */
    private void runTests(TestRunConfiguration options) throws MojoExecutionException {
        try {
            ClassicRequestBuilder requestBuilder;

            getLog().info("Running tests on Citrus agent service: %s".formatted(getServer().getUrl()));

            if (getRunConfig().isAsync()) {
                requestBuilder = ClassicRequestBuilder.put(getServer().getUrl() + "/run");
            } else {
                requestBuilder = ClassicRequestBuilder.post(getServer().getUrl() + "/run");
            }

            requestBuilder.addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()));

            StringEntity body = new StringEntity(getObjectMapper().writeValueAsString(options), ContentType.APPLICATION_JSON);
            requestBuilder.setEntity(body);

            try (var response = getHttpClient().executeOpen(null, requestBuilder.build(), null)) {
                if (HttpStatus.SC_OK != response.getCode()) {
                    throw new MojoExecutionException("Failed to run tests on Citrus agent server: " + EntityUtils.toString(response.getEntity()));
                }

                if (getRunConfig().isAsync()) {
                    handleTestResults(Arrays.stream(pollTestResults())
                            .map(RemoteResult::toTestResult)
                            .toList()
                            .toArray(TestResult[]::new));
                } else {
                    handleTestResults(Arrays.stream(getObjectMapper().readValue(response.getEntity().getContent(), RemoteResult[].class))
                            .map(RemoteResult::toTestResult)
                            .toList()
                            .toArray(TestResult[]::new));
                }
            }
        } catch (IOException | ParseException e) {
            throw new MojoExecutionException("Failed to run tests on Citrus agent server", e);
        }
    }

    private ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = JsonMapper.builder()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                    .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                    .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                    .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
                    .addMixIn(Resource.class, IgnoreTypeMixIn.class)
                    .build()
                    .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
        }

        return mapper;
    }

    /**
     * When using async test execution mode the client does not synchronously wait for test results as it might lead to read timeouts. Instead
     * this method polls for test results and waits for the test execution to completely finish.
     *
     * @throws MojoExecutionException
     */
    private RemoteResult[] pollTestResults() throws MojoExecutionException, IOException {
        ClassicHttpResponse response = null;
        try {
            do {
                if (response != null) {
                    response.close();
                }

                ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(getServer().getUrl() + "/results/latest")
                        .addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                        .addParameter("timeout", String.valueOf(getRunConfig().getPollingInterval()))
                        .build();
                response = getHttpClient().executeOpen(null, httpRequest, null);

                if (HttpStatus.SC_PARTIAL_CONTENT == response.getCode()) {
                    getLog().info("Waiting for Citrus agent tests to finish ...");
                    getLog().info(Stream.of(getObjectMapper().readValue(response.getEntity().getContent(), RemoteResult[].class))
                            .filter(Objects::nonNull)
                            .map(RunTestMojo::parseResultToStringRepresentation)
                            .collect(joining()));
                }
            } while (HttpStatus.SC_PARTIAL_CONTENT == response.getCode());

            if (HttpStatus.SC_OK != response.getCode()) {
                throw new MojoExecutionException("Failed to get test results from Citrus agent server: Http response %d %s - %s"
                        .formatted(response.getCode(), response.getReasonPhrase(), EntityUtils.toString(response.getEntity())));
            }

            return getObjectMapper().readValue(response.getEntity().getContent(), RemoteResult[].class);
        } catch (IOException | ParseException e) {
            throw new MojoExecutionException("Failed to get test results from Citrus agent server", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Check test results for failures.
     * @param results
     * @throws IOException
     */
    private void handleTestResults(TestResult[] results) throws IOException, MojoExecutionException {
        StringWriter resultWriter = new StringWriter();
        resultWriter.append(String.format("%n"));

        TestResults testResults = new TestResults();
        Arrays.stream(results).forEach(testResults::addResult);

        OutputStreamReporter reporter = new OutputStreamReporter(resultWriter);
        reporter.generate(testResults);
        getLog().info(resultWriter.toString());

        if (getReport().isHtmlReport()) {
            HtmlReporter htmlReporter = new HtmlReporter();
            htmlReporter.setReportDirectory(getOutputDirectory().getPath() + File.separator + getReport().getDirectory());
            htmlReporter.generate(testResults);
        }

        SummaryReporter summaryReporter = new SummaryReporter();
        summaryReporter.setReportDirectory(getOutputDirectory().getPath() + File.separator + getReport().getDirectory());
        summaryReporter.setReportFileName(getReport().getSummaryFile());
        summaryReporter.generate(testResults);

        if (getReport().isSaveReportFiles()) {
            getAndSaveReports();
        }
    }

    private void getAndSaveReports() throws MojoExecutionException {
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(getServer().getUrl() + "/results/files")
                .addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType()))
                .build();

        String[] reportFiles = {};
        try (var response = getHttpClient().executeOpen(null, httpRequest, null)){
            if (HttpStatus.SC_OK != response.getCode()) {
                getLog().warn("Failed to get test reports from Citrus agent server");
            }

            reportFiles = getObjectMapper().readValue(response.getEntity().getContent(), String[].class);
        } catch (IOException e) {
            getLog().warn("Failed to get test reports from Citrus agent server", e);
        }

        File citrusReportsDirectory = new File(getOutputDirectory() + File.separator + getReport().getDirectory());
        if (!citrusReportsDirectory.exists()&& !citrusReportsDirectory.mkdirs()) {
            throw new MojoExecutionException("Unable to create reports output directory: " + citrusReportsDirectory.getPath());
        }

        String suiteReportFile = String.format(JUnitReporterSettings.getReportFilePattern(), JUnitReporterSettings.getSuiteName());
        if (Stream.of(reportFiles).noneMatch(suiteReportFile::equals)) {
            loadAndSaveReportFile(new File(citrusReportsDirectory, suiteReportFile), getServer().getUrl() + "/results/suite", ContentType.APPLICATION_XML.getMimeType());
        }

        Stream.of(reportFiles)
            .filter(f -> !f.contains("/"))
            .map(reportFile -> new File(citrusReportsDirectory, reportFile))
            .forEach(reportFile -> {
                try {
                    loadAndSaveReportFile(reportFile, getServer().getUrl() + "/results/file/" + URLEncoder.encode(reportFile.getName(), ENCODING), ContentType.APPLICATION_XML.getMimeType());
                } catch (IOException e) {
                    getLog().warn("Failed to get report file: " + reportFile.getName(), e);
                }
            });

        Stream.of(reportFiles)
            .filter(f -> f.contains("/"))
            .map(f -> f.split("/", 2))
            .forEach(tokens -> {
                try {
                    File subDir = new File(citrusReportsDirectory, tokens[0]);
                    File reportFile = new File(subDir, tokens[1]);
                    loadAndSaveReportFile(reportFile, getServer().getUrl() + "/results/file/%s?folder=%s".formatted(URLEncoder.encode(reportFile.getName(), ENCODING), URLEncoder.encode(subDir.getName(), ENCODING)), ContentType.APPLICATION_XML.getMimeType());
                } catch (IOException e) {
                    getLog().warn("Failed to get report file: %s/%s".formatted(tokens[0], tokens[1]), e);
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
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(serverUrl)
                .addHeader(new BasicHeader(HttpHeaders.ACCEPT, contentType))
                .build();

        getLog().info("Loading report file: " + serverUrl);

        try (var fileResponse = getHttpClient().executeOpen(null, httpRequest, null)) {
            if (HttpStatus.SC_OK != fileResponse.getCode()) {
                getLog().warn("Failed to get report file: %s - Http response %d %s".formatted(reportFile.getName(), fileResponse.getCode(), fileResponse.getReasonPhrase()));
                return;
            }

            getLog().info("Writing report file: " + reportFile);
            if (!reportFile.getParentFile().exists() && !reportFile.getParentFile().mkdirs()) {
                getLog().warn("Failed to create report directory: %s".formatted(reportFile.getParent()));
            }
            Files.write(reportFile.toPath(), fileResponse.getEntity().getContent().readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            getLog().warn("Failed to get report file: " + reportFile.getName(), e);
        }
    }

    private static String parseResultToStringRepresentation(RemoteResult result) {
        if (result.isSkipped()) {
            return "o";
        } else if (result.isSuccess()) {
            return "+";
        }

        return "-";
    }

    public RunConfiguration getRunConfig() {
        if (run == null) {
            run = new RunConfiguration();
        }

        return run;
    }

    public KubernetesConfiguration getKubernetes() {
        if (kubernetes == null) {
            kubernetes = new KubernetesConfiguration();
        }

        return kubernetes;
    }

    @JsonIgnoreType
    private static class IgnoreTypeMixIn {
    }
}
