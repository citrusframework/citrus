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

package org.citrusframework.dsl.design;

import javax.sql.DataSource;
import java.util.Date;
import java.util.Map;
import java.util.Stack;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.AntRunAction;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.ExecutePLSQLAction;
import org.citrusframework.actions.ExecuteSQLAction;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.actions.InputAction;
import org.citrusframework.actions.JavaAction;
import org.citrusframework.actions.LoadPropertiesAction;
import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.actions.StartServerAction;
import org.citrusframework.actions.StopServerAction;
import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.actions.StopTimerAction;
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.actions.TransformAction;
import org.citrusframework.condition.ActionCondition;
import org.citrusframework.container.Assert;
import org.citrusframework.container.Async;
import org.citrusframework.container.Catch;
import org.citrusframework.container.Conditional;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.container.Iterate;
import org.citrusframework.container.Parallel;
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.container.RepeatUntilTrue;
import org.citrusframework.container.Sequence;
import org.citrusframework.container.Template;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.container.Timer;
import org.citrusframework.container.Wait;
import org.citrusframework.container.WaitActionConditionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.builder.AssertSoapFaultBuilder;
import org.citrusframework.dsl.builder.CamelRouteActionBuilder;
import org.citrusframework.dsl.builder.DockerExecuteActionBuilder;
import org.citrusframework.dsl.builder.HttpActionBuilder;
import org.citrusframework.dsl.builder.KubernetesExecuteActionBuilder;
import org.citrusframework.dsl.builder.PurgeJmsQueuesActionBuilder;
import org.citrusframework.dsl.builder.PurgeMessageChannelActionBuilder;
import org.citrusframework.dsl.builder.ReceiveMessageActionBuilder;
import org.citrusframework.dsl.builder.SeleniumActionBuilder;
import org.citrusframework.dsl.builder.SendMessageActionBuilder;
import org.citrusframework.dsl.builder.SoapActionBuilder;
import org.citrusframework.dsl.builder.ZooExecuteActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.server.Server;
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

    /** Optional stack of containers cached for execution */
    protected Stack<TestActionContainerBuilder<? extends TestActionContainer, ?>> containers = new Stack<>();

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
     * @param context
     */
    public DefaultTestDesigner(TestContext context) {
        this();
        this.context = context;
    }

    @Override
    public void testClass(Class<?> type) {
        testCase.setTestClass(type);
    }

    @Override
    public void name(String name) {
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
    public void groups(String[] groups) {
        testCase.setGroups(groups);
    }

    @Override
    public <T> T variable(String name, T value) {
        testCase.getVariableDefinitions().put(name, value);
        return value;
    }

    @Override
    public void action(TestAction testAction) {
        this.action(() -> testAction);
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
        behavior.setTestContext(context);
        builder.build().execute(context);
        return builder;
    }

    @Override
    public <T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container)  {
        TestActionContainerBuilder<T, B> builder = new AbstractTestContainerBuilder<T, B>() {
            @Override
            public B actions(TestActionBuilder<?>... actions) {
                B builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }

            @Override
            protected T doBuild() {
                return container;
            }

            @Override
            public T build() {
                if (container.getActions().size() > 0) {
                    return container;
                }

                return super.build();
            }
        };

        return container(builder);
    }

    @Override
    public <T extends TestActionContainerBuilder<? extends TestActionContainer, ?>> T container(T container) {
        containers.push(container);
        return container;
    }

    @Override
    public CreateVariablesAction.Builder createVariable(String variableName, String value) {
        CreateVariablesAction.Builder builder = CreateVariablesAction.Builder.createVariable(variableName, value);
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
    public PurgeJmsQueuesActionBuilder purgeQueues() {
        PurgeJmsQueuesActionBuilder builder = new PurgeJmsQueuesActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        action(builder);
        return builder;
    }

    @Override
    public PurgeMessageChannelActionBuilder purgeChannels() {
        PurgeMessageChannelActionBuilder builder = new PurgeMessageChannelActionBuilder();
        builder.channelResolver(context.getReferenceResolver());
        action(builder);
        return builder;
    }

    @Override
    public PurgeEndpointAction.Builder purgeEndpoints() {
        PurgeEndpointAction.Builder builder = PurgeEndpointAction.Builder.purgeEndpoints()
                .withReferenceResolver(context.getReferenceResolver());
        action(builder);
        return builder;
    }

    @Override
    public ReceiveMessageActionBuilder<?> receive(Endpoint messageEndpoint) {
        ReceiveMessageActionBuilder<?> builder = new ReceiveMessageActionBuilder<>(ReceiveMessageAction.Builder.receive(messageEndpoint)
                .withReferenceResolver(context.getReferenceResolver()));
        action(builder);
        return builder;
    }

    @Override
    public ReceiveMessageActionBuilder<?> receive(String messageEndpointUri) {
        ReceiveMessageActionBuilder<?> builder = new ReceiveMessageActionBuilder<>(ReceiveMessageAction.Builder.receive(messageEndpointUri)
                .withReferenceResolver(context.getReferenceResolver()));
        action(builder);
        return builder;
    }

    @Override
    public SendMessageActionBuilder<?> send(Endpoint messageEndpoint) {
        SendMessageActionBuilder<?> builder = new SendMessageActionBuilder<>(SendMessageAction.Builder.send(messageEndpoint)
                .withReferenceResolver(context.getReferenceResolver()));
        action(builder);
        return builder;
    }

    @Override
    public SendMessageActionBuilder<?> send(String messageEndpointUri) {
        SendMessageActionBuilder<?> builder = new SendMessageActionBuilder<>(SendMessageAction.Builder.send(messageEndpointUri)
                .withReferenceResolver(context.getReferenceResolver()));
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
    public Wait.Builder<ActionCondition> waitFor() {
        Wait.Builder<ActionCondition> builder = new Wait.Builder<>() {
            @Override
            public WaitActionConditionBuilder execution() {
                final Sequence.Builder dummy = new Sequence.Builder();
                DefaultTestDesigner.this.containers.push(dummy);
                this.condition = new ActionCondition();
                return new WaitActionConditionBuilder(this) {
                    @Override
                    public WaitActionConditionBuilder action(TestActionBuilder<?> action) {
                        super.action(action);
                        DefaultTestDesigner.this.containers.remove(dummy);
                        return this;
                    }
                };
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
    public AssertSoapFaultBuilder assertSoapFault() {
        AssertSoapFaultBuilder builder = new AssertSoapFaultBuilder() {
            @Override
            public AssertSoapFaultBuilder actions(TestActionBuilder<?>... actions) {
                AssertSoapFaultBuilder builder = super.actions(actions);
                DefaultTestDesigner.this.action(builder);
                return builder;
            }
        };
        builder.withReferenceResolver(context.getReferenceResolver());
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
    public DockerExecuteActionBuilder docker() {
        DockerExecuteActionBuilder builder = new DockerExecuteActionBuilder();
        action(builder);
        return builder;
    }

    @Override
    public KubernetesExecuteActionBuilder kubernetes() {
        KubernetesExecuteActionBuilder builder = new KubernetesExecuteActionBuilder();
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
        HttpActionBuilder builder = new HttpActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        action(builder);
        return builder;
    }

    @Override
    public SoapActionBuilder soap() {
        SoapActionBuilder builder = new SoapActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        action(builder);
        return builder;
    }

    @Override
    public CamelRouteActionBuilder camel() {
        CamelRouteActionBuilder builder = new CamelRouteActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        action(builder);
        return builder;
    }

    @Override
    public ZooExecuteActionBuilder zookeeper() {
        ZooExecuteActionBuilder builder = new ZooExecuteActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        action(builder);
        return builder;
    }

    @Override
    public Template.Builder applyTemplate(String name) {
        Template.Builder builder = Template.Builder.applyTemplate(name)
                .withReferenceResolver(context.getReferenceResolver());
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
     * Gets the test context.
     * @return
     */
    public TestContext getTestContext() {
        return context;
    }

    @Override
    public void setTestContext(TestContext context) {
        this.context = context;
    }
}
