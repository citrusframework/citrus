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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.server.Server;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public interface TestRunner extends ApplicationContextAware {

    /**
     * Set custom test case name.
     * @param name
     */
    void name(String name);

    /**
     * Sets custom package name for this test case.
     * @param packageName
     */
    void packageName(String packageName);

    /**
     * Starts the test case execution.
     */
    void start();

    /**
     * Stops test case execution.
     */
    void stop();

    /**
     * Adds a new variable definition to the set of test variables
     * for this test case and return its value.
     *
     * @param name
     * @param value
     */
    <T> T variable(String name, T value);

    /**
     * Runs a custom test action on current test case.
     * @param testAction
     */
    void run(TestAction testAction);

    /**
     * Creates and executes a new ANT run action definition
     * for further configuration.
     * @param configurer
     */
    void antrun(TestActionConfigurer<AntRunActionDefinition> configurer);

    /**
     * Creates and executes a new echo action.
     * @param message
     */
    void echo(String message);

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param configurer
     */
    void plsql(TestActionConfigurer<ExecutePLSQLActionDefinition> configurer);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param configurer
     */
    void sql(TestActionConfigurer<ExecuteSQLActionDefinition> configurer);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param configurer
     */
    void query(TestActionConfigurer<ExecuteSQLQueryActionDefinition> configurer);

    /**
     * Creates a new fail action.
     *
     * @param message
     */
    void fail(String message);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param configurer
     */
    void receiveTimeout(TestActionConfigurer<ReceiveTimeoutActionDefinition> configurer);

    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     */
    void load(String filePath);

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @param configurer
     */
    void purgeQueues(TestActionConfigurer<PurgeJmsQueueActionDefinition> configurer);

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @param configurer
     */
    void purgeChannels(TestActionConfigurer<PurgeMessageChannelActionDefinition> configurer);

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param configurer
     */
    void receive(TestActionConfigurer<ReceiveMessageActionDefinition> configurer);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param configurer
     */
    void send(TestActionConfigurer<SendMessageActionDefinition> configurer);

    /**
     * Add sleep action with default delay time.
     */
    void sleep();

    /**
     * Add sleep action with time in milliseconds.
     *
     * @param milliseconds
     */
    void sleep(long milliseconds);

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param servers
     */
    void start(Server... servers);

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param server
     */
    void start(Server server);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param servers
     */
    void stop(Server... servers);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param server
     */
    void stop(Server server);

    /**
     * Creates a new stop time action.
     */
    void stopTime();

    /**
     * Creates a new stop time action.
     *
     * @param id
     */
    void stopTime(String id);

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     */
    void traceVariables();

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @param variables
     */
    void traceVariables(String... variables);

    /**
     * Creates a new groovy action definition
     * for further configuration.
     *
     * @param configurer
     */
    void groovy(TestActionConfigurer<GroovyActionDefinition> configurer);

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @param configurer
     */
    void transform(TestActionConfigurer<TransformActionDefinition> configurer);

    /**
     * Apply test apply with all test actions, finally actions and test
     * variables defined in given apply.
     *
     * @param behavior
     */
    void applyBehavior(TestBehavior behavior);

    /**
     * Add test parameters to the test.
     * @param parameterNames
     * @param parameterValues
     */
    void parameter(String[] parameterNames, Object[] parameterValues);
}
