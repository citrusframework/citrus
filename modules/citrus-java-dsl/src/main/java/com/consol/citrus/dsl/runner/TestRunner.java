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

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.Template;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import org.springframework.context.ApplicationContextAware;

import java.util.Date;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public interface TestRunner extends ApplicationContextAware {

    /**
     * Builds the test case.
     * @return
     */
    TestCase getTestCase();

    /**
     * Set test class.
     * @param type
     */
    void testClass(Class<?> type);

    /**
     * Set custom test case name.
     * @param name
     */
    void name(String name);

    /**
     * Adds description to the test case.
     *
     * @param description
     */
    void description(String description);

    /**
     * Adds author to the test case.
     *
     * @param author
     */
    void author(String author);

    /**
     * Sets custom package name for this test case.
     * @param packageName
     */
    void packageName(String packageName);

    /**
     * Sets test case status.
     *
     * @param status
     */
    void status(TestCaseMetaInfo.Status status);

    /**
     * Sets the creation date.
     *
     * @param date
     */
    void creationDate(Date date);

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
     * @return
     */
    <T> T variable(String name, T value);

    /**
     * Runs test action and returns same action after execution.
     * @param testAction
     * @return
     */
    <T extends TestAction> T run(T testAction);

    /**
     * Apply test apply with all test actions, finally actions and test
     * variables defined in given apply.
     *
     * @param behavior
     */
    ApplyTestBehaviorAction applyBehavior(TestBehavior behavior);

    /**
     * Prepare and add a custom container implementation.
     * @param container
     * @return
     */
    <T extends AbstractActionContainer> AbstractTestContainerBuilder<T> container(T container);

    /**
     * Action creating a new test variable during a test.
     *
     * @param variableName
     * @param value
     * @return
     */
    CreateVariablesAction createVariable(String variableName, String value);

    /**
     * Creates and executes a new ANT run action definition
     * for further configuration.
     * @param configurer
     * @return
     */
    AntRunAction antrun(BuilderSupport<AntRunBuilder> configurer);

    /**
     * Creates and executes a new echo action.
     * @param message
     * @return
     */
    EchoAction echo(String message);

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ExecutePLSQLAction plsql(BuilderSupport<ExecutePLSQLBuilder> configurer);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ExecuteSQLAction sql(BuilderSupport<ExecuteSQLBuilder> configurer);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ExecuteSQLQueryAction query(BuilderSupport<ExecuteSQLQueryBuilder> configurer);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ReceiveTimeoutAction receiveTimeout(BuilderSupport<ReceiveTimeoutBuilder> configurer);

    /**
     * Creates a new fail action.
     *
     * @param message
     * @return
     */
    FailAction fail(String message);

    /**
     * Creates a new input action.
     *
     * @param configurer
     * @return
     */
    InputAction input(BuilderSupport<InputActionBuilder> configurer);

    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     * @return
     */
    LoadPropertiesAction load(String filePath);

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    TestAction purgeQueues(BuilderSupport<PurgeJmsQueuesBuilder> configurer);

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    PurgeMessageChannelAction purgeChannels(BuilderSupport<PurgeChannelsBuilder> configurer);

    /**
     * Creates a new purge message endpoint action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    PurgeEndpointAction purgeEndpoints(BuilderSupport<PurgeEndpointsBuilder> configurer);

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param configurer
     * @return
     */
    ReceiveMessageAction receive(BuilderSupport<ReceiveMessageBuilder> configurer);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param configurer
     * @return
     */
    SendMessageAction send(BuilderSupport<SendMessageBuilder> configurer);

    /**
     * Create SOAP fault send message action definition with message endpoint instance. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param configurer
     * @return
     * @deprecated since 2.6 in favor of using {@link TestRunner#soap(BuilderSupport)} )}
     */
    TestAction sendSoapFault(BuilderSupport<SendSoapFaultBuilder> configurer);

    /**
     * Add sleep action with default delay time.
     * @return
     */
    SleepAction sleep();

    /**
     * Add sleep action with time in milliseconds.
     *
     * @param milliseconds
     * @return
     */
    SleepAction sleep(long milliseconds);

    /**
     * Creates a wait action that waits for a condition to be satisfied before continuing.
     *
     * @param configurer
     * @return
     */
    WaitAction waitFor(BuilderSupport<WaitActionBuilder> configurer);

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    StartServerAction start(Server... servers);

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    StartServerAction start(Server server);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    StopServerAction stop(Server... servers);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    StopServerAction stop(Server server);

    /**
     * Creates a new stop time action.
     * @return
     */
    StopTimeAction stopTime();

    /**
     * Creates a new stop time action.
     *
     * @param id
     * @return
     */
    StopTimeAction stopTime(String id);

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @return
     */
    TraceVariablesAction traceVariables();

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @param variables
     * @return
     */
    TraceVariablesAction traceVariables(String... variables);

    /**
     * Creates a new groovy action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    GroovyAction groovy(BuilderSupport<GroovyActionBuilder> configurer);

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    TransformAction transform(BuilderSupport<TransformActionBuilder> configurer);

    /**
     * Assert exception to happen in nested test action.
     * @return
     */
    AssertExceptionBuilder assertException();

    /**
     * Catch exception when thrown in nested test action.
     * @return
     */
    CatchExceptionBuilder catchException();

    /**
     * Assert SOAP fault during action execution.
     * @return
     */
    AssertSoapFaultBuilder assertSoapFault();

    /**
     * Adds conditional container with nested test actions.
     * @return
     */
    ConditionalBuilder conditional();

    /**
     * Run nested test actions in iteration.
     * @return
     */
    IterateBuilder iterate();

    /**
     * Run nested test actions in parallel to each other using multiple threads.
     * @return
     */
    ParallelBuilder parallel();

    /**
     * Adds repeat on error until true container with nested test actions.
     * @return
     */
    RepeatOnErrorBuilder repeatOnError();

    /**
     * Adds repeat until true container with nested test actions.
     * @return
     */
    RepeatBuilder repeat();

    /**
     * Run nested test actions in sequence.
     * @return
     */
    SequenceBuilder sequential();

    /**
     * Repeat nested test actions based on a timer interval.
     * @return
     */
    TimerBuilder timer();

    /**
     * Stops timer matching the supplied timerId
     * @param timerId
     * @return
     */
    StopTimerAction stopTimer(String timerId);

    /**
     * Stops all timers
     * @return
     */
    StopTimerAction stopTimers();

    /**
     * Run docker command action.
     * @return
     */
    TestAction docker(BuilderSupport<DockerActionBuilder> configurer);

    /**
     * Run http command action.
     * @return
     */
    TestAction http(BuilderSupport<HttpActionBuilder> configurer);

    /**
     * Run soap command action.
     * @return
     */
    TestAction soap(BuilderSupport<SoapActionBuilder> configurer);

    /**
     * Run Camel route actions.
     * @return
     */
    TestAction camel(BuilderSupport<CamelRouteActionBuilder> configurer);

    /**
     * Run zookeeper command action.
     * @return
     */
    TestAction zookeeper(BuilderSupport<ZooActionBuilder> configurer);

    /**
     * Adds template container with nested test actions.
     *
     * @param configurer
     * @return
     */
    Template applyTemplate(BuilderSupport<TemplateBuilder> configurer);

    /**
     * Adds sequence of test actions to finally block.
     * @return
     */
    FinallySequenceBuilder doFinally();
}
