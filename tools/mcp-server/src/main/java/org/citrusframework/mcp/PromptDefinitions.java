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
                If appropriate, use special test actions regarding Testcontainers or Apache Camel JBang infra services to start the infrastructure as part of the test.

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

}
