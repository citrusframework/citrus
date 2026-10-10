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

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Ordinary-Java entry point owning the Citrus scenario and the Quarkus MCP host in
 * one JVM (T4-live). No test-extension bootstrap: the augmented runner jar starts here,
 * {@link Quarkus#run} boots the runtime in this process, the scenario evaluates, and
 * {@link Quarkus#asyncExit} stops it. Protocol on stdout only; {@code CITRUS_ASSERTIONS}
 * and all diagnostics on stderr.
 *
 * <p>Modes: {@code echo-smoke} (tool call + one resource read, sequential expectations)
 * and {@code mismatch} (Citrus expects a different tool payload, so the live receive
 * must fail).
 */
@QuarkusMain
public class QuarkusLiveScenarioMain {

    public static void main(String[] args) {
        Quarkus.run(QuarkusLiveApp.class, args);
    }

    public static class QuarkusLiveApp implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {
            String scenario = "echo-smoke";
            String endpointId = "poc-quarkus-live";
            String transport = "stdio";
            for (int i = 0; i < args.length - 1; i++) {
                if (args[i].equals("--scenario")) {
                    scenario = args[i + 1];
                }
                if (args[i].equals("--endpoint")) {
                    endpointId = args[i + 1];
                }
                if (args[i].equals("--transport")) {
                    transport = args[i + 1];
                }
            }
            System.err.println("quarkus-live-scenario: starting scenario=" + scenario
                    + " pid=" + ProcessHandle.current().pid());
            boolean mismatch = scenario.equals("mismatch");

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> expectedArgs = new LinkedHashMap<>();
            expectedArgs.put("nested", Map.of("value", "12345"));
            expectedArgs.put("items", java.util.List.of("a", "b"));
            expectedArgs.put("count", 3);
            expectedArgs.put("flag", true);
            // LIVE-NULL-1 (Quarkus path): the delivered argument form is observed from the
            // wire below; explicit nulls are characterized in results/comparison.md.
            String expectedJson = mapper.writeValueAsString(expectedArgs);

            ExchangeRegistry registry = new ExchangeRegistry();
            CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
            endpoint.setVariable("id", "12345");
            endpoint.expect("tools/call", "echo",
                    mismatch ? "{\"count\": 99999}" : expectedJson.replace("12345", "${id}"));

            // Sequential scenario: tool expectation live first; the supplier arms the
            // resource-read expectation once the tool dispatch is validated. Static
            // listings never reach the bridge (framework catalog = control case).
            QuarkusBridgeHolder.offer(endpoint, (operation, selector, payload) -> {
                if (operation.equals("tools/call") && !mismatch) {
                    endpoint.expect("resources/read", "poc://alpha", "{\"uri\": \"poc://alpha\"}");
                    return SimulationReply.toolResult("{\"echoed\": \"12345\"}");
                }
                if (operation.equals("resources/read")) {
                    return SimulationReply.resourceReadResult("alpha-content");
                }
                return SimulationReply.toolResult("{\"echoed\": \"12345\"}");
            }, endpointId, 0, 5);

            int expectedServed = mismatch ? 0 : 2;
            if (transport.equals("http")) {
                // HTTP children publish readiness on stdout (diagnostics stay on stderr);
                // the parent parses the bound port from the runtime's Listening line.
                System.out.println("HTTP_READY transport=http");
                System.out.flush();
            }
            long deadline = System.currentTimeMillis() + 60_000;
            while (QuarkusBridgeHolder.served().size() < expectedServed
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(100);
            }
            int exit;
            if (!mismatch && QuarkusBridgeHolder.served().size() < expectedServed) {
                System.err.println("CITRUS_ASSERTIONS status=fail detail=no-call-within-budget served="
                        + QuarkusBridgeHolder.served());
                exit = 1;
            } else if (mismatch) {
                // The mismatched receive throws inside the tool method; the supplier is
                // never consulted. Wait for the Citrus failure to materialize on the bean.
                Throwable failure = null;
                while (failure == null && System.currentTimeMillis() < deadline) {
                    failure = jakarta.enterprise.inject.spi.CDI.current()
                            .select(CitrusMcpFeatures.class).get().lastFailure();
                    Thread.sleep(100);
                }
                if (failure == null) {
                    System.err.println("CITRUS_ASSERTIONS status=fail detail=no-mismatched-call-within-budget");
                    exit = 1;
                } else {
                    System.err.println("CITRUS_ASSERTIONS status=fail-as-designed failure="
                            + failure.getClass().getSimpleName());
                    exit = 1;
                }
            } else {
                System.err.println("CITRUS_ASSERTIONS status=ok served=" + QuarkusBridgeHolder.served());
                exit = 0;
            }
            System.err.println("quarkus-live-scenario: exit=" + exit);
            Quarkus.asyncExit(exit);
            return exit;
        }
    }
}
