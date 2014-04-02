/*
 * Copyright 2006-2012 the original author or authors.
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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.server.Server;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.SoapReplyMessageReceiver;
import com.consol.citrus.ws.message.WebServiceMessageSender;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.testng.*;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 * TestNG Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestBuilder by simple method delegation.
 *
 * @author Christoph Deppisch
 */
public class TestNGCitrusTestBuilder extends AbstractTestNGCitrusTest implements TestBuilder {

    /** Test builder delegate */
    private CitrusTestBuilder testBuilder;

    @Override
    public void run(final IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method != null && method.getAnnotation(CitrusTest.class) != null) {
            CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);

            init();

            if (StringUtils.hasText(citrusTestAnnotation.name())) {
                name(citrusTestAnnotation.name());
            } else {
                name(method.getName());
            }

            if (getParameterValues().length > 0) {
                Object[] parameterValues = getParameterValues()[testResult.getMethod().getCurrentInvocationCount()];
                testBuilder.getTestCase().setParameters(convertParameterValues(parameterValues));
                ReflectionUtils.invokeMethod(method, this, parameterValues);
            } else {
                ReflectionUtils.invokeMethod(method, this);
            }

            TestContext testContext = prepareTestContext(createTestContext());
            TestCase testCase = testBuilder.getTestCase();

            try {
                testCase.execute(testContext);
            } catch (RuntimeException e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            } catch (Exception e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            }

            super.run(new FakeExecutionCallBack(callBack.getParameters()), testResult);
        } else {
            super.run(callBack, testResult);
        }
    }

    /**
     * Initialize test case and variables. Must be done with each test run.
     */
    public void init() {
        testBuilder = new CitrusTestBuilder(applicationContext);
        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    @Override
    protected void executeTest(ITestContext testContext) {
        init();
        configure();
        super.executeTest(testContext);
    }

    /**
     * Main entrance method for builder pattern usage. Subclasses may override
     * this method and call Java DSL builder methods for adding test actions and
     * basic test case properties.
     */
    protected void configure() {
    }

    /**
     * Set custom test case name.
     * @param name
     */
    public void name(String name) {
        testBuilder.name(name);
    }

    /**
     * Adds description to the test case.
     * @param description
     */
    public void description(String description) {
        testBuilder.description(description);
    }

    /**
     * Adds author to the test case.
     * @param author
     */
    public void author(String author) {
        testBuilder.author(author);
    }

    /**
     * Sets custom package name for this test case.
     * @param packageName
     */
    public void packageName(String packageName) {
        testBuilder.packageName(packageName);
    }

    /**
     * Sets test case status.
     * @param status
     */
    public void status(TestCaseMetaInfo.Status status) {
        testBuilder.status(status);
    }

    /**
     * Sets the creation date.
     * @param date
     */
    public void creationDate(Date date) {
        testBuilder.creationDate(date);
    }

    /**
     * Adds a new variable definition to the set of test variables
     * for this test case.
     * @param name
     * @param value
     */
    public void variable(String name, Object value) {
        testBuilder.variable(name, value);
    }

    /**
     * Action creating new test variables during a test.
     * @return
     */
    public CreateVariablesActionDefinition variables() {
        return testBuilder.variables();
    }

    /**
     * Action creating a new test variable during a test.
     * @return
     */
    public CreateVariablesAction setVariable(String variableName, String value) {
        return testBuilder.setVariable(variableName, value);
    }

    /**
     * Adds a custom test action implementation.
     * @param testAction
     */
    public void action(TestAction testAction) {
        testBuilder.action(testAction);
    }

    /**
     * Applies test apply to test builder.
     * @param behavior
     */
    public void applyBehavior(TestBehavior behavior) {
        testBuilder.applyBehavior(behavior);
    }

    /**
     * Creates a new ANT run action definition
     * for further configuration.
     * @param buildFilePath
     * @return
     */
    public AntRunActionDefinition antrun(String buildFilePath) {
        return testBuilder.antrun(buildFilePath);
    }

    /**
     * Creates a new echo action.
     * @param message
     * @return
     */
    public EchoAction echo(String message) {
        return testBuilder.echo(message);
    }

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    public ExecutePLSQLActionDefinition plsql(DataSource dataSource) {
        return testBuilder.plsql(dataSource);
    }

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    public ExecuteSQLActionDefinition sql(DataSource dataSource) {
        return testBuilder.sql(dataSource);
    }

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    public ExecuteSQLQueryActionDefinition query(DataSource dataSource) {
        return testBuilder.query(dataSource);
    }

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     * @param messageEndpoint
     * @return
     */
    public ReceiveTimeoutActionDefinition expectTimeout(Endpoint messageEndpoint) {
        return testBuilder.expectTimeout(messageEndpoint);
    }

    /**
     * Creates a new receive timeout action definition from message endpoint name as String.
     * @param messageEndpointName
     * @return
     */
    public ReceiveTimeoutActionDefinition expectTimeout(String messageEndpointName) {
        return testBuilder.expectTimeout(messageEndpointName);
    }

    /**
     * Creates a new fail action.
     * @param message
     * @return
     */
    public FailAction fail(String message) {
        return testBuilder.fail(message);
    }

    /**
     * Creates a new input action.
     * @return
     */
    public InputActionDefinition input() {
        return testBuilder.input();
    }

    /**
     * Creates a new Java action definition from class name.
     * @param className
     * @return
     */
    public JavaActionDefinition java(String className) {
        return testBuilder.java(className);
    }

    /**
     * Creates a new Java action definition from Java class.
     * @param clazz
     * @return
     */
    public JavaActionDefinition java(Class<?> clazz) {
        return testBuilder.java(clazz);
    }

    /**
     * Creates a new Java action definition from Java object instance.
     * @param instance
     * @return
     */
    public JavaActionDefinition java(Object instance) {
        return testBuilder.java(instance);
    }

    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     * @return
     */
    public LoadPropertiesAction load(String filePath) {
        return testBuilder.load(filePath);
    }

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     * @param connectionFactory
     * @return
     */
    public PurgeJMSQueuesActionDefinition purgeQueues(ConnectionFactory connectionFactory) {
        return testBuilder.purgeQueues(connectionFactory);
    }

    /**
     * Purge queues using default connection factory.
     * @return
     */
    public PurgeJMSQueuesActionDefinition purgeQueues() {
        return testBuilder.purgeQueues();
    }

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     * @return
     */
    public PurgeMessageChannelActionDefinition purgeChannels() {
        return testBuilder.purgeChannels();
    }

    /**
     * Creates special SOAP receive message action definition with message receiver instance.
     * @param messageReceiver
     * @return
     * @deprecated
     */
    public ReceiveSoapMessageActionDefinition receive(SoapReplyMessageReceiver messageReceiver) {
        return testBuilder.receive(messageReceiver);
    }

    /**
     * Creates special SOAP receive message action definition with web service server instance.
     * @param server
     * @return
     */
    public ReceiveSoapMessageActionDefinition receive(WebServiceServer server) {
        return testBuilder.receive(server);
    }

    /**
     * Creates receive message action definition with message endpoint instance.
     * @param messageEndpoint
     * @return
     */
    public ReceiveMessageActionDefinition receive(Endpoint messageEndpoint) {
        return testBuilder.receive(messageEndpoint);
    }

    /**
     * Creates receive message action definition with messsage endpoint name.
     * @param messageEndpointName
     * @return
     */
    public ReceiveMessageActionDefinition receive(String messageEndpointName) {
        return testBuilder.receive(messageEndpointName);
    }

    /**
     * Create special SOAP send message action definition with message sender instance.
     * @param messageSender
     * @return
     */
    public SendSoapMessageActionDefinition send(WebServiceMessageSender messageSender) {
        return testBuilder.send(messageSender);
    }

    /**
     * Create special SOAP send message action definition with web service client instance.
     * @param client
     * @return
     */
    public SendSoapMessageActionDefinition send(WebServiceClient client) {
        return testBuilder.send(client);
    }

    /**
     * Create send message action definition with message endpoint instance.
     * @param messageEndpoint
     * @return
     */
    public SendMessageActionDefinition send(Endpoint messageEndpoint) {
        return testBuilder.send(messageEndpoint);
    }

    /**
     * Create send message action definition with message endpoint name. According to message endpoint type
     * we can create a SOAP specific message sending action.
     * @param messageEndpointName
     * @return
     */
    public SendMessageActionDefinition send(String messageEndpointName) {
        return testBuilder.send(messageEndpointName);
    }

    /**
     * Create SOAP fault send message action definition with message endpoint name. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     * @param messageEndpointName
     * @return
     */
    public SendSoapFaultActionDefinition sendSoapFault(String messageEndpointName) {
        return testBuilder.sendSoapFault(messageEndpointName);
    }

    /**
     * Create SOAP fault send message action definition with message endpoint instance. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     * @param messageEndpoint
     * @return
     */
    public SendSoapFaultActionDefinition sendSoapFault(Endpoint messageEndpoint) {
        return testBuilder.sendSoapFault(messageEndpoint);
    }

    /**
     * Add sleep action with default delay time.
     */
    public SleepAction sleep() {
        return testBuilder.sleep();
    }

    /**
     * Add sleep action with time in milliseconds.
     * @param time
     */
    public SleepAction sleep(long time) {
        return testBuilder.sleep(time);
    }

    /**
     * Add sleep action with time in seconds.
     * @param time
     */
    public SleepAction sleep(double time) {
        return testBuilder.sleep(time);
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     * @param servers
     * @return
     */
    public StartServerAction start(Server... servers) {
        return testBuilder.start(servers);
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     * @param server
     * @return
     */
    public StartServerAction start(Server server) {
        return testBuilder.start(server);
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     * @param servers
     * @return
     */
    public StopServerAction stop(Server... servers) {
        return testBuilder.stop(servers);
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     * @param server
     * @return
     */
    public StopServerAction stop(Server server) {
        return testBuilder.stop(server);
    }

    /**
     * Creates a new stop time action.
     * @return
     */
    public StopTimeAction stopTime() {
        return testBuilder.stopTime();
    }

    /**
     * Creates a new stop time action.
     * @param id
     * @return
     */
    public StopTimeAction stopTime(String id) {
        return testBuilder.stopTime(id);
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     * @return
     */
    public TraceVariablesAction traceVariables() {
        return testBuilder.traceVariables();
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     * @param variables
     * @return
     */
    public TraceVariablesAction traceVariables(String... variables) {
        return testBuilder.traceVariables(variables);
    }

    /**
     * Creates a new groovy action definition with
     * script code.
     * @param script
     * @return
     */
    public GroovyActionDefinition groovy(String script) {
        return testBuilder.groovy(script);
    }

    /**
     * Creates a new groovy action definition with
     * script file resource.
     * @param scriptResource
     * @return
     */
    public GroovyActionDefinition groovy(Resource scriptResource) {
        return testBuilder.groovy(scriptResource);
    }

    /**
     * Creates a new transform action definition
     * for further configuration.
     * @return
     */
    public TransformActionDefinition transform() {
        return testBuilder.transform();
    }

    /**
     * Assert exception to happen in nested test action.
     * @param testAction the nested testAction
     * @return
     */
    public AssertDefinition assertException(TestAction testAction) {
        return testBuilder.assertException(testAction);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     * @param exception the exception to be caught
     * @param actions nested test actions
     * @return
     */
    public Catch catchException(String exception, TestAction ... actions) {
        return testBuilder.catchException(exception, actions);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     * @param exception
     * @param actions
     * @return
     */
    public Catch catchException(Class<? extends Throwable> exception, TestAction ... actions) {
        return testBuilder.catchException(exception, actions);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     * @param actions
     * @return
     */
    public Catch catchException(TestAction ... actions) {
        return testBuilder.catchException(actions);
    }

    /**
     * Assert SOAP fault during action execution.
     *
     * @param testAction
     * @return
     */
    public AssertSoapFaultDefinition assertSoapFault(TestAction testAction) {
        return testBuilder.assertSoapFault(testAction);
    }

    /**
     * Adds conditional container with nested test actions.
     * @param actions
     * @return
     */
    public ConditionalDefinition conditional(TestAction ... actions) {
        return testBuilder.conditional(actions);
    }

    /**
     * Adds iterate container with nested test actions.
     * @param actions
     * @return
     */
    public IterateDefinition iterate(TestAction ... actions) {
        return testBuilder.iterate(actions);
    }

    /**
     * Adds parallel container with nested test actions.
     * @param actions
     * @return
     */
    public Parallel parallel(TestAction ... actions) {
        return testBuilder.parallel(actions);
    }

    /**
     * Adds repeat on error until true container with nested test actions.
     * @param actions
     * @return
     */
    public RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions) {
        return testBuilder.repeatOnError(actions);
    }

    /**
     * Adds repeat until true container with nested test actions.
     * @param actions
     * @return
     */
    public RepeatUntilTrueDefinition repeat(TestAction... actions) {
        return testBuilder.repeat(actions);
    }

    /**
     * Adds sequential container with nested test actions.
     * @param actions
     * @return
     */
    public Sequence sequential(TestAction ... actions) {
        return testBuilder.sequential(actions);
    }

    /**
     * Adds template container with nested test actions.
     * @param name
     * @return
     */
    public TemplateDefinition template(String name) {
        return testBuilder.template(name);
    }

    /**
     * Adds sequence of test actions to finally block.
     * @param actions
     */
    public void doFinally(TestAction ... actions) {
        testBuilder.doFinally(actions);
    }

    /**
     * Get the test variables.
     * @return
     */
    protected Map<String, Object> getVariables() {
        return testBuilder.getVariables();
    }

    @Override
    public TestCase getTestCase(TestContext context) {
        return testBuilder.getTestCase();
    }

}
