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

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import org.citrusframework.TestClass;
import org.citrusframework.TestSource;
import org.citrusframework.agent.CitrusAgentConfiguration;
import org.citrusframework.agent.CitrusAgentSettings;
import org.citrusframework.common.TestLoader;
import org.citrusframework.main.CitrusAppConfiguration;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    FileUtils.getTestSource(URLDecoder.decode(queryParams.get("source"), StandardCharsets.UTF_8))));
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
        CitrusAgentConfiguration configuration = new CitrusAgentConfiguration();

        configuration.setPort(CitrusAgentSettings.getServerPort());

        configuration.setEngine(CitrusAgentSettings.getTestEngine());
        configuration.setIncludes(CitrusAgentSettings.getIncludes());
        configuration.setWorkDir(CitrusAgentSettings.getWorkDir());
        configuration.setSystemExit(CitrusAgentSettings.isSystemExit());
        configuration.setSkipTests(CitrusAgentSettings.isSkipTests());
        configuration.setConfigClass(CitrusAgentSettings.getConfigClass());

        configuration.setPackages(Arrays.asList(CitrusAgentSettings.getPackages()));
        configuration.setTestSources(Arrays.stream(CitrusAgentSettings.getTestSources())
                .map(FileUtils::getTestSource)
                .collect(Collectors.toList()));

        configuration.setVerbose(CitrusAgentSettings.isVerbose());
        configuration.setReset(CitrusAgentSettings.isReset());
        configuration.addDefaultProperties(CitrusAgentSettings.getDefaultProperties());

        String testJarPath = CitrusAgentSettings.getTestJar();
        Resource testJar = Resources.create(testJarPath);
        if (testJar.exists()) {
            configuration.setTestJar(testJar.getFile());
        } else {
            logger.debug("Ignore test jar artifact {} - not found", testJarPath);
        }

        configuration.setTimeToLive(CitrusAgentSettings.getTimeToLive());

        return configuration;
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

        options.getTestSources().add(
                new TestSource(type, FileUtils.getBaseName(fileName)).sourceCode(sourceCode));

        return options;
    }
}
