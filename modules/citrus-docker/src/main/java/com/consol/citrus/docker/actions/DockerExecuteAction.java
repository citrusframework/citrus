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

import java.util.ArrayList;
import java.util.List;

/**
 * Executes docker command with given docker client implementation. Possible command result is stored within command object.
 *
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class DockerExecuteAction extends AbstractTestAction {

    @Autowired(required = false)
    @Qualifier("dockerClient")
    /** Docker client instance  */
    private DockerClient dockerClient = new DockerClient();

    /** Docker command to execute */
    private List<DockerCommand> commands = new ArrayList<>();

    @Autowired(required = false)
    @Qualifier("dockerCommandResultMapper")
    /** JSON data binding */
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private JsonTextMessageValidator jsonTextMessageValidator;

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
            for (DockerCommand command : commands) {
                log.info(String.format("Executing Docker command '%s", command.getName()));
                command.execute(dockerClient, context);

                validateCommandResult(command, context);

                log.info(String.format("Successfully executed Docker command '%s", command.getName()));
            }
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
        if (StringUtils.hasText(command.getExpectedCommandResult())) {
            if (command.getCommandResult() == null) {
                throw new ValidationException("Missing command result for validation");
            }

            try {
                String commandResultJson = jsonMapper.writeValueAsString(command.getCommandResult());
                JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
                validationContext.setControlMessage(new DefaultMessage(command.getExpectedCommandResult()));
                jsonTextMessageValidator.validateMessage(new DefaultMessage(commandResultJson), context, validationContext);
                log.info("Validation of command result successful - all values OK!");
            } catch (JsonProcessingException e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * Gets the docker commands to execute.
     * @return
     */
    public List<DockerCommand> getCommands() {
        return commands;
    }

    /**
     * Sets the docker commands to execute.
     * @param commands
     * @return
     */
    public DockerExecuteAction setCommands(List<DockerCommand> commands) {
        this.commands = commands;
        return this;
    }

    /**
     * Adds docker command to execute.
     * @param command
     * @return
     */
    public DockerExecuteAction addCommand(DockerCommand command) {
        this.commands.add(command);
        return this;
    }

    /**
     * Sets single docker command to execute.
     * @param command
     * @return
     */
    public DockerExecuteAction setCommand(DockerCommand command) {
        return addCommand(command);
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
     * Sets the JSON object mapper.
     * @param jsonMapper
     */
    public void setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }
}
