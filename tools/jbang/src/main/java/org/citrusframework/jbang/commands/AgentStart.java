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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.agent.CitrusAgentConfiguration;
import org.citrusframework.agent.CitrusAgentServer;
import org.citrusframework.agent.util.ConfigurationHelper;
import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "start", description = "Starts the Citrus agent as a server")
public class AgentStart extends CitrusCommand {

    @Option(names = { "--engine" }, description = "Name of the test engine that is used ti run tests. One of junit, junit5, testng, cucumber")
    private String engine;

    @Option(names = { "--port" }, description = "Server port.")
    private String port;

    @Option(names = { "--system-exit" }, description = "Should the server exit based on success or failure of the test run.")
    private String systemExit;

    @Option(names = { "--skip-tests" }, description = "Should the server skip the test run at startup.")
    private String skipTests;

    @Option(names = { "--config-class" }, description = "Configuration class name.")
    private String configClass;

    @Option(names = { "--time-to-live" }, description = "If this time is set the server automatically terminates after the given time.")
    private String timeToLive;

    @Option(names = { "--test-jar" }, description = "Path to a Java archive that holds tests to run.")
    private String testJar;

    @Option(names = { "--packages" }, arity = "0..*", description = "Test package name to include in the test run.")
    private String[] packages;

    @Option(names = { "--includes" }, arity = "0..*", description = "Includes test name pattern.")
    private String[] includes;

    @Option(names = { "--property" }, arity = "0..*", description = "Default System property to set before the test run.")
    private String[] properties;

    public AgentStart(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        return start();
    }

    private int start() {
        CitrusAgentConfiguration configuration = fromCliOptions(ConfigurationHelper.fromEnvVars());
        CitrusAgentServer server = new CitrusAgentServer(configuration, Collections.emptyList());
        try {
            server.start();
            return server.waitForCompletion() ? 0 : 1;
        } finally {
            server.stop();
        }
    }

    private CitrusAgentConfiguration fromCliOptions(CitrusAgentConfiguration configuration) {
        if (StringUtils.hasText(engine)) {
            configuration.setEngine(engine);
        }

        if (StringUtils.hasText(port)) {
            configuration.setPort(Integer.parseInt(port));
        }

        if (StringUtils.hasText(systemExit)) {
            configuration.setSystemExit(Boolean.parseBoolean(systemExit));
        }

        if (StringUtils.hasText(skipTests)) {
            configuration.setSkipTests(Boolean.parseBoolean(skipTests));
        }

        if (StringUtils.hasText(testJar)) {
            configuration.setTestJar(Resources.create(testJar).getFile());
        }

        if (StringUtils.hasText(configClass)) {
            configuration.setConfigClass(configClass);
        }

        if (StringUtils.hasText(timeToLive)) {
            configuration.setTimeToLive(Long.parseLong(timeToLive));
        }

        if (includes != null) {
            configuration.setIncludes(includes);
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

        return configuration;
    }
}
