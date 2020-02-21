/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.zookeeper.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidator;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.zookeeper.client.ZooClient;
import com.consol.citrus.zookeeper.command.Create;
import com.consol.citrus.zookeeper.command.Delete;
import com.consol.citrus.zookeeper.command.Exists;
import com.consol.citrus.zookeeper.command.GetChildren;
import com.consol.citrus.zookeeper.command.GetData;
import com.consol.citrus.zookeeper.command.Info;
import com.consol.citrus.zookeeper.command.SetData;
import com.consol.citrus.zookeeper.command.ZooCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Executes zookeeper command with given zookeeper client implementation. Possible command result is stored within command object.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class ZooExecuteAction extends AbstractTestAction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ZooExecuteAction.class);

    /** Zookeeper client instance  */
    private final ZooClient zookeeperClient;

    /** Zookeeper command to execute*/
    private final ZooCommand<?> command;

    /** Expected command result for validation*/
    private final String expectedCommandResult;

    /** JSON data binding */
    private final ObjectMapper jsonMapper;

    private final JsonTextMessageValidator jsonTextMessageValidator;

    private final JsonPathMessageValidator jsonPathMessageValidator;

    /** An optional validation contextst containing json path validators to validate the command result */
    private final JsonPathMessageValidationContext jsonPathMessageValidationContext;

    /** List of variable extractors responsible for creating variables from received message content */
    private final List<VariableExtractor> variableExtractors;

    /**
     * Default constructor.
     */
    public ZooExecuteAction(Builder builder) {
        super("zookeeper-execute", builder);

        this.zookeeperClient = builder.zookeeperClient;
        this.command = builder.command;
        this.expectedCommandResult = builder.expectedCommandResult;
        this.jsonMapper = builder.jsonMapper;
        this.jsonTextMessageValidator = builder.jsonTextMessageValidator;
        this.jsonPathMessageValidator = builder.jsonPathMessageValidator;
        this.jsonPathMessageValidationContext = builder.jsonPathMessageValidationContext;
        this.variableExtractors = builder.variableExtractors;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Executing zookeeper command '%s'", command.getName()));
            }
            command.execute(zookeeperClient, context);

            validateCommandResult(command, context);

            log.info(String.format("Zookeeper command execution successful: '%s'", command.getName()));
        } catch (CitrusRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to perform zookeeper command", e);
        }
    }

    /**
     * Validate command results.
     *
     * @param command
     * @param context
     */
    private void validateCommandResult(ZooCommand command, TestContext context) {
        Message commandResult = getCommandResult(command);

        extractVariables(commandResult, context);

        if (log.isDebugEnabled()) {
            log.debug("Validating Zookeeper response");
        }

        if (StringUtils.hasText(expectedCommandResult)) {
            assertResultExists(commandResult);
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            jsonTextMessageValidator.validateMessage(commandResult, new DefaultMessage(expectedCommandResult), context, validationContext);
        }

        if (jsonPathMessageValidationContext != null) {
            assertResultExists(commandResult);
            jsonPathMessageValidator.validateMessage(commandResult, null, context, jsonPathMessageValidationContext);
        }

        log.info("Zookeeper command result validation successful - all values OK!");

        if (command.getResultCallback() != null) {
            command.getResultCallback().doWithCommandResult(command.getCommandResult(), context);
        }
    }

    private void assertResultExists(Message commandResult) {
        if (commandResult == null) {
            throw new ValidationException("Missing Zookeeper command result");
        }
    }

    private Message getCommandResult(ZooCommand<?> command) {
        if (command.getCommandResult() == null) {
            return null;
        }

        try {
            Object commandResult = command.getCommandResult();
            String commandResultJson = jsonMapper.writeValueAsString(commandResult);
            return new DefaultMessage(commandResultJson);
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    private void extractVariables(Message commandResult, TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Extracting variables from Zookeeper response");
        }

        for (VariableExtractor variableExtractor : variableExtractors) {
            variableExtractor.extractVariables(commandResult, context);
        }
    }

    /**
     * Gets the zookeeper command to execute.
     *
     * @return
     */
    public ZooCommand<?> getCommand() {
        return command;
    }

    /**
     * Gets the zookeeper client.
     *
     * @return
     */
    public ZooClient getZookeeperClient() {
        return zookeeperClient;
    }

    /**
     * Gets the expected command result data.
     *
     * @return
     */
    public String getExpectedCommandResult() {
        return expectedCommandResult;
    }

    /**
     * Adds a new variable extractor.
     *
     * @param variableExtractor the variableExtractor to add
     */
    public ZooExecuteAction addVariableExtractors(VariableExtractor variableExtractor) {
        this.variableExtractors.add(variableExtractor);
        return this;
    }

    /**
     * Gets the variable extractors.
     *
     * @return the variableExtractors
     */
    public List<VariableExtractor> getVariableExtractors() {
        return variableExtractors;
    }

    /**
     * Gets the JsonPathMessageValidationContext.
     *
     * @return the validationContexts
     */
    public JsonPathMessageValidationContext getJsonPathMessageValidationContext() {
        return jsonPathMessageValidationContext;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<ZooExecuteAction, Builder> {

        public static final String DEFAULT_MODE = "EPHEMERAL";
        public static final String DEFAULT_ACL = Create.ACL_OPEN;
        public static final int DEFAULT_VERSION = 0;

        private ZooClient zookeeperClient = new ZooClient();
        private ZooCommand<?> command;
        private String expectedCommandResult;
        private ObjectMapper jsonMapper = new ObjectMapper();
        private JsonTextMessageValidator jsonTextMessageValidator = new JsonTextMessageValidator();
        private JsonPathMessageValidator jsonPathMessageValidator = new JsonPathMessageValidator();
        private JsonPathMessageValidationContext jsonPathMessageValidationContext;
        private List<VariableExtractor> variableExtractors = new ArrayList<>();

        private ReferenceResolver referenceResolver;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder zookeeper() {
            return new Builder();
        }

        /**
         * Use a custom zoo client.
         */
        public Builder client(ZooClient zooClient) {
            this.zookeeperClient = zooClient;
            return this;
        }

        /**
         * Sets the zookeeper command to execute.
         */
        public Builder command(ZooCommand<?> command) {
            this.command = command;
            return this;
        }

        /**
         * Adds a create command.
         */
        public Create create(String path, String data) {
            Create command = new Create();
            command.path(path);
            command.data(data);
            command.mode(DEFAULT_MODE);
            command.acl(DEFAULT_ACL);
            command(command);
            return command;
        }

        /**
         * Adds a delete command.
         */
        public Delete delete(String path) {
            Delete command = new Delete();
            command.path(path);
            command.version(DEFAULT_VERSION);
            command(command);
            return command;
        }

        /**
         * Adds an exists command.
         */
        public Exists exists(String path) {
            Exists command = new Exists();
            command.path(path);
            command(command);
            return command;
        }

        /**
         * Adds an exists command.
         */
        public GetChildren children(String path) {
            GetChildren command = new GetChildren();
            command.path(path);
            command(command);
            return command;
        }

        /**
         * Adds a get-data command.
         */
        public GetData get(String path) {
            GetData command = new GetData();
            command.path(path);
            command(command);
            return command;
        }

        /**
         * Use an info command.
         */
        public Info info() {
            Info command = new Info();
            command(command);
            return command;
        }

        /**
         * Adds a set-data command.
         */
        public SetData set(String path, String data) {
            SetData command = new SetData();
            command.path(path);
            command.data(data);
            command.version(0);
            command(command);
            return command;
        }

        /**
         * Adds expected command result.
         *
         * @param result
         * @return
         */
        public Builder result(String result) {
            this.expectedCommandResult = result;
            return this;
        }

        public Builder mapper(ObjectMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
            return this;
        }

        public Builder validator(JsonTextMessageValidator validator) {
            this.jsonTextMessageValidator = validator;
            return this;
        }

        public Builder validator(JsonPathMessageValidator validator) {
            this.jsonPathMessageValidator = validator;
            return this;
        }

        /**
         * Adds variable extractor for extracting variable from command response.
         *
         * @param jsonPath the json path to reference the value to be extracted
         * @param variableName the name of the variable to store the extracted value in
         * @return
         */
        public Builder extract(String jsonPath, String variableName) {
            JsonPathVariableExtractor jsonPathVariableExtractor = new JsonPathVariableExtractor();
            Map<String, String> pathVariableMap = new HashMap<>();
            pathVariableMap.put(jsonPath, variableName);
            jsonPathVariableExtractor.setJsonPathExpressions(pathVariableMap);
            return extractor(jsonPathVariableExtractor);
        }

        public Builder extractor(VariableExtractor variableExtractor) {
            this.variableExtractors.add(variableExtractor);
            return this;
        }

        /**
         * Adds variable extractor for extracting variable from command response.
         *
         * @param jsonPath the json path to reference the value to be extracted
         * @param expectedValue the expected value (or variable to retrieve the expected value from)
         * @return
         */
        public Builder validate(String jsonPath, String expectedValue) {
            if (this.jsonPathMessageValidationContext == null) {
                this.jsonPathMessageValidationContext = new JsonPathMessageValidationContext();
            }

            this.jsonPathMessageValidationContext.getJsonPathExpressions().put(jsonPath, expectedValue);
            return this;
        }

        public Builder validationContext(JsonPathMessageValidationContext validationContext) {
            this.jsonPathMessageValidationContext = validationContext;
            return this;
        }

        /**
         * Sets the Spring bean application context.
         * @param referenceResolver
         */
        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;

            if (referenceResolver.isResolvable("zookeeperClient")) {
                this.zookeeperClient = referenceResolver.resolve("zookeeperClient", ZooClient.class);
            }

            if (referenceResolver.isResolvable("zookeeperCommandResultMapper")) {
                this.jsonMapper = referenceResolver.resolve("zookeeperCommandResultMapper", ObjectMapper.class);
            }

            return this;
        }

        @Override
        public ZooExecuteAction build() {
            return new ZooExecuteAction(this);
        }
    }

}
