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
     * @return
     */
    void antrun(TestActionConfigurer<AntRunActionDefinition> configurer);

    /**
     * Creates and executes a new echo action.
     * @param message
     * @return
     */
    void echo(String message);

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    void plsql(TestActionConfigurer<ExecutePLSQLActionDefinition> configurer);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    void sql(TestActionConfigurer<ExecuteSQLActionDefinition> configurer);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    void query(TestActionConfigurer<ExecuteSQLQueryActionDefinition> configurer);

    /**
     * Creates a new fail action.
     *
     * @param message
     * @return
     */
    void fail(String message);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    void receiveTimeout(TestActionConfigurer<ReceiveTimeoutActionDefinition> configurer);

    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     * @return
     */
    void load(String filePath);

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    void purgeQueues(TestActionConfigurer<PurgeJMSQueuesActionDefinition> configurer);

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    void purgeChannels(TestActionConfigurer<PurgeMessageChannelActionDefinition> configurer);

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param configurer
     * @return
     */
    void receive(TestActionConfigurer<ReceiveMessageActionDefinition> configurer);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param configurer
     * @return
     */
    void send(TestActionConfigurer<SendMessageActionDefinition> configurer);

    /**
     * Apply test apply with all test actions, finally actions and test
     * variables defined in given apply.
     *
     * @param behavior
     */
    void applyBehavior(TestBehavior behavior);

}
