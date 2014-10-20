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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.*;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

/**
 * Citrus test builder offers builder pattern methods in order to configure a
 * test case with test actions, variables and properties.
 *
 * Subclass may add its custom logic in configure() method by calling builder methods.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class CitrusTestBuilder implements TestBuilder, InitializingBean {

    /** This builders test case */
    private TestCase testCase;

    /** The test variables to set before execution */
    private Map<String, Object> variables;

    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /** Default constructor */
    public CitrusTestBuilder() {
        init();
    }

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     */
    public CitrusTestBuilder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        init();

        try {
            if (applicationContext != null) {
                afterPropertiesSet();
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to setup test builder with application context", e);
        }
    }

    /**
     * Build an execute test case with new test context generated from Spring bean application context.
     * If no Spring bean application context is set an exception is raised. Users may want to create proper test context
     * instance themselves in case Spring application context is not present. Otherwise set application context before execution properly.
     */
    public void execute() {
        if (applicationContext == null) {
            throw new CitrusRuntimeException("Unable to create test context for test builder execution without Spring bean application context set properly");
        }

        TestContext context = applicationContext.getBean(TestContext.class);
        context.setApplicationContext(applicationContext);
        execute(context);
    }

    /**
     * Build and execute test case with predefined test context. In case Spring bean application context is available with proper TestContextBeanFactory
     * you can also let the builder generate a new test context on the fly.
     * @param context
     */
    public void execute(TestContext context) {
        configure();
        getTestCase().execute(context);
    }

    /**
     * Initializing method.
     */
    public final void init() {
        variables = new LinkedHashMap<String, Object>();
        testCase = new TestCase();
        testCase.setVariableDefinitions(variables);

        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
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
        testCase.setBeanName(name);
        testCase.setName(name);
    }

    /**
     * Adds description to the test case.
     *
     * @param description
     */
    public void description(String description) {
        testCase.setDescription(description);
    }

    /**
     * Adds author to the test case.
     *
     * @param author
     */
    public void author(String author) {
        testCase.getMetaInfo().setAuthor(author);
    }

    /**
     * Sets custom package name for this test case.
     * @param packageName
     */
    public void packageName(String packageName) {
        testCase.setPackageName(packageName);
    }

    /**
     * Sets test case status.
     *
     * @param status
     */
    public void status(TestCaseMetaInfo.Status status) {
        testCase.getMetaInfo().setStatus(status);
    }

    /**
     * Sets the creation date.
     *
     * @param date
     */
    public void creationDate(Date date) {
        testCase.getMetaInfo().setCreationDate(date);
    }

    /**
     * Adds a new variable definition to the set of test variables
     * for this test case.
     *
     * @param name
     * @param value
     */
    public void variable(String name, Object value) {
        variables.put(name, value);
    }

    /**
     * Action creating new test variables during a test.
     *
     * @return
     */
    public CreateVariablesActionDefinition variables() {
        CreateVariablesAction action = new CreateVariablesAction();

        testCase.addTestAction(action);

        return new CreateVariablesActionDefinition(action);
    }

    /**
     * Action creating a new test variable during a test.
     *
     * @return
     */
    public CreateVariablesAction setVariable(String variableName, String value) {
        CreateVariablesAction action = new CreateVariablesAction();
        action.getVariables().put(variableName, value);
        testCase.addTestAction(action);

        return action;
    }

    /**
     * Adds a custom test action implementation.
     *
     * @param testAction
     */
    public void action(TestAction testAction) {
        testCase.addTestAction(testAction);
    }

    /**
     * Apply test apply to this builder.
     *
     * @param behavior
     */
    public void applyBehavior(TestBehavior behavior) {
        behavior.setApplicationContext(applicationContext);
        behavior.init();
        behavior.apply();

        for (Map.Entry<String, Object> variable : behavior.getVariableDefinitions().entrySet()) {
            variable(variable.getKey(), variable.getValue());
        }

        for (TestAction action : behavior.getTestActions()) {
            action(action);
        }

        for (TestAction action : behavior.getFinallyActions()) {
            testCase.getFinallyChain().add(action);
        }
    }

    /**
     * Creates a new ANT run action definition
     * for further configuration.
     *
     * @param buildFilePath
     * @return
     */
    public AntRunActionDefinition antrun(String buildFilePath) {
        AntRunAction action = new AntRunAction();
        action.setBuildFilePath(buildFilePath);
        testCase.addTestAction(action);
        return new AntRunActionDefinition(action);
    }

    /**
     * Creates a new echo action.
     *
     * @param message
     * @return
     */
    public EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        testCase.addTestAction(action);

        return action;
    }

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    public ExecutePLSQLActionDefinition plsql(DataSource dataSource) {
        ExecutePLSQLAction action = new ExecutePLSQLAction();
        action.setDataSource(dataSource);
        testCase.addTestAction(action);
        return new ExecutePLSQLActionDefinition(action);
    }

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    public ExecuteSQLActionDefinition sql(DataSource dataSource) {
        ExecuteSQLAction action = new ExecuteSQLAction();
        action.setDataSource(dataSource);
        testCase.addTestAction(action);
        return new ExecuteSQLActionDefinition(action);
    }

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    public ExecuteSQLQueryActionDefinition query(DataSource dataSource) {
        ExecuteSQLQueryAction action = new ExecuteSQLQueryAction();
        action.setDataSource(dataSource);
        testCase.addTestAction(action);
        return new ExecuteSQLQueryActionDefinition(action);
    }

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param messageEndpoint
     * @return
     */
    public ReceiveTimeoutActionDefinition expectTimeout(Endpoint messageEndpoint) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpoint(messageEndpoint);
        testCase.addTestAction(action);
        return new ReceiveTimeoutActionDefinition(action);
    }

    /**
     * Creates a new receive timeout action definition from message endpoint name as String.
     *
     * @param messageEndpointUri
     * @return
     */
    public ReceiveTimeoutActionDefinition expectTimeout(String messageEndpointUri) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpointUri(messageEndpointUri);
        testCase.addTestAction(action);
        return new ReceiveTimeoutActionDefinition(action);
    }

    /**
     * Creates a new fail action.
     *
     * @param message
     * @return
     */
    public FailAction fail(String message) {
        FailAction action = new FailAction();
        action.setMessage(message);
        testCase.addTestAction(action);

        return action;
    }

    /**
     * Creates a new input action.
     *
     * @return
     */
    public InputActionDefinition input() {
        InputAction action = new InputAction();
        testCase.addTestAction(action);
        return new InputActionDefinition(action);
    }

    /**
     * Creates a new Java action definition from class name.
     *
     * @param className
     * @return
     */
    public JavaActionDefinition java(String className) {
        JavaAction action = new JavaAction();
        action.setClassName(className);
        testCase.addTestAction(action);
        return new JavaActionDefinition(action);
    }

    /**
     * Creates a new Java action definition from Java class.
     *
     * @param clazz
     * @return
     */
    public JavaActionDefinition java(Class<?> clazz) {
        JavaAction action = new JavaAction();
        action.setClassName(clazz.getSimpleName());
        testCase.addTestAction(action);
        return new JavaActionDefinition(action);
    }

    /**
     * Creates a new Java action definition from Java object instance.
     *
     * @param instance
     * @return
     */
    public JavaActionDefinition java(Object instance) {
        JavaAction action = new JavaAction();
        action.setInstance(instance);
        testCase.addTestAction(action);
        return new JavaActionDefinition(action);
    }

    /**
     * Creates a new load properties action.
     *
     * @param filePath path to properties file.
     * @return
     */
    public LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = new LoadPropertiesAction();
        action.setFilePath(filePath);
        testCase.addTestAction(action);
        return action;
    }

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @param connectionFactory
     * @return
     */
    public PurgeJMSQueuesActionDefinition purgeQueues(ConnectionFactory connectionFactory) {
        PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
        action.setConnectionFactory(connectionFactory);
        testCase.addTestAction(action);
        return new PurgeJMSQueuesActionDefinition(action);
    }

    /**
     * Purge queues using default connection factory.
     *
     * @return
     */
    public PurgeJMSQueuesActionDefinition purgeQueues() {
        PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
        action.setConnectionFactory(applicationContext.getBean("connectionFactory", ConnectionFactory.class));
        testCase.addTestAction(action);
        return new PurgeJMSQueuesActionDefinition(action);
    }

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @return
     */
    public PurgeMessageChannelActionDefinition purgeChannels() {
        PurgeMessageChannelAction action = new PurgeMessageChannelAction();
        testCase.addTestAction(action);
        return new PurgeMessageChannelActionDefinition(action, applicationContext);
    }

    /**
     * Creates special SOAP receive message action definition with web service server instance.
     *
     * @param server
     * @return
     */
    public ReceiveSoapMessageActionDefinition receive(WebServiceServer server) {
        ReceiveSoapMessageAction action = new ReceiveSoapMessageAction();
        action.setEndpoint(server);

        testCase.addTestAction(action);
        return new ReceiveSoapMessageActionDefinition(action, applicationContext);
    }

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    public ReceiveMessageActionDefinition receive(Endpoint messageEndpoint) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpoint(messageEndpoint);

        testCase.addTestAction(action);
        return new ReceiveMessageActionDefinition(action, applicationContext, new PositionHandle(testCase.getActions()));
    }

    /**
     * Creates receive message action definition with message endpoint name.
     *
     * @param messageEndpointUri
     * @return
     */
    public ReceiveMessageActionDefinition receive(String messageEndpointUri) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpointUri(messageEndpointUri);

        testCase.addTestAction(action);
        return new ReceiveMessageActionDefinition(action, applicationContext, new PositionHandle(testCase.getActions()));
    }

    /**
     * Create special SOAP send message action definition with web service client instance.
     *
     * @param client
     * @return
     */
    public SendSoapMessageActionDefinition send(WebServiceClient client) {
        SendSoapMessageAction action = new SendSoapMessageAction();
        action.setEndpoint(client);

        testCase.addTestAction(action);
        return new SendSoapMessageActionDefinition(action, applicationContext);
    }

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    public SendMessageActionDefinition send(Endpoint messageEndpoint) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpoint(messageEndpoint);

        testCase.addTestAction(action);
        return new SendMessageActionDefinition<SendMessageAction, SendMessageActionDefinition>(action, applicationContext, new PositionHandle(testCase.getActions()));
    }

    /**
     * Create send message action definition with message endpoint name. According to message endpoint type
     * we can create a SOAP specific message sending action.
     *
     * @param messageEndpointUri
     * @return
     */
    public SendMessageActionDefinition send(String messageEndpointUri) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpointUri(messageEndpointUri);

        testCase.addTestAction(action);
        return new SendMessageActionDefinition<SendMessageAction, SendMessageActionDefinition>(action, applicationContext, new PositionHandle(testCase.getActions()));
    }

    /**
     * Create SOAP fault send message action definition with message endpoint name. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpointUri
     * @return
     */
    public SendSoapFaultActionDefinition sendSoapFault(String messageEndpointUri) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpointUri(messageEndpointUri);

        testCase.addTestAction(action);
        return new SendSoapFaultActionDefinition(action, applicationContext);
    }

    /**
     * Create SOAP fault send message action definition with message endpoint instance. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpoint
     * @return
     */
    public SendSoapFaultActionDefinition sendSoapFault(Endpoint messageEndpoint) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpoint(messageEndpoint);

        testCase.addTestAction(action);
        return new SendSoapFaultActionDefinition(action, applicationContext);
    }

    /**
     * Add sleep action with default delay time.
     */
    public SleepAction sleep() {
        SleepAction action = new SleepAction();
        testCase.addTestAction(action);

        return action;
    }

    /**
     * Add sleep action with time in milliseconds.
     *
     * @param milliseconds
     */
    public SleepAction sleep(long milliseconds) {
        SleepAction action = new SleepAction();
        action.setMilliseconds(String.valueOf(milliseconds));

        testCase.addTestAction(action);

        return action;
    }

    /**
     * Add sleep action with time in seconds.
     *
     * @param seconds
     */
    public SleepAction sleep(double seconds) {
        SleepAction action = new SleepAction();
        action.setSeconds(String.valueOf(seconds));

        testCase.addTestAction(action);

        return action;
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    public StartServerAction start(Server... servers) {
        StartServerAction action = new StartServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        testCase.addTestAction(action);
        return action;
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    public StartServerAction start(Server server) {
        StartServerAction action = new StartServerAction();
        action.setServer(server);
        testCase.addTestAction(action);
        return action;
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    public StopServerAction stop(Server... servers) {
        StopServerAction action = new StopServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        testCase.addTestAction(action);
        return action;
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    public StopServerAction stop(Server server) {
        StopServerAction action = new StopServerAction();
        action.setServer(server);
        testCase.addTestAction(action);
        return action;
    }

    /**
     * Creates a new stop time action.
     *
     * @return
     */
    public StopTimeAction stopTime() {
        StopTimeAction action = new StopTimeAction();
        testCase.addTestAction(action);
        return new StopTimeAction();
    }

    /**
     * Creates a new stop time action.
     *
     * @param id
     * @return
     */
    public StopTimeAction stopTime(String id) {
        StopTimeAction action = new StopTimeAction();
        action.setId(id);
        testCase.addTestAction(action);
        return new StopTimeAction();
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @return
     */
    public TraceVariablesAction traceVariables() {
        TraceVariablesAction action = new TraceVariablesAction();

        testCase.addTestAction(action);
        return action;
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @param variables
     * @return
     */
    public TraceVariablesAction traceVariables(String... variables) {
        TraceVariablesAction action = new TraceVariablesAction();
        action.setVariableNames(Arrays.asList(variables));

        testCase.addTestAction(action);
        return action;
    }

    /**
     * Creates a new groovy action definition with
     * script code.
     *
     * @param script
     * @return
     */
    public GroovyActionDefinition groovy(String script) {
        GroovyAction action = new GroovyAction();
        action.setScript(script);

        testCase.addTestAction(action);

        return new GroovyActionDefinition(action);
    }

    /**
     * Creates a new groovy action definition with
     * script file resource.
     *
     * @param scriptResource
     * @return
     */
    public GroovyActionDefinition groovy(Resource scriptResource) {
        GroovyAction action = new GroovyAction();
        try {
            action.setScript(FileUtils.readToString(scriptResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }

        testCase.addTestAction(action);

        return new GroovyActionDefinition(action);
    }

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @return
     */
    public TransformActionDefinition transform() {
        TransformAction action = new TransformAction();
        testCase.addTestAction(action);
        return new TransformActionDefinition(action);
    }

    /**
     * Assert exception to happen in nested test action.
     *
     * @param testAction the nested testAction
     * @return
     */
    public AssertDefinition assertException(TestAction testAction) {
        Assert action = new Assert();

        if (testAction instanceof AbstractActionDefinition<?>) {
            action.setAction(((AbstractActionDefinition<?>) testAction).getAction());
        } else {
            action.setAction(testAction);
        }

        if (!testAction.getClass().isAnonymousClass()) {
          testCase.getActions().remove((testCase.getActions().size()) - 1);
        }

        testCase.addTestAction(action);

        return new AssertDefinition(action);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param exception the exception to be caught
     * @param actions   nested test actions
     * @return
     */
    public Catch catchException(String exception, TestAction... actions) {
        Catch container = new Catch();
        container.setException(exception);

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            } else {
                container.addTestAction(action);
            }
        }

        testCase.getActions().add(container);

        return container;
    }

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param exception
     * @param actions
     * @return
     */
    public Catch catchException(Class<? extends Throwable> exception, TestAction... actions) {
        return catchException(exception.getName(), actions);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param actions
     * @return
     */
    public Catch catchException(TestAction... actions) {
        return catchException(CitrusRuntimeException.class.getName(), actions);
    }

    /**
     * Assert SOAP fault during action execution.
     *
     * @param testAction
     * @return
     */
    public AssertSoapFaultDefinition assertSoapFault(TestAction testAction) {
        AssertSoapFault action = new AssertSoapFault();

        if (testAction instanceof AbstractActionDefinition<?>) {
            action.setAction(((AbstractActionDefinition<?>) testAction).getAction());
        } else {
            action.setAction(testAction);
        }

        if (!testAction.getClass().isAnonymousClass()) {
            testCase.getActions().remove((testCase.getActions().size()) - 1);
        }

        testCase.addTestAction(action);

        return new AssertSoapFaultDefinition(action, applicationContext);
    }

    /**
     * Adds conditional container with nested test actions.
     *
     * @param actions
     * @return
     */
    public ConditionalDefinition conditional(TestAction... actions) {
        Conditional container = new Conditional();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            } else {
                container.addTestAction(action);
            }
        }

        testCase.getActions().add(container);

        return new ConditionalDefinition(container);
    }

    /**
     * Adds iterate container with nested test actions.
     *
     * @param actions
     * @return
     */
    public IterateDefinition iterate(TestAction... actions) {
        Iterate container = new Iterate();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            } else {
                container.addTestAction(action);
            }
        }

        testCase.getActions().add(container);

        return new IterateDefinition(container);
    }

    /**
     * Adds parallel container with nested test actions.
     *
     * @param actions
     * @return
     */
    public Parallel parallel(TestAction... actions) {
        Parallel container = new Parallel();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            } else {
                container.addTestAction(action);
            }
        }

        testCase.getActions().add(container);

        return container;
    }

    /**
     * Adds repeat on error until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    public RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions) {
        RepeatOnErrorUntilTrue container = new RepeatOnErrorUntilTrue();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            } else {
                container.addTestAction(action);
            }
        }

        testCase.addTestAction(container);
        return new RepeatOnErrorUntilTrueDefinition(container);
    }

    /**
     * Adds repeat until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    public RepeatUntilTrueDefinition repeat(TestAction... actions) {
        RepeatUntilTrue container = new RepeatUntilTrue();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            } else {
                container.addTestAction(action);
            }
        }

        testCase.addTestAction(container);
        return new RepeatUntilTrueDefinition(container);
    }

    /**
     * Adds sequential container with nested test actions.
     *
     * @param actions
     * @return
     */
    public Sequence sequential(TestAction... actions) {
        Sequence container = new Sequence();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            } else {
                container.addTestAction(action);
            }
        }

        testCase.getActions().add(container);

        return container;
    }

    /**
     * Adds template container with nested test actions.
     *
     * @param name
     * @return
     */
    public TemplateDefinition template(String name) {
        Template template = new Template();
        template.setName(name);

        Template rootTemplate = applicationContext.getBean(name, Template.class);

        template.setGlobalContext(rootTemplate.isGlobalContext());
        template.setActor(rootTemplate.getActor());
        template.setActions(rootTemplate.getActions());
        template.setParameter(rootTemplate.getParameter());

        testCase.addTestAction(template);
        return new TemplateDefinition(template);
    }

    /**
     * Adds sequence of test actions to finally block.
     *
     * @param actions
     */
    public void doFinally(TestAction... actions) {
        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                testCase.getFinallyChain().add(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                testCase.getActions().remove(action);
                testCase.getFinallyChain().add(action);
            } else {
                testCase.getFinallyChain().add(action);
            }
        }
    }

    /**
     * Gets the testCase.
     *
     * @return the testCase the testCase to get.
     */
    public TestCase getTestCase() {
        return testCase;
    }

    /**
     * Get the test variables.
     *
     * @return
     */
    protected Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Sets the application context either from ApplicationContextAware injection or from outside.
     * @param applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Called after Spring application context was set.
     * @throws Exception
     */
    public final void afterPropertiesSet() throws Exception {
        testCase.setTestListeners(applicationContext.getBean(TestListeners.class));
        testCase.setTestActionListeners(applicationContext.getBean(TestActionListeners.class));

        if (!applicationContext.getBeansOfType(SequenceBeforeTest.class).isEmpty()) {
            testCase.setBeforeTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceBeforeTest.class).values().toArray()));
        }

        if (!applicationContext.getBeansOfType(SequenceAfterTest.class).isEmpty()) {
            testCase.setAfterTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceAfterTest.class).values().toArray()));
        }
    }
}
