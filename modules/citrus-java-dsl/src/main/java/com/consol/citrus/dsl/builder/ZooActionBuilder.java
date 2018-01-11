/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.dsl.builder;

import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;
import com.consol.citrus.zookeeper.client.ZooClient;
import com.consol.citrus.zookeeper.command.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class ZooActionBuilder extends AbstractTestActionBuilder<ZooExecuteAction> {

    public static final String DEFAULT_MODE = "EPHEMERAL";
    public static final String DEFAULT_ACL = Create.ACL_OPEN;
    public static final int DEFAULT_VERSION = 0;

    private ApplicationContext applicationContext;

    /**
     * Constructor using action field.
     *
     * @param action
     */
    public ZooActionBuilder(ZooExecuteAction action) {
        super(action);
    }

    /**
     * Default constructor.
     */
    public ZooActionBuilder() {
        super(new ZooExecuteAction());
    }

    /**
     * Use a custom zoo client.
     */
    public ZooActionBuilder client(ZooClient zooClient) {
        action.setZookeeperClient(zooClient);
        return this;
    }

    /**
     * Adds a create command.
     */
    public Create create(String path, String data) {
        Create command = new Create();
        command.path(path);
        command.data(data);
        command.mode(DEFAULT_MODE);
        command.acl(DEFAULT_ACL);
        action.setCommand(command);
        return command;
    }

    /**
     * Adds a delete command.
     */
    public Delete delete(String path) {
        Delete command = new Delete();
        command.path(path);
        command.version(DEFAULT_VERSION);
        action.setCommand(command);
        return command;
    }

    /**
     * Adds an exists command.
     */
    public Exists exists(String path) {
        Exists command = new Exists();
        command.path(path);
        action.setCommand(command);
        return command;
    }

    /**
     * Adds an exists command.
     */
    public GetChildren children(String path) {
        GetChildren command = new GetChildren();
        command.path(path);
        action.setCommand(command);
        return command;
    }

    /**
     * Adds a get-data command.
     */
    public GetData get(String path) {
        GetData command = new GetData();
        command.path(path);
        action.setCommand(command);
        return command;
    }

    /**
     * Use an info command.
     */
    public Info info() {
        Info command = new Info();
        action.setCommand(command);
        return command;
    }

    /**
     * Adds a set-data command.
     */
    public SetData set(String path, String data) {
        SetData command = new SetData();
        command.path(path);
        command.data(data);
        command.version(0);
        action.setCommand(command);
        return command;
    }


    /**
     * Adds expected command result.
     *
     * @param result
     * @return
     */
    public ZooActionBuilder result(String result) {
        action.setExpectedCommandResult(result);
        return this;
    }

    /**
     * Adds variable extractor for extracting variable from command response.
     *
     * @param jsonPath the json path to reference the value to be extracted
     * @param variableName the name of the variable to store the extracted value in
     * @return
     */
    public ZooActionBuilder extract(String jsonPath, String variableName) {
        JsonPathVariableExtractor jsonPathVariableExtractor = new JsonPathVariableExtractor();
        Map<String, String> pathVariableMap = new HashMap<>();
        pathVariableMap.put(jsonPath, variableName);
        jsonPathVariableExtractor.setJsonPathExpressions(pathVariableMap);
        action.addVariableExtractors(jsonPathVariableExtractor);
        return this;
    }

    /**
     * Adds variable extractor for extracting variable from command response.
     *
     * @param jsonPath the json path to reference the value to be extracted
     * @param expectedValue the expected value (or variable to retrieve the expected value from)
     * @return
     */
    public ZooActionBuilder validate(String jsonPath, String expectedValue) {
        JsonPathMessageValidationContext validationContext = action.getJsonPathMessageValidationContext();
        if (validationContext == null) {
            validationContext = new JsonPathMessageValidationContext();
            action.setJsonPathMessageValidationContext(validationContext);
        }
        validationContext.getJsonPathExpressions().put(jsonPath, expectedValue);
        return this;
    }

    /**
     * Sets the Spring bean application context.
     * @param ctx
     */
    public ZooActionBuilder withApplicationContext(ApplicationContext ctx) {
        this.applicationContext = ctx;

        if (applicationContext.containsBean("zookeeperClient")) {
            action.setZookeeperClient(applicationContext.getBean("zookeeperClient", ZooClient.class));
        }

        if (applicationContext.containsBean("zookeeperCommandResultMapper")) {
            action.setJsonMapper(applicationContext.getBean("zookeeperCommandResultMapper", ObjectMapper.class));
        }

        return this;
    }

}
