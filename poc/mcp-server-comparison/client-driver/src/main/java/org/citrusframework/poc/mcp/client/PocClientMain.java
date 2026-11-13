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

package org.citrusframework.poc.mcp.client;

import java.util.List;

/**
 * Test-only command-line MCP client entry point. Selects exactly one server:
 * an HTTP URI or a JSON launch descriptor with a child argument array.
 * Never executes a shell command string from a fixture.
 */
public final class PocClientMain {

    public static void main(String[] args) throws Exception {
        LaunchDescriptor descriptor = parse(args);
        ScenarioDriver.RequestExecutor executor;
        if (descriptor.isHttp()) {
            String uri = descriptor.getHttpUri();
            executor = (sessionId, requestId, method, requestJson) ->
                    new ScenarioDriver.Exchange(sessionId, requestId, method, requestJson,
                            "{\"pending\": true, \"uri\": \"" + uri + "\"}", false);
        } else {
            ChildProcess child = new ChildProcess(descriptor.getChildArgs(), descriptor.getTimeoutSeconds());
            ChildProcess.Result result = child.run();
            if (result.timedOut() || result.forcedKill()) {
                throw new IllegalStateException("child did not exit within bound: " + descriptor.getChildArgs());
            }
            String stdout = result.stdout();
            executor = (sessionId, requestId, method, requestJson) ->
                    new ScenarioDriver.Exchange(sessionId, requestId, method, requestJson, stdout, false);
        }
        ScenarioDriver driver = new ScenarioDriver(executor);
        List<ScenarioDriver.Exchange> exchanges = driver.runRoundTrip("session-1");
        driver.writeEvidence(descriptor, exchanges, "server assertions recorded separately in probe report");
        System.out.println("poc-client: scenario=" + descriptor.getScenarioId()
                + " exchanges=" + exchanges.size()
                + " result=" + descriptor.getResultFile());
    }

    static LaunchDescriptor parse(String[] args) {
        String candidate = null;
        String transport = null;
        String scenario = null;
        String httpUri = null;
        List<String> childArgs = null;
        String resultFile = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--candidate" -> candidate = required(args, ++i, "--candidate");
                case "--transport" -> transport = required(args, ++i, "--transport");
                case "--scenario" -> scenario = required(args, ++i, "--scenario");
                case "--http-uri" -> httpUri = required(args, ++i, "--http-uri");
                case "--child-arg" -> {
                    String arg = required(args, ++i, "--child-arg");
                    childArgs = childArgs == null ? new java.util.ArrayList<>() : childArgs;
                    childArgs.add(arg);
                }
                case "--result-file" -> resultFile = required(args, ++i, "--result-file");
                default -> throw new IllegalArgumentException("unknown argument: " + args[i]);
            }
        }
        if (candidate == null || transport == null || scenario == null || resultFile == null) {
            throw new IllegalArgumentException(
                    "required: --candidate --transport --scenario --result-file plus --http-uri or --child-arg...");
        }
        LaunchDescriptor.Builder builder = httpUri != null
                ? LaunchDescriptor.http(candidate, transport, scenario, httpUri, resultFile)
                : LaunchDescriptor.child(candidate, transport, scenario, childArgs, resultFile);
        return builder.build();
    }

    private static String required(String[] args, int index, String flag) {
        if (index >= args.length) {
            throw new IllegalArgumentException("missing value for " + flag);
        }
        return args[index];
    }

    private PocClientMain() {
    }
}
