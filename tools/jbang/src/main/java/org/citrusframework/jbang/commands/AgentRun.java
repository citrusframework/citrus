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
package org.citrusframework.jbang.commands;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
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
import org.apache.hc.core5.util.Timeout;
import org.citrusframework.TestResult;
import org.citrusframework.agent.CitrusAgentConfiguration;
import org.citrusframework.agent.CitrusAgentSettings;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.jbang.JsonSupport;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.report.OutputStreamReporter;
import org.citrusframework.report.TestResults;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static java.util.stream.Collectors.joining;

@Command(name = "run", description = "Runs tests on the agent server")
public class AgentRun extends CitrusCommand {

    @Parameters(description = "Path to the test file (or a github link)", arity = "0..1",
            paramLabel = "<file>", parameterConsumer = FileConsumer.class)
    private Path filePath; // Defined only for file path completion; the field never used

    private String file;

    @Option(names = { "--engine" }, description = "Name of the test engine that is used ti run tests. One of junit, junit-jupiter, junit4, testng, cucumber")
    private String engine;

    @Option(names = { "--url" }, description = "Server endpoint URL to connect to.")
    private String url;

    @Option(names = { "--port" }, description = "Server port to connect to.")
    private String port;

    @Option(names = { "--polling-interval" }, defaultValue = "2000", description = "Interval used to poll for test results. Only used in asynchronous test execution mode.")
    private String pollingInterval;

    @Option(names = { "--timeout" }, defaultValue = "60000", description = "Http request timeout.")
    private String timeout;

    @Option(names = { "--async" }, description = "Should the test engine print verbose test summary information.")
    private boolean async;

    @Option(names = { "--background" }, description = "When enabled the command is not blocking for the test result response.")
    private boolean background;

    @Option(names = { "--verbose" }, defaultValue = "true", description = "Should the test engine print verbose test summary information.")
    private String verbose;

    @Option(names = { "--reset" }, defaultValue = "true", description = "Should the test engine reset the suite state for each run.")
    private String reset;

    @Option(names = { "--test-jar" }, description = "Path to a Java archive that holds tests to run.")
    private String testJar;

    @Option(names = { "--packages" }, arity = "0..*", description = "Test package name to include in the test run.")
    private String[] packages;

    @Option(names = { "--includes" }, arity = "0..*", description = "Includes test name pattern.")
    private String[] includes;

    @Option(names = { "--modules" }, description = "Comma delimited list of additional Citrus modules that should be loaded with the agent.")
    private String modules;

    @Option(names = { "--dep" }, arity = "0..*", description = "Set of additional Maven dependencies that should be loaded with the agent.")
    private String[] dependencies;

    @Option(names = { "--property" }, arity = "0..*", description = "Default System property to set before the test run.")
    private String[] properties;

    @Option(names = { "--work-directory" }, description = "The working directory used by the file based test engines to load file resources from.")
    private String workDir;

    public AgentRun(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() {
        return runTests();
    }

    private int runTests() {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            if (!doHealthCheck(httpClient, Boolean.parseBoolean(verbose))) {
                return 1;
            }

            TestRunConfiguration options = fromCliOptions(CitrusAgentConfiguration.fromEnvVars(TestSourceHelper::create));
            if (StringUtils.hasText(file)) {
                Resource sourceFile = Resources.create(file);
                String fileName = FileUtils.getFileName(file);
                return executeTest(fileName, sourceFile, httpClient, options);
            } else {
                return runTests(httpClient, options);
            }
        } catch (IOException e) {
            printer().printErr("Failed to connect to agent server " + getServerUrl() + ": " + e.getMessage());
            return 1;
        }
    }

    private int executeTest(String fileName, Resource sourceFile, HttpClient httpClient, TestRunConfiguration options) {
        try {
            ClassicRequestBuilder requestBuilder;

            printer().println("Running test '%s' on Citrus agent service: %s".formatted(fileName, getServerUrl()));

            if (async || background) {
                requestBuilder = ClassicRequestBuilder.put(
                        getServerUrl() + "/execute/%s".formatted(URLEncoder.encode(fileName, StandardCharsets.UTF_8)));
            } else {
                requestBuilder = ClassicRequestBuilder.post(
                        getServerUrl() + "/execute/%s".formatted(URLEncoder.encode(fileName, StandardCharsets.UTF_8)));
            }

            requestBuilder.addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()));

            String fileExt = FileUtils.getFileExtension(fileName);
            ContentType contentType = switch (fileExt) {
                case "json" -> ContentType.APPLICATION_JSON;
                case "xml" -> ContentType.APPLICATION_XML;
                case "yaml", "yml" -> ContentType.create("application/yaml", StandardCharsets.UTF_8);
                default -> ContentType.TEXT_PLAIN;
            };

            StringEntity body = new StringEntity(FileUtils.readToString(sourceFile), contentType);
            requestBuilder.setEntity(body);

            requestBuilder.addParameter("engine", options.getEngine());

            if (options.getWorkDir() != null) {
                requestBuilder.addParameter("workDir", URLEncoder.encode(options.getWorkDir(), StandardCharsets.UTF_8));
            }

            if (!options.getModules().isEmpty()) {
                requestBuilder.addParameter("modules", URLEncoder.encode(String.join(",", options.getModules()), StandardCharsets.UTF_8));
            }

            if (!options.getDependencies().isEmpty()) {
                requestBuilder.addParameter("deps", URLEncoder.encode(String.join(",", options.getDependencies()), StandardCharsets.UTF_8));
            }

            requestBuilder.addParameter("verbose", String.valueOf(options.isVerbose()));
            requestBuilder.addParameter("reset", String.valueOf(options.isReset()));

            try (var response = httpClient.executeOpen(null, requestBuilder.build(), null)) {
                if (HttpStatus.SC_OK != response.getCode()) {
                    printer().printErr("Failed to run tests on Citrus agent server: " + EntityUtils.toString(response.getEntity()));
                    return 1;
                }

                if (background) {
                    return 0;
                }

                if (async) {
                    RemoteResult[] results = pollTestResults(httpClient);
                    handleTestResults(Arrays.stream(results)
                            .map(RemoteResult::toTestResult)
                            .toList()
                            .toArray(TestResult[]::new));
                } else {
                    handleTestResults(Arrays.stream(JsonSupport.json().readValue(response.getEntity().getContent(), RemoteResult[].class))
                            .map(RemoteResult::toTestResult)
                            .toList()
                            .toArray(TestResult[]::new));
                }
            }
        } catch (IOException | ParseException e) {
            printer().printErr("Failed to run tests on Citrus agent server", e);
            return 1;
        }

        return 0;
    }

    /**
     * Invokes run tests Citrus agent service and provide response message. If async mode is used the service is called with request method PUT
     * that creates a new run job on the server. The test results are then polled with multiple requests instead of processing the single synchronous response.
     */
    private int runTests(HttpClient httpClient, TestRunConfiguration options) {
        try {
            ClassicRequestBuilder requestBuilder;

            printer().println("Running tests on Citrus agent service: %s".formatted(getServerUrl()));

            if (async) {
                requestBuilder = ClassicRequestBuilder.put(getServerUrl() + "/run");
            } else {
                requestBuilder = ClassicRequestBuilder.post(getServerUrl() + "/run");
            }

            requestBuilder.addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()));

            StringEntity body = new StringEntity(JsonSupport.json().writeValueAsString(options), ContentType.APPLICATION_JSON);
            requestBuilder.setEntity(body);

            try (var response = httpClient.executeOpen(null, requestBuilder.build(), null)) {
                if (HttpStatus.SC_OK != response.getCode()) {
                    printer().printErr("Failed to run tests on Citrus agent server: " + EntityUtils.toString(response.getEntity()));
                    return 1;
                }

                if (async) {
                    handleTestResults(Arrays.stream(pollTestResults(httpClient))
                            .map(RemoteResult::toTestResult)
                            .toList()
                            .toArray(TestResult[]::new));
                } else {
                    handleTestResults(Arrays.stream(JsonSupport.json().readValue(response.getEntity().getContent(), RemoteResult[].class))
                            .map(RemoteResult::toTestResult)
                            .toList()
                            .toArray(TestResult[]::new));
                }
            }
        } catch (IOException | ParseException e) {
            printer().printErr("Failed to run tests on Citrus agent server", e);
            return 1;
        }

        return 0;
    }

    private boolean doHealthCheck(HttpClient httpClient, boolean verbose) {
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(getServerUrl() + "/health").build();
        try (var response = httpClient.executeOpen(null, httpRequest, null)) {
            return HttpStatus.SC_OK == response.getCode();
        } catch (IOException e) {
            if (verbose) {
                printer().println("Health check failed for Citrus agent server url '%s/health'".formatted(getServerUrl()));
            }
        }

        return false;
    }

    private CloseableHttpClient getHttpClient() {
        Timeout timoutMillis = Timeout.ofMilliseconds(Integer.parseInt(timeout));

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(timoutMillis)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(timoutMillis)
                                .setResponseTimeout(timoutMillis)
                                .build())
                .build();
    }

    private String getServerUrl() {
        if (!StringUtils.hasText(url)) {
            url = "http://localhost:" + Optional.ofNullable(port).map(Integer::parseInt)
                    .orElse(CitrusAgentSettings.getServerPort());
        }

        return url;
    }

    /**
     * When using async test execution mode the client does not synchronously wait for test results as it might lead to read timeouts. Instead
     * this method polls for test results and waits for the test execution to completely finish.
     */
    private RemoteResult[] pollTestResults(HttpClient httpClient) throws IOException {
        ClassicHttpResponse response = null;
        try {
            do {
                if (response != null) {
                    response.close();
                }

                ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(getServerUrl() + "/results/latest")
                        .addHeader(new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                        .addParameter("timeout", pollingInterval)
                        .build();
                response = httpClient.executeOpen(null, httpRequest, null);

                if (HttpStatus.SC_PARTIAL_CONTENT == response.getCode()) {
                    printer().println("Waiting for Citrus agent tests to finish ...");
                    printer().println(Stream.of(JsonSupport.json().readValue(response.getEntity().getContent(), RemoteResult[].class))
                            .filter(Objects::nonNull)
                            .map(RemoteResult::toSimpleString)
                            .collect(joining()));
                }
            } while (HttpStatus.SC_PARTIAL_CONTENT == response.getCode());

            if (HttpStatus.SC_OK != response.getCode()) {
                printer().printErr("Failed to get test results from Citrus agent server: Http response %d %s - %s"
                        .formatted(response.getCode(), response.getReasonPhrase(), EntityUtils.toString(response.getEntity())));
                return new RemoteResult[0];
            }

            return JsonSupport.json().readValue(response.getEntity().getContent(), RemoteResult[].class);
        } catch (IOException | ParseException e) {
            printer().printErr("Failed to get test results from Citrus agent server", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return new RemoteResult[0];
    }

    /**
     * Check test results for failures.
     */
    private void handleTestResults(TestResult[] results) {
        StringWriter resultWriter = new StringWriter();
        resultWriter.append(String.format("%n"));

        TestResults testResults = new TestResults();
        Arrays.stream(results).forEach(testResults::addResult);

        OutputStreamReporter reporter = new OutputStreamReporter(resultWriter);
        reporter.generate(testResults);
        printer().println(resultWriter.toString());
    }

    private TestRunConfiguration fromCliOptions(TestRunConfiguration configuration) {
        if (StringUtils.hasText(engine)) {
            configuration.setEngine(engine);
        }

        if (StringUtils.hasText(verbose)) {
            configuration.setVerbose(Boolean.parseBoolean(verbose));
        }

        if (StringUtils.hasText(reset)) {
            configuration.setReset(Boolean.parseBoolean(reset));
        }

        if (StringUtils.hasText(testJar)) {
            configuration.setTestJar(Resources.create(testJar).file());
        }

        if (includes != null) {
            configuration.setIncludes(includes);
        }

        if (workDir != null) {
            configuration.setWorkDir(workDir);
        }

        if (packages != null) {
            configuration.setPackages(List.of(packages));
        }

        if (properties != null) {
            configuration.addDefaultProperties(Arrays.stream(properties)
                    .filter(p -> p.contains("="))
                    .map(p -> p.split("=", 2))
                    .collect(Collectors.toMap(p -> p[0], p -> p[1])));
        }

        if (StringUtils.hasText(modules)) {
            configuration.setModules(Arrays.stream(modules.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet()));
        }

        if (dependencies != null) {
            configuration.setDependencies(Arrays.stream(dependencies)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet()));
        }

        return configuration;
    }

    static class FileConsumer extends CitrusCommand.ParameterConsumer<AgentRun> {
        @Override
        protected void doConsumeParameters(Stack<String> args, AgentRun cmd) {
            if (!args.isEmpty()) {
                cmd.file = args.pop();
            }
        }
    }

    /**
     * Test result that is able to serialize/deserialize from Json objects.
     */
    public static class RemoteResult {

        /** Result as String */
        private String result;

        /** Name of the test */
        private String testName;

        /** Fully qualified test class name */
        private String className;

        /** Duration of the test run */
        private Long duration;

        /** Failure cause */
        private String cause;

        /** Failure message */
        private String errorMessage;

        /** Failure cause */
        private String failureStack;

        private boolean success;
        private boolean failed;
        private boolean skipped;

        /**
         * Convert remote result to traditional result.
         */
        public static TestResult toTestResult(RemoteResult remoteResult) {
            TestResult result;
            if (remoteResult.isSuccess()) {
                result = TestResult.success(remoteResult.getTestName(), remoteResult.getClassName());
            } else if (remoteResult.isSkipped()) {
                result = TestResult.skipped(remoteResult.getTestName(), remoteResult.getClassName());
            } else if (remoteResult.isFailed()) {
                // TODO: Check if this is fine, failure stack, failure type are never used in the new Citrus version
                result = TestResult
                        .failed(
                                remoteResult.getTestName(),
                                remoteResult.getClassName(),
                                remoteResult.getErrorMessage())
                        .withFailureType(remoteResult.getCause());
            } else {
                throw new CitrusRuntimeException(
                        "Unexpected test result state " + remoteResult.getTestName());
            }
            return result.withDuration(Duration.ofMillis(remoteResult.getDuration()));
        }

        /**
         * Converts result to simple string based on success or failed state.
         */
        public String toSimpleString() {
            if (isSkipped()) {
                return "o";
            } else if (isSuccess()) {
                return "+";
            }

            return "-";
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getCause() {
            return cause;
        }

        public void setCause(String cause) {
            this.cause = cause;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getFailureStack() {
            return failureStack;
        }

        public void setFailureStack(String failureStack) {
            this.failureStack = failureStack;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean isFailed() {
            return failed;
        }

        public void setFailed(boolean failed) {
            this.failed = failed;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public void setSkipped(boolean skipped) {
            this.skipped = skipped;
        }
    }
}
