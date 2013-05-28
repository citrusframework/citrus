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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.Catch;
import com.consol.citrus.container.Parallel;
import com.consol.citrus.container.Sequence;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.junit.AbstractJUnit4CitrusTest;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.message.SoapReplyMessageReceiver;
import com.consol.citrus.ws.message.WebServiceMessageSender;
import org.springframework.core.io.Resource;
import org.testng.ITestContext;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.util.Date;
import java.util.Map;

/**
 * Test case builder offers methods for constructing a test case with several
 * test actions in Java DSL language. Class uses CitrusTestBuilder builder implementation
 * delegating all method calls to this builder.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class JUnit4CitrusTestBuilder extends AbstractJUnit4CitrusTest {

    /** Test builder delegate */
    private CitrusTestBuilder testBuilder;

    /**
     * Initialize test case and variables. Must be done with each test run.
     */
    protected void init() {
        testBuilder = new CitrusTestBuilder(applicationContext);
        testBuilder.name(this.getClass().getSimpleName());
        testBuilder.packageName(this.getClass().getPackage().getName());
    }

    @Override
    protected void executeTest() {
        init();
        configure();
        super.executeTest();
    }

    /**
     * Configures the test case with test actions. Subclasses may override this method in order
     * to contribute test actions to this test case.
     */
    protected void configure() {
    }

    /**
     * Adds description to the test case.
     * @param description
     */
    protected void description(String description) {
        testBuilder.description(description);
    }

    /**
     * Adds author to the test case.
     * @param author
     */
    protected void author(String author) {
        testBuilder.author(author);
    }

    /**
     * Sets test case status.
     * @param status
     */
    protected void status(TestCaseMetaInfo.Status status) {
        testBuilder.status(status);
    }

    /**
     * Sets the creation date.
     * @param date
     */
    protected void creationDate(Date date) {
        testBuilder.creationDate(date);
    }

    /**
     * Adds a new variable definition to the set of test variables
     * for this test case.
     * @param name
     * @param value
     */
    protected void variable(String name, Object value) {
        testBuilder.variable(name, value);
    }

    /**
     * Get the test variables.
     * @return
     */
    protected Map<String, Object> getVariables() {
        return testBuilder.getVariables();
    }

    /**
     * Action creating new test variables during a test.
     * @return
     */
    protected CreateVariablesActionDefinition variables() {
        return testBuilder.variables();
    }

    /**
     * Adds a custom test action implementation.
     * @param testAction
     */
    protected void action(TestAction testAction) {
        testBuilder.action(testAction);
    }

    /**
     * Creates a new ANT run action definition
     * for further configuration.
     * @param buildFilePath
     * @return
     */
    protected AntRunActionDefinition antrun(String buildFilePath) {
        return testBuilder.antrun(buildFilePath);
    }

    /**
     * Creates a new echo action.
     * @param message
     * @return
     */
    protected EchoAction echo(String message) {
        return testBuilder.echo(message);
    }

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    protected ExecutePLSQLActionDefinition plsql(DataSource dataSource) {
        return testBuilder.plsql(dataSource);
    }

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    protected ExecuteSQLActionDefinition sql(DataSource dataSource) {
        return testBuilder.sql(dataSource);
    }

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    protected ExecuteSQLQueryActionDefinition query(DataSource dataSource) {
        return testBuilder.query(dataSource);
    }

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     * @param messageReceiver
     * @return
     */
    protected ReceiveTimeoutActionDefinition expectTimeout(MessageReceiver messageReceiver) {
        return testBuilder.expectTimeout(messageReceiver);
    }

    /**
     * Creates a new receive timeout action definition from message receiver name as String.
     * @param messageReceiverName
     * @return
     */
    protected ReceiveTimeoutActionDefinition expectTimeout(String messageReceiverName) {
        return testBuilder.expectTimeout(messageReceiverName);
    }

    /**
     * Creates a new fail action.
     * @param message
     * @return
     */
    protected FailAction fail(String message) {
        return testBuilder.fail(message);
    }

    /**
     * Creates a new input action.
     * @return
     */
    protected InputActionDefinition input() {
        return testBuilder.input();
    }

    /**
     * Creates a new Java action definition from class name.
     * @param className
     * @return
     */
    protected JavaActionDefinition java(String className) {
        return testBuilder.java(className);
    }

    /**
     * Creates a new Java action definition from Java class.
     * @param clazz
     * @return
     */
    protected JavaActionDefinition java(Class<?> clazz) {
        return testBuilder.java(clazz);
    }

    /**
     * Creates a new Java action definition from Java object instance.
     * @param instance
     * @return
     */
    protected JavaActionDefinition java(Object instance) {
        return testBuilder.java(instance);
    }

    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     * @return
     */
    protected LoadPropertiesAction load(String filePath) {
        return testBuilder.load(filePath);
    }

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     * @param connectionFactory
     * @return
     */
    protected PurgeJMSQueuesActionDefinition purgeQueues(ConnectionFactory connectionFactory) {
        return testBuilder.purgeQueues(connectionFactory);
    }

    /**
     * Purge queues using default connection factory.
     * @return
     */
    protected PurgeJMSQueuesActionDefinition purgeQueues() {
        return testBuilder.purgeQueues();
    }

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     * @return
     */
    protected PurgeMessageChannelActionDefinition purgeChannels() {
        return testBuilder.purgeChannels();
    }

    /**
     * Creates special SOAP receive message action definition with message receiver instance.
     * @param messageReceiver
     * @return
     */
    protected ReceiveSoapMessageActionDefinition receive(SoapReplyMessageReceiver messageReceiver) {
        return testBuilder.receive(messageReceiver);
    }

    /**
     * Creates receive message action definition with message receiver instance.
     * @param messageReceiver
     * @return
     */
    protected ReceiveMessageActionDefinition receive(MessageReceiver messageReceiver) {
        return testBuilder.receive(messageReceiver);
    }

    /**
     * Creates receive message action definition with messsage receiver name.
     * @param messageReceiverName
     * @return
     */
    protected ReceiveMessageActionDefinition receive(String messageReceiverName) {
        return testBuilder.receive(messageReceiverName);
    }

    /**
     * Create special SOAP send message action definition with message sender instance.
     * @param messageSender
     * @return
     */
    protected SendSoapMessageActionDefinition send(WebServiceMessageSender messageSender) {
        return testBuilder.send(messageSender);
    }

    /**
     * Create send message action definition with message sender instance.
     * @param messageSender
     * @return
     */
    protected SendMessageActionDefinition send(MessageSender messageSender) {
        return testBuilder.send(messageSender);
    }

    /**
     * Create send message action definition with message sender name. According to message sender type
     * we can create a SOAP specific message sending action.
     * @param messageSenderName
     * @return
     */
    protected SendMessageActionDefinition send(String messageSenderName) {
        return testBuilder.send(messageSenderName);
    }

    /**
     * Create SOAP fault send message action definition with message sender name. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     * @param messageSenderName
     * @return
     */
    protected SendSoapFaultActionDefinition sendSoapFault(String messageSenderName) {
        return testBuilder.sendSoapFault(messageSenderName);
    }

    /**
     * Add sleep action with default delay time.
     */
    protected SleepAction sleep() {
        return testBuilder.sleep();
    }

    /**
     * Add sleep action with time in milliseconds.
     * @param time
     */
    protected SleepAction sleep(long time) {
        return testBuilder.sleep(time);
    }

    /**
     * Add sleep action with time in seconds.
     * @param time
     */
    protected SleepAction sleep(double time) {
        return testBuilder.sleep(time);
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     * @param servers
     * @return
     */
    protected StartServerAction start(Server... servers) {
        return testBuilder.start(servers);
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     * @param server
     * @return
     */
    protected StartServerAction start(Server server) {
        return testBuilder.start(server);
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     * @param servers
     * @return
     */
    protected StopServerAction stop(Server... servers) {
        return testBuilder.stop(servers);
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     * @param server
     * @return
     */
    protected StopServerAction stop(Server server) {
        return testBuilder.stop(server);
    }

    /**
     * Creates a new stop time action.
     * @return
     */
    protected StopTimeAction stopTime() {
        return testBuilder.stopTime();
    }

    /**
     * Creates a new stop time action.
     * @param id
     * @return
     */
    protected StopTimeAction stopTime(String id) {
        return testBuilder.stopTime(id);
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     * @return
     */
    protected TraceVariablesAction traceVariables() {
        return testBuilder.traceVariables();
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     * @param variables
     * @return
     */
    protected TraceVariablesAction traceVariables(String... variables) {
        return testBuilder.traceVariables(variables);
    }

    /**
     * Creates a new groovy action definition with
     * script code.
     * @param script
     * @return
     */
    protected GroovyActionDefinition groovy(String script) {
        return testBuilder.groovy(script);
    }

    /**
     * Creates a new groovy action definition with
     * script file resource.
     * @param scriptResource
     * @return
     */
    protected GroovyActionDefinition groovy(Resource scriptResource) {
        return testBuilder.groovy(scriptResource);
    }

    /**
     * Creates a new transform action definition
     * for further configuration.
     * @return
     */
    protected TransformActionDefinition transform() {
        return testBuilder.transform();
    }

    /**
     * Assert exception to happen in nested test action.
     * @param testAction the nested testAction
     * @return
     */
    protected AssertDefinition assertException(TestAction testAction) {
        return testBuilder.assertException(testAction);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     * @param exception the exception to be caught
     * @param actions nested test actions
     * @return
     */
    protected Catch catchException(String exception, TestAction ... actions) {
        return testBuilder.catchException(exception, actions);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     * @param exception
     * @param actions
     * @return
     */
    protected Catch catchException(Class<? extends Throwable> exception, TestAction ... actions) {
        return testBuilder.catchException(exception, actions);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     * @param actions
     * @return
     */
    protected Catch catchException(TestAction ... actions) {
        return testBuilder.catchException(actions);
    }

    /**
     * Assert SOAP fault during action execution.
     *
     * @param testAction
     * @return
     */
    protected AssertSoapFaultDefinition assertSoapFault(TestAction testAction) {
        return testBuilder.assertSoapFault(testAction);
    }

    /**
     * Adds conditional container with nested test actions.
     * @param actions
     * @return
     */
    protected ConditionalDefinition conditional(TestAction ... actions) {
        return testBuilder.conditional(actions);
    }

    /**
     * Adds iterate container with nested test actions.
     * @param actions
     * @return
     */
    protected IterateDefinition iterate(TestAction ... actions) {
        return testBuilder.iterate(actions);
    }

    /**
     * Adds parallel container with nested test actions.
     * @param actions
     * @return
     */
    protected Parallel parallel(TestAction ... actions) {
        return testBuilder.parallel(actions);
    }

    /**
     * Adds repeat on error until true container with nested test actions.
     * @param actions
     * @return
     */
    protected RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions) {
        return testBuilder.repeatOnError(actions);
    }

    /**
     * Adds repeat until true container with nested test actions.
     * @param actions
     * @return
     */
    protected RepeatUntilTrueDefinition repeat(TestAction... actions) {
        return testBuilder.repeat(actions);
    }

    /**
     * Adds sequential container with nested test actions.
     * @param actions
     * @return
     */
    protected Sequence sequential(TestAction ... actions) {
        return testBuilder.sequential(actions);
    }

    /**
     * Adds template container with nested test actions.
     * @param name
     * @return
     */
    protected TemplateDefinition template(String name) {
        return testBuilder.template(name);
    }

    /**
     * Adds sequence of test actions to finally block.
     * @param actions
     */
    protected void doFinally(TestAction ... actions) {
        testBuilder.doFinally(actions);
    }

    /**
     * Gets the testCase.
     * @return the testCase the testCase to get.
     */
    protected TestCase getTestCase() {
        return testBuilder.getTestCase();
    }
}
