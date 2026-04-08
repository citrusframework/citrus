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

import java.util.Arrays;
import java.util.List;

public record McpToolResponse(boolean error, List<String> content) {

    public McpToolResponse(boolean error, String... contents) {
        this(error, Arrays.asList(contents));
    }

    public static McpToolResponse success(String... content) {
        return new McpToolResponse(false, Arrays.asList(content));
    }

    public static McpToolResponse error(String... content) {
        return new McpToolResponse(true, Arrays.asList(content));
    }
}
