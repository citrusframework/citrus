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

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Stack;

import com.consol.citrus.AbstractTestContainerBuilder;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActionContainerBuilder;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.AntRunAction;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.actions.InputAction;
import com.consol.citrus.actions.JavaAction;
import com.consol.citrus.actions.LoadPropertiesAction;
import com.consol.citrus.actions.PurgeEndpointAction;
import com.consol.citrus.actions.PurgeMessageChannelAction;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.actions.StartServerAction;
import com.consol.citrus.actions.StopServerAction;
import com.consol.citrus.actions.StopTimeAction;
import com.consol.citrus.actions.StopTimerAction;
import com.consol.citrus.actions.TraceVariablesAction;
import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.camel.actions.CamelRouteActionBuilder;
import com.consol.citrus.condition.ActionCondition;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.Assert;
import com.consol.citrus.container.Async;
import com.consol.citrus.container.Catch;
import com.consol.citrus.container.Conditional;
import com.consol.citrus.container.FinallySequence;
import com.consol.citrus.container.Iterate;
import com.consol.citrus.container.Parallel;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.container.RepeatUntilTrue;
import com.consol.citrus.container.Sequence;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.container.Template;
import com.consol.citrus.container.Timer;
import com.consol.citrus.container.Wait;
import com.consol.citrus.container.WaitActionConditionBuilder;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.selenium.actions.SeleniumActionBuilder;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.actions.SoapActionBuilder;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Default test builder offers builder pattern methods in order to configure a
 * test case with test actions, variables and properties.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class DefaultTestDesigner implements TestDesigner {

    /** This builders test case */
    private final DefaultTestCase testCase;

    /** This runners test context */
    private TestContext context;

    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /** Optional stack of containers cached for execution */
    protected Stack<TestActionContainerBuilder<? extends AbstractActionContainer, ?>> containers = new Stack<>();

    /** Default constructor */
    public DefaultTestDesigner() {
        testCase = new DefaultTestCase();
        testClass(this.getClass());
        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    /**
     * Constructor initializing test case.
     * @param testCase
     */
    protected DefaultTestDesigner(DefaultTestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     * @param context
     */
    public DefaultTestDesigner(ApplicationContext applicationContext, TestContext context) {
        this();
        this.context = context;
        this.applicationContext = applicationContext;

        if (applicationContext != null) {
            try {
                testCase.setTestActionListeners(applicationContext.getBean(TestActionListeners.class));

                if (!applicationContext.getBeansOfType(SequenceBeforeTest.class).isEmpty()) {
                    testCase.setBeforeTest(Arrays.asList(applicationContext.getBeansOfType(SequenceBeforeTest.class).values().toArray(new SequenceBeforeTest[]{})));
                }

                if (!applicationContext.getBeansOfType(SequenceAfterTest.class).isEmpty()) {
                    testCase.setAfterTest(Arrays.asList(applicationContext.getBeansOfType(SequenceAfterTest.class).values().toArray(new SequenceAfterTest[]{})));
                }
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to setup test designer", e);
            }
        }
    }

    @Override
    public void testClass(Class<?> type) {
        testCase.setTestClass(type);
    }

    @Override
    public void name(String name) {
        testCase.setBeanName(name);
        testCase.setName(name);
    }

    @Override
    public void description(String description) {
        testCase.setDescription(description);
    }

    @Override
    public void author(String author) {
        testCase.getMetaInfo().setAuthor(author);
    }

    @Override
    public void packageName(String packageName) {
        testCase.setPackageName(packageName);
    }

    @Override
    public void status(TestCaseMetaInfo.Status status) {
        testCase.getMetaInfo().setStatus(status);
    }

    @Override
    public void creationDate(Date date) {
        testCase.getMetaInfo().setCreationDate(date);
    }

    @Override
    public <T> T variable(String name, T value) {
        testCase.getVariableDefinitions().put(name, value);
        return value;
    }

    @Override
    public void action(TestActionBuilder<?> builder) {
        if (builder instanceof TestActionContainerBuilder<?, ?>) {
            if (containers.lastElement().equals(builder)) {
                containers.pop();
            } else {
                throw new CitrusRuntimeException("Invalid use of action containers - the container execution is not expected!");
            }

            if (builder instanceof FinallySequence.Builder) {
                ((FinallySequence.Builder) builder).getActions().forEach(testCase::addFinalAction);
                return;
            }
        }

        if (containers.isEmpty()) {
            testCase.addTestAction(builder);
        } else {
            containers.lastElement().getActions().add(builder);
        }
    }

    @Override
    public ApplyTestBehaviorAction.Builder applyBehavior(TestBehavior behavior) {
        ApplyTestBehaviorAction.Builder builder = new ApplyTestBehaviorAction.Builder()
                .designer(this)
                .behavior(behavior);
        behavior.setApplicationContext(getApplicationContext());
        builder.build().execute(context);
        return builder;
    }

    @Override
    public <T extends AbstractActionContainer, B extends AbstractTestContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container)  {
        TestActionContainerBuilder<T, B> builder = new AbstractTestContainerBuilder<T, B>() {
            @Override
            public B actions(TestActionBuilder<?>... actions) {
                B builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }

            @Override
            public T build() {
                if (container.getActions().size() > 0) {
                    return container;
                }

                return build(container);
            }
        };

        return container(builder);
    }

    @Override
    public <T extends TestActionContainerBuilder<? extends AbstractActionContainer, ?>> T container(T container) {
        containers.push(container);
        return container;
    }

    @Override
    public CreateVariablesAction.Builder createVariable(String variableName, String value) {
        CreateVariablesAction.Builder builder = CreateVariablesAction.Builder.createVariables(variableName, value);
        action(builder);
        return builder;
    }

    @Override
    public AntRunAction.Builder antrun(String buildFilePath) {
        AntRunAction.Builder builder = AntRunAction.Builder.antrun(buildFilePath);
        action(builder);
        return builder;
    }

    @Override
    public EchoAction.Builder echo(String message) {
        EchoAction.Builder builder = EchoAction.Builder.echo(message);
        action(builder);
        return builder;
    }

    @Override
    public ExecutePLSQLAction.Builder plsql(DataSource dataSource) {
        ExecutePLSQLAction.Builder builder = ExecutePLSQLAction.Builder.plsql(dataSource);
        action(builder);
        return builder;
    }

    @Override
    public ExecuteSQLAction.Builder sql(DataSource dataSource) {
        ExecuteSQLAction.Builder builder = ExecuteSQLAction.Builder.sql(dataSource);
        action(builder);
        return builder;
    }

    @Override
    public ExecuteSQLQueryAction.Builder query(DataSource dataSource) {
        ExecuteSQLQueryAction.Builder builder = ExecuteSQLQueryAction.Builder.query(dataSource);
        action(builder);
        return builder;
    }

    @Override
    public ReceiveTimeoutAction.Builder receiveTimeout(Endpoint messageEndpoint) {
        ReceiveTimeoutAction.Builder builder = ReceiveTimeoutAction.Builder.receiveTimeout(messageEndpoint);
        action(builder);
        return builder;
    }

    @Override
    public ReceiveTimeoutAction.Builder receiveTimeout(String messageEndpointUri) {
        ReceiveTimeoutAction.Builder builder = ReceiveTimeoutAction.Builder.receiveTimeout(messageEndpointUri);
        action(builder);
        return builder;
    }

    @Override
    public FailAction.Builder fail(String message) {
        FailAction.Builder builder = FailAction.Builder.fail(message);
        action(builder);
        return builder;
    }

    @Override
    public InputAction.Builder input() {
        InputAction.Builder builder = InputAction.Builder.input();
        action(builder);
        return builder;
    }

    @Override
    public JavaAction.Builder java(String className) {
        JavaAction.Builder builder = JavaAction.Builder.java(className);
        action(builder);
        return builder;
    }

    @Override
    public JavaAction.Builder java(Class<?> clazz) {
        JavaAction.Builder builder = JavaAction.Builder.java(clazz.getSimpleName());
        action(builder);
        return builder;
    }

    @Override
    public JavaAction.Builder java(Object instance) {
        JavaAction.Builder builder = JavaAction.Builder.java(instance);
        action(builder);
        return builder;
    }

    @Override
    public LoadPropertiesAction.Builder load(String filePath) {
        LoadPropertiesAction.Builder builder = LoadPropertiesAction.Builder.load(filePath);
        action(builder);
        return builder;
    }

    @Override
    public PurgeJmsQueuesAction.Builder purgeQueues() {
        PurgeJmsQueuesAction.Builder builder = PurgeJmsQueuesAction.Builder.purgeQueues()
                .withApplicationContext(applicationContext);
        action(builder);
        return builder;
    }

    @Override
    public PurgeMessageChannelAction.Builder purgeChannels() {
        PurgeMessageChannelAction.Builder builder = PurgeMessageChannelAction.Builder.purgeChannels();
        builder.channelResolver(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public PurgeEndpointAction.Builder purgeEndpoints() {
        PurgeEndpointAction.Builder builder = PurgeEndpointAction.Builder.purgeEndpoints()
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public ReceiveMessageAction.Builder receive(Endpoint messageEndpoint) {
        ReceiveMessageAction.Builder builder = ReceiveMessageAction.Builder.receive(messageEndpoint)
                .messageType(MessageType.XML)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public ReceiveMessageAction.Builder receive(String messageEndpointUri) {
        ReceiveMessageAction.Builder builder = ReceiveMessageAction.Builder.receive(messageEndpointUri)
                .messageType(MessageType.XML)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public SendMessageAction.Builder send(Endpoint messageEndpoint) {
        SendMessageAction.Builder builder = SendMessageAction.Builder.send(messageEndpoint)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public SendMessageAction.Builder send(String messageEndpointUri) {
        SendMessageAction.Builder builder = SendMessageAction.Builder.send(messageEndpointUri)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public SleepAction.Builder sleep() {
        SleepAction.Builder builder = SleepAction.Builder.sleep();
        action(builder);
        return builder;
    }

    @Override
    public SleepAction.Builder sleep(long milliseconds) {
        SleepAction.Builder builder = SleepAction.Builder.sleep().milliseconds(milliseconds);
        action(builder);
        return builder;
    }

    @Override
    public SleepAction.Builder sleep(double seconds) {
        SleepAction.Builder builder = SleepAction.Builder.sleep().seconds(seconds);
        action(builder);
        return builder;
    }

    @Override
    public Wait.Builder waitFor() {
        Wait.Builder builder = new Wait.Builder() {
            @Override
            public WaitActionConditionBuilder execution() {
                final Sequence.Builder dummy = new Sequence.Builder();
                DefaultTestDesigner.this.containers.push(dummy);
                return condition(new WaitActionConditionBuilder(new ActionCondition(), this) {
                    @Override
                    public WaitActionConditionBuilder action(TestActionBuilder<?> action) {
                        super.action(action);
                        DefaultTestDesigner.this.containers.remove(dummy);
                        return this;
                    }
                });
            }
        };

        action(builder);
        return builder;
    }

    @Override
    public StartServerAction.Builder start(Server... servers) {
        StartServerAction.Builder builder = StartServerAction.Builder.start(servers);
        action(builder);
        return builder;
    }

    @Override
    public StartServerAction.Builder start(Server server) {
        StartServerAction.Builder builder = StartServerAction.Builder.start(server);
        action(builder);
        return builder;
    }

    @Override
    public StopServerAction.Builder stop(Server... servers) {
        StopServerAction.Builder builder = StopServerAction.Builder.stop(servers);
        action(builder);
        return builder;
    }

    @Override
    public StopServerAction.Builder stop(Server server) {
        StopServerAction.Builder builder = StopServerAction.Builder.stop(server);
        action(builder);
        return builder;
    }

    @Override
    public StopTimeAction.Builder stopTime() {
        StopTimeAction.Builder builder = StopTimeAction.Builder.stopTime();
        action(builder);
        return builder;
    }

    @Override
    public StopTimeAction.Builder stopTime(String id) {
        StopTimeAction.Builder builder = StopTimeAction.Builder.stopTime(id);
        action(builder);
        return builder;
    }

    @Override
    public StopTimeAction.Builder stopTime(String id, String suffix) {
        StopTimeAction.Builder builder = StopTimeAction.Builder.stopTime(id, suffix);
        action(builder);
        return builder;
    }

    @Override
    public TraceVariablesAction.Builder traceVariables() {
        TraceVariablesAction.Builder builder = TraceVariablesAction.Builder.traceVariables();
        action(builder);
        return builder;
    }

    @Override
    public TraceVariablesAction.Builder traceVariables(String... variables) {
        TraceVariablesAction.Builder builder = TraceVariablesAction.Builder.traceVariables(variables);
        action(builder);
        return builder;
    }

    @Override
    public GroovyAction.Builder groovy(String script) {
        GroovyAction.Builder builder = GroovyAction.Builder.groovy(script);
        action(builder);
        return builder;
    }

    @Override
    public GroovyAction.Builder groovy(Resource scriptResource) {
        GroovyAction.Builder builder = GroovyAction.Builder.groovy(scriptResource);
        action(builder);
        return builder;
    }

    @Override
    public TransformAction.Builder transform() {
        TransformAction.Builder builder = new TransformAction.Builder();
        action(builder);
        return builder;
    }

    @Override
    public Assert.Builder assertException() {
        Assert.Builder builder = new Assert.Builder() {
            @Override
            public Assert.Builder actions(TestActionBuilder<?>... actions) {
                Assert.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public Catch.Builder catchException() {
        Catch.Builder builder = new Catch.Builder() {
            @Override
            public Catch.Builder actions(TestActionBuilder<?>... actions) {
                Catch.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        }.exception(CitrusRuntimeException.class.getName());
        return container(builder);
    }

    @Override
    public AssertSoapFault.Builder assertSoapFault() {
        AssertSoapFault.Builder builder = new AssertSoapFault.Builder() {
            @Override
            public AssertSoapFault.Builder actions(TestActionBuilder<?>... actions) {
                AssertSoapFault.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        }.withApplicationContext(applicationContext);
        return container(builder);
    }

    @Override
    public Conditional.Builder conditional() {
        Conditional.Builder builder = new Conditional.Builder() {
            @Override
            public Conditional.Builder actions(TestActionBuilder<?>... actions) {
                Conditional.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public Iterate.Builder iterate() {
        Iterate.Builder builder = new Iterate.Builder() {
            @Override
            public Iterate.Builder actions(TestActionBuilder<?>... actions) {
                Iterate.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public Parallel.Builder parallel() {
        Parallel.Builder builder = new Parallel.Builder() {
            @Override
            public Parallel.Builder actions(TestActionBuilder<?>... actions) {
                Parallel.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public RepeatOnErrorUntilTrue.Builder repeatOnError() {
        RepeatOnErrorUntilTrue.Builder builder = new RepeatOnErrorUntilTrue.Builder() {
            @Override
            public RepeatOnErrorUntilTrue.Builder actions(TestActionBuilder<?>... actions) {
                RepeatOnErrorUntilTrue.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public RepeatUntilTrue.Builder repeat() {
        RepeatUntilTrue.Builder builder = new RepeatUntilTrue.Builder() {
            @Override
            public RepeatUntilTrue.Builder actions(TestActionBuilder<?>... actions) {
                RepeatUntilTrue.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public Sequence.Builder sequential() {
        Sequence.Builder builder = new Sequence.Builder() {
            @Override
            public Sequence.Builder actions(TestActionBuilder<?>... actions) {
                Sequence.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public Async.Builder async() {
        Async.Builder builder = new Async.Builder() {
            @Override
            public Async.Builder actions(TestActionBuilder<?>... actions) {
                Async.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public Timer.Builder timer() {
        Timer.Builder builder = new Timer.Builder() {
            @Override
            public Timer.Builder actions(TestActionBuilder<?>... actions) {
                Timer.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
    }

    @Override
    public StopTimerAction.Builder stopTimer(String timerId) {
        StopTimerAction.Builder builder = StopTimerAction.Builder.stopTimer(timerId);
        action(builder);
        return builder;
    }

    @Override
    public StopTimerAction.Builder stopTimers() {
        StopTimerAction.Builder builder = StopTimerAction.Builder.stopTimers();
        action(builder);
        return builder;
    }

    @Override
    public DockerExecuteAction.Builder docker() {
        DockerExecuteAction.Builder builder = DockerExecuteAction.Builder.docker();
        action(builder);
        return builder;
    }

    @Override
    public KubernetesExecuteAction.Builder kubernetes() {
        KubernetesExecuteAction.Builder builder = KubernetesExecuteAction.Builder.kubernetes();
        action(builder);
        return builder;
    }

    @Override
    public SeleniumActionBuilder selenium() {
        SeleniumActionBuilder builder = new SeleniumActionBuilder();
        action(builder);
        return builder;
    }

    @Override
    public HttpActionBuilder http() {
        HttpActionBuilder builder = new HttpActionBuilder()
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public SoapActionBuilder soap() {
        SoapActionBuilder builder = new SoapActionBuilder()
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public CamelRouteActionBuilder camel() {
        CamelRouteActionBuilder builder = CamelRouteActionBuilder.camel()
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public ZooExecuteAction.Builder zookeeper() {
        ZooExecuteAction.Builder builder = ZooExecuteAction.Builder.zookeeper()
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public Template.Builder applyTemplate(String name) {
        Template.Builder builder = Template.Builder.applyTemplate(name)
                .withApplicationContext(getApplicationContext());
        action(builder);
        return builder;
    }

    @Override
    public FinallySequence.Builder doFinally() {
        FinallySequence.Builder builder = new FinallySequence.Builder() {
            @Override
            public FinallySequence.Builder actions(TestActionBuilder<?>... actions) {
                FinallySequence.Builder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        return container(builder);
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
        return testCase.getVariableDefinitions();
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
}
