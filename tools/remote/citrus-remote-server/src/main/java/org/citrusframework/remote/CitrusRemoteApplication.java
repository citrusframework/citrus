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

package org.citrusframework.remote;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusInstanceStrategy;
import org.citrusframework.TestClass;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.main.CitrusAppConfiguration;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.remote.controller.RunController;
import org.citrusframework.remote.job.RunJob;
import org.citrusframework.remote.model.RemoteResult;
import org.citrusframework.remote.reporter.RemoteTestResultReporter;
import org.citrusframework.remote.transformer.JsonRequestTransformer;
import org.citrusframework.remote.transformer.JsonResponseTransformer;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import spark.Filter;
import spark.servlet.SparkApplication;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * Remote application creates routes for this web application.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusRemoteApplication implements SparkApplication {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CitrusRemoteApplication.class);

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
    private final RemoteTestResultReporter remoteTestResultReporter = new RemoteTestResultReporter();

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
        CitrusInstanceManager.mode(CitrusInstanceStrategy.SINGLETON);
        CitrusInstanceManager.addInstanceProcessor(citrus -> {
            citrus.addTestReporter(remoteTestResultReporter);
        });

        before((Filter) (request, response) -> LOG.info(request.requestMethod() + " " + request.url() + Optional.ofNullable(request.queryString()).map(query -> "?" + query).orElse("")));

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
                remoteTestResultReporter.getLatestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
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

                if (req.queryParams().contains("engine")) {
                    runConfiguration.setEngine(URLDecoder.decode(req.queryParams("engine"), ENCODING));
                } else {
                    runConfiguration.setEngine(configuration.getEngine());
                }

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

        runController.setEngine(runConfiguration.getEngine());
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
        remoteTestResultReporter.getLatestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
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
        Optional<Citrus> citrus = CitrusInstanceManager.get();
        if (citrus.isPresent()) {
            LOG.info("Closing Citrus and its application context");
            citrus.get().close();
        }
    }
}
