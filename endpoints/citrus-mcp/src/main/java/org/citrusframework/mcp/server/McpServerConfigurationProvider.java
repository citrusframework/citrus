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

import java.util.Collections;

import io.quarkus.runtime.annotations.CommandLineArguments;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.citrusframework.api.xml.StringSource;
import org.citrusframework.mcp.model.McpMarshaller;
import org.citrusframework.mcp.model.McpServerConfiguration;

@ApplicationScoped
public class McpServerConfigurationProvider {

    private McpServerConfiguration configuration;

    @Inject
    @CommandLineArguments
    String[] args;

    @PostConstruct
    public void init() {
        if (args.length > 0) {
            configuration = new McpMarshaller().json().readValue(new StringSource(args[0]).getReader(), McpServerConfiguration.class);
        } else {
            configuration = new McpServerConfiguration(McpServer.class.getSimpleName(), 10000L, Collections.emptySet());
        }
    }

    @Produces
    public McpServerConfiguration getConfiguration() {
        return configuration;
    }
}
