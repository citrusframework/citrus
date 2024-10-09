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

package org.citrusframework.kubernetes.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;

/**
 * @since 2.7
 */
public abstract class AbstractKubernetesCommand<T extends HasMetadata, O, C extends KubernetesCommand<T, O>> implements KubernetesCommand<T, O> {

    /** Self reference for generics support */
    private final C self;

    /** Command name */
    private final String name;

    /** Command parameters */
    private Map<String, Object> parameters = new HashMap<>();

    /** Command result if any */
    private CommandResult<O> commandResult;

    /** Optional command result validation */
    private CommandResultCallback<O> resultCallback;

    /**
     * Default constructor initializing the command name.
     * @param name
     */
    public AbstractKubernetesCommand(String name) {
        this.name = name;
        this.self = (C) this;
    }

    /**
     * Checks existence of command parameter.
     * @param parameterName
     * @return
     */
    protected boolean hasParameter(String parameterName) {
        return getParameters().containsKey(parameterName);
    }

    /**
     * Gets the kubernetes command parameter.
     * @return
     */
    protected String getParameter(String parameterName, TestContext context) {
        if (getParameters().containsKey(parameterName)) {
            return context.replaceDynamicContentInString(getParameters().get(parameterName).toString());
        } else {
            throw new CitrusRuntimeException(String.format("Missing kubernetes command parameter '%s'", parameterName));
        }
    }

    @Override
    public CommandResult<O> getCommandResult() {
        return commandResult;
    }

    /**
     * Sets the command result if any.
     * @param commandResult
     */
    protected void setCommandResult(CommandResult<O> commandResult) {
        this.commandResult = commandResult;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Sets the command parameters.
     * @param parameters
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Adds command parameter to current command.
     * @param name
     * @param value
     * @return
     */
    public C withParam(String name, String value) {
        parameters.put(name, value);
        return self;
    }

    @Override
    public C validate(CommandResultCallback<O> callback) {
        this.resultCallback = callback;
        return self;
    }

    @Override
    public CommandResultCallback<O> getResultCallback() {
        return resultCallback;
    }

    @Override
    public C label(String key, String value) {
        if (!hasParameter(KubernetesMessageHeaders.LABEL)) {
            withParam(KubernetesMessageHeaders.LABEL, key + "=" + value);
        } else {
            withParam(KubernetesMessageHeaders.LABEL, getParameters().get(KubernetesMessageHeaders.LABEL) + "," + key + "=" + value);
        }
        return self;
    }

    @Override
    public C label(String key) {
        if (!hasParameter(KubernetesMessageHeaders.LABEL)) {
            withParam(KubernetesMessageHeaders.LABEL, key);
        } else {
            withParam(KubernetesMessageHeaders.LABEL, getParameters().get(KubernetesMessageHeaders.LABEL) + "," + key);
        }
        return self;
    }

    @Override
    public C namespace(String key) {
        withParam(KubernetesMessageHeaders.NAMESPACE, key);
        return self;
    }

    @Override
    public C name(String key) {
        withParam(KubernetesMessageHeaders.NAME, key);
        return self;
    }

    @Override
    public C withoutLabel(String key, String value) {
        if (!hasParameter(KubernetesMessageHeaders.LABEL)) {
            withParam(KubernetesMessageHeaders.LABEL, key + "!=" + value);
        } else {
            withParam(KubernetesMessageHeaders.LABEL, getParameters().get(KubernetesMessageHeaders.LABEL) + "," + key + "!=" + value);
        }
        return self;
    }

    @Override
    public C withoutLabel(String key) {
        if (!hasParameter(KubernetesMessageHeaders.LABEL)) {
            withParam(KubernetesMessageHeaders.LABEL, "!" + key);
        } else {
            withParam(KubernetesMessageHeaders.LABEL, getParameters().get(KubernetesMessageHeaders.LABEL) + ",!" + key);
        }
        return self;
    }

    /**
     * Reads labels from expression string.
     * @param labelExpression
     * @param context
     * @return
     */
    protected Map<String, String> getLabels(String labelExpression, TestContext context) {
        Map<String, String> labels = new HashMap<>();

        Set<String> values = Arrays.stream(labelExpression.split(",")).collect(Collectors.toSet());
        for (String item : values) {
            if (item.contains("!=")) {
                continue;
            } else if (item.contains("=")) {
                labels.put(context.replaceDynamicContentInString(item.substring(0, item.indexOf("="))), context.replaceDynamicContentInString(item.substring(item.indexOf("=") + 1)));
            } else if (!item.startsWith("!")) {
                labels.put(context.replaceDynamicContentInString(item), null);
            }
        }

        return labels;
    }

    /**
     * Reads without labels from expression string.
     * @param labelExpression
     * @param context
     * @return
     */
    protected Map<String, String> getWithoutLabels(String labelExpression, TestContext context) {
        Map<String, String> labels = new HashMap<>();

        Set<String> values = Arrays.stream(labelExpression.split(",")).collect(Collectors.toSet());
        for (String item : values) {
            if (item.contains("!=")) {
                labels.put(context.replaceDynamicContentInString(item.substring(0, item.indexOf("!="))), context.replaceDynamicContentInString(item.substring(item.indexOf("!=") + 2)));
            } else if (item.startsWith("!")) {
                labels.put(context.replaceDynamicContentInString(item.substring(1)), null);
            }
        }

        return labels;
    }
}
