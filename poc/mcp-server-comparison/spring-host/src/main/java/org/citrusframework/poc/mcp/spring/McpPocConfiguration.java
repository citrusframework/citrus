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

/**
 * Intended Spring configuration for the live T5 experiment (documentation placeholder).
 *
 * <p>Live wiring keeps the Spring AI-managed high-level MCP server as protocol owner
 * (MVC Streamable HTTP starter or stdio configuration, single selected transport),
 * declares the PoC endpoint with {@code autoStart=false}, disables the owned
 * context's independent shutdown-hook registration, and routes banners/logging away
 * from stdout for stdio. Ordinary resource registration alone does not satisfy AC4;
 * the whole-list customizer from T7 must preserve high-level ownership. A manually
 * wired transport plus the same high-level server is only a bounded secondary variant;
 * replacing the high-level server with Citrus-owned session policy is a
 * boundary-changing alternative for maintainer review, not the Spring PoC.
 */
public final class McpPocConfiguration {

    public static final String ENDPOINT_BEAN = "mcpPocEndpoint";
    public static final boolean AUTO_START = false;

    private McpPocConfiguration() {
    }
}
