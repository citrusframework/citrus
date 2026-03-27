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

package org.citrusframework.agent.util;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import org.apache.camel.tooling.maven.MavenArtifact;
import org.citrusframework.TestClass;
import org.citrusframework.TestSource;
import org.citrusframework.agent.CitrusAgentConfiguration;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.jbang.maven.MavenDependencyResolver;
import org.citrusframework.jbang.util.CodeAnalyzer;
import org.citrusframework.jbang.util.DelegatingCodeAnalyzer;
import org.citrusframework.main.CitrusAppConfiguration;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ConfigurationHelper {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationHelper.class);

    private ConfigurationHelper() {
        // prevent instantiation of utility class
    }

    public static TestRunConfiguration fromRequestQueryParams(MultiMap queryParams, CitrusAppConfiguration parent) {
        TestRunConfiguration options = new TestRunConfiguration();

        if (queryParams.contains("engine")) {
            options.setEngine(URLDecoder.decode(queryParams.get("engine"), StandardCharsets.UTF_8));
        } else {
            options.setEngine(parent.getEngine());
        }

        if (queryParams.contains("includes")) {
            options.setIncludes(URLDecoder.decode(queryParams.get("includes"), StandardCharsets.UTF_8)
                    .split(","));
        }

        if (queryParams.contains("modules")) {
            options.setModules(Arrays.stream(URLDecoder.decode(queryParams.get("modules"), StandardCharsets.UTF_8)
                    .split(",")).collect(Collectors.toSet()));
        }

        if (queryParams.contains("dependencies")) {
            options.setDependencies(Arrays.stream(URLDecoder.decode(queryParams.get("dependencies"), StandardCharsets.UTF_8)
                    .split(",")).collect(Collectors.toSet()));
        }

        if (queryParams.contains("workDir")) {
            options.setWorkDir(URLDecoder.decode(queryParams.get("workDir"), StandardCharsets.UTF_8));
        }

        if (queryParams.contains("package")) {
            options.setPackages(Collections.singletonList(
                    URLDecoder.decode(queryParams.get("package"), StandardCharsets.UTF_8)));
        } else if (queryParams.contains("class")) {
            options.setTestSources(Collections.singletonList(
                    TestClass.fromString(URLDecoder.decode(queryParams.get("class"), StandardCharsets.UTF_8))));
        } else if (queryParams.contains("source")) {
            options.setTestSources(Collections.singletonList(
                    TestSourceHelper.create(URLDecoder.decode(queryParams.get("source"), StandardCharsets.UTF_8))));
        } else {
            options.setPackages(parent.getPackages());
            options.setTestSources(parent.getTestSources());
        }

        if (queryParams.contains("verbose")) {
            options.setVerbose(Boolean.parseBoolean(queryParams.get("verbose")));
        } else {
            options.setVerbose(parent.isVerbose());
        }

        boolean reset = queryParams.contains("reset") ? Boolean.parseBoolean(queryParams.get("reset")) : parent.isReset();
        options.setReset(reset);
        options.setTestJar(parent.getTestJar());

        return options;
    }

    public static TestRunConfiguration fromRequestBody(RequestBody body, CitrusAppConfiguration parent) {
        TestRunConfiguration options = JsonSupport.read(body.asString(), TestRunConfiguration.class);

        if (!StringUtils.hasText(options.getEngine())) {
            options.setEngine(parent.getEngine());
        }

        if (options.getPackages().isEmpty() && options.getTestSources().isEmpty()) {
            options.setPackages(parent.getPackages());
            options.setTestSources(parent.getTestSources());
        }

        options.setVerbose(options.isVerbose() && parent.isVerbose());
        options.setReset(options.isReset() && parent.isReset());

        options.setTestJar(parent.getTestJar());

        return options;
    }

    public static CitrusAgentConfiguration fromEnvVars() {
        return CitrusAgentConfiguration.fromEnvVars(TestSourceHelper::create);
    }

    /**
     * Creates test run configuration from the given execution request.
     * The request body represents a test code that is run as a test source.
     */
    public static TestRunConfiguration fromExecutionRequest(RoutingContext ctx, CitrusAgentConfiguration configuration) {
        TestRunConfiguration options = ConfigurationHelper.fromRequestQueryParams(ctx.request().params(), configuration);

        // Remove default parent tests and test jar from run configuration
        options.getTestSources().clear();
        options.setTestJar((File) null);

        String sourceCode = ctx.body().asString();
        String fileName = ctx.pathParam("name");
        String type = null;
        if (StringUtils.hasText(FileUtils.getFileExtension(fileName))) {
            type = FileUtils.getFileExtension(fileName);
        } else if (ctx.parsedHeaders().contentType() != null && ctx.parsedHeaders().contentType().subComponent() != null) {
            type = ctx.parsedHeaders().contentType().subComponent();
        }

        if (type == null || TestLoader.lookup(type).isEmpty()) {
            if (IsXmlPredicate.getInstance().test(sourceCode)) {
                type = TestLoader.XML;
            } else if (sourceCode.contains("public void run()")) {
                type = TestLoader.JAVA;
            } else if (sourceCode.trim().startsWith("- name:") || sourceCode.trim().startsWith("- actions:")) {
                type = TestLoader.YAML;
            } else {
                type = TestLoader.GROOVY;
            }
        }

        if (configuration.isInspectCode()) {
            try {
                CodeAnalyzer analyzer = new DelegatingCodeAnalyzer();
                CodeAnalyzer.ScanResult scanResult = analyzer.scan(fileName, sourceCode);

                options.getModules().addAll(Arrays.asList(scanResult.modules()));
                options.getDependencies().addAll(Arrays.asList(scanResult.dependencies()));
            } catch (Exception e) {
                logger.warn("Failed to analyze test source {} due to '{}'", fileName, e.getMessage(), e);
            }
        }

        options.getTestSources().add(
                new TestSource(type, FileUtils.getBaseName(fileName)).sourceCode(sourceCode));

        return options;
    }

    /**
     * Reads additional artifacts from given configuration and adds those to the classpath.
     */
    public static void resolveArtifacts(CitrusAgentConfiguration configuration) {
        if (!configuration.isOffline()) {
            List<MavenArtifact> resolved = resolveArtifacts(configuration.getModules(), configuration.getDependencies());

            if (!resolved.isEmpty()) {
                resolved.forEach(mavenArtifact -> {
                    try {
                        ClassLoaderHelper.addArtifact(mavenArtifact.toString(), mavenArtifact.getFile().toURI().toURL());
                    } catch (MalformedURLException e) {
                        logger.warn("Error resolving artifact {} due to '{}'", mavenArtifact, e.getMessage());
                    }
                });

                // Adapt and set class loader in current thread
                ClassLoaderHelper.updateContextClassloader();
            }
        }
    }

    public static List<MavenArtifact> resolveArtifacts(Set<String> modules, Set<String> dependencies) {
        if (modules.isEmpty() && dependencies.isEmpty()) {
            return Collections.emptyList();
        }

        List<MavenArtifact> additionalArtifacts = new ArrayList<>();
        MavenDependencyResolver resolver = new MavenDependencyResolver();

        String systemClasspath = System.getProperty("java.class.path");
        modules.stream()
                .map(module -> {
                    if (module.startsWith("citrus-")) {
                        return module;
                    } else {
                        return "citrus-" + module;
                    }
                })
                .filter(module -> !systemClasspath.contains(module))
                .forEach(module -> additionalArtifacts.addAll(resolver.resolveModule(module)));

        dependencies.forEach(dependency ->
                additionalArtifacts.addAll(resolver.resolve(dependency.trim(), dependency.contains("-SNAPSHOT"), true)));

        return additionalArtifacts;
    }
}
