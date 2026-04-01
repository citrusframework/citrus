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

package org.citrusframework.mcp;

import java.util.stream.Collectors;

import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.TextResourceContents;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * MCP Resources exposing Citrus documentation and best practices.
 */
@ApplicationScoped
public class DocsResources {

    @Inject
    DocsData docsData;

    /**
     * Resource provides access to Citrus YAML DSL schema.
     */
    @Resource(uri = "citrus://docs/best-practices",
            name = "citrus_docs_best_practices",
            title = "Citrus Best Practices",
            description = "Collection of best practices for Writing Citrus tests.",
            mimeType = "application/json")
    public TextResourceContents citrus_docs_best_practices() {
        String uri = "citrus://docs/best-practices";
        return new TextResourceContents(uri, "[" + docsData.getBestPractices()
                .stream()
                .map("\"%s\""::formatted)
                .collect(Collectors.joining(",")) + "]", "application/json");
    }

}
