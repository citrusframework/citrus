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
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.jms.ConnectionFactory;
import java.util.Arrays;
import java.util.Stack;

/**
 * Default test runner implementation. Provides Java DSL methods for test actions. Immediately executes test actions as
 * they were built. This way the test case grows with each test action and changes for instance to the test context (variables) are
 * immediately visible.
 *
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class DefaultTestRunner implements TestRunner {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultTestRunner.class);

    /** This builders test case */
    private final TestCase testCase = new TestCase();

    /** This runners test context */
    private TestContext context;

    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /** Optional stack of containers cached for execution */
    private Stack<AbstractActionContainer> containers = new Stack<>();

    /** Default constructor */
    public DefaultTestRunner() {
        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     */
    public DefaultTestRunner(ApplicationContext applicationContext) {
        this();

        this.applicationContext = applicationContext;

        try {
            initialize();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to setup test runner", e);
        }
    }

    protected void initialize() {
        createTestContext();

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
        testCase.setBeanName(name);
        testCase.setName(name);
    }

    @Override
    public void packageName(String packageName) {
        testCase.setPackageName(packageName);
    }

    @Override
    public void start() {
        testCase.start(context);
    }

    @Override
    public void stop() {
        testCase.finish(context);
    }

    @Override
    public <T> T variable(String name, T value) {
        testCase.getVariableDefinitions().put(name, value);

        if (value instanceof String) {
            String resolved = context.resolveDynamicValue((String) value);
            context.setVariable(name, resolved);
            return (T) resolved;
        } else {
            context.setVariable(name, value);
            return value;
        }
    }

    @Override
    public void parameter(String[] parameterNames, Object[] parameterValues) {
        testCase.setParameters(parameterNames, parameterValues);

        for (int i = 0; i < parameterNames.length; i++) {
            log.info(String.format("Initializing test parameter '%s' as variable", parameterNames[i]));
            context.setVariable(parameterNames[i], parameterValues[i]);
        }
    }

    @Override
    public <T extends TestAction> T run(T testAction) {
        if (testAction instanceof TestActionContainer) {

            if (containers.lastElement().equals(testAction)) {
                containers.pop();
            } else {
                throw new CitrusRuntimeException("Invalid use of action containers - the container execution is not expected!");
            }

            if (testAction instanceof FinallySequence) {
                testCase.getFinalActions().addAll(((FinallySequence) testAction).getActions());
                return testAction;
            }
        }

        if (!containers.isEmpty()) {
            containers.lastElement().addTestAction(testAction);
        } else {
            testCase.addTestAction(testAction);
            testCase.executeAction(testAction, context);
        }

        return testAction;
    }

    @Override
    public void applyBehavior(TestBehavior behavior) {
        behavior.setApplicationContext(applicationContext);
        behavior.apply(this);
    }

    @Override
    public CreateVariablesAction createVariable(String variableName, String value) {
        CreateVariablesAction action = new CreateVariablesAction();
        action.getVariables().put(variableName, value);
        return run(action);
    }

    @Override
    public AntRunAction antrun(TestActionConfigurer<AntRunBuilder> configurer) {
        AntRunBuilder definition = new AntRunBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        return run(action);
    }

    @Override
    public ExecutePLSQLAction plsql(TestActionConfigurer<ExecutePLSQLBuilder> configurer) {
        ExecutePLSQLBuilder definition = new ExecutePLSQLBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public ExecuteSQLAction sql(TestActionConfigurer<ExecuteSQLBuilder> configurer) {
        ExecuteSQLBuilder definition = new ExecuteSQLBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public ExecuteSQLQueryAction query(TestActionConfigurer<ExecuteSQLQueryBuilder> configurer) {
        ExecuteSQLQueryBuilder definition = new ExecuteSQLQueryBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public FailAction fail(String message) {
        FailAction action = new FailAction();
        action.setMessage(message);
        return run(action);
    }

    @Override
    public InputAction input(TestActionConfigurer<InputActionBuilder> configurer) {
        InputActionBuilder definition = new InputActionBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public ReceiveTimeoutAction receiveTimeout(TestActionConfigurer<ReceiveTimeoutBuilder> configurer) {
        ReceiveTimeoutBuilder definition = new ReceiveTimeoutBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = new LoadPropertiesAction();
        action.setFilePath(filePath);
        return run(action);
    }

    @Override
    public PurgeJmsQueuesAction purgeQueues(TestActionConfigurer<PurgeJmsQueuesBuilder> configurer) {
        PurgeJmsQueuesBuilder definition = new PurgeJmsQueuesBuilder();
        configurer.configure(definition);

        if (!definition.hasConnectionFactory()) {
            definition.connectionFactory(applicationContext.getBean("connectionFactory", ConnectionFactory.class));
        }
        return run(definition.build());
    }

    @Override
    public PurgeMessageChannelAction purgeChannels(TestActionConfigurer<PurgeChannelsBuilder> configurer) {
        PurgeChannelsBuilder definition = new PurgeChannelsBuilder();
        definition.channelResolver(applicationContext);
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public ReceiveMessageAction receive(TestActionConfigurer<ReceiveMessageBuilder> configurer) {
        ReceiveMessageBuilder definition = new ReceiveMessageBuilder();
        definition.withApplicationContext(applicationContext);
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public SendMessageAction send(TestActionConfigurer<SendMessageBuilder> configurer) {
        SendMessageBuilder definition = new SendMessageBuilder();
        definition.withApplicationContext(applicationContext);
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public SendSoapFaultAction sendSoapFault(TestActionConfigurer<SendSoapFaultBuilder> configurer) {
        SendSoapFaultBuilder definition = new SendSoapFaultBuilder();
        definition.withApplicationContext(applicationContext);
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public SleepAction sleep() {
        return run(new SleepAction());
    }

    @Override
    public SleepAction sleep(long milliseconds) {
        SleepAction action = new SleepAction();
        action.setMilliseconds(String.valueOf(milliseconds));
        return run(action);
    }

    @Override
    public StartServerAction start(Server... servers) {
        StartServerAction action = new StartServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        return run(action);
    }

    @Override
    public StartServerAction start(Server server) {
        StartServerAction action = new StartServerAction();
        action.setServer(server);
        return run(action);
    }

    @Override
    public StopServerAction stop(Server... servers) {
        StopServerAction action = new StopServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        return run(action);
    }

    @Override
    public StopServerAction stop(Server server) {
        StopServerAction action = new StopServerAction();
        action.setServer(server);
        return run(action);
    }

    @Override
    public StopTimeAction stopTime() {
        return run(new StopTimeAction());
    }

    @Override
    public StopTimeAction stopTime(String id) {
        StopTimeAction action = new StopTimeAction();
        action.setId(id);
        return run(action);
    }

    @Override
    public TraceVariablesAction traceVariables() {
        return run(new TraceVariablesAction());
    }

    @Override
    public TraceVariablesAction traceVariables(String... variables) {
        TraceVariablesAction action = new TraceVariablesAction();
        action.setVariableNames(Arrays.asList(variables));
        return run(action);
    }

    @Override
    public GroovyAction groovy(TestActionConfigurer<GroovyActionBuilder> configurer) {
        GroovyActionBuilder definition = new GroovyActionBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public TransformAction transform(TestActionConfigurer<TransformActionBuilder> configurer) {
        TransformActionBuilder definition = new TransformActionBuilder();
        configurer.configure(definition);
        return run(definition.build());
    }

    @Override
    public ExceptionContainerRunner assertException() {
        return assertException(new TestActionConfigurer<AssertExceptionBuilder>() {
            @Override
            public void configure(AssertExceptionBuilder definition) {
            }
        });
    }

    @Override
    public ExceptionContainerRunner assertException(TestActionConfigurer<AssertExceptionBuilder> configurer) {
        AssertExceptionBuilder definition = new AssertExceptionBuilder();
        configurer.configure(definition);
        containers.push(definition.build());

        return new DefaultContainerRunner(definition.build(), this);
    }

    @Override
    public ExceptionContainerRunner catchException() {
        return catchException(new TestActionConfigurer<CatchExceptionBuilder>() {
            @Override
            public void configure(CatchExceptionBuilder definition) {
            }
        });
    }

    @Override
    public ExceptionContainerRunner catchException(TestActionConfigurer<CatchExceptionBuilder> configurer) {
        CatchExceptionBuilder definition = new CatchExceptionBuilder();
        configurer.configure(definition);
        containers.push(definition.build());

        return new DefaultContainerRunner(definition.build(), this);
    }

    @Override
    public ExceptionContainerRunner assertSoapFault(TestActionConfigurer<AssertSoapFaultBuilder> configurer) {
        AssertSoapFaultBuilder definition = new AssertSoapFaultBuilder();

        if (applicationContext.containsBean("soapFaultValidator")) {
            definition.validator(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class));
        }

        configurer.configure(definition);
        containers.push(definition.build());

        return new DefaultContainerRunner(definition.build(), this);
    }

    @Override
    public ContainerRunner conditional(TestActionConfigurer<ConditionalBuilder> configurer) {
        ConditionalBuilder definition = new ConditionalBuilder();
        configurer.configure(definition);
        containers.push(definition.build());

        return new DefaultContainerRunner(definition.build(), this);
    }

    @Override
    public ContainerRunner iterate(TestActionConfigurer<IterateBuilder> configurer) {
        IterateBuilder definition = new IterateBuilder();
        configurer.configure(definition);
        containers.push(definition.build());

        return new DefaultContainerRunner(definition.build(), this);
    }

    @Override
    public ContainerRunner parallel() {
        Parallel container = new Parallel();
        containers.push(container);

        return new DefaultContainerRunner(container, this);
    }

    @Override
    public ContainerRunner repeatOnError(TestActionConfigurer<RepeatOnErrorBuilder> configurer) {
        RepeatOnErrorBuilder definition = new RepeatOnErrorBuilder();
        configurer.configure(definition);
        containers.push(definition.build());

        return new DefaultContainerRunner(definition.build(), this);
    }

    @Override
    public ContainerRunner repeat(TestActionConfigurer<RepeatBuilder> configurer) {
        RepeatBuilder definition = new RepeatBuilder();
        configurer.configure(definition);
        containers.push(definition.build());

        return new DefaultContainerRunner(definition.build(), this);
    }

    @Override
    public ContainerRunner sequential() {
        Sequence container = new Sequence();
        containers.push(container);

        return new DefaultContainerRunner(container, this);
    }

    @Override
    public Template applyTemplate(TestActionConfigurer<TemplateBuilder> configurer) {
        TemplateBuilder definition = new TemplateBuilder();
        configurer.configure(definition);
        definition.load(applicationContext);
        configurer.configure(definition);

        return run(definition.build());
    }

    @Override
    public ContainerRunner doFinally() {
        FinallySequence container = new FinallySequence();
        containers.push(container);

        return new DefaultContainerRunner(container, this);
    }

    /**
     * Creates new test context from Spring bean application context.
     * @return
     */
    protected TestContext createTestContext() {
        if (context == null) {
            context = applicationContext.getBean(TestContext.class);
            context.setApplicationContext(applicationContext);
        }

        return context;
    }

    /**
     * Sets the application context either from ApplicationContextAware injection or from outside.
     * @param applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected TestCase getTestCase() {
        return testCase;
    }

    /**
     * Helper sequence to mark actions as finally actions that should be
     * executed in finally block of test case.
     */
    private class FinallySequence extends Sequence {
    }
}
