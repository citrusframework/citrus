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

package org.citrusframework.poc.mcp.quarkus;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.PocConfig;
import org.citrusframework.poc.mcp.ScenarioCatalog;

/**
 * Stdio scenario entry point for the Quarkus child (T10).
 *
 * <p>The client driver launches this as a server child containing the Citrus
 * scenario and the framework host. Protocol goes on stdout only; diagnostics on
 * stderr. The live child additionally hosts the Quarkus MCP runtime; this entry
 * point isolates the scenario/bridge half with the same exit contract so parent
 * assertions (protocol-only stdout, bounded exit, EOF) are meaningful before the
 * framework is wired in.
 */
public final class QuarkusScenarioMain {

    public static void main(String[] args) throws Exception {
        String scenario = "stdio-smoke";
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--scenario")) {
                scenario = args[i + 1];
            }
        }
        System.err.println("quarkus-scenario: starting scenario=" + scenario);

        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.setVariable("id", "12345");
        endpoint.expect("tools/call", "echo", ScenarioCatalog.sampleToolArguments("${id}"));
        String token = endpoint.receive("poc-quarkus-stdio", 0, "stdio-session", "req-1",
                "tools/call", "echo", ScenarioCatalog.sampleToolArguments("12345"));
        endpoint.reply(token, org.citrusframework.poc.mcp.SimulationReply.toolResult("{\"echoed\": \"12345\"}"));

        PocConfig config = PocConfig.builder(PocConfig.Candidate.QUARKUS, PocConfig.Transport.STDIO)
                .endpointId("poc-quarkus-stdio")
                .scenarioId(scenario)
                .build();
        try (QuarkusPocHost host = new QuarkusPocHost()) {
            host.start(config, endpoint);
            host.stop();
        }

        // Protocol-only stdout: exactly one JSON line.
        System.out.println("{\"scenario\": \"" + scenario + "\", \"status\": \"ok\", \"echoed\": \"12345\"}");
        System.err.println("quarkus-scenario: done scenario=" + scenario);
    }

    private QuarkusScenarioMain() {
    }
}
