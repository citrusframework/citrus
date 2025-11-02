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

package org.citrusframework.agent;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.NoStackTraceTimeoutException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.apache.hc.core5.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.io.IoBuilder;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusInstanceProcessor;
import org.citrusframework.TestResult;
import org.citrusframework.agent.listener.AgentTestListener;
import org.citrusframework.agent.util.ConfigurationHelper;
import org.citrusframework.agent.util.JsonSupport;
import org.citrusframework.main.CitrusAppConfiguration;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.OutputStreamReporter;
import org.citrusframework.report.TestReporterSettings;
import org.citrusframework.report.TestResults;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CitrusAgentApplication extends AbstractVerticle implements CitrusInstanceProcessor {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusAgentApplication.class);

    private final AgentTestListener agentTestListener = new AgentTestListener();

    private final RunService runService = new RunService();

    private final CitrusAgentConfiguration configuration;

    /** Single thread job scheduler */
    private Future<TestResults> remoteResultFuture;

    /** Router customizations */
    private final List<Consumer<Router>> routerCustomizations;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private TemplateEngine templateEngine;

    static {
      System.setOut(IoBuilder
          .forLogger(LogManager.getLogger("system.out"))
          .buildPrintStream());
    }

    /**
     * Constructor with given application configuration and route customizations.
     */
    public CitrusAgentApplication(CitrusAgentConfiguration configuration, List<Consumer<Router>> routerCustomizations) {
        this.configuration = configuration;
        this.routerCustomizations = Optional.ofNullable(routerCustomizations)
                .orElse(Collections.emptyList());
    }

    @Override
    public void start() {
        CitrusInstanceManager.addInstanceProcessor(this);

        Router router = Router.router(getVertx());
        router.route().handler(CorsHandler.create()
                .addOriginWithRegex(CitrusAgentSettings.getCorsAllowedOrigin())
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.POST));
        router.route().handler(BodyHandler.create());
        router.route().handler(ctx -> {
            if (configuration.isVerbose()) {
                logger.info("{} {}", ctx.request().method(), ctx.request().uri());
            }
            ctx.next();
        });
        addWebEndpoint(router);
        addHealthEndpoint(router);
        addFilesEndpoint(router);
        addResultsEndpoints(router);
        addRunEndpoints(router);
        addExecuteEndpoints(router);
        addConfigEndpoints(router);

        router.get("/logs")
            .handler(wrapThrowingHandler(ctx -> {
                ctx.response().end(agentTestListener.getLogs());
            }));

        routerCustomizations.forEach(customization -> customization.accept(router));

        templateEngine = ThymeleafTemplateEngine.create(getVertx());

        getVertx().createHttpServer()
                .requestHandler(router)
                .listen(configuration.getPort())
                .onFailure(handler ->
                        logger.info("Failed to start server on port {} - error message is {}", configuration.getPort(), handler.getMessage()))
                .onSuccess(unused ->
                        logger.info("Server started, listening on port {}", configuration.getPort()));
    }

    @Override
    public void process(Citrus instance) {
        logger.info("Adding agent test listener");
        instance.addTestSuiteListener(agentTestListener);
        instance.addTestListener(agentTestListener);
        instance.addTestActionListener(agentTestListener);
        instance.addTestReporter(agentTestListener);
        instance.addMessageListener(agentTestListener);
    }

    private void addWebEndpoint(Router router) {
        router.get("/")
                .handler(wrapThrowingHandler(ctx -> {
                    JsonObject data = new JsonObject()
                            .put("results", agentTestListener.getResults().asList());

                    templateEngine.render(data, "templates/index.html").onComplete(res -> {
                        if (res.succeeded()) {
                            ctx.response()
                                    .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML.toString())
                                    .end(res.result());
                        } else {
                            ctx.fail(res.cause());
                        }
                    });
                }));

        router.get("/static/*")
                .handler(wrapThrowingHandler(ctx -> ctx.response().sendFile(ctx.request().path().substring(1))));
    }

    private static void addHealthEndpoint(Router router) {
        router.get("/health")
                .handler(wrapThrowingHandler(ctx ->
                        ctx.response()
                                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                                .end("{ \"status\": \"UP\" }")));
    }

    private static void addFilesEndpoint(Router router) {
        router.get("/files/:name")
                .handler(wrapThrowingHandler(ctx -> {
                    HttpServerResponse response = ctx.response();
                    String fileName = ctx.pathParam("name");
                    Path file = Path.of(fileName);
                    if (Files.isRegularFile(file)) {
                        response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString())
                                .putHeader(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        "attachment; filename=\"" + file.getFileName() + "\"")
                                .sendFile(fileName);
                    } else {
                        response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
                    }
                }));
    }

    private void addResultsEndpoints(Router router) {
        router.get("/results")
                .handler(wrapThrowingHandler(ctx -> {
                    HttpServerResponse response = ctx.response();
                    if (ctx.request().headers().contains(HttpHeaders.ACCEPT) &&
                            ctx.request().headers().get(HttpHeaders.ACCEPT).equals(ContentType.APPLICATION_JSON.toString())) {
                        final List<TestResult> results = agentTestListener.getResults().asList();
                        logger.info("found results: {}", results.size());
                        response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                                .end(JsonSupport.render(results));
                    } else {
                        response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString())
                                .end(createReport(agentTestListener.getResults()));
                    }
                }));
        router.get("/results/latest")
                .handler(wrapThrowingHandler(ctx -> {
                    HttpServerResponse response = ctx.response();
                    if (remoteResultFuture != null) {
                        long timeout = Optional.ofNullable(ctx.request().params().get("timeout"))
                                .map(Long::valueOf)
                                .orElse(10000L);

                        remoteResultFuture.timeout(timeout, TimeUnit.MILLISECONDS)
                                .onSuccess(results ->
                                        response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                                                .end(JsonSupport.render(results.asList())))
                                .onFailure(throwable -> {
                                    if (throwable instanceof NoStackTraceTimeoutException) {
                                        logger.info("Tests still pending - returning partial test results");
                                        response.setStatusCode(HttpResponseStatus.PARTIAL_CONTENT.code())
                                                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                                                .end(JsonSupport.render(agentTestListener.getPendingResults().asList()));
                                    } else {
                                        logger.warn("Error while waiting for pending test results", throwable);
                                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                                                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString())
                                                .end("%s - %s".formatted(throwable.getClass().getName(), throwable.getMessage()));
                                    }
                                });
                    } else {
                        final List<TestResult> results = agentTestListener.getLatestResults().asList();
                        logger.info("found latest results: {}", results.size());
                        response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                                .end(JsonSupport.render(results));
                    }
                }));
        router.put("/results/clear")
                .handler(ctx -> {
                    agentTestListener.reset();
                    ctx.response().end("");
                });
        router.get("/results/files")
                .handler(wrapThrowingHandler(ctx -> {
                    File reportsFolder = new File(TestReporterSettings.getReportDirectory());

                    List<String> result = new ArrayList<>();
                    if (reportsFolder.exists()) {
                        result.addAll(Optional.ofNullable(reportsFolder.listFiles(f -> !f.isDirectory()))
                                .stream()
                                .flatMap(Stream::of)
                                .map(File::getName)
                                .toList());

                        result.addAll(Optional.ofNullable(reportsFolder.listFiles(File::isDirectory))
                                .stream()
                                .flatMap(Stream::of)
                                .filter(folder -> "junitreports".equals(folder.getName()))
                                .map(folder -> folder.listFiles(f -> !f.isDirectory()))
                                .flatMap(Stream::of)
                                .map(file -> file.getParentFile().getName() + "/" + file.getName())
                                .toList());
                    }
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                            .end(JsonSupport.render(result));
                }));
        router.get("/results/file/:name")
                .handler(wrapThrowingHandler(ctx -> {
                    HttpServerResponse response = ctx.response();
                    response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_XML.toString());
                    String fileName = ctx.pathParam("name");
                    String folder = Optional.ofNullable(ctx.request().params().get("folder")).orElse("");
                    String reportsFolder = TestReporterSettings.getReportDirectory();

                    Path testResultFile;
                    if (StringUtils.hasText(folder)) {
                        testResultFile = Path.of(reportsFolder).resolve(folder).resolve(fileName);
                    } else {
                        testResultFile = Path.of(reportsFolder).resolve(fileName);
                    }

                    if (Files.exists(testResultFile)) {
                        response.sendFile(testResultFile.toString());
                    } else {
                        response.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                                .end("Failed to find test result file: %s".formatted(fileName));
                    }
                }));
        router.get("/results/suite")
                .handler(wrapThrowingHandler(ctx -> {
                    HttpServerResponse response = ctx.response();
                    response.putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                    JUnitReporter jUnitReporter = new JUnitReporter();
                    Path suiteResultFile = Path.of(jUnitReporter.getReportDirectory())
                            .resolve(String.format(
                                    jUnitReporter.getReportFileNamePattern(),
                                    jUnitReporter.getSuiteName()));
                    if (Files.exists(suiteResultFile)) {
                        response.sendFile(suiteResultFile.toString());
                    } else {
                        response.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                                .end("Failed to find suite result file: %s"
                                        .formatted(suiteResultFile));
                    }
                }));
    }

    private void addRunEndpoints(Router router) {
        router.get("/run")
                .handler(wrapThrowingHandler(ctx ->
                        runTests(ConfigurationHelper.fromRequestQueryParams(ctx.request().params(), configuration), ctx.response())));
        router.post("/run")
                .handler(wrapThrowingHandler(ctx ->
                        runTests(ConfigurationHelper.fromRequestBody(ctx.body(), configuration), ctx.response())));
        router.put("/run")
                .handler(wrapThrowingHandler(ctx -> {
                    remoteResultFuture = startTestsAsync(ConfigurationHelper.fromRequestBody(ctx.body(), configuration));
                    ctx.response().end("");
                }));
    }

    private void addExecuteEndpoints(Router router) {
        router.post("/execute/:name")
                .handler(wrapThrowingHandler(ctx -> {
                            runTests(ConfigurationHelper.fromExecutionRequest(ctx, configuration), ctx.response());
                        }));
        router.put("/execute/:name")
                .handler(wrapThrowingHandler(ctx -> {
                    remoteResultFuture = startTestsAsync(ConfigurationHelper.fromExecutionRequest(ctx, configuration));
                    ctx.response().end("");
                }));
    }

    public static Handler<RoutingContext> wrapThrowingHandler(ThrowingHandler<RoutingContext> handler) {
        return ctx -> {
            try {
                handler.handle(ctx);
            } catch (Exception e) {
                logger.error("Request failed with:", e);

                ctx.response()
                        .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .end(e.getMessage());
            }
        };
    }

    private void runTests(TestRunConfiguration runConfiguration, HttpServerResponse response) {
        try {
            if (configuration.isReset()) {
                agentTestListener.clearLogs();
            }

            runService.run(runConfiguration);
            response.end(JsonSupport.render(agentTestListener.getLatestResults().asList()));
        } catch (Exception error) {
            logger.error(error.getMessage());
            StringWriter stackTrace = new StringWriter();
            error.printStackTrace(new PrintWriter(stackTrace));
            logger.error(stackTrace.toString());
            response
                .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .end(error.getMessage());
        }
    }

    private Future<TestResults> startTestsAsync(TestRunConfiguration testRunConfiguration) {
        return Future.fromCompletionStage(CompletableFuture.supplyAsync(
                new RunJob(runService, testRunConfiguration, agentTestListener),
                executorService));
    }

    private void addConfigEndpoints(Router router) {
        router.get("/configuration")
                .handler(wrapThrowingHandler(ctx ->
                        ctx.response()
                                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                                .end(JsonSupport.render(configuration))));
        router.put("/configuration")
                .handler(wrapThrowingHandler(ctx ->
                        configuration.apply(JsonSupport.read(
                                ctx.body().asString(),
                                CitrusAppConfiguration.class))));
        router.post("/configuration")
                .handler(wrapThrowingHandler(ctx ->
                        configuration.apply(JsonSupport.read(
                                ctx.body().asString(),
                                CitrusAppConfiguration.class))));
    }

    @Override
    public void stop() {
        Optional<Citrus> citrus = CitrusInstanceManager.get();
        if (citrus.isPresent()) {
            logger.info("Closing Citrus and its application context");
            citrus.get().close();
        }
        getVertx().close();
    }

    private static String createReport(TestResults results) {
        StringWriter reportWriter = new StringWriter();
        OutputStreamReporter reporter = new OutputStreamReporter(reportWriter);
        reporter.generate(results);
        return reportWriter.toString();
    }
}
