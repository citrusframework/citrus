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
import com.consol.citrus.dsl.*;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
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
        CreateVariablesActionDefinition definition = TestActions.createVariables();
        action(definition);
        return definition;
    }

    @Override
    public CreateVariablesAction setVariable(String variableName, String value) {
        return createVariable(variableName, value);
    }

    @Override
    public CreateVariablesAction createVariable(String variableName, String value) {
        CreateVariablesAction action = TestActions.createVariable(variableName, value);
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
        AntRunActionDefinition definition = TestActions.antrun(buildFilePath);
        action(definition);
        return definition;
    }

    @Override
    public EchoAction echo(String message) {
        EchoAction action = TestActions.echo(message);
        action(action);
        return action;
    }

    @Override
    public ExecutePLSQLActionDefinition plsql(DataSource dataSource) {
        ExecutePLSQLActionDefinition definition = TestActions.plsql(dataSource);
        action(definition);
        return definition;
    }

    @Override
    public ExecuteSQLActionDefinition sql(DataSource dataSource) {
        ExecuteSQLActionDefinition definition = TestActions.sql(dataSource);
        action(definition);
        return definition;
    }

    @Override
    public ExecuteSQLQueryActionDefinition query(DataSource dataSource) {
        ExecuteSQLQueryActionDefinition definition = TestActions.query(dataSource);
        action(definition);
        return definition;
    }

    @Override
    public ReceiveTimeoutActionDefinition expectTimeout(Endpoint messageEndpoint) {
        ReceiveTimeoutActionDefinition definition = TestActions.expectTimeout(messageEndpoint);
        action(definition);
        return definition;
    }

    @Override
    public ReceiveTimeoutActionDefinition expectTimeout(String messageEndpointUri) {
        ReceiveTimeoutActionDefinition definition = TestActions.expectTimeout(messageEndpointUri);
        action(definition);
        return definition;
    }

    @Override
    public FailAction fail(String message) {
        FailAction action = TestActions.fail(message);
        action(action);
        return action;
    }

    @Override
    public InputActionDefinition input() {
        InputActionDefinition definition = TestActions.input();
        action(definition);
        return definition;
    }

    @Override
    public JavaActionDefinition java(String className) {
        JavaActionDefinition definition = TestActions.java(className);
        action(definition);
        return definition;
    }

    @Override
    public JavaActionDefinition java(Class<?> clazz) {
        JavaActionDefinition definition = TestActions.java(clazz);
        action(definition);
        return definition;
    }

    @Override
    public JavaActionDefinition java(Object instance) {
        JavaActionDefinition definition = TestActions.java(instance);
        action(definition);
        return definition;
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = TestActions.load(filePath);
        action(action);
        return action;
    }

    @Override
    public PurgeJmsQueueActionDefinition purgeQueues(ConnectionFactory connectionFactory) {
        PurgeJmsQueueActionDefinition definition = TestActions.purgeQueues(connectionFactory);
        action(definition);
        return definition;
    }

    @Override
    public PurgeJmsQueueActionDefinition purgeQueues() {
        PurgeJmsQueueActionDefinition definition = TestActions.purgeQueues(getApplicationContext().getBean("connectionFactory", ConnectionFactory.class));
        action(definition);
        return definition;
    }

    @Override
    public PurgeMessageChannelActionDefinition purgeChannels() {
        PurgeMessageChannelActionDefinition definition = TestActions.purgeChannels().channelResolver(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public ReceiveSoapMessageActionDefinition receive(WebServiceServer server) {
        ReceiveSoapMessageActionDefinition definition = TestActions.receive(server).withApplicationContext(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public ReceiveMessageActionDefinition receive(Endpoint messageEndpoint) {
        ReceiveMessageActionDefinition definition = TestActions.receive(messageEndpoint).withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public ReceiveMessageActionDefinition receive(String messageEndpointUri) {
        ReceiveMessageActionDefinition definition = TestActions.receive(messageEndpointUri).withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public SendSoapMessageActionDefinition send(WebServiceClient client) {
        SendSoapMessageActionDefinition definition = TestActions.send(client).withApplicationContext(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public SendMessageActionDefinition send(Endpoint messageEndpoint) {
        SendMessageActionDefinition definition = TestActions.send(messageEndpoint).withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public SendMessageActionDefinition send(String messageEndpointUri) {
        SendMessageActionDefinition definition = TestActions.send(messageEndpointUri).withApplicationContext(getApplicationContext());
        action(definition);

        definition.position(positionHandle());
        return definition;
    }

    @Override
    public SendSoapFaultActionDefinition sendSoapFault(String messageEndpointUri) {
        SendSoapFaultActionDefinition definition = TestActions.sendSoapFault(messageEndpointUri).withApplicationContext(getApplicationContext());
        action(definition);
        return definition;
    }

    @Override
    public SendSoapFaultActionDefinition sendSoapFault(Endpoint messageEndpoint) {
        SendSoapFaultActionDefinition definition = TestActions.sendSoapFault(messageEndpoint).withApplicationContext(getApplicationContext());

        action(definition);
        return definition;
    }

    @Override
    public SleepAction sleep() {
        SleepAction action = TestActions.sleep();
        action(action);
        return action;
    }

    @Override
    public SleepAction sleep(long milliseconds) {
        SleepAction action = TestActions.sleep(milliseconds);
        action(action);
        return action;
    }

    @Override
    public SleepAction sleep(double seconds) {
        SleepAction action = TestActions.sleep(seconds);
        action(action);
        return action;
    }

    @Override
    public StartServerAction start(Server... servers) {
        StartServerAction action = TestActions.start(servers);
        action(action);
        return action;
    }

    @Override
    public StartServerAction start(Server server) {
        StartServerAction action = TestActions.start(server);
        action(action);
        return action;
    }

    @Override
    public StopServerAction stop(Server... servers) {
        StopServerAction action = TestActions.stop(servers);
        action(action);
        return action;
    }

    @Override
    public StopServerAction stop(Server server) {
        StopServerAction action = TestActions.stop(server);
        action(action);
        return action;
    }

    @Override
    public StopTimeAction stopTime() {
        StopTimeAction action = TestActions.stopTime();
        action(action);
        return action;
    }

    @Override
    public StopTimeAction stopTime(String id) {
        StopTimeAction action = TestActions.stopTime(id);
        action(action);
        return action;
    }

    @Override
    public TraceVariablesAction traceVariables() {
        TraceVariablesAction action = TestActions.traceVariables();
        action(action);
        return action;
    }

    @Override
    public TraceVariablesAction traceVariables(String... variables) {
        TraceVariablesAction action = TestActions.traceVariables(variables);
        action(action);
        return action;
    }

    @Override
    public GroovyActionDefinition groovy(String script) {
        GroovyActionDefinition definition = TestActions.groovy(script);
        action(definition);
        return definition;
    }

    @Override
    public GroovyActionDefinition groovy(Resource scriptResource) {
        GroovyActionDefinition definition = TestActions.groovy(scriptResource);
        action(definition);
        return definition;
    }

    @Override
    public TransformActionDefinition transform() {
        TransformActionDefinition definition = TestActions.transform();
        action(definition);
        return definition;
    }

    @Override
    public AssertDefinition assertException(TestAction testAction) {
        AssertDefinition definition = TestActions.assertException(testAction);
        action(definition);
        return definition;
    }

    @Override
    public CatchDefinition catchException(TestAction... actions) {
        CatchDefinition definition = TestActions.catchException(actions);
        definition.exception(CitrusRuntimeException.class.getName());
        action(definition);
        return definition;
    }

    @Override
    public AssertSoapFaultDefinition assertSoapFault(TestAction testAction) {
        AssertSoapFaultDefinition definition = TestActions.assertSoapFault(testAction);

        if (getApplicationContext().containsBean("soapFaultValidator")) {
            definition.validator(getApplicationContext().getBean("soapFaultValidator", SoapFaultValidator.class));
        }

        action(definition);
        return definition;
    }

    @Override
    public ConditionalDefinition conditional(TestAction... actions) {
        ConditionalDefinition container = TestActions.conditional(actions);
        action(container);
        return container;
    }

    @Override
    public IterateDefinition iterate(TestAction... actions) {
        IterateDefinition container = TestActions.iterate(actions);
        action(container);
        return container;
    }

    @Override
    public Parallel parallel(TestAction... actions) {
        Parallel container = TestActions.parallel(actions);
        action(container);
        return container;
    }

    @Override
    public RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions) {
        RepeatOnErrorUntilTrueDefinition container = TestActions.repeatOnError(actions);
        action(container);
        return container;
    }

    @Override
    public RepeatUntilTrueDefinition repeat(TestAction... actions) {
        RepeatUntilTrueDefinition container = TestActions.repeat(actions);
        action(container);
        return container;
    }

    @Override
    public Sequence sequential(TestAction... actions) {
        Sequence container = TestActions.sequential(actions);
        action(container);
        return container;
    }

    @Override
    public TemplateDefinition template(String name) {
        return applyTemplate(name);
    }

    @Override
    public TemplateDefinition applyTemplate(String name) {
        TemplateDefinition template = TestActions.template(name).load(getApplicationContext());
        action(template);
        return template;
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
