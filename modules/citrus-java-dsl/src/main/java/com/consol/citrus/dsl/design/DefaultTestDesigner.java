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
import com.consol.citrus.dsl.builder.*;
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
 * @since 2.3
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
        if (testAction instanceof TestActionContainerBuilder) {
            actions = ((TestActionContainerBuilder) testAction).getActions();
        } else if (testAction instanceof TestActionContainer) {
            actions = ((TestActionContainer) testAction).getActions();
        }

        if (!CollectionUtils.isEmpty(actions)) {
            for (TestAction action : actions) {
                if (action instanceof TestActionBuilder<?>) {
                    testCase.getActions().remove(((TestActionBuilder<?>) action).build());
                } else if (!action.getClass().isAnonymousClass()) {
                    testCase.getActions().remove(action);
                }
            }
        }

        if (testAction instanceof TestActionBuilder<?>) {
            testCase.addTestAction(((TestActionBuilder<?>) testAction).build());
        } else {
            testCase.addTestAction(testAction);
        }
    }

    @Override
    public AntRunBuilder antrun(String buildFilePath) {
        AntRunAction action = new AntRunAction();
        action.setBuildFilePath(buildFilePath);
        AntRunBuilder builder = new AntRunBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        action(action);
        return action;
    }

    @Override
    public ExecutePLSQLBuilder plsql(DataSource dataSource) {
        ExecutePLSQLAction action = new ExecutePLSQLAction();
        action.setDataSource(dataSource);
        ExecutePLSQLBuilder builder = new ExecutePLSQLBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public ExecuteSQLBuilder sql(DataSource dataSource) {
        ExecuteSQLAction action = new ExecuteSQLAction();
        action.setDataSource(dataSource);
        ExecuteSQLBuilder builder = new ExecuteSQLBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public ExecuteSQLQueryBuilder query(DataSource dataSource) {
        ExecuteSQLQueryAction action = new ExecuteSQLQueryAction();
        action.setDataSource(dataSource);
        ExecuteSQLQueryBuilder builder = new ExecuteSQLQueryBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public ReceiveTimeoutBuilder receiveTimeout(Endpoint messageEndpoint) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpoint(messageEndpoint);
        ReceiveTimeoutBuilder builder = new ReceiveTimeoutBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public ReceiveTimeoutBuilder receiveTimeout(String messageEndpointUri) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpointUri(messageEndpointUri);
        ReceiveTimeoutBuilder builder = new ReceiveTimeoutBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public FailAction fail(String message) {
        FailAction action = new FailAction();
        action.setMessage(message);
        action(action);
        return action;
    }

    @Override
    public InputActionBuilder input() {
        InputActionBuilder builder = new InputActionBuilder();
        action(builder);
        return builder;
    }

    @Override
    public JavaActionBuilder java(String className) {
        JavaAction action = new JavaAction();
        action.setClassName(className);
        JavaActionBuilder builder = new JavaActionBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public JavaActionBuilder java(Class<?> clazz) {
        JavaAction action = new JavaAction();
        action.setClassName(clazz.getSimpleName());
        JavaActionBuilder builder = new JavaActionBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public JavaActionBuilder java(Object instance) {
        JavaAction action = new JavaAction();
        action.setInstance(instance);
        JavaActionBuilder builder = new JavaActionBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = new LoadPropertiesAction();
        action.setFilePath(filePath);
        action(action);
        return action;
    }

    @Override
    public PurgeJmsQueuesBuilder purgeQueues(ConnectionFactory connectionFactory) {
        PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
        action.setConnectionFactory(connectionFactory);
        PurgeJmsQueuesBuilder builder = new PurgeJmsQueuesBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public PurgeJmsQueuesBuilder purgeQueues() {
        PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
        action.setConnectionFactory(getApplicationContext().getBean("connectionFactory", ConnectionFactory.class));
        PurgeJmsQueuesBuilder builder = new PurgeJmsQueuesBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public PurgeChannelsBuilder purgeChannels() {
        PurgeChannelsBuilder builder = new PurgeChannelsBuilder();
        builder.channelResolver(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public ReceiveSoapMessageBuilder receive(WebServiceServer server) {
        ReceiveSoapMessageAction action = new ReceiveSoapMessageAction();
        action.setEndpoint(server);
        ReceiveSoapMessageBuilder builder = new ReceiveSoapMessageBuilder(action)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public ReceiveMessageBuilder receive(Endpoint messageEndpoint) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpoint(messageEndpoint);
        ReceiveMessageBuilder builder = new ReceiveMessageBuilder(action)
                .withApplicationContext(getApplicationContext());
        action(builder);

        builder.position(positionHandle());
        return builder;
    }

    @Override
    public ReceiveMessageBuilder receive(String messageEndpointUri) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpointUri(messageEndpointUri);
        ReceiveMessageBuilder builder = new ReceiveMessageBuilder(action)
                .withApplicationContext(getApplicationContext());
        action(builder);

        builder.position(positionHandle());
        return builder;
    }

    @Override
    public SendSoapMessageBuilder send(WebServiceClient client) {
        SendSoapMessageAction action = new SendSoapMessageAction();
        action.setEndpoint(client);
        SendSoapMessageBuilder builder = new SendSoapMessageBuilder(action)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public SendMessageBuilder send(Endpoint messageEndpoint) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpoint(messageEndpoint);
        SendMessageBuilder builder = new SendMessageBuilder(action)
                .withApplicationContext(getApplicationContext());
        action(builder);

        builder.position(positionHandle());
        return builder;
    }

    @Override
    public SendMessageBuilder send(String messageEndpointUri) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpointUri(messageEndpointUri);
        SendMessageBuilder builder = new SendMessageBuilder(action)
                .withApplicationContext(getApplicationContext());
        action(builder);

        builder.position(positionHandle());
        return builder;
    }

    @Override
    public SendSoapFaultBuilder sendSoapFault(String messageEndpointUri) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpointUri(messageEndpointUri);
        SendSoapFaultBuilder builder = new SendSoapFaultBuilder(action)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public SendSoapFaultBuilder sendSoapFault(Endpoint messageEndpoint) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpoint(messageEndpoint);
        SendSoapFaultBuilder builder = new SendSoapFaultBuilder(action)
                .withApplicationContext(getApplicationContext());

        action(builder);
        return builder;
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
    public GroovyActionBuilder groovy(String script) {
        GroovyAction action = new GroovyAction();
        action.setScript(script);
        GroovyActionBuilder builder = new GroovyActionBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public GroovyActionBuilder groovy(Resource scriptResource) {
        GroovyAction action = new GroovyAction();
        try {
            action.setScript(FileUtils.readToString(scriptResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }
        GroovyActionBuilder builder = new GroovyActionBuilder(action);
        action(builder);
        return builder;
    }

    @Override
    public TransformActionBuilder transform() {
        TransformActionBuilder builder = new TransformActionBuilder();
        action(builder);
        return builder;
    }

    @Override
    public AssertExceptionBuilder assertException(TestAction testAction) {
        AssertExceptionBuilder builder = new AssertExceptionBuilder(this);
        builder.actions(testAction);
        return builder;
    }

    @Override
    public AssertExceptionBuilder assertException() {
        AssertExceptionBuilder builder = new AssertExceptionBuilder(this);
        return builder;
    }

    @Override
    public CatchExceptionBuilder catchException(TestAction... actions) {
        CatchExceptionBuilder builder = new CatchExceptionBuilder(this)
                .exception(CitrusRuntimeException.class.getName());
        builder.actions(actions);
        return builder;
    }

    @Override
    public CatchExceptionBuilder catchException() {
        CatchExceptionBuilder builder = new CatchExceptionBuilder(this)
                .exception(CitrusRuntimeException.class.getName());
        return builder;
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault(TestAction testAction) {
        AssertSoapFaultBuilder builder = new AssertSoapFaultBuilder(this);
        builder.actions(testAction);

        if (getApplicationContext().containsBean("soapFaultValidator")) {
            builder.validator(getApplicationContext().getBean("soapFaultValidator", SoapFaultValidator.class));
        }
        return builder;
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault() {
        AssertSoapFaultBuilder builder = new AssertSoapFaultBuilder(this);
        if (getApplicationContext().containsBean("soapFaultValidator")) {
            builder.validator(getApplicationContext().getBean("soapFaultValidator", SoapFaultValidator.class));
        }
        return builder;
    }

    @Override
    public ConditionalBuilder conditional(TestAction... actions) {
        ConditionalBuilder builder = new ConditionalBuilder(this);
        builder.actions(actions);
        return builder;
    }

    @Override
    public ConditionalBuilder conditional() {
        ConditionalBuilder builder = new ConditionalBuilder(this);
        return builder;
    }

    @Override
    public IterateBuilder iterate(TestAction... actions) {
        IterateBuilder builder = new IterateBuilder(this);
        builder.actions(actions);
        return builder;
    }

    @Override
    public IterateBuilder iterate() {
        IterateBuilder builder = new IterateBuilder(this);
        return builder;
    }

    @Override
    public ParallelBuilder parallel(TestAction... actions) {
        ParallelBuilder builder = new ParallelBuilder(this);
        builder.actions(actions);
        return builder;
    }

    @Override
    public ParallelBuilder parallel() {
        ParallelBuilder builder = new ParallelBuilder(this);
        return builder;
    }

    @Override
    public RepeatOnErrorBuilder repeatOnError(TestAction... actions) {
        RepeatOnErrorBuilder builder = new RepeatOnErrorBuilder(this);
        builder.actions(actions);
        return builder;
    }

    @Override
    public RepeatOnErrorBuilder repeatOnError() {
        RepeatOnErrorBuilder builder = new RepeatOnErrorBuilder(this);
        return builder;
    }

    @Override
    public RepeatBuilder repeat(TestAction... actions) {
        RepeatBuilder builder = new RepeatBuilder(this);
        builder.actions(actions);
        return builder;
    }

    @Override
    public RepeatBuilder repeat() {
        RepeatBuilder builder = new RepeatBuilder(this);
        return builder;
    }

    @Override
    public SequenceBuilder sequential(TestAction... actions) {
        SequenceBuilder builder = new SequenceBuilder(this);
        builder.actions(actions);
        return builder;
    }

    @Override
    public SequenceBuilder sequential() {
        SequenceBuilder builder = new SequenceBuilder(this);
        return builder;
    }

    @Override
    public TemplateBuilder applyTemplate(String name) {
        Template template = new Template();
        template.setName(name);
        TemplateBuilder builder = new TemplateBuilder(template)
                .load(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public FinallySequenceBuilder doFinally(TestAction... actions) {
        FinallySequenceBuilder builder = new FinallySequenceBuilder(this);
        builder.actions(actions);
        return builder;
    }

    @Override
    public FinallySequenceBuilder doFinally() {
        FinallySequenceBuilder builder = new FinallySequenceBuilder(this);
        return builder;
    }

    @Override
    public PositionHandle positionHandle() {
        return new PositionHandle(testCase.getActions());
    }

    /**
     * Builds the test case.
     * @return
     */
    public TestCase getTestCase() {
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
