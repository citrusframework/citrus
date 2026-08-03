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

import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MCP Prompt definitions that provide structured multi-step workflows for LLMs.
 * <p>
 * Prompts guide the LLM through orchestrating multiple existing tools in the correct sequence, rather than requiring it
 * to discover the workflow on its own.
 */
@ApplicationScoped
public class PromptDefinitions {

    /**
     * Guided workflow for writing a Citrus test in given domain specific language and from given requirements.
     */
    @Prompt(name = "citrus_write_test",
            description = "Guided workflow to write a Citrus test in given domain specific language and from given requirements: "
                          + "consolidate best practices for writing Citrus tests, analyze requirements, "
                          + "discover test infrastructure, discover endpoints, discover test actions, "
                          + "generate a test in given domain specific language, validate it, and present the results.")
    public List<PromptMessage> citrus_write_test(
            @PromptArg(name = "requirements",
                       description = "Natural-language description of what the test should do") String requirements,
            @PromptArg(name = "language", description = "Domain specific language to use, one of yaml, xml, groovy, feature, Java (default: yaml)",
                       required = false) String language) {

        String resolvedDsl = language != null && !language.isBlank() ? language : "yaml";

        String instructions = """
                You are writing a Citrus test using the "%s" domain specific language.

                ## Requirements
                %s

                ## Workflow

                Follow these steps in order:

                ### Step 1: Use best practices
                Retrieve best practices using the MCP resource `citrus_docs_best_practices` and follow these rules when writing the test.

                ### Step 2: Identify required infrastructure
                Analyze the requirements above and identify test infrastructure needed such as databases, message brokers, 3rd party services.
                If appropriate, use special test actions regarding Testcontainers or Apache Camel CLI infra services to start the infrastructure as part of the test.

                ### Step 3: Identify endpoints
                Analyze the requirements above and identify the Citrus endpoints needed.
                Call `citrus_catalog_endpoints` with a relevant filter to find matching endpoints and gather the information given.

                ### Step 4: Identify Test actions
                Determine which Citrus test actions are needed (e.g., send, receive, print).
                Call `citrus_catalog_actions` with a relevant filter to find matching test actions.

                ### Step 5: Get endpoint details
                For each endpoint you selected, call `citrus_catalog_endpoint` with the endpoint name \
                to get its endpoint property options, required parameters, and URI syntax.

                ### Step 6: Get test action details
                For each test action you selected, call `citrus_catalog_action` with the action name \
                to get its property options, required parameters, and usage information.

                ### Step 7: Write the test
                Using the gathered information, write a complete test definition using the given domain specific language. \
                Use correct endpoint URI syntax and required options from the documentation.

                ### Step 8: Validate
                Validate the test to check for syntax errors and unused or undeclared test variables.
                If available, retrieve the schema via MCP resource `citrus_dsl_schema_%s` and use this schema to check for syntax errors.
                If validation fails, fix the issues and re-validate.

                ### Step 9: Present result
                Present the final test along with:
                - A brief explanation of each endpoint and test action used
                - Instructions for running the route (e.g., with Citrus JBang)
                """.formatted(resolvedDsl, requirements, resolvedDsl);

        return List.of(PromptMessage.withUserRole(instructions));
    }

    /**
     * Guided workflow for migrating a Citrus project from one version to another.
     */
    @Prompt(name = "citrus_migrate_project",
            description = "Guided workflow to migrate a Citrus project from one version to another: "
                          + "retrieve the migration guide, analyze dependencies, apply package renames, "
                          + "update DSL syntax, update configuration properties, flag API and SPI changes, "
                          + "and present a summary of all changes.")
    public List<PromptMessage> citrus_migrate_project(
            @PromptArg(name = "sourceVersion",
                       description = "Current Citrus version of the project (e.g. 4.x)") String sourceVersion,
            @PromptArg(name = "targetVersion",
                       description = "Target Citrus version to migrate to (e.g. 5.0)") String targetVersion) {

        String instructions = """
                You are migrating a Citrus project from version %s to version %s.

                ## Versions
                - **Source version:** %s
                - **Target version:** %s

                ## Workflow

                Follow these steps in order:

                ### Step 1: Retrieve the migration guide
                Retrieve the migration guide by reading the MCP resource `citrus://docs/migration-guide/%s`. \
                This contains the structured migration data including artifact renames, dependency upgrades, \
                package renames, DSL renames, property renames, API changes, and SPI changes. \
                Parse the JSON response and use it to drive the remaining steps.

                ### Step 2: Analyze dependencies (pom.xml / build files)
                Examine the project's `pom.xml` (or Gradle build files) and apply the following changes:
                - **Artifact renames:** For each artifact rename in the migration guide, find the old artifactId \
                in the project's dependencies and replace it with the new artifactId.
                - **Dependency upgrades:** For each dependency upgrade in the migration guide, check if the project \
                uses the listed dependency and update the version accordingly.
                - Update the Citrus version from %s to %s in the project's dependency management.

                ### Step 3: Apply package renames across source code
                For each package rename in the migration guide, search the project's Java source files \
                (both main and test sources) for import statements and fully-qualified references using \
                the old package name. Replace them with the new package name. \
                Pay attention to any notes on the renames — some renames apply only to API interfaces, \
                not to implementation classes.

                ### Step 4: Update DSL syntax
                For each DSL rename in the migration guide, search the project's test files for the old syntax \
                and replace it with the new syntax. Check all DSL formats:
                - **Java DSL:** method calls in `.java` files
                - **XML DSL:** elements in `.xml` test files
                - **YAML DSL:** keys in `.yaml` / `.yml` test files

                ### Step 5: Update configuration properties
                For each property rename in the migration guide, search the project's configuration files \
                (`application.properties`, `citrus-application.properties`, `citrus.properties`, \
                and any YAML configuration files) for the old property name and replace it with the new one.

                ### Step 6: Flag API changes
                Review the API changes listed in the migration guide and check if the project is affected:
                - For interface extractions: check if the project instantiates the old concrete class directly.
                - For class relocations: check if the project references the old package.
                - For class deletions: check if the project uses the deleted class.
                Present each API change that affects the project and provide specific guidance for resolving it.

                ### Step 7: Flag SPI changes
                Review the SPI changes listed in the migration guide and check if the project has custom \
                `META-INF/services/` files or `META-INF/citrus/` SPI files that reference the old class names. \
                Flag any that need updating and provide the correct new values.

                ### Step 8: Present migration summary
                Present a comprehensive summary of all changes made and any manual steps remaining:
                - List all dependency changes applied
                - List all package renames applied (grouped by module)
                - List all DSL syntax updates applied
                - List all property renames applied
                - List all API changes that require manual attention
                - List all SPI changes that require manual attention
                - Highlight any changes that could not be applied automatically and need manual review
                """.formatted(sourceVersion, targetVersion,
                              sourceVersion, targetVersion,
                              targetVersion,
                              sourceVersion, targetVersion);

        return List.of(PromptMessage.withUserRole(instructions));
    }

}
