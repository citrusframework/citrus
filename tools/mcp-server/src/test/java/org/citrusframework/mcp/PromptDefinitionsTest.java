/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.mcp;

import java.util.List;

import io.quarkiverse.mcp.server.PromptMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptDefinitionsTest {

    private final PromptDefinitions prompts = new PromptDefinitions();

    @Test
    void buildIntegrationReturnsNonEmptyMessages() {
        List<PromptMessage> result = prompts.citrus_write_test("Send and receive messages to/from a direct endpoint", "yaml");
        assertThat(result).isNotEmpty();
    }

    @Test
    void migrateProjectReturnsNonEmptyMessages() {
        List<PromptMessage> result = prompts.citrus_migrate_project("4.x", "5.0");
        assertThat(result).isNotEmpty();
    }

    @Test
    void migrateProjectContainsVersions() {
        List<PromptMessage> result = prompts.citrus_migrate_project("4.x", "5.0");
        assertThat(result).hasSize(1);
        PromptMessage message = result.get(0);
        assertThat(message.content().asText().text())
                .contains("4.x")
                .contains("5.0")
                .contains("migration guide");
    }

    @Test
    void migrateProjectContainsAllWorkflowSteps() {
        List<PromptMessage> result = prompts.citrus_migrate_project("4.x", "5.0");
        String content = result.get(0).content().asText().text();
        assertThat(content)
                .contains("Step 1:")
                .contains("Step 2:")
                .contains("Step 3:")
                .contains("Step 4:")
                .contains("Step 5:")
                .contains("Step 6:")
                .contains("Step 7:")
                .contains("Step 8:");
    }

    @Test
    void migrateProjectReferencesResourceTemplate() {
        List<PromptMessage> result = prompts.citrus_migrate_project("4.x", "5.0");
        String content = result.get(0).content().asText().text();
        assertThat(content).contains("citrus://docs/migration-guide/5.0");
    }
}
