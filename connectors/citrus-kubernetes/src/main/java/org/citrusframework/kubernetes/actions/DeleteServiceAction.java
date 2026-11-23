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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.Service;
import tools.jackson.core.JacksonException;
import org.citrusframework.api.actions.kubernetes.KubernetesServiceDeleteActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.kubernetes.command.CommandResult;
import org.citrusframework.kubernetes.command.DeleteResult;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;

public class DeleteServiceAction extends AbstractKubernetesAction {

    private final String serviceName;

    private final String commandResult;
    private final Map<String, Object> commandResultExpressions;
    private final MessageValidator<? extends ValidationContext> jsonMessageValidator;
    private final MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

    public static final String DEFAULT_JSON_MESSAGE_VALIDATOR = "defaultJsonMessageValidator";
    public static final String DEFAULT_JSON_PATH_MESSAGE_VALIDATOR = "defaultJsonPathMessageValidator";

    public DeleteServiceAction(Builder builder) {
        super("delete-service", builder);

        this.serviceName = builder.serviceName;
        this.commandResult = builder.commandResult;
        this.commandResultExpressions = builder.commandResultExpressions;
        this.jsonMessageValidator = builder.jsonMessageValidator;
        this.jsonPathMessageValidator = builder.jsonPathMessageValidator;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCommandResult() {
        return commandResult;
    }

    public Map<String, Object> getCommandResultExpressions() {
        return commandResultExpressions;
    }

    @Override
    public void doExecute(TestContext context) {
        boolean success;
        if (serviceName != null) {
            List<?> results = getKubernetesClient().services()
                    .inNamespace(namespace(context))
                    .withName(context.replaceDynamicContentInString(serviceName))
                    .delete();
            success = !results.isEmpty();
        } else {
            // delete all services in current namespace
            List<Service> services = getKubernetesClient().services()
                    .list()
                    .getItems();

            List<?> results = getKubernetesClient().resourceList(services)
                    .inNamespace(namespace(context))
                    .delete();
            success = !results.isEmpty();
        }

        validateCommandResult(success, context);
    }

    private void validateCommandResult(boolean success, TestContext context) {
        if (StringUtils.hasText(commandResult) || !commandResultExpressions.isEmpty()) {
            DeleteResult deleteResult = new DeleteResult();
            deleteResult.setSuccess(success);
            CommandResult<DeleteResult> result = new CommandResult<>(deleteResult);

            try {
                String commandResultJson = KubernetesSupport.json().writeValueAsString(result);
                if (StringUtils.hasText(commandResult)) {
                    getMessageValidator(context).validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(commandResult), context, Collections.singletonList(new JsonMessageValidationContext()));
                }

                if (!commandResultExpressions.isEmpty()) {
                    JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                            .expressions(commandResultExpressions)
                            .build();
                    getPathValidator(context).validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(commandResult), context, Collections.singletonList(validationContext));
                }
            } catch (JacksonException e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private MessageValidator<? extends ValidationContext> getMessageValidator(TestContext context) {
        if (jsonMessageValidator != null) {
            return jsonMessageValidator;
        }

        Optional<MessageValidator<? extends ValidationContext>> defaultJsonMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_JSON_MESSAGE_VALIDATOR);

        if (defaultJsonMessageValidator.isEmpty()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_MESSAGE_VALIDATOR)) {
            defaultJsonMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (defaultJsonMessageValidator.isEmpty()) {
            defaultJsonMessageValidator = MessageValidator.lookup("json");
        }

        if (defaultJsonMessageValidator.isPresent()) {
            return defaultJsonMessageValidator.get();
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON message validator - please add validator to project");
    }

    @SuppressWarnings("unchecked")
    private MessageValidator<? extends ValidationContext> getPathValidator(TestContext context) {
        if (jsonPathMessageValidator != null) {
            return jsonPathMessageValidator;
        }

        Optional<MessageValidator<? extends ValidationContext>> defaultJsonMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR);

        if (defaultJsonMessageValidator.isEmpty()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR)) {
            defaultJsonMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (defaultJsonMessageValidator.isEmpty()) {
            defaultJsonMessageValidator = MessageValidator.lookup("json-path");
        }

        if (defaultJsonMessageValidator.isPresent()) {
            return defaultJsonMessageValidator.get();
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON path message validator - please add validator to project");
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<DeleteServiceAction, Builder>
            implements KubernetesServiceDeleteActionBuilder<DeleteServiceAction, Builder> {

        private String serviceName;
        private String commandResult;
        private final Map<String, Object> commandResultExpressions = new HashMap<>();
        private MessageValidator<? extends ValidationContext> jsonMessageValidator;
        private MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

        @Override
        public Builder service(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override
        public Builder result(String result) {
            this.commandResult = result;
            return this;
        }

        @Override
        public Builder validate(String path, Object value) {
            this.commandResultExpressions.put(path, value);
            return this;
        }

        @Override
        public Builder validator(MessageValidator<? extends ValidationContext> validator) {
            this.jsonMessageValidator = validator;
            return this;
        }

        @Override
        public Builder pathExpressionValidator(MessageValidator<? extends ValidationContext> validator) {
            this.jsonPathMessageValidator = validator;
            return this;
        }

        @Override
        public DeleteServiceAction doBuild() {
            return new DeleteServiceAction(this);
        }
    }
}
