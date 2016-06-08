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

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidator;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.zookeeper.client.ZooClient;
import com.consol.citrus.zookeeper.command.ZooCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes zookeeper command with given zookeeper client implementation. Possible command result is stored within command object.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class ZooExecuteAction extends AbstractTestAction {

    @Autowired(required = false)
    @Qualifier("zookeeperClient")
    /** Zookeeper client instance  */
    private ZooClient zookeeperClient = new ZooClient();

    /**
     * Zookeeper command to execute
     */
    private ZooCommand command;

    /**
     * Expected command result for validation
     */
    private String expectedCommandResult;

    @Autowired(required = false)
    @Qualifier("zookeeperCommandResultMapper")
    /** JSON data binding */
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private JsonTextMessageValidator jsonTextMessageValidator = new JsonTextMessageValidator();

    @Autowired
    private JsonPathMessageValidator jsonPathMessageValidator = new JsonPathMessageValidator();

    /**
     * An optional validation contextst containing json path validators to validate the command result
     */
    private JsonPathMessageValidationContext jsonPathMessageValidationContext;

    /**
     * List of variable extractors responsible for creating variables from received message content
     */
    private List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ZooExecuteAction.class);

    /**
     * Default constructor.
     */
    public ZooExecuteAction() {
        setName("zookeeper-execute");
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

    private Message getCommandResult(ZooCommand command) {
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
    public ZooCommand getCommand() {
        return command;
    }

    /**
     * Sets zookeeper command to execute.
     *
     * @param command
     * @return
     */
    public ZooExecuteAction setCommand(ZooCommand command) {
        this.command = command;
        return this;
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
     * Sets the zookeeper client.
     *
     * @param zookeeperClient
     */
    public ZooExecuteAction setZookeeperClient(ZooClient zookeeperClient) {
        this.zookeeperClient = zookeeperClient;
        return this;
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
     * Sets the expected command result data.
     *
     * @param expectedCommandResult
     */
    public ZooExecuteAction setExpectedCommandResult(String expectedCommandResult) {
        this.expectedCommandResult = expectedCommandResult;
        return this;
    }

    /**
     * Sets the JSON object mapper.
     *
     * @param jsonMapper
     */
    public ZooExecuteAction setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
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
     * Set the list of variable extractors.
     *
     * @param variableExtractors the variableExtractors to set
     */
    public ZooExecuteAction setVariableExtractors(List<VariableExtractor> variableExtractors) {
        this.variableExtractors = variableExtractors;
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
     * Sets the JsonPathMessageValidationContext for this action.
     *
     * @param jsonPathMessageValidationContext the json-path validation context
     */
    public ZooExecuteAction setJsonPathMessageValidationContext(JsonPathMessageValidationContext jsonPathMessageValidationContext) {
        this.jsonPathMessageValidationContext = jsonPathMessageValidationContext;
        return this;
    }

    /**
     * Gets the JsonPathMessageValidationContext.
     *
     * @return the validationContexts
     */
    public JsonPathMessageValidationContext getJsonPathMessageValidationContext() {
        return jsonPathMessageValidationContext;
    }

}
