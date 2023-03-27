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

package org.citrusframework.citrus.dsl.runner;

import java.io.File;
import java.util.Date;
import java.util.Stack;

import org.citrusframework.citrus.AbstractTestContainerBuilder;
import org.citrusframework.citrus.DefaultTestCase;
import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.TestActionContainerBuilder;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.TestCaseMetaInfo;
import org.citrusframework.citrus.actions.AntRunAction;
import org.citrusframework.citrus.actions.CreateVariablesAction;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.actions.ExecutePLSQLAction;
import org.citrusframework.citrus.actions.ExecuteSQLAction;
import org.citrusframework.citrus.actions.ExecuteSQLQueryAction;
import org.citrusframework.citrus.actions.FailAction;
import org.citrusframework.citrus.actions.InputAction;
import org.citrusframework.citrus.actions.LoadPropertiesAction;
import org.citrusframework.citrus.actions.PurgeEndpointAction;
import org.citrusframework.citrus.actions.ReceiveMessageAction;
import org.citrusframework.citrus.actions.ReceiveTimeoutAction;
import org.citrusframework.citrus.actions.SendMessageAction;
import org.citrusframework.citrus.actions.SleepAction;
import org.citrusframework.citrus.actions.StartServerAction;
import org.citrusframework.citrus.actions.StopServerAction;
import org.citrusframework.citrus.actions.StopTimeAction;
import org.citrusframework.citrus.actions.StopTimerAction;
import org.citrusframework.citrus.actions.TraceVariablesAction;
import org.citrusframework.citrus.actions.TransformAction;
import org.citrusframework.citrus.condition.ActionCondition;
import org.citrusframework.citrus.condition.Condition;
import org.citrusframework.citrus.condition.FileCondition;
import org.citrusframework.citrus.condition.HttpCondition;
import org.citrusframework.citrus.container.Assert;
import org.citrusframework.citrus.container.Async;
import org.citrusframework.citrus.container.Catch;
import org.citrusframework.citrus.container.Conditional;
import org.citrusframework.citrus.container.FinallySequence;
import org.citrusframework.citrus.container.Iterate;
import org.citrusframework.citrus.container.Parallel;
import org.citrusframework.citrus.container.RepeatOnErrorUntilTrue;
import org.citrusframework.citrus.container.RepeatUntilTrue;
import org.citrusframework.citrus.container.Sequence;
import org.citrusframework.citrus.container.Template;
import org.citrusframework.citrus.container.TestActionContainer;
import org.citrusframework.citrus.container.Timer;
import org.citrusframework.citrus.container.Wait;
import org.citrusframework.citrus.container.WaitActionConditionBuilder;
import org.citrusframework.citrus.container.WaitFileConditionBuilder;
import org.citrusframework.citrus.container.WaitHttpConditionBuilder;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.builder.AssertSoapFaultBuilder;
import org.citrusframework.citrus.dsl.builder.BuilderSupport;
import org.citrusframework.citrus.dsl.builder.CamelRouteActionBuilder;
import org.citrusframework.citrus.dsl.builder.DockerExecuteActionBuilder;
import org.citrusframework.citrus.dsl.builder.HttpActionBuilder;
import org.citrusframework.citrus.dsl.builder.KubernetesExecuteActionBuilder;
import org.citrusframework.citrus.dsl.builder.PurgeJmsQueuesActionBuilder;
import org.citrusframework.citrus.dsl.builder.PurgeMessageChannelActionBuilder;
import org.citrusframework.citrus.dsl.builder.ReceiveMessageActionBuilder;
import org.citrusframework.citrus.dsl.builder.SeleniumActionBuilder;
import org.citrusframework.citrus.dsl.builder.SendMessageActionBuilder;
import org.citrusframework.citrus.dsl.builder.SoapActionBuilder;
import org.citrusframework.citrus.dsl.builder.ZooExecuteActionBuilder;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.script.GroovyAction;
import org.citrusframework.citrus.server.Server;

/**
 * Default test runner implementation. Provides Java DSL methods for test actions. Immediately executes test actions as
 * they were built. This way the test case grows with each test action and changes for instance to the test context (variables) are
 * immediately visible.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class DefaultTestRunner implements TestRunner {

    /** Test case */
    private final DefaultTestCase testCase;

    /** This runners test context */
    private TestContext context;

    /** Optional stack of containers cached for execution */
    protected Stack<TestActionContainerBuilder<? extends TestActionContainer, ?>> containers = new Stack<>();

    /** Default constructor */
    public DefaultTestRunner() {
        testCase = new DefaultTestCase();
        testClass(this.getClass());
        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    /**
     * Constructor initializing test case.
     * @param testCase
     */
    protected DefaultTestRunner(DefaultTestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Constructor using Spring bean application context.
     * @param context
     */
    public DefaultTestRunner(TestContext context) {
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
        this.testCase.setGroups(groups);
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
    public <A extends TestAction> TestActionBuilder<A> run(A testAction) {
        return run((TestActionBuilder<A>)() -> testAction);
    }

    @Override
    public <T extends TestActionBuilder<?>> T run(T builder) {
        if (builder instanceof TestActionContainerBuilder<?, ?>) {
            if (containers.lastElement().equals(builder)) {
                containers.pop();
            } else {
                throw new CitrusRuntimeException("Invalid use of action containers - the container execution is not expected!");
            }

            if (builder instanceof FinallySequence.Builder) {
                ((FinallySequence.Builder) builder).getActions().forEach(testCase::addFinalAction);
                return builder;
            }
        }

        if (containers.isEmpty()) {
            testCase.addTestAction(builder);
            testCase.executeAction(builder.build(), context);
        } else {
            containers.lastElement().getActions().add(builder);
        }

        return builder;
    }

    @Override
    public ApplyTestBehaviorAction.Builder applyBehavior(TestBehavior behavior) {
        ApplyTestBehaviorAction.Builder builder = new ApplyTestBehaviorAction.Builder()
                .runner(this)
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
                return run(super.actions(actions));
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
        return run(builder);
    }

    @Override
    public AntRunAction.Builder antrun(BuilderSupport<AntRunAction.Builder> configurer) {
        AntRunAction.Builder builder = new AntRunAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public EchoAction.Builder echo(String message) {
        return run(new EchoAction.Builder().message(message));
    }

    @Override
    public ExecutePLSQLAction.Builder plsql(BuilderSupport<ExecutePLSQLAction.Builder> configurer) {
        ExecutePLSQLAction.Builder builder = new ExecutePLSQLAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public ExecuteSQLAction.Builder sql(BuilderSupport<ExecuteSQLAction.Builder> configurer) {
        ExecuteSQLAction.Builder builder = new ExecuteSQLAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public ExecuteSQLQueryAction.Builder query(BuilderSupport<ExecuteSQLQueryAction.Builder> configurer) {
        ExecuteSQLQueryAction.Builder builder = new ExecuteSQLQueryAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public FailAction.Builder fail(String message) {
        return run(new FailAction.Builder().message(message));
    }

    @Override
    public InputAction.Builder input(BuilderSupport<InputAction.Builder> configurer) {
        InputAction.Builder builder = new InputAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public ReceiveTimeoutAction.Builder receiveTimeout(BuilderSupport<ReceiveTimeoutAction.Builder> configurer) {
        ReceiveTimeoutAction.Builder builder = new ReceiveTimeoutAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public LoadPropertiesAction.Builder load(String filePath) {
        LoadPropertiesAction.Builder builder = new LoadPropertiesAction.Builder()
                .filePath(filePath);
        return run(builder);
    }

    @Override
    public PurgeJmsQueuesActionBuilder purgeQueues(BuilderSupport<PurgeJmsQueuesActionBuilder> configurer) {
        PurgeJmsQueuesActionBuilder builder = new PurgeJmsQueuesActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public PurgeMessageChannelActionBuilder purgeChannels(BuilderSupport<PurgeMessageChannelActionBuilder> configurer) {
        PurgeMessageChannelActionBuilder builder = new PurgeMessageChannelActionBuilder();
        builder.channelResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public PurgeEndpointAction.Builder purgeEndpoints(BuilderSupport<PurgeEndpointAction.Builder> configurer) {
        PurgeEndpointAction.Builder builder = new PurgeEndpointAction.Builder()
                .withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public ReceiveMessageActionBuilder<?> receive(BuilderSupport<ReceiveMessageActionBuilder<?>> configurer) {
        ReceiveMessageActionBuilder<?> builder = new ReceiveMessageActionBuilder<>(new ReceiveMessageAction.Builder()
                .withReferenceResolver(context.getReferenceResolver()));
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public SendMessageActionBuilder<?> send(BuilderSupport<SendMessageActionBuilder<?>> configurer) {
        SendMessageActionBuilder<?> builder = new SendMessageActionBuilder<>(new SendMessageAction.Builder()
                .withReferenceResolver(context.getReferenceResolver()));
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public SleepAction.Builder sleep() {
        return run(new SleepAction.Builder());
    }

    @Override
    public SleepAction.Builder sleep(long milliseconds) {
        SleepAction.Builder builder = new SleepAction.Builder()
                .milliseconds(milliseconds);
        return run(builder);
    }

    @Override
    public Wait.Builder<Condition> waitFor() {
        return new Wait.Builder<>() {
            @Override
            public WaitActionConditionBuilder execution() {
                final Sequence.Builder dummy = new Sequence.Builder();
                DefaultTestRunner.this.containers.push(dummy);

                Wait.Builder<ActionCondition> actionConditionBuilder = new Wait.Builder<>();
                actionConditionBuilder.interval(this.interval);
                actionConditionBuilder.milliseconds(this.time);
                actionConditionBuilder.condition(new ActionCondition());

                this.condition = actionConditionBuilder.getCondition();

                return new WaitActionConditionBuilder(actionConditionBuilder) {
                    @Override
                    public WaitActionConditionBuilder action(TestActionBuilder<?> action) {
                        super.action(action);
                        DefaultTestRunner.this.containers.remove(dummy);
                        return run(this);
                    }
                };
            }

            @Override
            public WaitFileConditionBuilder file() {
                Wait.Builder<FileCondition> fileConditionBuilder = new Wait.Builder<>();
                fileConditionBuilder.interval(this.interval);
                fileConditionBuilder.milliseconds(this.time);
                fileConditionBuilder.condition(new FileCondition());

                this.condition = fileConditionBuilder.getCondition();

                return new WaitFileConditionBuilder(fileConditionBuilder) {
                    @Override
                    public WaitFileConditionBuilder resource(File file) {
                        super.resource(file);
                        return run(this);
                    }
                };
            }

            @Override
            public WaitHttpConditionBuilder http() {
                Wait.Builder<HttpCondition> httpConditionBuilder = new Wait.Builder<>();
                httpConditionBuilder.interval(this.interval);
                httpConditionBuilder.milliseconds(this.time);
                httpConditionBuilder.condition(new HttpCondition());

                this.condition = httpConditionBuilder.getCondition();

                return new WaitHttpConditionBuilder(httpConditionBuilder) {
                    @Override
                    public WaitHttpConditionBuilder url(String requestUrl) {
                        super.url(requestUrl);
                        return run(this);
                    }
                };
            }

            @Override
            public Wait.Builder<Condition> condition(Condition condition) {
                super.condition(condition);
                return run(this);
            }
        };
    }

    @Override
    public StartServerAction.Builder start(Server... servers) {
        StartServerAction.Builder builder = new StartServerAction.Builder()
                .server(servers);
        return run(builder);
    }

    @Override
    public StartServerAction.Builder start(Server server) {
        StartServerAction.Builder builder = new StartServerAction.Builder()
                .server(server);
        return run(builder);
    }

    @Override
    public StopServerAction.Builder stop(Server... servers) {
        StopServerAction.Builder builder = new StopServerAction.Builder()
                .server(servers);
        return run(builder);
    }

    @Override
    public StopServerAction.Builder stop(Server server) {
        StopServerAction.Builder builder = new StopServerAction.Builder()
                .server(server);
        return run(builder);
    }

    @Override
    public StopTimeAction.Builder stopTime() {
        return run(new StopTimeAction.Builder());
    }

    @Override
    public StopTimeAction.Builder stopTime(String id) {
        StopTimeAction.Builder builder = new StopTimeAction.Builder()
                .id(id);
        return run(builder);
    }

    @Override
    public StopTimeAction.Builder stopTime(String id, String suffix) {
        StopTimeAction.Builder builder = new StopTimeAction.Builder()
                .id(id)
                .suffix(suffix);
        return run(builder);
    }

    @Override
    public TraceVariablesAction.Builder traceVariables() {
        return run(new TraceVariablesAction.Builder());
    }

    @Override
    public TraceVariablesAction.Builder traceVariables(String... variables) {
        TraceVariablesAction.Builder builder = new TraceVariablesAction.Builder()
                .variables(variables);
        return run(builder);
    }

    @Override
    public GroovyAction.Builder groovy(BuilderSupport<GroovyAction.Builder> configurer) {
        GroovyAction.Builder builder = new GroovyAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public TransformAction.Builder transform(BuilderSupport<TransformAction.Builder> configurer) {
        TransformAction.Builder builder = new TransformAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public Assert.Builder assertException() {
        Assert.Builder builder = new Assert.Builder() {
            @Override
            public Assert.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public Catch.Builder catchException() {
        Catch.Builder builder = new Catch.Builder() {
            @Override
            public Catch.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault() {
        AssertSoapFaultBuilder builder = new AssertSoapFaultBuilder() {
            @Override
            public AssertSoapFaultBuilder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
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
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public Iterate.Builder iterate() {
        Iterate.Builder builder = new Iterate.Builder() {
            @Override
            public Iterate.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public Parallel.Builder parallel() {
        Parallel.Builder builder = new Parallel.Builder() {
            @Override
            public Parallel.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public RepeatOnErrorUntilTrue.Builder repeatOnError() {
        RepeatOnErrorUntilTrue.Builder builder = new RepeatOnErrorUntilTrue.Builder() {
            @Override
            public RepeatOnErrorUntilTrue.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public RepeatUntilTrue.Builder repeat() {
        RepeatUntilTrue.Builder builder = new RepeatUntilTrue.Builder() {
            @Override
            public RepeatUntilTrue.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public Sequence.Builder sequential() {
        Sequence.Builder builder = new Sequence.Builder() {
            @Override
            public Sequence.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public Async.Builder async() {
        Async.Builder builder = new Async.Builder() {
            @Override
            public Async.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public Timer.Builder timer() {
        Timer.Builder builder = new Timer.Builder() {
            @Override
            public Timer.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
    }

    @Override
    public StopTimerAction.Builder stopTimer(String timerId) {
        StopTimerAction.Builder builder = new StopTimerAction.Builder()
                .id(timerId);
        return run(builder);
    }

    @Override
    public StopTimerAction.Builder stopTimers() {
        return run(new StopTimerAction.Builder());
    }

    @Override
    public DockerExecuteActionBuilder docker(BuilderSupport<DockerExecuteActionBuilder> configurer) {
        DockerExecuteActionBuilder builder = new DockerExecuteActionBuilder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public KubernetesExecuteActionBuilder kubernetes(BuilderSupport<KubernetesExecuteActionBuilder> configurer) {
        KubernetesExecuteActionBuilder builder = new KubernetesExecuteActionBuilder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public SeleniumActionBuilder selenium(BuilderSupport<SeleniumActionBuilder> configurer) {
        SeleniumActionBuilder builder = new SeleniumActionBuilder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public HttpActionBuilder http(BuilderSupport<HttpActionBuilder> configurer) {
        HttpActionBuilder builder = new HttpActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public SoapActionBuilder soap(BuilderSupport<SoapActionBuilder> configurer) {
        SoapActionBuilder builder = new SoapActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public CamelRouteActionBuilder camel(BuilderSupport<CamelRouteActionBuilder> configurer) {
        CamelRouteActionBuilder builder = new CamelRouteActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public ZooExecuteActionBuilder zookeeper(BuilderSupport<ZooExecuteActionBuilder> configurer) {
        ZooExecuteActionBuilder builder = new ZooExecuteActionBuilder();
        builder.withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public Template.Builder applyTemplate(BuilderSupport<Template.Builder> configurer) {
        Template.Builder builder = new Template.Builder()
                .withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);

        return run(builder);
    }

    @Override
    public FinallySequence.Builder doFinally() {
        FinallySequence.Builder builder = new FinallySequence.Builder() {
            @Override
            public FinallySequence.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        };
        return container(builder);
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

    @Override
    public TestCase getTestCase() {
        testCase.setIncremental(true);
        return testCase;
    }

}
