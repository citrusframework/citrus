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


import org.citrusframework.endpoint.AbstractEndpointConfiguration;
import org.citrusframework.mcp.model.McpMarshaller;

public class McpEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Server port*/
    private int port = 8080;

    /** Mcp message marshaller converts from XML to MCP message object */
    private McpMarshaller marshaller = new McpMarshaller();

    /**
     * Gets the MCP server port.
     * @return the MCP server port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the MCP server port.
     * @param port the MCP server port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the MCP message marshaller implementation.
     * @return the MCP message marshaller.
     */
    public McpMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the MCP message marshaller implementation.
     * @param marshaller the MCP message marshaller.
     */
    public void setMarshaller(McpMarshaller marshaller) {
        this.marshaller = marshaller;
    }

}
