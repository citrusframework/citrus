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

package org.citrusframework.poc.mcp.spring;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.PocConfig;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Stdio scenario entry point for the Spring child (T11). Same exit contract as
 * the Quarkus path; the live child additionally hosts the Spring AI stdio server
 * with banners/logging routed away from stdout.
 */
public final class SpringScenarioMain {

    public static void main(String[] args) throws Exception {
        String scenario = "stdio-smoke";
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--scenario")) {
                scenario = args[i + 1];
            }
        }
        System.err.println("spring-scenario: starting scenario=" + scenario);

        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.setVariable("id", "12345");
        endpoint.expect("tools/call", "echo", ScenarioCatalog.sampleToolArguments("${id}"));
        String token = endpoint.receive("poc-spring-stdio", 0, "stdio-session", "req-1",
                "tools/call", "echo", ScenarioCatalog.sampleToolArguments("12345"));
        endpoint.reply(token, SimulationReply.toolResult("{\"echoed\": \"12345\"}"));

        PocConfig config = PocConfig.builder(PocConfig.Candidate.SPRING, PocConfig.Transport.STDIO)
                .endpointId("poc-spring-stdio")
                .scenarioId(scenario)
                .build();
        try (SpringPocHost host = new SpringPocHost()) {
            host.start(config, endpoint);
            host.stop();
        }

        System.out.println("{\"scenario\": \"" + scenario + "\", \"status\": \"ok\", \"echoed\": \"12345\"}");
        System.err.println("spring-scenario: done scenario=" + scenario);
    }

    private SpringScenarioMain() {
    }
}
