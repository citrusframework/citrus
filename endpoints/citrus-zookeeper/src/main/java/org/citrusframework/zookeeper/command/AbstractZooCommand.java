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

package org.citrusframework.zookeeper.command;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @author Martin Maher
 * @since 2.5
 */
public abstract class AbstractZooCommand<R> implements ZooCommand {

    public static final String DATA = "data";
    public static final String PATH = "path";
    public static final String MODE = "mode";
    public static final String ACL = "acl";
    public static final String VERSION = "version";

    public static final String CHILDREN = "children";
    public static final String RESPONSE_CODE = "responseCode";

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
    public AbstractZooCommand(String name) {
        this.name = name;
    }

    /**
     * Construct default success response for commands without return value.
     * @return
     */
    protected ZooResponse success() {
        ZooResponse response = new ZooResponse();
        return response;
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
     * Gets the zookeeper command parameter.
     * @return
     */
    protected String getParameter(String parameterName, TestContext context) {
        if (getParameters().containsKey(parameterName)) {
            return context.replaceDynamicContentInString(getParameters().get(parameterName).toString());
        } else {
            throw new CitrusRuntimeException(String.format("Missing zookeeper command parameter '%s'", parameterName));
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
    public AbstractZooCommand withParam(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Adds validation callback with command result.
     * @param callback
     * @return
     */
    public AbstractZooCommand validateCommandResult(CommandResultCallback<R> callback) {
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
