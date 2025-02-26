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

package org.citrusframework.agent.connector.actions;

import java.io.IOException;
import java.util.Optional;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidator;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

/**
 * Action runs given test actions with Citrus agent.
 * Connects to the agent via Http and calls the agent service to execute the given test code.
 */
public class AgentRunAction extends AbstractAgentAction {

    private final String type;
    private final String sourceCode;
    private final Resource sourceFile;
    private final String sourceFilePath;
    private final boolean autoConnect;

    public AgentRunAction(Builder builder) {
        super("run", builder);

        this.type = builder.type;
        this.sourceCode = builder.sourceCode;
        this.sourceFile = builder.sourceFile;
        this.sourceFilePath = builder.sourceFilePath;
        this.autoConnect = builder.autoConnect;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            String testName;
            if (context.getVariables().containsKey(CitrusSettings.TEST_NAME_VARIABLE)) {
                testName = context.getVariable(CitrusSettings.TEST_NAME_VARIABLE);
            } else {
                testName = "citrus-test";
            }

            String agent = context.replaceDynamicContentInString(agentName);

            logger.info("Running test '%s' on Citrus agent '%s'".formatted(testName, agent));

            String fileExt = type;
            String source = "";
            if (StringUtils.hasText(sourceCode)) {
                source = context.replaceDynamicContentInString(sourceCode);
            } else if (sourceFile != null) {
                fileExt = Optional.ofNullable(fileExt).orElse(FileUtils.getFileExtension(sourceFile.getLocation()));
                testName = FileUtils.getBaseName(FileUtils.getFileName(sourceFile.getLocation()));
                source = context.replaceDynamicContentInString(FileUtils.readToString(sourceFile));
            } else if (StringUtils.hasText(sourceFilePath)) {
                fileExt = Optional.ofNullable(fileExt).orElse(FileUtils.getFileExtension(sourceFilePath));
                testName = FileUtils.getBaseName(FileUtils.getFileName(sourceFilePath));
                source = context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(sourceFilePath, context)));
            }

            String clientName = agent + ".client";
            HttpClient httpClient = resolveHttpClient(agent, clientName, context);

            String name;
            if (StringUtils.hasText(fileExt)) {
                name = "%s.%s".formatted(testName, fileExt);
            } else {
                name = testName;
            }

            HttpMessage request = new HttpMessage(source)
                    .method(HttpMethod.POST)
                    .path("/execute/" + name);

            if (IsXmlPredicate.getInstance().test(source)) {
                request.contentType(MediaType.APPLICATION_XML_VALUE);
            } else if (source.trim().startsWith("name:") || source.trim().startsWith("actions:")) {
                request.contentType(MediaType.APPLICATION_YAML_VALUE);
            } else {
                request.contentType(MediaType.TEXT_PLAIN_VALUE);
            }

            httpClient.send(request, context);

            Message response = httpClient.receive(context);
            if (response instanceof HttpMessage httpResponse) {
                verifyAgentResponse(httpResponse, context);
            } else {
                verifyAgentResponse(new HttpMessage(response), context);
            }

            logger.info("Test '%s' on Citrus agent '%s' finished successfully - All values OK".formatted(testName, agent));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read test source file", e);
        }
    }

    private HttpClient resolveHttpClient(String agent, String agentClient, TestContext context) {
        HttpClient httpClient;
        if (context.getReferenceResolver().isResolvable(agentClient, HttpClient.class)) {
            httpClient = context.getReferenceResolver().resolve(agentClient, HttpClient.class);
        } else if (autoConnect) {
            AgentConnectAction connectAction = new AgentConnectAction.Builder()
                    .agent(agent)
                    .build();
            connectAction.execute(context);

            httpClient = context.getReferenceResolver().resolve(agentClient, HttpClient.class);
        } else {
            throw new CitrusRuntimeException("Missing connection to Citrus agent - no Http client '%s' found in registry".formatted(agentClient));
        }

        return httpClient;
    }

    private void verifyAgentResponse(HttpMessage response, TestContext context) {
        if (!HttpStatusCode.valueOf(200).equals(response.getStatusCode())) {
            throw new CitrusRuntimeException(("Failed to verify Citrus agent response, expected 200 OK success status code, " +
                    "but was %d %s").formatted(response.getStatusCode().value(), response.getReasonPhrase()));
        }

        JsonPathMessageValidator validator = new JsonPathMessageValidator();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.getJsonPathExpressions().put("$..result", "SUCCESS");
        validator.validateMessage(response, response, context, validationContext);
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractAgentAction.Builder<AgentRunAction, Builder> {

        private String type;
        private String sourceCode;
        private Resource sourceFile;
        private String sourceFilePath;
        private boolean autoConnect = true;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder sourceCode(String sourceCode) {
            this.sourceCode = sourceCode;
            return this;
        }

        public Builder sourceFile(Resource sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public Builder sourceFile(String sourceFilePath) {
            this.sourceFilePath = sourceFilePath;
            return this;
        }

        public Builder autoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
            return this;
        }

        @Override
        public AgentRunAction build() {
            return new AgentRunAction(this);
        }
    }

}
