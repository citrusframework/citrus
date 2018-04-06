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
import com.consol.citrus.report.JUnitReporter;
import com.consol.citrus.report.LoggingReporter;
import com.consol.citrus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.*;
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

    private final JsonRequestTransformer requestTransformer = new JsonRequestTransformer();
    private final JsonResponseTransformer responseTransformer = new JsonResponseTransformer();

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
            citrus.addTestSuiteListener(remoteTestResultReporter);
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
            }, responseTransformer);

            get("", (req, res) -> remoteTestResultReporter.getTestReport());

            get("/files", (req, res) -> {
                res.type(APPLICATION_JSON);
                File junitReportsFolder = new File(getJUnitReportsFolder());

                if (junitReportsFolder.exists()) {
                    return Stream.of(Optional.ofNullable(junitReportsFolder.list()).orElse(new String[] {})).collect(Collectors.toList());
                }

                return Collections.emptyList();
            }, responseTransformer);

            get("/file/:name", (req, res) -> {
                res.type(APPLICATION_XML);
                File junitReportsFolder = new File(getJUnitReportsFolder());
                File testResultFile = new File(junitReportsFolder, req.params(":name"));

                if (junitReportsFolder.exists() && testResultFile.exists()) {
                    return FileUtils.readToString(testResultFile);
                }

                throw halt(404, "Failed to find test result file: " + req.params(":name"));
            });

            get("/suite", (req, res) -> {
                res.type(APPLICATION_XML);
                JUnitReporter jUnitReporter = new JUnitReporter();
                File citrusReportsFolder = new File(jUnitReporter.getReportDirectory());
                File suiteResultFile = new File(citrusReportsFolder, String.format(jUnitReporter.getReportFileNamePattern(), jUnitReporter.getSuiteName()));

                if (citrusReportsFolder.exists() && suiteResultFile.exists()) {
                    return FileUtils.readToString(suiteResultFile);
                }

                throw halt(404, "Failed to find suite result file: " + suiteResultFile.getPath());
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

                res.type(APPLICATION_JSON);

                return runTests(runConfiguration);
            }, responseTransformer);

            put("", (req, res) -> {
                remoteResultFuture = jobs.submit(new RunJob(requestTransformer.read(req.body(), TestRunConfiguration.class)) {
                    @Override
                    public List<RemoteResult> run(TestRunConfiguration runConfiguration) {
                        return runTests(runConfiguration);
                    }
                });

                return "";
            });

            post("", (req, res) -> {
                TestRunConfiguration runConfiguration = requestTransformer.read(req.body(), TestRunConfiguration.class);
                return runTests(runConfiguration);
            }, responseTransformer);
        });

        path("/configuration", () -> {
            get("", (req, res) -> {
                res.type(APPLICATION_JSON);
                return configuration;
            }, responseTransformer);

            put("", (req, res) -> {
                configuration.apply(requestTransformer.read(req.body(), CitrusAppConfiguration.class));
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
     * @return remote results
     */
    private List<RemoteResult> runTests(TestRunConfiguration runConfiguration) {
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

        List<RemoteResult> results = new ArrayList<>();
        remoteTestResultReporter.getTestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
        return results;
    }

    /**
     * Find reports folder based in unit testing framework present on classpath.
     * @return
     */
    private String getJUnitReportsFolder() {
        if (ClassUtils.isPresent("org.testng.annotations.Test", getClass().getClassLoader())) {
            return "test-output" + File.separator + "junitreports";
        } else if (ClassUtils.isPresent("org.junit.Test", getClass().getClassLoader())) {
            JUnitReporter jUnitReporter = new JUnitReporter();
            return jUnitReporter.getReportDirectory() + File.separator + jUnitReporter.getOutputDirectory();
        } else {
            return new LoggingReporter().getReportDirectory();
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
