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

package com.consol.citrus.dsl.design;

import javax.sql.DataSource;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActionContainerBuilder;
import com.consol.citrus.TestCaseBuilder;
import com.consol.citrus.actions.AntRunAction;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.actions.InputAction;
import com.consol.citrus.actions.JavaAction;
import com.consol.citrus.actions.LoadPropertiesAction;
import com.consol.citrus.actions.PurgeEndpointAction;
import com.consol.citrus.actions.PurgeMessageChannelAction;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.actions.StartServerAction;
import com.consol.citrus.actions.StopServerAction;
import com.consol.citrus.actions.StopTimeAction;
import com.consol.citrus.actions.StopTimerAction;
import com.consol.citrus.actions.TraceVariablesAction;
import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.camel.actions.CamelRouteActionBuilder;
import com.consol.citrus.container.Assert;
import com.consol.citrus.container.Async;
import com.consol.citrus.container.Catch;
import com.consol.citrus.container.Conditional;
import com.consol.citrus.container.FinallySequence;
import com.consol.citrus.container.Iterate;
import com.consol.citrus.container.Parallel;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.container.RepeatUntilTrue;
import com.consol.citrus.container.Sequence;
import com.consol.citrus.container.Template;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.container.Timer;
import com.consol.citrus.container.Wait;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.selenium.actions.SeleniumActionBuilder;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.actions.SoapActionBuilder;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;
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
    PurgeJmsQueuesAction.Builder purgeQueues();

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @return
     */
    PurgeMessageChannelAction.Builder purgeChannels();

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
    ReceiveMessageAction.Builder receive(Endpoint messageEndpoint);

    /**
     * Creates receive message action definition with messsage endpoint name.
     *
     * @param messageEndpointName
     * @return
     */
    ReceiveMessageAction.Builder receive(String messageEndpointName);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    SendMessageAction.Builder send(Endpoint messageEndpoint);

    /**
     * Create send message action definition with message endpoint name. According to message endpoint type
     * we can create a SOAP specific message sending action.
     *
     * @param messageEndpointName
     * @return
     */
    SendMessageAction.Builder send(String messageEndpointName);

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
    AssertSoapFault.Builder assertSoapFault();

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
    DockerExecuteAction.Builder docker();

    /**
     * Creates a new kubernetes execute action.
     * @return
     */
    KubernetesExecuteAction.Builder kubernetes();

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
    ZooExecuteAction.Builder zookeeper();

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
