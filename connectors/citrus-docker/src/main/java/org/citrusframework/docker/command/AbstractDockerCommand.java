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

package org.citrusframework.docker.command;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import com.github.dockerjava.api.model.ResponseItem;
import org.citrusframework.util.ReflectionHelper;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class AbstractDockerCommand<R> implements DockerCommand<R> {

    public static final String IMAGE_ID = "image";
    public static final String CONTAINER_ID = "container";

    /** Command name */
    private final String name;

    /** Command parameters */
    private Map<String, Object> parameters = new HashMap<>();

    /** Command result if any */
    private R commandResult;

    /** Optional command result validation */
    private CommandResultCallback<R> resultCallback;

    /**
     * Default constructor initializing the command name.
     * @param name
     */
    public AbstractDockerCommand(String name) {
        this.name = name;
    }

    /**
     * Construct default success response for commands without return value.
     * @return
     */
    protected ResponseItem success() {
        ResponseItem response = new ResponseItem();

        Field statusField = ReflectionHelper.findField(ResponseItem.class, "status");
        ReflectionHelper.setField(statusField, response, "success");
        return response;
    }

    /**
     * Gets the docker image id
     * @return
     */
    protected String getImageId(TestContext context) {
        return getParameter(IMAGE_ID, context);
    }

    /**
     * Gets the docker container id.
     * @return
     */
    protected String getContainerId(TestContext context) {
        return getParameter(CONTAINER_ID, context);
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
     * Gets the docker command parameter.
     * @return
     */
    protected String getParameter(String parameterName, TestContext context) {
        if (getParameters().containsKey(parameterName)) {
            return context.replaceDynamicContentInString(getParameters().get(parameterName).toString());
        } else {
            throw new CitrusRuntimeException(String.format("Missing docker command parameter '%s'", parameterName));
        }
    }

    @Override
    public R getCommandResult() {
        return commandResult;
    }

    /**
     * Sets the command result if any.
     * @param commandResult
     */
    public void setCommandResult(R commandResult) {
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
    public AbstractDockerCommand<R> withParam(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Adds validation callback with command result.
     * @param callback
     * @return
     */
    public AbstractDockerCommand<R> validateCommandResult(CommandResultCallback<R> callback) {
        this.resultCallback = callback;
        return this;
    }

    /**
     * Gets the result validation callback.
     * @return
     */
    public CommandResultCallback<R> getResultCallback() {
        return resultCallback;
    }
}
