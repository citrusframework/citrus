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

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.container.FinallySequence;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Default test runner implementation. Provides Java DSL methods for test actions. Immediately executes test actions as
 * they were built. This way the test case grows with each test action and changes for instance to the test context (variables) are
 * immediately visible.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class DefaultTestRunner implements TestRunner {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultTestRunner.class);

    /** This builders test case */
    private final TestCase testCase;

    /** This runners test context */
    private TestContext context;

    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /** Optional stack of containers cached for execution */
    protected Stack<AbstractActionContainer> containers = new Stack<>();

    /** Default constructor */
    public DefaultTestRunner() {
        this(new TestCase());
        testClass(this.getClass());
        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    /**
     * Constructor initializing test case.
     * @param testCase
     */
    protected DefaultTestRunner(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     * @param context
     */
    public DefaultTestRunner(ApplicationContext applicationContext, TestContext context) {
        this();

        this.applicationContext = applicationContext;
        this.context = context;

        try {
            initialize();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to setup test runner", e);
        }
    }

    protected void initialize() {
        testCase.setTestRunner(true);
        testCase.setTestActionListeners(applicationContext.getBean(TestActionListeners.class));

        if (!applicationContext.getBeansOfType(SequenceBeforeTest.class).isEmpty()) {
            testCase.setBeforeTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceBeforeTest.class).values().toArray()));
        }

        if (!applicationContext.getBeansOfType(SequenceAfterTest.class).isEmpty()) {
            testCase.setAfterTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceAfterTest.class).values().toArray()));
        }
    }

    @Override
    public void testClass(Class<?> type) {
        getTestCase().setTestClass(type);
    }

    @Override
    public void name(String name) {
        testCase.setBeanName(name);
        testCase.setName(name);
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
            String resolved = context.replaceDynamicContentInString(value.toString());
            context.setVariable(name, resolved);
            return (T) resolved;
        } else {
            context.setVariable(name, value);
            return value;
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
    public ApplyTestBehaviorAction applyBehavior(TestBehavior behavior) {
        ApplyTestBehaviorAction action = new ApplyTestBehaviorAction(this, behavior);
        behavior.setApplicationContext(applicationContext);
        action.execute(context);
        return action;
    }

    @Override
    public <T extends AbstractActionContainer> AbstractTestContainerBuilder<T> container(T container) {
        AbstractTestContainerBuilder<T> containerBuilder = new AbstractTestContainerBuilder<T>(this, container) {};
        containers.push(containerBuilder.build());
        return containerBuilder;
    }

    @Override
    public CreateVariablesAction createVariable(String variableName, String value) {
        CreateVariablesAction action = new CreateVariablesAction();
        action.getVariables().put(variableName, value);
        return run(action);
    }

    @Override
    public AntRunAction antrun(BuilderSupport<AntRunBuilder> configurer) {
        AntRunBuilder builder = new AntRunBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        return run(action);
    }

    @Override
    public ExecutePLSQLAction plsql(BuilderSupport<ExecutePLSQLBuilder> configurer) {
        ExecutePLSQLBuilder builder = new ExecutePLSQLBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public ExecuteSQLAction sql(BuilderSupport<ExecuteSQLBuilder> configurer) {
        ExecuteSQLBuilder builder = new ExecuteSQLBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public ExecuteSQLQueryAction query(BuilderSupport<ExecuteSQLQueryBuilder> configurer) {
        ExecuteSQLQueryBuilder builder = new ExecuteSQLQueryBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public FailAction fail(String message) {
        FailAction action = new FailAction();
        action.setMessage(message);
        return run(action);
    }

    @Override
    public InputAction input(BuilderSupport<InputActionBuilder> configurer) {
        InputActionBuilder builder = new InputActionBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public ReceiveTimeoutAction receiveTimeout(BuilderSupport<ReceiveTimeoutBuilder> configurer) {
        ReceiveTimeoutBuilder builder = new ReceiveTimeoutBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = new LoadPropertiesAction();
        action.setFilePath(filePath);
        return run(action);
    }

    @Override
    public TestAction purgeQueues(BuilderSupport<PurgeJmsQueuesBuilder> configurer) {
        PurgeJmsQueuesBuilder builder = new PurgeJmsQueuesBuilder()
                .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public PurgeMessageChannelAction purgeChannels(BuilderSupport<PurgeChannelsBuilder> configurer) {
        PurgeChannelsBuilder builder = new PurgeChannelsBuilder();
        builder.channelResolver(applicationContext);
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public PurgeEndpointAction purgeEndpoints(BuilderSupport<PurgeEndpointsBuilder> configurer) {
        PurgeEndpointsBuilder builder = new PurgeEndpointsBuilder()
                .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public ReceiveMessageAction receive(BuilderSupport<ReceiveMessageBuilder> configurer) {
        ReceiveMessageBuilder<ReceiveMessageAction, ReceiveMessageBuilder> builder = new ReceiveMessageBuilder()
                .messageType(MessageType.XML)
                .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return (ReceiveMessageAction) run(builder.build().getDelegate());
    }

    @Override
    public SendMessageAction send(BuilderSupport<SendMessageBuilder> configurer) {
        SendMessageBuilder<SendMessageAction, SendMessageBuilder> builder = new SendMessageBuilder()
                .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return (SendMessageAction) run(builder.build().getDelegate());
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
    @Deprecated
    public Wait waitFor(BuilderSupport<WaitBuilder> configurer) {
        WaitBuilder builder = new WaitBuilder(null, new Wait());
        configurer.configure(builder);
        containers.push(builder.build());
        return run(builder.build());
    }

    @Override
    public WaitBuilder waitFor() {
        WaitBuilder builder = new WaitBuilder(this, new Wait());
        containers.push(builder.build());
        return builder;
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
    public StopTimeAction stopTime(String id, String suffix) {
        StopTimeAction action = new StopTimeAction();
        action.setId(id);
        action.setSuffix(suffix);
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
    public GroovyAction groovy(BuilderSupport<GroovyActionBuilder> configurer) {
        GroovyActionBuilder builder = new GroovyActionBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public TransformAction transform(BuilderSupport<TransformActionBuilder> configurer) {
        TransformActionBuilder builder = new TransformActionBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public AssertExceptionBuilder assertException() {
        AssertExceptionBuilder builder = new AssertExceptionBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public CatchExceptionBuilder catchException() {
        CatchExceptionBuilder builder = new CatchExceptionBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault() {
        AssertSoapFaultBuilder builder = new AssertSoapFaultBuilder(this)
                .withApplicationContext(applicationContext);
        containers.push(builder.build());

        return builder;
    }

    @Override
    public ConditionalBuilder conditional() {
        ConditionalBuilder builder = new ConditionalBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public IterateBuilder iterate() {
        IterateBuilder builder = new IterateBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public ParallelBuilder parallel() {
        ParallelBuilder builder = new ParallelBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public RepeatOnErrorBuilder repeatOnError() {
        RepeatOnErrorBuilder builder = new RepeatOnErrorBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public RepeatBuilder repeat() {
        RepeatBuilder builder = new RepeatBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public SequenceBuilder sequential() {
        SequenceBuilder builder = new SequenceBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public AsyncBuilder async() {
        AsyncBuilder builder = new AsyncBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public TimerBuilder timer() {
        TimerBuilder builder = new TimerBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    @Override
    public StopTimerAction stopTimer(String timerId) {
        StopTimerAction action = new StopTimerAction();
        action.setTimerId(timerId);
        return run(action);
    }

    @Override
    public StopTimerAction stopTimers() {
        StopTimerAction action = new StopTimerAction();
        return run(action);
    }

    @Override
    public TestAction docker(BuilderSupport<DockerActionBuilder> configurer) {
        DockerActionBuilder builder = new DockerActionBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public TestAction kubernetes(BuilderSupport<KubernetesActionBuilder> configurer) {
        KubernetesActionBuilder builder = new KubernetesActionBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public TestAction selenium(BuilderSupport<SeleniumActionBuilder> configurer) {
        SeleniumActionBuilder builder = new SeleniumActionBuilder();
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public TestAction http(BuilderSupport<HttpActionBuilder> configurer) {
        HttpActionBuilder builder = new HttpActionBuilder()
                    .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return run(builder.build()).getDelegate();
    }

    @Override
    public TestAction soap(BuilderSupport<SoapActionBuilder> configurer) {
        SoapActionBuilder builder = new SoapActionBuilder()
                    .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return run(builder.build()).getDelegate();
    }

    @Override
    public TestAction camel(BuilderSupport<CamelRouteActionBuilder> configurer) {
        CamelRouteActionBuilder builder = new CamelRouteActionBuilder()
                    .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return run(builder.build()).getDelegate();
    }

    @Override
    public TestAction zookeeper(BuilderSupport<ZooActionBuilder> configurer) {
        ZooActionBuilder builder = new ZooActionBuilder()
                .withApplicationContext(applicationContext);
        configurer.configure(builder);
        return run(builder.build());
    }

    @Override
    public Template applyTemplate(BuilderSupport<TemplateBuilder> configurer) {
        TemplateBuilder builder = new TemplateBuilder();
        configurer.configure(builder);
        builder.load(applicationContext);
        configurer.configure(builder);

        return run(builder.build());
    }

    @Override
    public FinallySequenceBuilder doFinally() {
        FinallySequenceBuilder builder = new FinallySequenceBuilder(this);
        containers.push(builder.build());
        return builder;
    }

    /**
     * Gets the test context.
     * @return
     */
    public TestContext getTestContext() {
        return context;
    }

    /**
     * Sets the test context.
     * @param context
     */
    public void setTestContext(TestContext context) {
        this.context = context;
    }

    /**
     * Gets the value of the applicationContext property.
     *
     * @return the applicationContext
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

    @Override
    public TestCase getTestCase() {
        return testCase;
    }

}
