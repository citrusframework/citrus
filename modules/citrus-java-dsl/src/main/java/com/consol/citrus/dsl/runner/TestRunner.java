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
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestCaseBuilder;
import com.consol.citrus.actions.AntRunAction;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.actions.InputAction;
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
import com.consol.citrus.container.Iterate;
import com.consol.citrus.container.Parallel;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.container.RepeatUntilTrue;
import com.consol.citrus.container.Sequence;
import com.consol.citrus.container.Template;
import com.consol.citrus.container.Timer;
import com.consol.citrus.container.Wait;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.selenium.actions.SeleniumActionBuilder;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.actions.SoapActionBuilder;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;

/**
 * Test builder interface defines builder pattern methods for creating a new
 * Citrus test case.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public interface TestRunner extends TestCaseBuilder {

    /**
     * Starts the test case execution.
     */
    void start();

    /**
     * Stops test case execution.
     */
    void stop();

    /**
     * Runs test action and returns same action after execution.
     * @param testAction
     * @return
     */
    default <A extends TestAction> TestActionBuilder<A> run(A testAction) {
        return run((TestActionBuilder<A>)() -> testAction);
    }

    /**
     * Runs test action and returns same action after execution.
     * @param builder
     * @return
     */
    <T extends TestActionBuilder<?>> T run(T builder);

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
     * @param configurer
     * @return
     */
    AntRunAction.Builder antrun(BuilderSupport<AntRunAction.Builder> configurer);

    /**
     * Creates and executes a new echo action.
     * @param message
     * @return
     */
    EchoAction.Builder echo(String message);

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ExecutePLSQLAction.Builder plsql(BuilderSupport<ExecutePLSQLAction.Builder> configurer);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ExecuteSQLAction.Builder sql(BuilderSupport<ExecuteSQLAction.Builder> configurer);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ExecuteSQLQueryAction.Builder query(BuilderSupport<ExecuteSQLQueryAction.Builder> configurer);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    ReceiveTimeoutAction.Builder receiveTimeout(BuilderSupport<ReceiveTimeoutAction.Builder> configurer);

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
     * @param configurer
     * @return
     */
    InputAction.Builder input(BuilderSupport<InputAction.Builder> configurer);

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
     * @param configurer
     * @return
     */
    PurgeJmsQueuesAction.Builder purgeQueues(BuilderSupport<PurgeJmsQueuesAction.Builder> configurer);

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    PurgeMessageChannelAction.Builder purgeChannels(BuilderSupport<PurgeMessageChannelAction.Builder> configurer);

    /**
     * Creates a new purge message endpoint action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    PurgeEndpointAction.Builder purgeEndpoints(BuilderSupport<PurgeEndpointAction.Builder> configurer);

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param configurer
     * @return
     */
    ReceiveMessageAction.Builder receive(BuilderSupport<ReceiveMessageAction.Builder> configurer);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param configurer
     * @return
     */
    SendMessageAction.Builder send(BuilderSupport<SendMessageAction.Builder> configurer);

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
     * Creates a wait action that waits for a condition to be satisfied before continuing.
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
     * Creates a new groovy action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    GroovyAction.Builder groovy(BuilderSupport<GroovyAction.Builder> configurer);

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @param configurer
     * @return
     */
    TransformAction.Builder transform(BuilderSupport<TransformAction.Builder> configurer);

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
     * Run docker command action.
     * @return
     */
    DockerExecuteAction.Builder docker(BuilderSupport<DockerExecuteAction.Builder> configurer);

    /**
     * Run kubernetes command action.
     * @return
     */
    KubernetesExecuteAction.Builder kubernetes(BuilderSupport<KubernetesExecuteAction.Builder> configurer);

    /**
     * Run selenium command action.
     * @return
     */
    SeleniumActionBuilder selenium(BuilderSupport<SeleniumActionBuilder> configurer);

    /**
     * Run http command action.
     * @return
     */
    HttpActionBuilder http(BuilderSupport<HttpActionBuilder> configurer);

    /**
     * Run soap command action.
     * @return
     */
    SoapActionBuilder soap(BuilderSupport<SoapActionBuilder> configurer);

    /**
     * Run Camel route actions.
     * @return
     */
    CamelRouteActionBuilder camel(BuilderSupport<CamelRouteActionBuilder> configurer);

    /**
     * Run zookeeper command action.
     * @return
     */
    ZooExecuteAction.Builder zookeeper(BuilderSupport<ZooExecuteAction.Builder> configurer);

    /**
     * Adds template container with nested test actions.
     *
     * @param configurer
     * @return
     */
    Template.Builder applyTemplate(BuilderSupport<Template.Builder> configurer);
}
