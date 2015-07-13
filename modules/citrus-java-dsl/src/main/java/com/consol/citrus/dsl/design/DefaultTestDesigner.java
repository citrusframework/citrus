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
import com.consol.citrus.container.*;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.*;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

/**
 * Default test builder offers builder pattern methods in order to configure a
 * test case with test actions, variables and properties.
 *
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class DefaultTestDesigner implements TestDesigner {

    /** This builders test case */
    private final TestCase testCase = new TestCase();

    /** The test variables to set before execution */
    private Map<String, Object> variables= new LinkedHashMap<>();

    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /** Default constructor */
    public DefaultTestDesigner() {
        testCase.setVariableDefinitions(variables);

        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     */
    public DefaultTestDesigner(ApplicationContext applicationContext) {
        this();

        try {
            if (applicationContext != null) {
                this.applicationContext = applicationContext;
                initialize();
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to setup test designer", e);
        }
    }

    protected void initialize() {
        testCase.setTestActionListeners(applicationContext.getBean(TestActionListeners.class));

        if (!applicationContext.getBeansOfType(SequenceBeforeTest.class).isEmpty()) {
            testCase.setBeforeTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceBeforeTest.class).values().toArray()));
        }

        if (!applicationContext.getBeansOfType(SequenceAfterTest.class).isEmpty()) {
            testCase.setAfterTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceAfterTest.class).values().toArray()));
        }
    }

    @Override
    public void name(String name) {
        getTestCase().setBeanName(name);
        getTestCase().setName(name);
    }

    @Override
    public void description(String description) {
        getTestCase().setDescription(description);
    }

    @Override
    public void author(String author) {
        getTestCase().getMetaInfo().setAuthor(author);
    }

    @Override
    public void packageName(String packageName) {
        getTestCase().setPackageName(packageName);
    }

    @Override
    public void status(TestCaseMetaInfo.Status status) {
        getTestCase().getMetaInfo().setStatus(status);
    }

    @Override
    public void creationDate(Date date) {
        getTestCase().getMetaInfo().setCreationDate(date);
    }

    @Override
    public void variable(String name, Object value) {
        getVariables().put(name, value);
    }

    @Override
    public CreateVariablesActionDefinition variables() {
        CreateVariablesActionDefinition definition = new CreateVariablesActionDefinition(new CreateVariablesAction());
        action(definition);
        return definition;
    }

    @Override
    public CreateVariablesAction setVariable(String variableName, String value) {
        return createVariable(variableName, value);
    }

    @Override
    public CreateVariablesAction createVariable(String variableName, String value) {
        CreateVariablesAction action = new CreateVariablesAction();
        action.getVariables().put(variableName, value);
        action(action);
        return action;
    }

    @Override
    public void applyBehavior(TestBehavior behavior) {
        behavior.setApplicationContext(getApplicationContext());
        behavior.apply(this);
    }

    @Override
    public void action(TestAction testAction) {
        List<TestAction> actions = null;
        if (testAction instanceof AbstractActionContainerDefinition) {
            actions = ((AbstractActionContainerDefinition) testAction).getActions();
        } else if (testAction instanceof AbstractActionContainer) {
            actions = ((AbstractActionContainer) testAction).getActions();
        }

        if (!CollectionUtils.isEmpty(actions)) {
            for (TestAction action : actions) {
                if (action instanceof AbstractActionDefinition<?>) {
                    testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                } else if (!action.getClass().isAnonymousClass()) {
                    testCase.getActions().remove(action);
                }
            }
        }

        if (testAction instanceof AbstractActionDefinition<?>) {
            testCase.addTestAction(((AbstractActionDefinition<?>) testAction).getAction());
        } else {
            testCase.addTestAction(testAction);
        }
    }

    @Override
    public AntRunActionDefinition antrun(String buildFilePath) {
        AntRunAction action = new AntRunAction();
        action.setBuildFilePath(buildFilePath);
        AntRunActionDefinition definition = new AntRunActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        action(action);
        return action;
    }

    @Override
    public ExecutePLSQLActionDefinition plsql(DataSource dataSource) {
        ExecutePLSQLAction action = new ExecutePLSQLAction();
        action.setDataSource(dataSource);
        ExecutePLSQLActionDefinition definition = new ExecutePLSQLActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public ExecuteSQLActionDefinition sql(DataSource dataSource) {
        ExecuteSQLAction action = new ExecuteSQLAction();
        action.setDataSource(dataSource);
        ExecuteSQLActionDefinition definition = new ExecuteSQLActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public ExecuteSQLQueryActionDefinition query(DataSource dataSource) {
        ExecuteSQLQueryAction action = new ExecuteSQLQueryAction();
        action.setDataSource(dataSource);
        ExecuteSQLQueryActionDefinition definition = new ExecuteSQLQueryActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public ReceiveTimeoutActionDefinition expectTimeout(Endpoint messageEndpoint) {
        return receiveTimeout(messageEndpoint);
    }

    @Override
    public ReceiveTimeoutActionDefinition expectTimeout(String messageEndpointUri) {
        return receiveTimeout(messageEndpointUri);
    }

    @Override
    public ReceiveTimeoutActionDefinition receiveTimeout(Endpoint messageEndpoint) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpoint(messageEndpoint);
        ReceiveTimeoutActionDefinition definition = new ReceiveTimeoutActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public ReceiveTimeoutActionDefinition receiveTimeout(String messageEndpointUri) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpointUri(messageEndpointUri);
        ReceiveTimeoutActionDefinition definition = new ReceiveTimeoutActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public FailAction fail(String message) {
        FailAction action = new FailAction();
        action.setMessage(message);
        action(action);
        return action;
    }

    @Override
    public InputActionDefinition input() {
        InputActionDefinition definition = new InputActionDefinition();
        action(definition);
        return definition;
    }

    @Override
    public JavaActionDefinition java(String className) {
        JavaAction action = new JavaAction();
        action.setClassName(className);
        JavaActionDefinition definition = new JavaActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public JavaActionDefinition java(Class<?> clazz) {
        JavaAction action = new JavaAction();
        action.setClassName(clazz.getSimpleName());
        JavaActionDefinition definition = new JavaActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public JavaActionDefinition java(Object instance) {
        JavaAction action = new JavaAction();
        action.setInstance(instance);
        JavaActionDefinition definition = new JavaActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = new LoadPropertiesAction();
        action.setFilePath(filePath);
        action(action);
        return action;
    }

    @Override
    public PurgeJmsQueueActionDefinition purgeQueues(ConnectionFactory connectionFactory) {
        PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
        action.setConnectionFactory(connectionFactory);
        PurgeJmsQueueActionDefinition definition = new PurgeJmsQueueActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public PurgeJmsQueueActionDefinition purgeQueues() {
        PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
        action.setConnectionFactory(getApplicationContext().getBean("connectionFactory", ConnectionFactory.class));
        PurgeJmsQueueActionDefinition definition = new PurgeJmsQueueActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public PurgeMessageChannelActionDefinition purgeChannels() {
        PurgeMessageChannelActionDefinition definition = new PurgeMessageChannelActionDefinition();
        definition.channelResolver(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public ReceiveSoapMessageActionDefinition receive(WebServiceServer server) {
        ReceiveSoapMessageAction action = new ReceiveSoapMessageAction();
        action.setEndpoint(server);
        ReceiveSoapMessageActionDefinition definition = new ReceiveSoapMessageActionDefinition(action)
                .withApplicationContext(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public ReceiveMessageActionDefinition receive(Endpoint messageEndpoint) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpoint(messageEndpoint);
        ReceiveMessageActionDefinition definition = new ReceiveMessageActionDefinition(action)
                .withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public ReceiveMessageActionDefinition receive(String messageEndpointUri) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpointUri(messageEndpointUri);
        ReceiveMessageActionDefinition definition = new ReceiveMessageActionDefinition(action)
                .withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public SendSoapMessageActionDefinition send(WebServiceClient client) {
        SendSoapMessageAction action = new SendSoapMessageAction();
        action.setEndpoint(client);
        SendSoapMessageActionDefinition definition = new SendSoapMessageActionDefinition(action)
                .withApplicationContext(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public SendMessageActionDefinition send(Endpoint messageEndpoint) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpoint(messageEndpoint);
        SendMessageActionDefinition definition = new SendMessageActionDefinition(action)
                .withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public SendMessageActionDefinition send(String messageEndpointUri) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpointUri(messageEndpointUri);
        SendMessageActionDefinition definition = new SendMessageActionDefinition(action)
                .withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public SendSoapFaultActionDefinition sendSoapFault(String messageEndpointUri) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpointUri(messageEndpointUri);
        SendSoapFaultActionDefinition definition = new SendSoapFaultActionDefinition(action)
                .withApplicationContext(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public SendSoapFaultActionDefinition sendSoapFault(Endpoint messageEndpoint) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpoint(messageEndpoint);
        SendSoapFaultActionDefinition definition = new SendSoapFaultActionDefinition(action)
                .withApplicationContext(getApplicationContext());

        action(definition);
        return definition;
    }

    @Override
    public SleepAction sleep() {
        SleepAction action = new SleepAction();
        action(action);
        return action;
    }

    @Override
    public SleepAction sleep(long milliseconds) {
        SleepAction action = new SleepAction();
        action.setMilliseconds(String.valueOf(milliseconds));
        action(action);
        return action;
    }

    @Override
    public SleepAction sleep(double seconds) {
        SleepAction action = new SleepAction();
        action.setSeconds(String.valueOf(seconds));
        action(action);
        return action;
    }

    @Override
    public StartServerAction start(Server... servers) {
        StartServerAction action = new StartServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        action(action);
        return action;
    }

    @Override
    public StartServerAction start(Server server) {
        StartServerAction action = new StartServerAction();
        action.setServer(server);
        action(action);
        return action;
    }

    @Override
    public StopServerAction stop(Server... servers) {
        StopServerAction action = new StopServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        action(action);
        return action;
    }

    @Override
    public StopServerAction stop(Server server) {
        StopServerAction action = new StopServerAction();
        action.setServer(server);
        action(action);
        return action;
    }

    @Override
    public StopTimeAction stopTime() {
        StopTimeAction action = new StopTimeAction();
        action(action);
        return action;
    }

    @Override
    public StopTimeAction stopTime(String id) {
        StopTimeAction action = new StopTimeAction();
        action.setId(id);
        action(action);
        return action;
    }

    @Override
    public TraceVariablesAction traceVariables() {
        TraceVariablesAction action = new TraceVariablesAction();
        action(action);
        return action;
    }

    @Override
    public TraceVariablesAction traceVariables(String... variables) {
        TraceVariablesAction action = new TraceVariablesAction();
        action.setVariableNames(Arrays.asList(variables));
        action(action);
        return action;
    }

    @Override
    public GroovyActionDefinition groovy(String script) {
        GroovyAction action = new GroovyAction();
        action.setScript(script);
        GroovyActionDefinition definition = new GroovyActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public GroovyActionDefinition groovy(Resource scriptResource) {
        GroovyAction action = new GroovyAction();
        try {
            action.setScript(FileUtils.readToString(scriptResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }
        GroovyActionDefinition definition = new GroovyActionDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public TransformActionDefinition transform() {
        TransformActionDefinition definition = new TransformActionDefinition();
        action(definition);
        return definition;
    }

    @Override
    public AssertDefinition assertException(TestAction testAction) {
        Assert action = new Assert();

        if (testAction instanceof AbstractActionDefinition<?>) {
            action.setAction(((AbstractActionDefinition<?>) testAction).getAction());
        } else {
            action.setAction(testAction);
        }
        AssertDefinition definition = new AssertDefinition(action);
        action(definition);
        return definition;
    }

    @Override
    public CatchDefinition catchException(TestAction... actions) {
        Catch container = new Catch();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }
        CatchDefinition definition = new CatchDefinition(container)
                .exception(CitrusRuntimeException.class.getName());
        action(definition);
        return definition;
    }

    @Override
    public AssertSoapFaultDefinition assertSoapFault(TestAction testAction) {
        AssertSoapFault action = new AssertSoapFault();

        if (testAction instanceof AbstractActionDefinition<?>) {
            action.setAction(((AbstractActionDefinition<?>) testAction).getAction());
        } else {
            action.setAction(testAction);
        }
        AssertSoapFaultDefinition definition = new AssertSoapFaultDefinition(action);

        if (getApplicationContext().containsBean("soapFaultValidator")) {
            definition.validator(getApplicationContext().getBean("soapFaultValidator", SoapFaultValidator.class));
        }

        action(definition);
        return definition;
    }

    @Override
    public ConditionalDefinition conditional(TestAction... actions) {
        Conditional container = new Conditional();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        ConditionalDefinition definition = new ConditionalDefinition(container);
        action(definition);
        return definition;
    }

    @Override
    public IterateDefinition iterate(TestAction... actions) {
        Iterate container = new Iterate();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }
        IterateDefinition definition = new IterateDefinition(container);
        action(definition);
        return definition;
    }

    @Override
    public Parallel parallel(TestAction... actions) {
        Parallel container = new Parallel();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }
        action(container);
        return container;
    }

    @Override
    public RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions) {
        RepeatOnErrorUntilTrue container = new RepeatOnErrorUntilTrue();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }
        RepeatOnErrorUntilTrueDefinition definition = new RepeatOnErrorUntilTrueDefinition(container);
        action(definition);
        return definition;
    }

    @Override
    public RepeatUntilTrueDefinition repeat(TestAction... actions) {
        RepeatUntilTrue container = new RepeatUntilTrue();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }
        RepeatUntilTrueDefinition definition = new RepeatUntilTrueDefinition(container);
        action(definition);
        return definition;
    }

    @Override
    public Sequence sequential(TestAction... actions) {
        Sequence container = new Sequence();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }
        action(container);
        return container;
    }

    @Override
    public TemplateDefinition template(String name) {
        return applyTemplate(name);
    }

    @Override
    public TemplateDefinition applyTemplate(String name) {
        Template template = new Template();
        template.setName(name);
        TemplateDefinition definition = new TemplateDefinition(template)
                .load(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public void doFinally(TestAction... actions) {
        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                getTestCase().getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                getTestCase().getFinalActions().add(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                getTestCase().getActions().remove(action);
                getTestCase().getFinalActions().add(action);
            } else {
                getTestCase().getFinalActions().add(action);
            }
        }
    }

    @Override
    public PositionHandle positionHandle() {
        return new PositionHandle(testCase.getActions());
    }

    /**
     * Gets the test case.
     * @return
     */
    protected TestCase getTestCase() {
        return testCase;
    }

    /**
     * Builds the test case.
     * @return
     */
    public TestCase build() {
        return testCase;
    }

    /**
     * Get the test variables.
     * @return
     */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Gets the Spring bean application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Sets the application context either from ApplicationContextAware injection or from outside.
     * @param applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
