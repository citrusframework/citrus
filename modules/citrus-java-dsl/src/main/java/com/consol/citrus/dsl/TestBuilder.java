/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.dsl;

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.dsl.definition.*;
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
 * @since 1.3.1
 * @deprecated since 2.3 in favor of {@link com.consol.citrus.dsl.design.TestDesigner}
 */
public interface TestBuilder extends ApplicationContextAware {

    /**
     * Builds the test case.
     * @return
     */
    TestCase build();

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
     * Action creating new test variables during a test.
     *
     * @return
     */
    CreateVariablesActionDefinition variables();

    /**
     * Action creating a new test variable during a test.
     *
     * @return
     */
    CreateVariablesAction setVariable(String variableName, String value);

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
     * Creates a new ANT run action definition
     * for further configuration.
     *
     * @param buildFilePath
     * @return
     */
    AntRunActionDefinition antrun(String buildFilePath);

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
    ExecutePLSQLActionDefinition plsql(DataSource dataSource);

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecuteSQLActionDefinition sql(DataSource dataSource);

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    ExecuteSQLQueryActionDefinition query(DataSource dataSource);

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param messageEndpoint
     * @return
     */
    ReceiveTimeoutActionDefinition expectTimeout(Endpoint messageEndpoint);

    /**
     * Creates a new receive timeout action definition from message endpoint name as String.
     *
     * @param messageEndpointName
     * @return
     */
    ReceiveTimeoutActionDefinition expectTimeout(String messageEndpointName);

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
    InputActionDefinition input();

    /**
     * Creates a new Java action definition from class name.
     *
     * @param className
     * @return
     */
    JavaActionDefinition java(String className);

    /**
     * Creates a new Java action definition from Java class.
     *
     * @param clazz
     * @return
     */
    JavaActionDefinition java(Class<?> clazz);

    /**
     * Creates a new Java action definition from Java object instance.
     *
     * @param instance
     * @return
     */
    JavaActionDefinition java(Object instance);

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
    PurgeJmsQueueActionDefinition purgeQueues(ConnectionFactory connectionFactory);

    /**
     * Purge queues using default connection factory.
     *
     * @return
     */
    PurgeJmsQueueActionDefinition purgeQueues();

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @return
     */
    PurgeMessageChannelActionDefinition purgeChannels();

    /**
     * Creates special SOAP receive message action definition with web service server instance.
     *
     * @param server
     * @return
     */
    ReceiveSoapMessageActionDefinition receive(WebServiceServer server);

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    ReceiveMessageActionDefinition receive(Endpoint messageEndpoint);

    /**
     * Creates receive message action definition with messsage endpoint name.
     *
     * @param messageEndpointName
     * @return
     */
    ReceiveMessageActionDefinition receive(String messageEndpointName);

    /**
     * Create special SOAP send message action definition with web service client instance.
     *
     * @param client
     * @return
     */
    SendSoapMessageActionDefinition send(WebServiceClient client);

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    SendMessageActionDefinition send(Endpoint messageEndpoint);

    /**
     * Create send message action definition with message endpoint name. According to message endpoint type
     * we can create a SOAP specific message sending action.
     *
     * @param messageEndpointName
     * @return
     */
    SendMessageActionDefinition send(String messageEndpointName);

    /**
     * Create SOAP fault send message action definition with message endpoint name. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpointName
     * @return
     */
    SendSoapFaultActionDefinition sendSoapFault(String messageEndpointName);

    /**
     * Create SOAP fault send message action definition with message endpoint instance. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpoint
     * @return
     */
    SendSoapFaultActionDefinition sendSoapFault(Endpoint messageEndpoint);

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
     * Add sleep action with time in seconds.
     *
     * @param seconds
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
    GroovyActionDefinition groovy(String script);

    /**
     * Creates a new groovy action definition with
     * script file resource.
     *
     * @param scriptResource
     * @return
     */
    GroovyActionDefinition groovy(Resource scriptResource);

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @return
     */
    TransformActionDefinition transform();

    /**
     * Assert exception to happen in nested test action.
     *
     * @param testAction the nested testAction
     * @return
     */
    AssertDefinition assertException(TestAction testAction);

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param exception the exception to be caught
     * @param actions   nested test actions
     * @return
     */
    CatchDefinition catchException(String exception, TestAction... actions);

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param exception
     * @param actions
     * @return
     */
    CatchDefinition catchException(Class<? extends Throwable> exception, TestAction... actions);

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param actions
     * @return
     */
    CatchDefinition catchException(TestAction... actions);

    /**
     * Assert SOAP fault during action execution.
     *
     * @param testAction
     * @return
     */
    AssertSoapFaultDefinition assertSoapFault(TestAction testAction);

    /**
     * Adds conditional container with nested test actions.
     *
     * @param actions
     * @return
     */
    ConditionalDefinition conditional(TestAction... actions);

    /**
     * Adds iterate container with nested test actions.
     *
     * @param actions
     * @return
     */
    IterateDefinition iterate(TestAction... actions);

    /**
     * Adds parallel container with nested test actions.
     *
     * @param actions
     * @return
     */
    Parallel parallel(TestAction... actions);

    /**
     * Adds repeat on error until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions);

    /**
     * Adds repeat until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    RepeatUntilTrueDefinition repeat(TestAction... actions);

    /**
     * Adds sequential container with nested test actions.
     *
     * @param actions
     * @return
     */
    Sequence sequential(TestAction... actions);

    /**
     * Adds template container with nested test actions.
     *
     * @param name
     * @return
     */
    TemplateDefinition template(String name);

    /**
     * Adds sequence of test actions to finally block.
     *
     * @param actions
     */
    void doFinally(TestAction... actions);

    /**
     * Gets new position handle of current test action situation.
     * @return
     */
    PositionHandle positionHandle();
}
