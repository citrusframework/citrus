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
import com.consol.citrus.dsl.TestActions;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.jms.ConnectionFactory;
import java.util.Stack;

/**
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
    public AntRunAction antrun(TestActionConfigurer<AntRunActionDefinition> configurer) {
        AntRunActionDefinition definition = new AntRunActionDefinition(new AntRunAction());
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public EchoAction echo(String message) {
        return run(TestActions.echo(message));
    }

    @Override
    public ExecutePLSQLAction plsql(TestActionConfigurer<ExecutePLSQLActionDefinition> configurer) {
        ExecutePLSQLActionDefinition definition = new ExecutePLSQLActionDefinition(new ExecutePLSQLAction());
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public ExecuteSQLAction sql(TestActionConfigurer<ExecuteSQLActionDefinition> configurer) {
        ExecuteSQLActionDefinition definition = new ExecuteSQLActionDefinition(new ExecuteSQLAction());
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public ExecuteSQLQueryAction query(TestActionConfigurer<ExecuteSQLQueryActionDefinition> configurer) {
        ExecuteSQLQueryActionDefinition definition = new ExecuteSQLQueryActionDefinition(new ExecuteSQLQueryAction());
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public FailAction fail(String message) {
        return run(TestActions.fail(message));
    }

    @Override
    public ReceiveTimeoutAction receiveTimeout(TestActionConfigurer<ReceiveTimeoutActionDefinition> configurer) {
        ReceiveTimeoutActionDefinition definition = new ReceiveTimeoutActionDefinition(new ReceiveTimeoutAction());
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        return run(TestActions.load(filePath));
    }

    @Override
    public PurgeJmsQueuesAction purgeQueues(TestActionConfigurer<PurgeJmsQueueActionDefinition> configurer) {
        PurgeJmsQueueActionDefinition definition = new PurgeJmsQueueActionDefinition(new PurgeJmsQueuesAction());
        configurer.configure(definition);

        if (!definition.hasConnectionFactory()) {
            definition.connectionFactory(applicationContext.getBean("connectionFactory", ConnectionFactory.class));
        }
        return run(definition.getAction());
    }

    @Override
    public PurgeMessageChannelAction purgeChannels(TestActionConfigurer<PurgeMessageChannelActionDefinition> configurer) {
        PurgeMessageChannelActionDefinition definition = new PurgeMessageChannelActionDefinition(new PurgeMessageChannelAction());
        definition.channelResolver(applicationContext);
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public ReceiveMessageAction receive(TestActionConfigurer<ReceiveMessageActionDefinition> configurer) {
        ReceiveMessageActionDefinition definition = new ReceiveMessageActionDefinition(new ReceiveMessageAction());
        definition.withApplicationContext(applicationContext);
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public SendMessageAction send(TestActionConfigurer<SendMessageActionDefinition> configurer) {
        SendMessageActionDefinition definition = new SendMessageActionDefinition(new SendMessageAction());
        definition.withApplicationContext(applicationContext);
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public SleepAction sleep() {
        return run(TestActions.sleep());
    }

    @Override
    public SleepAction sleep(long milliseconds) {
        return run(TestActions.sleep(milliseconds));
    }

    @Override
    public StartServerAction start(Server... servers) {
        return (StartServerAction) run(TestActions.start(servers));
    }

    @Override
    public StartServerAction start(Server server) {
        return run(TestActions.start(server));
    }

    @Override
    public StopServerAction stop(Server... servers) {
        return run(TestActions.stop(servers));
    }

    @Override
    public StopServerAction stop(Server server) {
        return run(TestActions.stop(server));
    }

    @Override
    public StopTimeAction stopTime() {
        return (StopTimeAction) run(TestActions.stopTime());
    }

    @Override
    public StopTimeAction stopTime(String id) {
        return run(TestActions.stopTime(id));
    }

    @Override
    public TraceVariablesAction traceVariables() {
        return run(TestActions.traceVariables());
    }

    @Override
    public TraceVariablesAction traceVariables(String... variables) {
        return run(TestActions.traceVariables(variables));
    }

    @Override
    public GroovyAction groovy(TestActionConfigurer<GroovyActionDefinition> configurer) {
        GroovyActionDefinition definition = new GroovyActionDefinition(new GroovyAction());
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public TransformAction transform(TestActionConfigurer<TransformActionDefinition> configurer) {
        TransformActionDefinition definition = TestActions.transform();
        configurer.configure(definition);
        return run(definition.getAction());
    }

    @Override
    public TestRunner assertException(TestActionConfigurer<AssertDefinition> configurer) {
        AssertDefinition definition = new AssertDefinition(new Assert());
        configurer.configure(definition);
        containers.push(definition.getAction());

        return this;
    }

    @Override
    public TestAction when(TestAction ... predicate) {
        if (containers.isEmpty()) {
            throw new CitrusRuntimeException("Invalid use of 'when' predicate in Java DSL - " +
                    "define action container first!");
        }

        AbstractActionContainer container = containers.pop();
        return run(container);
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
}
