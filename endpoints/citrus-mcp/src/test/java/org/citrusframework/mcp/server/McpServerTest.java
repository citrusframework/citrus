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

package org.citrusframework.mcp.server;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.mcp.model.McpTool;
import org.citrusframework.mcp.model.McpToolResponse;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;

@CitrusSupport
public class McpServerTest implements TestActionSupport {

    @CitrusResource
    private TestCaseRunner t;

    @BindToRegistry
    private final McpServer mcpServer = new McpServerBuilder()
            .port(8080)
            .timeout(10000L)
            .tools(
                new McpTool("weather-service", "Get the actual temperature in current location.",
                        new McpToolResponse(false, "0.0"))
            )
            .autoStart(true)
            .build();

    @Test
    public void shouldHandleWeatherServiceToolInvocation() {
        t.when(receive()
                .endpoint(mcpServer)
                .timeout(60000L)
                .message()
                .body("""
                {
                    "name" : "weather-service",
                    "method" : "tools/call",
                    "args" : []
                }
                """)
        );

        t.then(send().endpoint(mcpServer)
                .message()
                .body("""
                {
                    "error": false,
                    "content": [ "24.5" ]
                }
                """)
        );

        t.then(delay().milliseconds(5000L));
    }
}
