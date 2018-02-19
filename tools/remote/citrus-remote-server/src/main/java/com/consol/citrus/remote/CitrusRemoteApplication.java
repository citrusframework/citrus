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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.remote.controller.RunController;
import com.consol.citrus.remote.job.RunJob;
import com.consol.citrus.remote.model.RemoteResult;
import com.consol.citrus.remote.reporter.RemoteTestResultReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.servlet.SparkApplication;

import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    /** Application configuration */
    private final CitrusRemoteConfiguration configuration;

    /** Single thread job scheduler */
    private final ExecutorService jobs = Executors.newSingleThreadExecutor();

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

        get("/run", (req, res) -> {
            RunController runController = new RunController(configuration);

            if (!req.queryParams().contains("package") && !req.queryParams().contains("class")) {
                runController.runAll();
            }

            if (req.queryParams().contains("package")) {
                runController.runPackage(URLDecoder.decode(req.queryParams("package"), "UTF-8"));
            }

            if (req.queryParams().contains("class")) {
                runController.runClass(URLDecoder.decode(req.queryParams("class"), "UTF-8"));
            }

            List<RemoteResult> results = new ArrayList<>();
            remoteTestResultReporter.getTestResults().doWithResults(result -> results.add(RemoteResult.fromTestResult(result)));
            return results;
        }, model -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                return mapper.writeValueAsString(model);
            } catch (JsonProcessingException e) {
                throw new CitrusRuntimeException("Failed to write json test results", e);
            }
        });

        put("/run", (req, res) -> {
            jobs.submit((RunJob) () -> {
                RunController runController = new RunController(configuration);

                if (req.queryParams().contains("package")) {
                    runController.runPackage(URLDecoder.decode(req.queryParams("package"), "UTF-8"));
                }

                if (req.queryParams().contains("class")) {
                    runController.runClass(URLDecoder.decode(req.queryParams("class"), "UTF-8"));
                }

                return "";
            });

            return "";
        });

        exception(CitrusRuntimeException.class, (exception, request, response) -> {
            response.status(500);
            response.body(exception.getMessage());
        });
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
