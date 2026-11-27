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

package org.citrusframework.http.server;

import org.citrusframework.api.yaml.TestVariable;

/**
 * Test variables automatically set by an HTTP server endpoint when auto-started.
 * Variable names use the server name as prefix (e.g. {@code probe.port}).
 */
public enum HttpServerTestVariables implements TestVariable {
    PORT("{serverName}.port", "The port the HTTP server is listening on."),
    URL("{serverName}.url", "The base URL of the HTTP server (http://localhost:<port><contextPath>).");

    private final String variableName;
    private final String description;

    HttpServerTestVariables(String variableName, String description) {
        this.variableName = variableName;
        this.description = description;
    }

    @Override
    public String variableName() {
        return variableName;
    }

    @Override
    public String description() {
        return description;
    }
}
