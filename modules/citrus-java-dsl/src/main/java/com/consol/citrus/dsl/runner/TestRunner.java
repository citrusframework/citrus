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
import com.consol.citrus.actions.*;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.script.GroovyAction;
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
     * Runs test action and returns same action after execution.
     * @param testAction
     * @return
     */
    <T extends TestAction> T run(T testAction);

    /**
     * Creates and executes a new ANT run action definition
     * for further configuration.
     * @param configurer
     */
    AntRunAction antrun(TestActionConfigurer<AntRunActionDefinition> configurer);

    /**
     * Creates and executes a new echo action.
     * @param message
     */
    EchoAction echo(String message);

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param configurer
     */
    ExecutePLSQLAction plsql(TestActionConfigurer<ExecutePLSQLActionDefinition> configurer);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param configurer
     */
    ExecuteSQLAction sql(TestActionConfigurer<ExecuteSQLActionDefinition> configurer);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param configurer
     */
    ExecuteSQLQueryAction query(TestActionConfigurer<ExecuteSQLQueryActionDefinition> configurer);

    /**
     * Creates a new fail action.
     *
     * @param message
     */
    FailAction fail(String message);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param configurer
     */
    ReceiveTimeoutAction receiveTimeout(TestActionConfigurer<ReceiveTimeoutActionDefinition> configurer);

    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     */
    LoadPropertiesAction load(String filePath);

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @param configurer
     */
    PurgeJmsQueuesAction purgeQueues(TestActionConfigurer<PurgeJmsQueueActionDefinition> configurer);

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @param configurer
     */
    PurgeMessageChannelAction purgeChannels(TestActionConfigurer<PurgeMessageChannelActionDefinition> configurer);

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param configurer
     */
    ReceiveMessageAction receive(TestActionConfigurer<ReceiveMessageActionDefinition> configurer);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param configurer
     */
    SendMessageAction send(TestActionConfigurer<SendMessageActionDefinition> configurer);

    /**
     * Add sleep action with default delay time.
     */
    SleepAction sleep();

    /**
     * Add sleep action with time in milliseconds.
     *
     * @param milliseconds
     */
    SleepAction sleep(long milliseconds);

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param servers
     */
    StartServerAction start(Server... servers);

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param server
     */
    StartServerAction start(Server server);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param servers
     */
    StopServerAction stop(Server... servers);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param server
     */
    StopServerAction stop(Server server);

    /**
     * Creates a new stop time action.
     */
    StopTimeAction stopTime();

    /**
     * Creates a new stop time action.
     *
     * @param id
     */
    StopTimeAction stopTime(String id);

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     */
    TraceVariablesAction traceVariables();

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @param variables
     */
    TraceVariablesAction traceVariables(String... variables);

    /**
     * Creates a new groovy action definition
     * for further configuration.
     *
     * @param configurer
     */
    GroovyAction groovy(TestActionConfigurer<GroovyActionDefinition> configurer);

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @param configurer
     */
    TransformAction transform(TestActionConfigurer<TransformActionDefinition> configurer);

    /**
     * Assert exception to happen in nested test action.
     *
     * @param configurer
     * @return
     */
    ContainerRunner assertException(TestActionConfigurer<AssertDefinition> configurer);

    /**
     * Catch exception when thrown in nested test action.
     *
     * @param configurer
     * @return
     */
    ContainerRunner catchException(TestActionConfigurer<CatchDefinition> configurer);

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
