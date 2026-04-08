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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.base.server.AbstractServerBuilder;
import org.citrusframework.mcp.model.McpMarshaller;
import org.citrusframework.mcp.model.McpTool;
import org.citrusframework.util.StringUtils;

@SchemaType(module = "citrus-mcp")
public class McpServerBuilder extends AbstractServerBuilder<McpServer, McpServerBuilder> {

    /** Endpoint target */
    private final McpServer endpoint = new McpServer();

    private String marshaller;

    @Override
    public McpServer build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(marshaller)) {
                marshaller(referenceResolver.resolve(marshaller, McpMarshaller.class));
            }
        }
        return super.build();
    }

    @Override
    protected McpServer getEndpoint() {
        return endpoint;
    }

    public McpServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    @SchemaProperty(description = "The MCP server port.")
    public void setPort(int port) {
        port(port);
    }

    public McpServerBuilder marshaller(McpMarshaller marshaller) {
        endpoint.setMarshaller(marshaller);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom mail message marshaller.")
    public void setMarshaller(String marshaller) {
        this.marshaller = marshaller;
    }

    public McpServerBuilder tools(Set<McpTool> tools) {
        endpoint.setTools(tools);
        return this;
    }

    @SchemaProperty(description = "Sets a list of tools that the MCP server does provide.")
    public void setTools(Set<McpTool> tools) {
        tools(tools);
    }

    public McpServerBuilder tools(McpTool... tools) {
        endpoint.setTools(Arrays.stream(tools).collect(Collectors.toSet()));
        return this;
    }

    public McpServerBuilder tool(String name, String description) {
        endpoint.addTool(new McpTool(name, description));
        return this;
    }
}
