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

package org.citrusframework.kubernetes.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.events.v1.Event;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.command.*;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes kubernetes command with given kubernetes client implementation. Possible command result is stored within command object.
 *
 * @since 2.7
 */
public class KubernetesExecuteAction extends AbstractTestAction {

    /** Kubernetes client instance  */
    private final KubernetesClient kubernetesClient;

    /** Kubernetes command to execute */
    private final KubernetesCommand<?, ?> command;

    /** Control command result for validation */
    private final String commandResult;

    /** Control path expressions in command result */
    private final Map<String, Object> commandResultExpressions;

    /** Validator used to validate expected json results */
    private final MessageValidator<? extends ValidationContext> jsonMessageValidator;
    private final MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

    public static final String DEFAULT_JSON_MESSAGE_VALIDATOR = "defaultJsonMessageValidator";
    public static final String DEFAULT_JSON_PATH_MESSAGE_VALIDATOR = "defaultJsonPathMessageValidator";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(KubernetesExecuteAction.class);

    /**
     * Default constructor.
     */
    public KubernetesExecuteAction(Builder builder) {
        super("kubernetes-execute", builder);

        this.kubernetesClient = builder.kubernetesClient;
        this.command = builder.command;
        this.commandResult = builder.commandResult;
        this.commandResultExpressions = builder.commandResultExpressions;
        this.jsonMessageValidator = builder.jsonMessageValidator;
        this.jsonPathMessageValidator = builder.jsonPathMessageValidator;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            logger.debug("Executing Kubernetes command '{}'", command.getName());
            command.execute(kubernetesClient, context);

            validateCommandResult(command, context);

            logger.debug("Kubernetes command execution successful: '{}'", command.getName());
        } catch (CitrusRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to perform kubernetes command", e);
        }
    }

    /**
     * Validate command results.
     * @param command
     * @param context
     */
    private void validateCommandResult(KubernetesCommand<?, ?> command, TestContext context) {
        logger.debug("Starting Kubernetes command result validation");

        CommandResult<?> result = command.getCommandResult();
        if (StringUtils.hasText(commandResult) || !commandResultExpressions.isEmpty()) {
            if (result == null) {
                throw new ValidationException("Missing Kubernetes command result");
            }

            try {
                String commandResultJson = kubernetesClient.getEndpointConfiguration()
                        .getObjectMapper().writeValueAsString(result);
                if (StringUtils.hasText(commandResult)) {
                    getMessageValidator(context).validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(commandResult), context, Collections.singletonList(new JsonMessageValidationContext()));
                    logger.debug("Kubernetes command result validation successful - all values OK!");
                }

                if (!commandResultExpressions.isEmpty()) {
                    JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                            .expressions(commandResultExpressions)
                            .build();
                    getPathValidator(context).validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(commandResult), context, Collections.singletonList(validationContext));
                    logger.debug("Kubernetes command result path validation successful - all values OK!");
                }
            } catch (JsonProcessingException e) {
                throw new CitrusRuntimeException(e);
            }
        }

        if (command.getResultCallback() != null && result != null) {
            command.validateCommandResult(context);
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

        if (defaultJsonMessageValidator.isEmpty()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_MESSAGE_VALIDATOR)) {
            defaultJsonMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (defaultJsonMessageValidator.isEmpty()) {
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

        if (defaultJsonMessageValidator.isEmpty()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR)) {
            defaultJsonMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (defaultJsonMessageValidator.isEmpty()) {
            // try to find json message validator via resource path lookup
            defaultJsonMessageValidator = MessageValidator.lookup("json-path");
        }

        if (defaultJsonMessageValidator.isPresent()) {
            return defaultJsonMessageValidator.get();
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON path message validator - please add validator to project");
    }

    /**
     * Gets the kubernetes command to execute.
     * @return
     */
    public KubernetesCommand getCommand() {
        return command;
    }

    /**
     * Gets the kubernetes client.
     * @return
     */
    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    /**
     * Gets the expected control command result data.
     * @return
     */
    public String getCommandResult() {
        return commandResult;
    }

    /**
     * Gets the expected control command result expressions such as JsonPath expressions.
     * @return
     */
    public Map<String, Object> getCommandResultExpressions() {
        return commandResultExpressions;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<KubernetesExecuteAction, Builder> implements ReferenceResolverAware {

        private KubernetesClient kubernetesClient;
        private KubernetesCommand<?, ?> command;
        private String commandResult;
        private final Map<String, Object> commandResultExpressions = new HashMap<>();
        private MessageValidator<? extends ValidationContext> jsonMessageValidator;
        private MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

        private ReferenceResolver referenceResolver;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder kubernetes() {
            return new Builder();
        }

        /**
         * Use a custom kubernetes client.
         */
        public Builder client(KubernetesClient kubernetesClient) {
            this.kubernetesClient = kubernetesClient;
            return this;
        }

        /**
         * Use a kubernetes command.
         */
        public Builder command(KubernetesCommand<?, ?> command) {
            this.command = command;
            return this;
        }

        /**
         * Adds expected command result.
         * @param result
         * @return
         */
        public Builder result(String result) {
            this.commandResult = result;
            return this;
        }

        /**
         * Adds JsonPath command result validation.
         * @param path
         * @param value
         * @return
         */
        public Builder validate(String path, Object value) {
            this.commandResultExpressions.put(path, value);
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

        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        /**
         * Use a info command.
         */
        public BaseActionBuilder<InfoResult, InfoResult, ?> info() {
            return new BaseActionBuilder<>(new Info());
        }

        /**
         * Pods action builder.
         */
        public PodsActionBuilder pods() {
            return new PodsActionBuilder();
        }

        /**
         * Services action builder.
         */
        public ServicesActionBuilder services() {
            return new ServicesActionBuilder();
        }

        /**
         * ReplicationControllers action builder.
         */
        public ReplicationControllersActionBuilder replicationControllers() {
            return new ReplicationControllersActionBuilder();
        }

        /**
         * Endpoints action builder.
         */
        public EndpointsActionBuilder endpoints() {
            return new EndpointsActionBuilder();
        }

        /**
         * Nodes action builder.
         */
        public NodesActionBuilder nodes() {
            return new NodesActionBuilder();
        }

        /**
         * Events action builder.
         */
        public EventsActionBuilder events() {
            return new EventsActionBuilder();
        }

        /**
         * Namespaces action builder.
         */
        public NamespacesActionBuilder namespaces() {
            return new NamespacesActionBuilder();
        }

        /**
         * Base kubernetes action builder with namespace.
         */
        public class NamespacedActionBuilder<T extends HasMetadata, O> extends BaseActionBuilder<T, O, NamespacedActionBuilder<T, O>> {
            /**
             * Constructor using command.
             * @param command
             */
            NamespacedActionBuilder(KubernetesCommand<T, O> command) {
                super(command);
            }

            /**
             * Sets the namespace parameter.
             * @param key
             * @return
             */
            public NamespacedActionBuilder<T, O> namespace(String key) {
                command.namespace(key);
                return this;
            }
        }

        /**
         * Base kubernetes action builder with name option.
         */
        public class NamedActionBuilder<T extends HasMetadata, O> extends BaseActionBuilder<T, O, NamedActionBuilder<T, O>> {
            /**
             * Constructor using command.
             * @param command
             */
            NamedActionBuilder(KubernetesCommand<T, O> command) {
                super(command);
            }

            /**
             * Sets the name parameter.
             * @param key
             * @return
             */
            public NamedActionBuilder<T, O> name(String key) {
                command.name(key);
                return this;
            }

            /**
             * Sets the namespace parameter.
             * @param key
             * @return
             */
            public NamedActionBuilder<T, O> namespace(String key) {
                command.namespace(key);
                return this;
            }
        }

        /**
         * Base kubernetes action builder.
         */
        public class BaseActionBuilder<T extends HasMetadata, O, B extends BaseActionBuilder<T, O, B>> extends AbstractTestActionBuilder<KubernetesExecuteAction, B> {

            /** Kubernetes command */
            protected final KubernetesCommand<T, O> command;

            /**
             * Constructor using command.
             * @param command
             */
            BaseActionBuilder(KubernetesCommand<T, O> command) {
                this.command = command;
                command(command);
            }

            /**
             * Adds expected command result.
             * @param result
             * @return
             */
            public B result(String result) {
                commandResult = result;
                return self;
            }

            /**
             * Adds JsonPath command result validation.
             * @param path
             * @param value
             * @return
             */
            public B validate(String path, Object value) {
                commandResultExpressions.put(path, value);
                return self;
            }

            /**
             * Adds command result callback.
             * @param callback
             * @return
             */
            public B validate(CommandResultCallback<O> callback) {
                command.validate(callback);
                return self;
            }

            /**
             * Sets the label parameter.
             * @param key
             * @param value
             * @return
             */
            public B label(String key, String value) {
                command.label(key, value);
                return self;
            }

            /**
             * Sets the label parameter.
             * @param key
             * @return
             */
            public B label(String key) {
                command.label(key);
                return self;
            }

            /**
             * Sets the without label parameter.
             * @param key
             * @param value
             * @return
             */
            public B withoutLabel(String key, String value) {
                command.withoutLabel(key, value);
                return self;
            }

            /**
             * Sets the without label parameter.
             * @param key
             * @return
             */
            public B withoutLabel(String key) {
                command.withoutLabel(key);
                return self;
            }

            /**
             * Sets command.
             * @param command
             * @return
             */
            protected B command(KubernetesCommand<T, O> command) {
                Builder.this.command(command);
                return self;
            }

            @Override
            public KubernetesExecuteAction build() {
                return Builder.this.build();
            }
        }

        /**
         * Pods action builder.
         */
        public class PodsActionBuilder {
            /**
             * List pods.
             */
            public NamespacedActionBuilder<Pod, ListResult<Pod>> list() {
                ListPods command = new ListPods();
                return new NamespacedActionBuilder<>(command);
            }

            /**
             * Creates new pod.
             * @param pod
             */
            public NamedActionBuilder<Pod, Pod> create(Pod pod) {
                CreatePod command = new CreatePod();
                command.setPod(pod);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Create new pod from template.
             * @param template
             */
            public NamedActionBuilder<Pod, Pod> create(Resource template) {
                CreatePod command = new CreatePod();
                command.setTemplateResource(template);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Create new pod from template path.
             * @param templatePath
             */
            public NamedActionBuilder<Pod, Pod> create(String templatePath) {
                CreatePod command = new CreatePod();
                command.setTemplate(templatePath);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Gets pod by name.
             * @param name
             */
            public NamedActionBuilder<Pod, Pod> get(String name) {
                GetPod command = new GetPod();
                command.name(name);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Deletes pod by name.
             * @param name
             */
            public NamedActionBuilder<Pod, DeleteResult> delete(String name) {
                DeletePod command = new DeletePod();
                command.name(name);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Deletes pods.
             */
            public NamedActionBuilder<Pod, DeleteResult> delete() {
                DeletePod command = new DeletePod();
                return new NamedActionBuilder<>(command);
            }

            /**
             * Watch pods.
             */
            public NamedActionBuilder<Pod, Pod> watch() {
                return new NamedActionBuilder<>(new WatchPods());
            }
        }

        /**
         * Services action builder.
         */
        public class ServicesActionBuilder {
            /**
             * List services.
             */
            public NamespacedActionBuilder<Service, ListResult<Service>> list() {
                return new NamespacedActionBuilder<>(new ListServices());
            }

            /**
             * Creates new service.
             * @param pod
             */
            public NamedActionBuilder<Service, Service> create(Service pod) {
                CreateService command = new CreateService();
                command.setService(pod);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Create new service from template.
             * @param template
             */
            public NamedActionBuilder<Service, Service> create(Resource template) {
                CreateService command = new CreateService();
                command.setTemplateResource(template);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Create new service from template path.
             * @param templatePath
             */
            public NamedActionBuilder<Service, Service> create(String templatePath) {
                CreateService command = new CreateService();
                command.setTemplate(templatePath);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Gets service by name.
             * @param name
             */
            public NamedActionBuilder<Service, Service> get(String name) {
                GetService command = new GetService();
                command.name(name);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Deletes service by name.
             * @param name
             */
            public NamedActionBuilder<Service, DeleteResult> delete(String name) {
                DeleteService command = new DeleteService();
                command.name(name);
                return new NamedActionBuilder<>(command);
            }

            /**
             * Deletes services.
             */
            public NamedActionBuilder<Service, DeleteResult> delete() {
                DeleteService command = new DeleteService();
                return new NamedActionBuilder<>(command);
            }

            /**
             * Watch services.
             */
            public NamedActionBuilder<Service, Service> watch() {
                return new NamedActionBuilder<>(new WatchServices());
            }
        }

        /**
         * Endpoints action builder.
         */
        public class EndpointsActionBuilder {
            /**
             * List endpoints.
             */
            public NamespacedActionBuilder<Endpoints, ListResult<Endpoints>> list() {
                return new NamespacedActionBuilder<>(new ListEndpoints());
            }
        }

        /**
         * Nodes action builder.
         */
        public class NodesActionBuilder {
            /**
             * List nodes.
             */
            public BaseActionBuilder<Node, ListResult<Node>, ?> list() {
                return new BaseActionBuilder<>(new ListNodes());
            }

            /**
             * Watch nodes.
             */
            public BaseActionBuilder<Node, Node, ?> watch() {
                return new BaseActionBuilder<>(new WatchNodes());
            }
        }

        /**
         * Namespaces action builder.
         */
        public class NamespacesActionBuilder {
            /**
             * List namespaces.
             */
            public BaseActionBuilder<Namespace, ListResult<Namespace>, ?> list() {
                return new BaseActionBuilder<>(new ListNamespaces());
            }

            /**
             * Watch namespaces.
             */
            public BaseActionBuilder<Namespace, Namespace, ?> watch() {
                return new BaseActionBuilder<>(new WatchNamespaces());
            }
        }

        /**
         * Events action builder.
         */
        public class EventsActionBuilder {
            /**
             * List endpoints.
             */
            public NamespacedActionBuilder<Event, ListResult<Event>> list() {
                return new NamespacedActionBuilder<>(new ListEvents());
            }
        }

        /**
         * ReplicationControllers action builder.
         */
        public class ReplicationControllersActionBuilder {
            /**
             * List replication controllers.
             */
            public NamespacedActionBuilder<ReplicationController, ListResult<ReplicationController>> list() {
                return new NamespacedActionBuilder<>(new ListReplicationControllers());
            }

            /**
             * Watch pods.
             */
            public NamespacedActionBuilder<ReplicationController, ReplicationController> watch() {
                return new NamespacedActionBuilder<>(new WatchReplicationControllers());
            }
        }

        @Override
        public KubernetesExecuteAction build() {
            if (kubernetesClient == null) {
                if (referenceResolver != null && referenceResolver.isResolvable(KubernetesClient.class)) {
                    kubernetesClient = referenceResolver.resolve(KubernetesClient.class);
                } else {
                    kubernetesClient = new KubernetesClient();
                }
            }

            return new KubernetesExecuteAction(this);
        }
    }
}
