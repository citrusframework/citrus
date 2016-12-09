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
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.command.KubernetesCommand;
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
 * Executes kubernetes command with given kubernetes client implementation. Possible command result is stored within command object.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesExecuteAction extends AbstractTestAction {

    @Autowired(required = false)
    @Qualifier("kubernetesClient")
    /** Kubernetes client instance  */
    private KubernetesClient kubernetesClient = new KubernetesClient();

    /** Kubernetes command to execute */
    private KubernetesCommand command;

    /** Expected command result for validation */
    private String expectedCommandResult;

    @Autowired(required = false)
    @Qualifier("kubernetesCommandResultMapper")
    /** JSON data binding */
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private JsonTextMessageValidator jsonTextMessageValidator = new JsonTextMessageValidator();

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

        if (StringUtils.hasText(expectedCommandResult)) {
            if (command.getCommandResult() == null) {
                throw new ValidationException("Missing Kubernetes command result");
            }

            try {
                String commandResultJson = jsonMapper.writeValueAsString(command.getCommandResult());
                JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
                jsonTextMessageValidator.validateMessage(new DefaultMessage(commandResultJson), new DefaultMessage(expectedCommandResult), context, validationContext);
                log.info("Kubernetes command result validation successful - all values OK!");
            } catch (JsonProcessingException e) {
                throw new CitrusRuntimeException(e);
            }
        }

        if (command.getResultCallback() != null) {
            command.getResultCallback().doWithCommandResult(command.getCommandResult(), context);
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
    public KubernetesExecuteAction setExpectedCommandResult(String expectedCommandResult) {
        this.expectedCommandResult = expectedCommandResult;
        return this;
    }

    /**
     * Sets the JSON object mapper.
     * @param jsonMapper
     */
    public KubernetesExecuteAction setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }
}
