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

package com.consol.citrus.docker.actions;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.DockerCommand;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

/**
 * Executes docker command with given docker client implementation. Possible command result is stored within command object.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerExecuteAction extends AbstractTestAction {

    @Autowired(required = false)
    @Qualifier("dockerClient")
    /** Docker client instance  */
    private DockerClient dockerClient = new DockerClient();

    /** Docker command to execute */
    private DockerCommand command;

    /** Expected command result for validation */
    private String expectedCommandResult;

    @Autowired(required = false)
    @Qualifier("dockerCommandResultMapper")
    /** JSON data binding */
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private JsonTextMessageValidator jsonTextMessageValidator = new JsonTextMessageValidator();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DockerExecuteAction.class);

    /**
     * Default constructor.
     */
    public DockerExecuteAction() {
        setName("docker-execute");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Executing Docker command '%s'", command.getName()));
            }
            command.execute(dockerClient, context);

            validateCommandResult(command, context);

            log.info(String.format("Docker command execution successful: '%s'", command.getName()));
        } catch (CitrusRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to perform docker command", e);
        }
    }

    /**
     * Validate command results.
     * @param command
     * @param context
     */
    private void validateCommandResult(DockerCommand command, TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Docker command result validation");
        }

        if (StringUtils.hasText(expectedCommandResult)) {
            if (command.getCommandResult() == null) {
                throw new ValidationException("Missing Docker command result");
            }

            try {
                String commandResultJson = jsonMapper.writeValueAsString(command.getCommandResult());
                JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
                jsonTextMessageValidator.validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(expectedCommandResult), context, validationContext);
                log.info("Docker command result validation successful - all values OK!");
            } catch (JsonProcessingException e) {
                throw new CitrusRuntimeException(e);
            }
        }

        if (command.getResultCallback() != null) {
            command.getResultCallback().doWithCommandResult(command.getCommandResult(), context);
        }
    }

    /**
     * Gets the docker command to execute.
     * @return
     */
    public DockerCommand getCommand() {
        return command;
    }

    /**
     * Sets docker command to execute.
     * @param command
     * @return
     */
    public DockerExecuteAction setCommand(DockerCommand command) {
        this.command = command;
        return this;
    }

    /**
     * Gets the docker client.
     * @return
     */
    public DockerClient getDockerClient() {
        return dockerClient;
    }

    /**
     * Sets the docker client.
     * @param dockerClient
     */
    public DockerExecuteAction setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        return this;
    }

    /**
     * Gets the expected command result data.
     * @return
     */
    public String getExpectedCommandResult() {
        return expectedCommandResult;
    }

    /**
     * Sets the expected command result data.
     * @param expectedCommandResult
     */
    public DockerExecuteAction setExpectedCommandResult(String expectedCommandResult) {
        this.expectedCommandResult = expectedCommandResult;
        return this;
    }

    /**
     * Sets the JSON object mapper.
     * @param jsonMapper
     */
    public DockerExecuteAction setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }
}
