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

package com.consol.citrus.remote;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestClass;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.main.CitrusAppConfiguration;
import com.consol.citrus.main.TestRunConfiguration;
import com.consol.citrus.remote.controller.RunController;
import com.consol.citrus.remote.job.RunJob;
import com.consol.citrus.remote.model.RemoteResult;
import com.consol.citrus.remote.reporter.RemoteTestResultReporter;
import com.consol.citrus.remote.transformer.JsonRequestTransformer;
import com.consol.citrus.remote.transformer.JsonResponseTransformer;
import com.consol.citrus.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import spark.Filter;
import spark.servlet.SparkApplication;

import java.io.File;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static spark.Spark.*;

/**
 * Remote application creates routes for this web application.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusRemoteApplication implements SparkApplication {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusRemoteApplication.class);

    /** Global url encoding */
    private static final String ENCODING = "UTF-8";
    /** Content types */
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";

    /** Application configuration */
    private final CitrusRemoteConfiguration configuration;

    /** Single thread job scheduler */
    private final ExecutorService jobs = Executors.newSingleThreadExecutor();
    private Future<List<RemoteResult>> remoteResultFuture;

    /** Latest test reports */
    private RemoteTestResultReporter remoteTestResultReporter = new RemoteTestResultReporter();

    /**
     * Default constructor using default configuration.
     */
    public CitrusRemoteApplication() {
        this(new CitrusRemoteConfiguration());
    }

    /**
     * Constructor with given application configuration.
     * @param configuration
     */
    public CitrusRemoteApplication(CitrusRemoteConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void init() {
        Citrus.mode(Citrus.InstanceStrategy.SINGLETON);
        Citrus.CitrusInstanceManager.addInstanceProcessor(citrus -> {
            citrus.addTestReporter(remoteTestResultReporter);
            citrus.addTestListener(remoteTestResultReporter);
        });

        before((Filter) (request, response) -> log.info(request.requestMethod() + " " + request.url() + Optional.ofNullable(request.queryString()).map(query -> "?" + query).orElse("")));

        get("/health", (req, res) -> {
            res.type(APPLICATION_JSON);
            return "{ \"status\": \"UP\" }";
        });

        path("/results", () -> {
            get("", APPLICATION_JSON, (req, res) -> {
                res.type(APPLICATION_JSON);

                long timeout = Optional.ofNullable(req.queryParams("timeout"))
                                        .map(Long::valueOf)
                                        .orElse(10000L);

                if (remoteResultFuture != null) {
                    try {
                        return remoteResultFuture.get(timeout, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        res.status(206); // partial content
                    }
                }

                List<RemoteResult> results = new ArrayList<>();
                remoteTestResultReporter.getTestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
                return results;
            }, new JsonResponseTransformer());

            get("", (req, res) -> remoteTestResultReporter.getTestReport());

            get("/files", (req, res) -> {
                res.type(APPLICATION_JSON);
                File junitReportsFolder = new File("test-output/junitreports");

                if (junitReportsFolder.exists()) {
                    return Stream.of(Optional.ofNullable(junitReportsFolder.list()).orElse(new String[] {})).collect(Collectors.toList());
                }

                return Collections.emptyList();
            }, new JsonResponseTransformer());

            get("/file/:name", (req, res) -> {
                res.type(APPLICATION_XML);
                File junitReportsFolder = new File("test-output/junitreports");
                File testResultFile = new File(junitReportsFolder, req.params(":name"));

                if (junitReportsFolder.exists() && testResultFile.exists()) {
                    return FileUtils.readToString(testResultFile);
                }

                throw halt(404, "Failed to find test result file: " + req.params(":name"));
            });
        });

        path("/run", () -> {
            get("", (req, res) -> {
                TestRunConfiguration runConfiguration = new TestRunConfiguration();

                if (req.queryParams().contains("includes")) {
                    runConfiguration.setIncludes(StringUtils.commaDelimitedListToStringArray(URLDecoder.decode(req.queryParams("includes"), ENCODING)));
                }

                if (req.queryParams().contains("package")) {
                    runConfiguration.setPackages(Collections.singletonList(URLDecoder.decode(req.queryParams("package"), ENCODING)));
                }

                if (req.queryParams().contains("class")) {
                    runConfiguration.setTestClasses(Collections.singletonList(TestClass.fromString(URLDecoder.decode(req.queryParams("class"), ENCODING))));
                }

                runTests(runConfiguration);

                res.type(APPLICATION_JSON);

                List<RemoteResult> results = new ArrayList<>();
                remoteTestResultReporter.getTestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
                return results;
            }, new JsonResponseTransformer());

            put("", (req, res) -> {
                remoteResultFuture = jobs.submit(new RunJob(new ObjectMapper().readValue(req.body(), TestRunConfiguration.class)) {
                    @Override
                    public List<RemoteResult> call() {
                        runTests(runConfiguration);

                        List<RemoteResult> results = new ArrayList<>();
                        remoteTestResultReporter.getTestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
                        return results;
                    }
                });

                return "";
            });

            post("", (req, res) -> {
                TestRunConfiguration runConfiguration = new ObjectMapper().readValue(req.body(), TestRunConfiguration.class);
                runTests(runConfiguration);

                List<RemoteResult> results = new ArrayList<>();
                remoteTestResultReporter.getTestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
                return results;
            }, new JsonResponseTransformer());
        });

        path("/configuration", () -> {
            get("", (req, res) -> {
                res.type(APPLICATION_JSON);
                return configuration;
            }, new JsonResponseTransformer());

            put("", (req, res) -> {
                configuration.apply(new JsonRequestTransformer().read(req.body(), CitrusAppConfiguration.class));
                return "";
            });
        });

        exception(CitrusRuntimeException.class, (exception, request, response) -> {
            response.status(500);
            response.body(exception.getMessage());
        });
    }

    /**
     * Construct run controller and execute with given configuration.
     * @param runConfiguration
     */
    private void runTests(TestRunConfiguration runConfiguration) {
        RunController runController = new RunController(configuration);

        runController.setIncludes(runConfiguration.getIncludes());

        if (!CollectionUtils.isEmpty(runConfiguration.getDefaultProperties())) {
            runController.addDefaultProperties(runConfiguration.getDefaultProperties());
        }

        if (CollectionUtils.isEmpty(runConfiguration.getPackages()) && CollectionUtils.isEmpty(runConfiguration.getTestClasses())) {
            runController.runAll();
        }

        if (!CollectionUtils.isEmpty(runConfiguration.getPackages())) {
            runController.runPackages(runConfiguration.getPackages());
        }

        if (!CollectionUtils.isEmpty(runConfiguration.getTestClasses())) {
            runController.runClasses(runConfiguration.getTestClasses());
        }
    }

    @Override
    public void destroy() {
        Citrus citrus = Citrus.CitrusInstanceManager.getSingleton();
        if (citrus != null) {
            log.info("Closing Citrus and its application context");
            citrus.close();
        }
    }
}
