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

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.inject.Inject;
import org.citrusframework.mcp.model.McpMarshaller;
import org.citrusframework.mcp.model.McpToolResponse;
import org.eclipse.microprofile.reactive.messaging.Channel;

@WebSocket(path = "/websocket")
public class McpWebSocketEndpoint {

    @Inject
    @Channel("tool-response")
    MutinyEmitter<McpToolResponse> responseEmitter;

    private final McpMarshaller marshaller = new McpMarshaller();

    @OnTextMessage
    public Uni<Void> process(String response) {
        McpToolResponse toolResponse = marshaller.json().readValue(response, McpToolResponse.class);
        return responseEmitter.send(toolResponse);
    }
}
