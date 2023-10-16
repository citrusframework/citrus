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

package org.citrusframework.docker.actions;

import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.*;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(DockerExecuteAction.class);

    /**
     * Default constructor.
     */
    public DockerExecuteAction(Builder builder) {
        super("docker-execute", builder);

        this.dockerClient = builder.dockerClient;
        this.command = builder.commandBuilder.command();
        this.expectedCommandResult = builder.expectedCommandResult;
        this.jsonMapper = builder.jsonMapper;
        this.jsonMessageValidator = builder.validator;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Executing Docker command '%s'", command.getName()));
            }
            command.execute(dockerClient, context);

            validateCommandResult(command, context);

            logger.info(String.format("Docker command execution successful: '%s'", command.getName()));
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
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Docker command result validation");
        }

        if (StringUtils.hasText(expectedCommandResult)) {
            if (command.getCommandResult() == null) {
                throw new ValidationException("Missing Docker command result");
            }

            try {
                String commandResultJson = jsonMapper.writeValueAsString(command.getCommandResult());
                JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
                getMessageValidator(context).validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(expectedCommandResult), context, Collections.singletonList(validationContext));
                logger.info("Docker command result validation successful - all values OK!");
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
        Optional<MessageValidator<? extends ValidationContext>> defaultJsonMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_JSON_MESSAGE_VALIDATOR);

        if (!defaultJsonMessageValidator.isPresent()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_MESSAGE_VALIDATOR)) {
            defaultJsonMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (!defaultJsonMessageValidator.isPresent()) {
            // try to find json message validator via resource path lookup
            defaultJsonMessageValidator = MessageValidator.lookup("json");
        }

        if (defaultJsonMessageValidator.isPresent()) {
            return defaultJsonMessageValidator.get();
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON message validator - please add validator to project");
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
    public static class Builder extends AbstractTestActionBuilder<DockerExecuteAction, Builder> {

        private DockerClient dockerClient = new DockerClient();
        private AbstractDockerCommandBuilder<?, ?, ?> commandBuilder;
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
         * Adds some command via abstract command builder.
         */
        public <R, S extends AbstractDockerCommandBuilder<R, AbstractDockerCommand<R>, S>> Builder command(final DockerCommand<R> dockerCommand) {
            this.commandBuilder = new AbstractDockerCommandBuilder<R, AbstractDockerCommand<R>, S>(this, null) {
                public AbstractDockerCommand<R> command() {
                    return (AbstractDockerCommand<R>) dockerCommand;
                }
            };
            return this;
        }

        /**
         * Sets the command builder.
         * @param builder
         * @param <T>
         * @return
         */
        private <T extends AbstractDockerCommandBuilder<?, ?, ?>> T commandBuilder(T builder) {
            this.commandBuilder = builder;
            return builder;
        }

        /**
         * Use a info command.
         */
        public Info.Builder info() {
            return commandBuilder(new Info.Builder(this));
        }

        /**
         * Adds a ping command.
         */
        public Ping.Builder ping() {
            return commandBuilder(new Ping.Builder(this));
        }

        /**
         * Adds a version command.
         */
        public Version.Builder version() {
            return commandBuilder(new Version.Builder(this));
        }

        /**
         * Adds a create command.
         */
        public ContainerCreate.Builder create(String imageId) {
            return commandBuilder(new ContainerCreate.Builder(this))
                    .image(imageId);
        }

        /**
         * Adds a start command.
         */
        public ContainerStart.Builder start(String containerId) {
            return commandBuilder(new ContainerStart.Builder(this))
                    .container(containerId);
        }

        /**
         * Adds a stop command.
         */
        public ContainerStop.Builder stop(String containerId) {
            return commandBuilder(new ContainerStop.Builder(this))
                    .container(containerId);
        }

        /**
         * Adds a wait command.
         */
        public ContainerWait.Builder wait(String containerId) {
            return commandBuilder(new ContainerWait.Builder(this))
                    .container(containerId);
        }

        /**
         * Adds a inspect container command.
         */
        public ContainerInspect.Builder inspectContainer(String containerId) {
            return commandBuilder(new ContainerInspect.Builder(this))
                    .container(containerId);
        }

        /**
         * Adds a inspect image command.
         */
        public ImageInspect.Builder inspectImage(String imageId) {
            return commandBuilder(new ImageInspect.Builder(this))
                    .image(imageId);
        }

        /**
         * Adds a build image command.
         */
        public ImageBuild.Builder buildImage() {
            return commandBuilder(new ImageBuild.Builder(this));
        }

        /**
         * Adds a pull image command.
         */
        public ImagePull.Builder pullImage(String imageId) {
            return commandBuilder(new ImagePull.Builder(this))
                    .image(imageId);
        }

        /**
         * Adds a remove image command.
         */
        public ImageRemove.Builder removeImage(String imageId) {
            return commandBuilder(new ImageRemove.Builder(this))
                    .image(imageId);
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
