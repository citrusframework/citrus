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

package org.citrusframework.mcp.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.quarkiverse.mcp.server.RawMessage;
import io.vertx.core.json.JsonObject;

public record McpToolInvocation(String name, String method, List<Argument> args) {

    public McpToolInvocation(String name) {
        this(name, Collections.emptyList());
    }

    public McpToolInvocation(String name, List<Argument> args) {
        this(name, "tools/call", args);
    }

    public McpToolInvocation(String name, String method, List<Argument> args) {
        this.name = name;
        this.method = method;
        this.args = args;
    }

    public static List<Argument> args(Map<String, Object> args) {
        return args.entrySet()
                .stream()
                .map(entry -> arg(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static Argument arg(String name, Object value) {
        return new Argument(name, Optional.ofNullable(value).map(Object::toString).orElse(""));
    }

    public static McpToolInvocation from(RawMessage message) {
        JsonObject json = message.asJsonObject();

        String name = json.getJsonObject("params").getString("name");
        return new McpToolInvocation(name, message.method(), McpToolInvocation.args(json.getJsonObject("params").getJsonObject("arguments").getMap()));
    }

    public record Argument(String name, String value) {
    }
}
