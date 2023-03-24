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

package org.citrusframework.dsl.design;

import javax.sql.DataSource;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestCaseBuilder;
import org.citrusframework.actions.AntRunAction;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.ExecutePLSQLAction;
import org.citrusframework.actions.ExecuteSQLAction;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.actions.InputAction;
import org.citrusframework.actions.JavaAction;
import org.citrusframework.actions.LoadPropertiesAction;
import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.actions.StartServerAction;
import org.citrusframework.actions.StopServerAction;
import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.actions.StopTimerAction;
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.actions.TransformAction;
import org.citrusframework.container.Assert;
import org.citrusframework.container.Async;
import org.citrusframework.container.Catch;
import org.citrusframework.container.Conditional;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.container.Iterate;
import org.citrusframework.container.Parallel;
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.container.RepeatUntilTrue;
import org.citrusframework.container.Sequence;
import org.citrusframework.container.Template;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.container.Timer;
import org.citrusframework.container.Wait;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.builder.AssertSoapFaultBuilder;
import org.citrusframework.dsl.builder.CamelRouteActionBuilder;
import org.citrusframework.dsl.builder.DockerExecuteActionBuilder;
import org.citrusframework.dsl.builder.HttpActionBuilder;
import org.citrusframework.dsl.builder.KubernetesExecuteActionBuilder;
import org.citrusframework.dsl.builder.PurgeJmsQueuesActionBuilder;
import org.citrusframework.dsl.builder.PurgeMessageChannelActionBuilder;
import org.citrusframework.dsl.builder.ReceiveMessageActionBuilder;
import org.citrusframework.dsl.builder.SeleniumActionBuilder;
import org.citrusframework.dsl.builder.SendMessageActionBuilder;
import org.citrusframework.dsl.builder.SoapActionBuilder;
import org.citrusframework.dsl.builder.ZooExecuteActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.server.Server;
import org.springframework.core.io.Resource;

/**
 * Test builder interface defines builder pattern methods for creating a new
 * Citrus test case.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public interface TestDesigner extends TestCaseBuilder {

    /**
     * Adds a custom test action implementation.
     *
     * @param testAction
     */
    void action(TestAction testAction);

    /**
     * Adds a custom test action implementation.
     * @param builder
     */
    void action(TestActionBuilder<?> builder);

    /**
     * Prepare and add a custom container implementation.
     * @param container
     * @return
     */
    <T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container);

    /**
     * Prepare and add a custom container implementation.
     * @param builder
     * @return
     */
    <T extends TestActionContainerBuilder<? extends TestActionContainer, ?>> T container(T builder);

    /**
     * Apply test apply with all test actions, finally actions and test
     * variables defined in given apply.
     *
     * @param behavior
     */
    ApplyTestBehaviorAction.Builder applyBehavior(TestBehavior behavior);

    /**
     * Action creating a new test variable during a test.
     *
     * @param variableName
     * @param value
     * @return
     */
    CreateVariablesAction.Builder createVariable(String variableName, String value);

    /**
     * Creates a new ANT run action definition
     * for further configuration.
     *
     * @param buildFilePath
     * @return
     */
    AntRunAction.Builder antrun(String buildFilePath);

    /**
     * Creates a new echo action.
     * @param message
     * @return
     */
    EchoAction.Builder echo(String message);

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecutePLSQLAction.Builder plsql(DataSource dataSource);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecuteSQLAction.Builder sql(DataSource dataSource);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecuteSQLQueryAction.Builder query(DataSource dataSource);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param messageEndpoint
     * @return
     */
    ReceiveTimeoutAction.Builder receiveTimeout(Endpoint messageEndpoint);

    /**
     * Creates a new receive timeout action definition from message endpoint name as String.
     *
     * @param messageEndpointName
     * @return
     */
    ReceiveTimeoutAction.Builder receiveTimeout(String messageEndpointName);

    /**
     * Creates a new fail action.
     *
     * @param message
     * @return
     */
    FailAction.Builder fail(String message);

    /**
     * Creates a new input action.
     *
     * @return
     */
    InputAction.Builder input();

    /**
     * Creates a new Java action definition from class name.
     *
     * @param className
     * @return
     */
    JavaAction.Builder java(String className);

    /**
     * Creates a new Java action definition from Java class.
     *
     * @param clazz
     * @return
     */
    JavaAction.Builder java(Class<?> clazz);

    /**
     * Creates a new Java action definition from Java object instance.
     *
     * @param instance
     * @return
     */
    JavaAction.Builder java(Object instance);

    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     * @return
     */
    LoadPropertiesAction.Builder load(String filePath);

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @return
     */
    PurgeJmsQueuesActionBuilder purgeQueues();

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @return
     */
    PurgeMessageChannelActionBuilder purgeChannels();

    /**
     * Creates a new purge message endpoint action definition
     * for further configuration.
     *
     * @return
     */
    PurgeEndpointAction.Builder purgeEndpoints();

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    ReceiveMessageActionBuilder<?> receive(Endpoint messageEndpoint);

    /**
     * Creates receive message action definition with messsage endpoint name.
     *
     * @param messageEndpointName
     * @return
     */
    ReceiveMessageActionBuilder<?> receive(String messageEndpointName);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    SendMessageActionBuilder<?> send(Endpoint messageEndpoint);

    /**
     * Create send message action definition with message endpoint name. According to message endpoint type
     * we can create a SOAP specific message sending action.
     *
     * @param messageEndpointName
     * @return
     */
    SendMessageActionBuilder<?> send(String messageEndpointName);

    /**
     * Add sleep action with default delay time.
     * @return
     */
    SleepAction.Builder sleep();

    /**
     * Add sleep action with time in milliseconds.
     *
     * @param milliseconds
     * @return
     */
    SleepAction.Builder sleep(long milliseconds);

    /**
     * Add sleep action with time in seconds.
     *
     * @param seconds
     * @return
     */
    SleepAction.Builder sleep(double seconds);

    /**
     * Add wait action.
     *
     * @return
     */
    Wait.Builder waitFor();

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    StartServerAction.Builder start(Server... servers);

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    StartServerAction.Builder start(Server server);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    StopServerAction.Builder stop(Server... servers);

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    StopServerAction.Builder stop(Server server);

    /**
     * Creates a new stop time action.
     * @return
     */
    StopTimeAction.Builder stopTime();

    /**
     * Creates a new stop time action.
     *
     * @param id
     * @return
     */
    StopTimeAction.Builder stopTime(String id);

    /**
     * Creates a new stop time action.
     *
     * @param id
     * @param suffix
     * @return
     */
    StopTimeAction.Builder stopTime(String id, String suffix);

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @return
     */
    TraceVariablesAction.Builder traceVariables();

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @param variables
     * @return
     */
    TraceVariablesAction.Builder traceVariables(String... variables);

    /**
     * Creates a new groovy action definition with
     * script code.
     *
     * @param script
     * @return
     */
    GroovyAction.Builder groovy(String script);

    /**
     * Creates a new groovy action definition with
     * script file resource.
     *
     * @param scriptResource
     * @return
     */
    GroovyAction.Builder groovy(Resource scriptResource);

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @return
     */
    TransformAction.Builder transform();

    /**
     * Assert exception to happen in nested test action.
     * @return
     */
    Assert.Builder assertException();

    /**
     * Action catches possible exceptions in nested test actions.
     * @return
     */
    Catch.Builder catchException();

    /**
     * Assert SOAP fault during action execution.
     * @return
     */
    AssertSoapFaultBuilder assertSoapFault();

    /**
     * Adds conditional container with nested test actions.
     * @return
     */
    Conditional.Builder conditional();

    /**
     * Adds iterate container with nested test actions.
     * @return
     */
    Iterate.Builder iterate();

    /**
     * Run nested test actions in parallel to each other using multiple threads.
     * @return
     */
    Parallel.Builder parallel();

    /**
     * Adds repeat on error until true container with nested test actions.
     * @return
     */
    RepeatOnErrorUntilTrue.Builder repeatOnError();

    /**
     * Adds repeat until true container with nested test actions.
     * @return
     */
    RepeatUntilTrue.Builder repeat();

    /**
     * Adds sequential container with nested test actions.
     * @return
     */
    Sequence.Builder sequential();

    /**
     * Adds async container with nested test actions.
     * @return
     */
    Async.Builder async();

    /**
     * Repeat nested test actions based on a timer interval.
     * @return
     */
    Timer.Builder timer();

    /**
     * Stops the timer matching the supplied timerId
     * @param timerId
     * @return
     */
    StopTimerAction.Builder stopTimer(String timerId);

    /**
     * Stops all timers within the current test context
     * @return
     */
    StopTimerAction.Builder stopTimers();

    /**
     * Creates a new docker execute action.
     * @return
     */
    DockerExecuteActionBuilder docker();

    /**
     * Creates a new kubernetes execute action.
     * @return
     */
    KubernetesExecuteActionBuilder kubernetes();

    /**
     * Creates a new selenium action builder.
     * @return
     */
    SeleniumActionBuilder selenium();

    /**
     *
     */
    HttpActionBuilder http();

    /**
     *
     */
    SoapActionBuilder soap();

    /**
     * Creates a new Camel route action.
     * @return
     */
    CamelRouteActionBuilder camel();

    /**
     * Creates a new zookeeper execute action.
     * @return
     */
    ZooExecuteActionBuilder zookeeper();

    /**
     * Adds template container with nested test actions.
     *
     * @param name
     * @return
     */
    Template.Builder applyTemplate(String name);

    /**
     * Adds sequence of test actions to finally block.
     * @return
     */
    FinallySequence.Builder doFinally();

    /**
     * Sets the test context.
     * @param context
     */
    void setTestContext(TestContext context);
}
