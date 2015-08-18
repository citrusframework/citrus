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

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.util.Date;

/**
 * Test builder interface defines builder pattern methods for creating a new
 * Citrus test case.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public interface TestDesigner extends ApplicationContextAware {

    /**
     * Builds the test case.
     * @return
     */
    TestCase getTestCase();

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
     * Adds a new variable definition to the set of test variables
     * for this test case.
     *
     * @param name
     * @param value
     */
    void variable(String name, Object value);

    /**
     * Adds a custom test action implementation.
     *
     * @param testAction
     */
    void action(TestAction testAction);

    /**
     * Apply test apply with all test actions, finally actions and test
     * variables defined in given apply.
     *
     * @param behavior
     */
    void applyBehavior(TestBehavior behavior);

    /**
     * Action creating a new test variable during a test.
     *
     * @param variableName
     * @param value
     * @return
     */
    CreateVariablesAction createVariable(String variableName, String value);

    /**
     * Creates a new ANT run action definition
     * for further configuration.
     *
     * @param buildFilePath
     * @return
     */
    AntRunBuilder antrun(String buildFilePath);

    /**
     * Creates a new echo action.
     *
     * @param message
     * @return
     */
    EchoAction echo(String message);

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecutePLSQLBuilder plsql(DataSource dataSource);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecuteSQLBuilder sql(DataSource dataSource);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecuteSQLQueryBuilder query(DataSource dataSource);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param messageEndpoint
     * @return
     */
    ReceiveTimeoutBuilder receiveTimeout(Endpoint messageEndpoint);

    /**
     * Creates a new receive timeout action definition from message endpoint name as String.
     *
     * @param messageEndpointName
     * @return
     */
    ReceiveTimeoutBuilder receiveTimeout(String messageEndpointName);

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
     * @return
     */
    InputActionBuilder input();

    /**
     * Creates a new Java action definition from class name.
     *
     * @param className
     * @return
     */
    JavaActionBuilder java(String className);

    /**
     * Creates a new Java action definition from Java class.
     *
     * @param clazz
     * @return
     */
    JavaActionBuilder java(Class<?> clazz);

    /**
     * Creates a new Java action definition from Java object instance.
     *
     * @param instance
     * @return
     */
    JavaActionBuilder java(Object instance);

    /**
     * Creates a new load properties action.
     *
     * @param filePath path to properties file.
     * @return
     */
    LoadPropertiesAction load(String filePath);

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @param connectionFactory
     * @return
     */
    PurgeJmsQueuesBuilder purgeQueues(ConnectionFactory connectionFactory);

    /**
     * Purge queues using default connection factory.
     *
     * @return
     */
    PurgeJmsQueuesBuilder purgeQueues();

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @return
     */
    PurgeChannelsBuilder purgeChannels();

    /**
     * Creates special SOAP receive message action definition with web service server instance.
     *
     * @param server
     * @return
     */
    ReceiveSoapMessageBuilder receive(WebServiceServer server);

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    ReceiveMessageBuilder receive(Endpoint messageEndpoint);

    /**
     * Creates receive message action definition with messsage endpoint name.
     *
     * @param messageEndpointName
     * @return
     */
    ReceiveMessageBuilder receive(String messageEndpointName);

    /**
     * Create special SOAP send message action definition with web service client instance.
     *
     * @param client
     * @return
     */
    SendSoapMessageBuilder send(WebServiceClient client);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    SendMessageBuilder send(Endpoint messageEndpoint);

    /**
     * Create send message action definition with message endpoint name. According to message endpoint type
     * we can create a SOAP specific message sending action.
     *
     * @param messageEndpointName
     * @return
     */
    SendMessageBuilder send(String messageEndpointName);

    /**
     * Create SOAP fault send message action definition with message endpoint name. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpointName
     * @return
     */
    SendSoapFaultBuilder sendSoapFault(String messageEndpointName);

    /**
     * Create SOAP fault send message action definition with message endpoint instance. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpoint
     * @return
     */
    SendSoapFaultBuilder sendSoapFault(Endpoint messageEndpoint);

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
     * Add sleep action with time in seconds.
     *
     * @param seconds
     * @return
     */
    SleepAction sleep(double seconds);

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
     *
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
     * Creates a new groovy action definition with
     * script code.
     *
     * @param script
     * @return
     */
    GroovyActionBuilder groovy(String script);

    /**
     * Creates a new groovy action definition with
     * script file resource.
     *
     * @param scriptResource
     * @return
     */
    GroovyActionBuilder groovy(Resource scriptResource);

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @return
     */
    TransformActionBuilder transform();

    /**
     * Assert exception to happen in nested test action.
     *
     * @param testAction the nested testAction
     * @return
     */
    AssertExceptionBuilder assertException(TestAction testAction);

    /**
     * Assert exception to happen in nested test action.
     * @return
     */
    AssertExceptionBuilder assertException();

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param actions
     * @return
     */
    CatchExceptionBuilder catchException(TestAction... actions);

    /**
     * Action catches possible exceptions in nested test actions.
     * @return
     */
    CatchExceptionBuilder catchException();

    /**
     * Assert SOAP fault during action execution.
     *
     * @param testAction
     * @return
     */
    AssertSoapFaultBuilder assertSoapFault(TestAction testAction);

    /**
     * Assert SOAP fault during action execution.
     * @return
     */
    AssertSoapFaultBuilder assertSoapFault();

    /**
     * Adds conditional container with nested test actions.
     *
     * @param actions
     * @return
     */
    ConditionalBuilder conditional(TestAction... actions);

    /**
     * Adds conditional container with nested test actions.
     * @return
     */
    ConditionalBuilder conditional();

    /**
     * Adds iterate container with nested test actions.
     *
     * @param actions
     * @return
     */
    IterateBuilder iterate(TestAction... actions);

    /**
     * Adds iterate container with nested test actions.
     * @return
     */
    IterateBuilder iterate();

    /**
     * Adds parallel container with nested test actions.
     *
     * @param actions
     * @return
     */
    ParallelBuilder parallel(TestAction... actions);

    /**
     * Adds parallel container with nested test actions.
     * @return
     */
    ParallelBuilder parallel();

    /**
     * Adds repeat on error until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    RepeatOnErrorBuilder repeatOnError(TestAction... actions);

    /**
     * Adds repeat on error until true container with nested test actions.
     * @return
     */
    RepeatOnErrorBuilder repeatOnError();

    /**
     * Adds repeat until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    RepeatBuilder repeat(TestAction... actions);

    /**
     * Adds repeat until true container with nested test actions.
     * @return
     */
    RepeatBuilder repeat();

    /**
     * Adds sequential container with nested test actions.
     *
     * @param actions
     * @return
     */
    SequenceBuilder sequential(TestAction... actions);

    /**
     * Adds sequential container with nested test actions.
     * @return
     */
    SequenceBuilder sequential();

    /**
     * Adds template container with nested test actions.
     *
     * @param name
     * @return
     */
    TemplateBuilder applyTemplate(String name);

    /**
     * Adds sequence of test actions to finally block.
     *
     * @param actions
     */
    FinallySequenceBuilder doFinally(TestAction... actions);

    /**
     * Adds sequence of test actions to finally block.
     */
    FinallySequenceBuilder doFinally();

    /**
     * Gets new position handle of current test action situation.
     * @return
     */
    PositionHandle positionHandle();
}
