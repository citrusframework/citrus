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

import java.util.Collections;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.command.ContainerCreate;
import com.consol.citrus.docker.command.ContainerInspect;
import com.consol.citrus.docker.command.ContainerStart;
import com.consol.citrus.docker.command.ContainerStop;
import com.consol.citrus.docker.command.ContainerWait;
import com.consol.citrus.docker.command.DockerCommand;
import com.consol.citrus.docker.command.ImageBuild;
import com.consol.citrus.docker.command.ImageInspect;
import com.consol.citrus.docker.command.Info;
import com.consol.citrus.docker.command.Ping;
import com.consol.citrus.docker.command.Version;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Executes docker command with given docker client implementation. Possible command result is stored within command object.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerExecuteAction extends AbstractTestAction {

    /** Docker client instance  */
    private final DockerClient dockerClient;

    /** Docker command to execute */
    private final DockerCommand<?> command;

    /** Expected command result for validation */
    private final String expectedCommandResult;

    /** JSON data binding */
    private final ObjectMapper jsonMapper;

    /** Validator used to validate expected json results */
    private final MessageValidator<? extends ValidationContext> jsonMessageValidator;

    public static final String DEFAULT_JSON_MESSAGE_VALIDATOR = "defaultJsonMessageValidator";

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DockerExecuteAction.class);

    /**
     * Default constructor.
     */
    public DockerExecuteAction(Builder builder) {
        super("docker-execute", builder);

        this.dockerClient = builder.dockerClient;
        this.command = builder.command;
        this.expectedCommandResult = builder.expectedCommandResult;
        this.jsonMapper = builder.jsonMapper;
        this.jsonMessageValidator = builder.validator;
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
                getMessageValidator(context).validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(expectedCommandResult), context, Collections.singletonList(validationContext));
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
     * Find proper JSON message validator. Uses several strategies to lookup default JSON message validator.
     * @param context
     * @return
     */
    private MessageValidator<? extends ValidationContext> getMessageValidator(TestContext context) {
        if (jsonMessageValidator != null) {
            return jsonMessageValidator;
        }

        // try to find json message validator in registry
        MessageValidator<? extends ValidationContext> defaultJsonMessageValidator = context.getMessageValidatorRegistry().getMessageValidators().get(DEFAULT_JSON_MESSAGE_VALIDATOR);

        if (defaultJsonMessageValidator == null) {
            try {
                defaultJsonMessageValidator = context.getReferenceResolver().resolve(DEFAULT_JSON_MESSAGE_VALIDATOR, MessageValidator.class);
            } catch (CitrusRuntimeException e) {
                log.warn("Unable to find default JSON message validator in message validator registry");
            }
        }

        if (defaultJsonMessageValidator == null) {
            // try to find json message validator via resource path lookup
            defaultJsonMessageValidator = MessageValidator.lookup("json")
                    .orElseThrow(() -> new CitrusRuntimeException("Unable to locate proper JSON message validator - please add validator to project"));
        }

        return defaultJsonMessageValidator;
    }

    /**
     * Gets the docker command to execute.
     * @return
     */
    public DockerCommand<?> getCommand() {
        return command;
    }

    /**
     * Gets the docker client.
     * @return
     */
    public DockerClient getDockerClient() {
        return dockerClient;
    }

    /**
     * Gets the expected command result data.
     * @return
     */
    public String getExpectedCommandResult() {
        return expectedCommandResult;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<DockerExecuteAction, Builder> {

        private DockerClient dockerClient = new DockerClient();
        private DockerCommand<?> command;
        private String expectedCommandResult;
        private ObjectMapper jsonMapper = new ObjectMapper();
        private MessageValidator<? extends ValidationContext> validator;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder docker() {
            return new Builder();
        }

        /**
         * Use a custom docker client.
         */
        public Builder client(DockerClient dockerClient) {
            this.dockerClient = dockerClient;
            return this;
        }

        public Builder mapper(ObjectMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
            return this;
        }

        public Builder validator(MessageValidator<? extends ValidationContext> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Use a info command.
         */
        public Builder command(DockerCommand<?> command) {
            this.command = command;
            return this;
        }

        /**
         * Use a info command.
         */
        public Info info() {
            Info command = new Info();
            this.command = command;
            return command;
        }

        /**
         * Adds a ping command.
         */
        public Ping ping() {
            Ping command = new Ping();
            this.command = command;
            return command;
        }

        /**
         * Adds a version command.
         */
        public Version version() {
            Version command = new Version();
            this.command = command;
            return command;
        }

        /**
         * Adds a create command.
         */
        public ContainerCreate create(String imageId) {
            ContainerCreate command = new ContainerCreate();
            command.image(imageId);
            this.command = command;
            return command;
        }

        /**
         * Adds a start command.
         */
        public ContainerStart start(String containerId) {
            ContainerStart command = new ContainerStart();
            command.container(containerId);
            this.command = command;
            return command;
        }

        /**
         * Adds a stop command.
         */
        public ContainerStop stop(String containerId) {
            ContainerStop command = new ContainerStop();
            command.container(containerId);
            this.command = command;
            return command;
        }

        /**
         * Adds a wait command.
         */
        public ContainerWait wait(String containerId) {
            ContainerWait command = new ContainerWait();
            command.container(containerId);
            this.command = command;
            return command;
        }

        /**
         * Adds a inspect container command.
         */
        public ContainerInspect inspectContainer(String containerId) {
            ContainerInspect command = new ContainerInspect();
            command.container(containerId);
            this.command = command;
            return command;
        }

        /**
         * Adds a inspect container command.
         */
        public ImageInspect inspectImage(String imageId) {
            ImageInspect command = new ImageInspect();
            command.image(imageId);
            this.command = command;
            return command;
        }

        /**
         * Adds a inspect container command.
         */
        public ImageBuild buildImage() {
            ImageBuild command = new ImageBuild();
            this.command = command;
            return command;
        }

        /**
         * Adds expected command result.
         * @param result
         * @return
         */
        public Builder result(String result) {
            this.expectedCommandResult = result;
            return this;
        }

        @Override
        public DockerExecuteAction build() {
            return new DockerExecuteAction(this);
        }
    }
}
