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

package org.citrusframework.zookeeper.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.VariableExtractorAdapter;
import org.citrusframework.zookeeper.client.ZooClient;
import org.citrusframework.zookeeper.command.AbstractZooCommand;
import org.citrusframework.zookeeper.command.CommandResultCallback;
import org.citrusframework.zookeeper.command.Create;
import org.citrusframework.zookeeper.command.Delete;
import org.citrusframework.zookeeper.command.Exists;
import org.citrusframework.zookeeper.command.GetChildren;
import org.citrusframework.zookeeper.command.GetData;
import org.citrusframework.zookeeper.command.Info;
import org.citrusframework.zookeeper.command.SetData;
import org.citrusframework.zookeeper.command.ZooCommand;
import org.citrusframework.zookeeper.command.ZooResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes zookeeper command with given zookeeper client implementation. Possible command result is stored within command object.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class ZooExecuteAction extends AbstractTestAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ZooExecuteAction.class);

    /** Zookeeper client instance  */
    private final ZooClient zookeeperClient;

    /** Zookeeper command to execute*/
    private final ZooCommand<?> command;

    /** Expected command result for validation*/
    private final String expectedCommandResult;

    /** JSON data binding */
    private final ObjectMapper jsonMapper;

    /** Validator used to validate expected json results */
    private final MessageValidator<? extends ValidationContext> jsonMessageValidator;
    private final MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

    public static final String DEFAULT_JSON_MESSAGE_VALIDATOR = "defaultJsonMessageValidator";
    public static final String DEFAULT_JSON_PATH_MESSAGE_VALIDATOR = "defaultJsonPathMessageValidator";

    /** An optional validation contextst containing json path validators to validate the command result */
    private final JsonPathMessageValidationContext jsonPathMessageValidationContext;

    /** List of message processors responsible for working with received message content */
    private final List<MessageProcessor> messageProcessors;

    /**
     * Default constructor.
     */
    public ZooExecuteAction(Builder builder) {
        super("zookeeper-execute", builder);

        this.zookeeperClient = builder.zookeeperClient;
        this.command = builder.command;
        this.expectedCommandResult = builder.expectedCommandResult;
        this.jsonMapper = builder.jsonMapper;
        this.jsonMessageValidator = builder.jsonMessageValidator;
        this.jsonPathMessageValidator = builder.jsonPathMessageValidator;
        this.jsonPathMessageValidationContext = builder.jsonPathMessageValidationContext;
        this.messageProcessors = builder.messageProcessors;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Executing zookeeper command '%s'", command.getName()));
            }
            command.execute(zookeeperClient, context);

            validateCommandResult(command, context);

            logger.info(String.format("Zookeeper command execution successful: '%s'", command.getName()));
        } catch (CitrusRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to perform zookeeper command", e);
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
     * Find proper JSON path message validator. Uses several strategies to lookup default JSON path message validator.
     * @param context
     * @return
     */
    private MessageValidator<? extends ValidationContext> getPathValidator(TestContext context) {
        if (jsonPathMessageValidator != null) {
            return jsonPathMessageValidator;
        }

        // try to find json message validator in registry
        Optional<MessageValidator<? extends ValidationContext>> defaultJsonMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR);

        if (!defaultJsonMessageValidator.isPresent()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR)) {
            defaultJsonMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (!defaultJsonMessageValidator.isPresent()) {
            // try to find json message validator via resource path lookup
            defaultJsonMessageValidator = MessageValidator.lookup("json-path");
        }

        if (defaultJsonMessageValidator.isPresent()) {
            return defaultJsonMessageValidator.get();
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON message validator - please add validator to project");
    }

    /**
     * Validate command results.
     * @param command
     * @param context
     */
    private void validateCommandResult(ZooCommand command, TestContext context) {
        Message commandResult = getCommandResult(command);

        for (MessageProcessor processor : messageProcessors) {
            processor.process(commandResult, context);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Validating Zookeeper response");
        }

        if (StringUtils.hasText(expectedCommandResult)) {
            assertResultExists(commandResult);
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            getMessageValidator(context).validateMessage(commandResult, new DefaultMessage(expectedCommandResult), context, Collections.singletonList(validationContext));
        }

        if (jsonPathMessageValidationContext != null) {
            assertResultExists(commandResult);
            getPathValidator(context).validateMessage(commandResult, null, context, Collections.singletonList(jsonPathMessageValidationContext));
        }

        logger.info("Zookeeper command result validation successful - all values OK!");

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
        private MessageValidator<? extends ValidationContext> jsonMessageValidator;
        private MessageValidator<? extends ValidationContext> jsonPathMessageValidator;
        private JsonPathMessageValidationContext jsonPathMessageValidationContext;
        private List<MessageProcessor> messageProcessors = new ArrayList<>();

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
        public Builder create(String path, String data) {
            Create command = new Create();
            command.path(path);
            command.data(data);
            command.mode(DEFAULT_MODE);
            command.acl(DEFAULT_ACL);
            return command(command);
        }

        /**
         * Sets the mode parameter.
         * @param mode
         * @return
         */
        public Builder mode(String mode) {
            ((Create)command).mode(mode);
            return this;
        }

        /**
         * Sets the acl parameter.
         * @param acl
         * @return
         */
        public Builder acl(String acl) {
            ((Create)command).acl(acl);
            return this;
        }

        /**
         * Adds a delete command.
         */
        public Builder delete(String path) {
            Delete command = new Delete();
            command.path(path);
            command.version(DEFAULT_VERSION);
            return command(command);
        }

        /**
         * Sets the version parameter.
         * @param version
         * @return
         */
        public Builder version(int version) {
            if (command instanceof Delete) {
                ((Delete) command).version(version);
            } else {
                ((SetData) command).version(version);
            }
            return this;
        }

        /**
         * Adds an exists command.
         */
        public Builder exists(String path) {
            Exists command = new Exists();
            command.path(path);
            return command(command);
        }

        /**
         * Adds an exists command.
         */
        public Builder children(String path) {
            GetChildren command = new GetChildren();
            command.path(path);
            return command(command);
        }

        /**
         * Adds a get-data command.
         */
        public Builder get(String path) {
            GetData command = new GetData();
            command.path(path);
            return command(command);
        }

        /**
         * Use an info command.
         */
        public Builder info() {
            Info command = new Info();
            return command(command);
        }

        /**
         * Adds a set-data command.
         */
        public Builder set(String path, String data) {
            SetData command = new SetData();
            command.path(path);
            command.data(data);
            command.version(0);
            return command(command);
        }

        /**
         * Adds command result callback.
         * @param callback
         * @return
         */
        public Builder validateCommandResult(CommandResultCallback<ZooResponse> callback) {
            ((AbstractZooCommand<ZooResponse>) command).validateCommandResult(callback);
            return this;
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

        public Builder validator(MessageValidator<? extends ValidationContext> validator) {
            this.jsonMessageValidator = validator;
            return this;
        }

        public Builder pathExpressionValidator(MessageValidator<? extends ValidationContext> validator) {
            this.jsonPathMessageValidator = validator;
            return this;
        }

        public Builder extract(VariableExtractor variableExtractor) {
            this.messageProcessors.add(variableExtractor);
            return this;
        }

        public Builder extract(VariableExtractorAdapter adapter) {
            this.messageProcessors.add(adapter.asExtractor());
            return this;
        }

        public Builder extract(VariableExtractor.Builder<?, ?> builder) {
            this.messageProcessors.add(builder.build());
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
