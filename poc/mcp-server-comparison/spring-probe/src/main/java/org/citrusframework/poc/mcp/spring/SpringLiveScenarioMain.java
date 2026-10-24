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

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.HostHandle;
import org.citrusframework.poc.mcp.PocConfig;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Live stdio scenario entry point for the Spring child (T5-live/T11-live).
 *
 * <p>The parent (SDK client) launches this as the server child containing the Citrus
 * scenario runner and the Spring AI high-level server in one JVM. Protocol goes on
 * stdout only; all diagnostics — including the {@code CITRUS_ASSERTIONS} summary —
 * go to stderr. Exit 0 means the Citrus scenario passed; any other exit is failure.
 *
 * <p>Modes: {@code echo-smoke} (client sends the expected typed args) and
 * {@code mismatch} (Citrus expects a different payload, so the live receive must fail).
 */
public final class SpringLiveScenarioMain {

    public static void main(String[] args) throws Exception {
        String scenario = "echo-smoke";
        String endpointId = "poc-spring-live";
        String transport = "stdio";
        int port = 0;
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
            if (args[i].equals("--port")) {
                port = Integer.parseInt(args[i + 1]);
            }
        }
        System.err.println("spring-live-scenario: starting scenario=" + scenario + " pid=" + ProcessHandle.current().pid());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> expectedArgs = new LinkedHashMap<>();
        expectedArgs.put("nested", Map.of("value", "12345"));
        expectedArgs.put("items", java.util.List.of("a", "b"));
        expectedArgs.put("count", 3);
        expectedArgs.put("flag", true);
        // LIVE-NULL-1: the SDK stack elides explicit-null arguments on the wire
        // (observed: "maybeNull":null sent by the 2.0.1 client never reaches the
        // 2.0.0 server callback). The shared fixtures keep the explicit null for
        // the contract matrix; the live expectation uses the delivered form and the
        // elision is recorded as a finding in results/comparison.md.
        String expectedJson = mapper.writeValueAsString(expectedArgs);

        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.setVariable("id", "12345");
        boolean mismatch = scenario.equals("mismatch");
        endpoint.expect("tools/call", "echo",
                mismatch ? "{\"count\": 99999}" : expectedJson.replace("12345", "${id}"));

        // Sequential scenario: the tool expectation is live first; once the tool dispatch
        // is validated, the supplier arms the resource-read expectation for the client's
        // next step. Static listings (tools/list, resources/list) never reach the bridge —
        // the framework serves them from its own catalog (control case).
        java.util.List<String> served = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        SpringLiveHost.LiveSupplierHolder.offer((operation, selector, payload) -> {
            served.add(operation + " " + selector);
            if (operation.equals("tools/call") && !mismatch) {
                endpoint.expect("resources/read", "poc://alpha", "{\"uri\": \"poc://alpha\"}");
                return SimulationReply.toolResult("{\"echoed\": \"12345\"}");
            }
            if (operation.equals("resources/read")) {
                return SimulationReply.resourceReadResult("alpha-content");
            }
            return SimulationReply.toolResult("{\"echoed\": \"12345\"}");
        });

        PocConfig.Candidate candidate = PocConfig.Candidate.SPRING;
        PocConfig.Transport selected = transport.equals("http") ? PocConfig.Transport.HTTP : PocConfig.Transport.STDIO;
        PocConfig config = PocConfig.builder(candidate, selected)
                .endpointId(endpointId)
                .scenarioId(scenario)
                .port(port)
                .build();
        SpringLiveHost host = new SpringLiveHost();
        int exit;
        try {
            HostHandle handle = host.start(config, endpoint);
            if (host.ownerPid() != ProcessHandle.current().pid()) {
                throw new IllegalStateException("host not owned by this JVM");
            }
            System.err.println("spring-live-scenario: host=" + handle + " owner=" + host.serverProtocolOwner());
            if (selected == PocConfig.Transport.HTTP) {
                // HTTP children publish readiness on stdout (diagnostics stay on stderr).
                System.out.println("HTTP_READY http://" + handle.getTransportIdentity());
                System.out.flush();
            }
            CitrusToolCallback callback = host.ownedContext().getBean(CitrusToolCallback.class);
            int expectedServed = mismatch ? 1 : 2;
            long deadline = System.currentTimeMillis() + 60_000;
            // Mismatch never reaches the supplier (receive throws first): the settled
            // callback with a recorded failure is the expected signal there.
            while (served.size() < expectedServed && !(mismatch && callback.callsSettled() >= 1)
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(100);
            }
            Throwable failure = callback.lastFailure();
            if (!mismatch && served.size() < expectedServed) {
                System.err.println("CITRUS_ASSERTIONS status=fail detail=no-call-within-budget served=" + served);
                exit = 1;
            } else if (mismatch && callback.callsSettled() < 1) {
                System.err.println("CITRUS_ASSERTIONS status=fail detail=no-mismatched-call-within-budget");
                exit = 1;
            } else if (mismatch && failure == null) {
                System.err.println("CITRUS_ASSERTIONS status=fail detail=mismatch-expected-but-passed");
                exit = 1;
            } else if (!mismatch && failure != null) {
                System.err.println("CITRUS_ASSERTIONS status=fail detail=" + failure);
                exit = 1;
            } else {
                System.err.println("CITRUS_ASSERTIONS status=" + (mismatch ? "fail-as-designed" : "ok")
                        + " calls=" + callback.callsSettled()
                        + (failure == null ? "" : " failure=" + failure.getClass().getSimpleName()));
                exit = mismatch ? 1 : 0;
            }
        } catch (Exception e) {
            System.err.println("CITRUS_ASSERTIONS status=fail detail=" + e);
            exit = 1;
        } finally {
            try {
                host.stop();
            } catch (Exception e) {
                System.err.println("spring-live-scenario: stop failed: " + e);
                exit = 1;
            }
        }
        System.err.println("spring-live-scenario: exit=" + exit);
        System.exit(exit);
    }

    private SpringLiveScenarioMain() {
    }
}
