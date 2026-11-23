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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Service;
import tools.jackson.core.JacksonException;
import org.citrusframework.api.actions.kubernetes.KubernetesServiceDeleteActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.kubernetes.command.CommandResult;
import org.citrusframework.kubernetes.command.DeleteResult;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;

public class DeleteServiceAction extends AbstractKubernetesAction {

    private final String serviceName;

    private final String commandResult;
    private final Map<String, Object> commandResultExpressions;
    private final KubernetesResultValidator resultValidator;

    public static final String DEFAULT_JSON_MESSAGE_VALIDATOR = KubernetesResultValidator.DEFAULT_JSON_MESSAGE_VALIDATOR;
    public static final String DEFAULT_JSON_PATH_MESSAGE_VALIDATOR = KubernetesResultValidator.DEFAULT_JSON_PATH_MESSAGE_VALIDATOR;

    public DeleteServiceAction(Builder builder) {
        super("delete-service", builder);

        this.serviceName = builder.serviceName;
        this.commandResult = builder.commandResult;
        this.commandResultExpressions = builder.commandResultExpressions;
        this.resultValidator = new KubernetesResultValidator(commandResult, commandResultExpressions,
                builder.jsonMessageValidator, builder.jsonPathMessageValidator);
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
        if (resultValidator.hasValidation()) {
            DeleteResult deleteResult = new DeleteResult();
            deleteResult.setKind(Service.class.getSimpleName());
            deleteResult.setSuccess(success);
            CommandResult<DeleteResult> result = new CommandResult<>(deleteResult);

            try {
                String commandResultJson = KubernetesSupport.json().writeValueAsString(result);
                resultValidator.validate(commandResultJson, context);
            } catch (JacksonException e) {
                throw new CitrusRuntimeException(e);
            }
        }
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
