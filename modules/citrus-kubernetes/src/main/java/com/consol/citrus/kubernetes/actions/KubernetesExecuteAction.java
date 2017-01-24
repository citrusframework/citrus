/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.kubernetes.actions;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.command.CommandResult;
import com.consol.citrus.kubernetes.command.KubernetesCommand;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.validation.json.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Executes kubernetes command with given kubernetes client implementation. Possible command result is stored within command object.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesExecuteAction extends AbstractTestAction {

    @Autowired(required = false)
    @Qualifier("k8sClient")
    /** Kubernetes client instance  */
    private KubernetesClient kubernetesClient = new KubernetesClient();

    /** Kubernetes command to execute */
    private KubernetesCommand command;

    /** Control command result for validation */
    private String commandResult;

    /** Control path expressions in command result */
    private Map<String, Object> commandResultExpressions = new HashMap<>();

    @Autowired
    private JsonTextMessageValidator jsonTextMessageValidator = new JsonTextMessageValidator();

    @Autowired
    private JsonPathMessageValidator jsonPathMessageValidator = new JsonPathMessageValidator();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(KubernetesExecuteAction.class);

    /**
     * Default constructor.
     */
    public KubernetesExecuteAction() {
        setName("kubernetes-execute");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Executing Kubernetes command '%s'", command.getName()));
            }
            command.execute(kubernetesClient, context);

            validateCommandResult(command, context);

            log.info(String.format("Kubernetes command execution successful: '%s'", command.getName()));
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
    private void validateCommandResult(KubernetesCommand command, TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Kubernetes command result validation");
        }

        CommandResult<?> result = command.getCommandResult();
        if (StringUtils.hasText(commandResult) || !CollectionUtils.isEmpty(commandResultExpressions)) {
            if (result == null) {
                throw new ValidationException("Missing Kubernetes command result");
            }

            try {
                String commandResultJson = kubernetesClient.getEndpointConfiguration()
                        .getObjectMapper().writeValueAsString(result);
                if (StringUtils.hasText(commandResult)) {
                    jsonTextMessageValidator.validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(commandResult), context, new JsonMessageValidationContext());
                    log.info("Kubernetes command result validation successful - all values OK!");
                }

                if (!CollectionUtils.isEmpty(commandResultExpressions)) {
                    JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
                    validationContext.setJsonPathExpressions(commandResultExpressions);
                    jsonPathMessageValidator.validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(commandResult), context, validationContext);
                    log.info("Kubernetes command result path validation successful - all values OK!");
                }
            } catch (JsonProcessingException e) {
                throw new CitrusRuntimeException(e);
            }
        }

        if (command.getResultCallback() != null && result != null) {
            command.getResultCallback().validateCommandResult(result, context);
        }
    }

    /**
     * Gets the kubernetes command to execute.
     * @return
     */
    public KubernetesCommand getCommand() {
        return command;
    }

    /**
     * Sets kubernetes command to execute.
     * @param command
     * @return
     */
    public KubernetesExecuteAction setCommand(KubernetesCommand command) {
        this.command = command;
        return this;
    }

    /**
     * Gets the kubernetes client.
     * @return
     */
    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    /**
     * Sets the kubernetes client.
     * @param kubernetesClient
     */
    public KubernetesExecuteAction setKubernetesClient(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        return this;
    }

    /**
     * Gets the expected control command result data.
     * @return
     */
    public String getCommandResult() {
        return commandResult;
    }

    /**
     * Sets the expected control command result data.
     * @param controlCommandResult
     */
    public KubernetesExecuteAction setCommandResult(String controlCommandResult) {
        this.commandResult = controlCommandResult;
        return this;
    }

    /**
     * Gets the expected control command result expressions such as JsonPath expressions.
     * @return
     */
    public Map<String, Object> getCommandResultExpressions() {
        return commandResultExpressions;
    }

    /**
     * Sets the expected command result expressions for path validation.
     * @param commandResultExpressions
     */
    public void setCommandResultExpressions(Map<String, Object> commandResultExpressions) {
        this.commandResultExpressions = commandResultExpressions;
    }
}
