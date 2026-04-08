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

package org.citrusframework.mcp.message;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.mcp.model.McpMarshaller;
import org.citrusframework.mcp.model.McpToolInvocation;
import org.citrusframework.mcp.model.McpToolResponse;
import org.citrusframework.message.DefaultMessage;

public class McpMessage extends DefaultMessage {

    private McpMarshaller marshaller;

    /**
     * Prevent traditional instantiation.
     */
    private McpMessage() { super(); }

    /**
     * Constructor initializes new tool invocation message.
     */
    public McpMessage(McpToolInvocation toolInvocation) {
        super(toolInvocation);
    }

    /**
     * Constructor initializes new tool response message.
     */
    public McpMessage(McpToolResponse toolResponse) {
        super(toolResponse);
    }

    @Override
    public Object getPayload() {
        Object payload = super.getPayload();
        if (payload instanceof McpToolInvocation || payload instanceof McpToolResponse) {
            if (marshaller == null) {
                marshaller = new McpMarshaller();
            }
            return marshaller.json().writeValueAsString(getPayload());
        }

        return payload;
    }

    public McpMessage marshaller(McpMarshaller marshaller) {
        this.marshaller = marshaller;
        return this;
    }

    public static McpMessage toolInvocation(String name) {
        return toolInvocation(name, Collections.emptyMap());
    }

    public static McpMessage toolInvocation(String name, Map<String, Object> args) {
        return new McpMessage(new McpToolInvocation(name, McpToolInvocation.args(args)));
    }

    public static McpMessage toolResponse() {
        return toolResponse(false);
    }

    public static McpMessage toolResponse(boolean isError) {
        return toolResponse(isError, "");
    }

    public static McpMessage toolResponse(boolean isError, String content) {
        return new McpMessage(new McpToolResponse(isError, content));
    }
}
