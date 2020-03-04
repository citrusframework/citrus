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

import java.io.File;
import java.util.Arrays;
import java.util.Date;
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
import com.consol.citrus.condition.Condition;
import com.consol.citrus.condition.FileCondition;
import com.consol.citrus.condition.HttpCondition;
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
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.container.Timer;
import com.consol.citrus.container.Wait;
import com.consol.citrus.container.WaitActionConditionBuilder;
import com.consol.citrus.container.WaitFileConditionBuilder;
import com.consol.citrus.container.WaitHttpConditionBuilder;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.selenium.actions.SeleniumActionBuilder;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.actions.SoapActionBuilder;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;

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

        if (context.getReferenceResolver() != null) {
            try {
                testCase.setTestActionListeners(context.getReferenceResolver().resolve(TestActionListeners.class));

                if (!context.getReferenceResolver().resolveAll(SequenceBeforeTest.class).isEmpty()) {
                    testCase.setBeforeTest(Arrays.asList(context.getReferenceResolver().resolveAll(SequenceBeforeTest.class).values().toArray(new SequenceBeforeTest[]{})));
                }

                if (!context.getReferenceResolver().resolveAll(SequenceAfterTest.class).isEmpty()) {
                    testCase.setAfterTest(Arrays.asList(context.getReferenceResolver().resolveAll(SequenceAfterTest.class).values().toArray(new SequenceAfterTest[]{})));
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
    public PurgeJmsQueuesAction.Builder purgeQueues(BuilderSupport<PurgeJmsQueuesAction.Builder> configurer) {
        PurgeJmsQueuesAction.Builder builder = new PurgeJmsQueuesAction.Builder()
                .withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public PurgeMessageChannelAction.Builder purgeChannels(BuilderSupport<PurgeMessageChannelAction.Builder> configurer) {
        PurgeMessageChannelAction.Builder builder = new PurgeMessageChannelAction.Builder()
                .channelResolver(context.getReferenceResolver());
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
    public ReceiveMessageAction.Builder receive(BuilderSupport<ReceiveMessageAction.Builder> configurer) {
        ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder()
                .withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public SendMessageAction.Builder send(BuilderSupport<SendMessageAction.Builder> configurer) {
        SendMessageAction.Builder builder = new SendMessageAction.Builder()
                .withReferenceResolver(context.getReferenceResolver());
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
    public Wait.Builder waitFor() {
        return new Wait.Builder() {
            @Override
            public WaitActionConditionBuilder execution() {
                final Sequence.Builder dummy = new Sequence.Builder();
                ActionCondition condition = new ActionCondition();
                this.condition = condition;
                DefaultTestRunner.this.containers.push(dummy);
                return new WaitActionConditionBuilder(condition, this) {
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
                FileCondition condition = new FileCondition();
                this.condition = condition;
                return new WaitFileConditionBuilder(condition, this) {
                    @Override
                    public WaitFileConditionBuilder resource(File file) {
                        super.resource(file);
                        return run(this);
                    }
                };
            }

            @Override
            public WaitHttpConditionBuilder http() {
                HttpCondition condition = new HttpCondition();
                this.condition = condition;
                return new WaitHttpConditionBuilder(condition, this) {
                    @Override
                    public WaitHttpConditionBuilder url(String requestUrl) {
                        super.url(requestUrl);
                        return run(this);
                    }
                };
            }

            @Override
            public Wait.Builder condition(Condition condition) {
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
    public AssertSoapFault.Builder assertSoapFault() {
        AssertSoapFault.Builder builder = new AssertSoapFault.Builder() {
            @Override
            public AssertSoapFault.Builder actions(TestActionBuilder<?>... actions) {
                return run(super.actions(actions));
            }
        }.withReferenceResolver(context.getReferenceResolver());
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
    public DockerExecuteAction.Builder docker(BuilderSupport<DockerExecuteAction.Builder> configurer) {
        DockerExecuteAction.Builder builder = new DockerExecuteAction.Builder();
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public KubernetesExecuteAction.Builder kubernetes(BuilderSupport<KubernetesExecuteAction.Builder> configurer) {
        KubernetesExecuteAction.Builder builder = new KubernetesExecuteAction.Builder();
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
        HttpActionBuilder builder = HttpActionBuilder.http()
                    .withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public SoapActionBuilder soap(BuilderSupport<SoapActionBuilder> configurer) {
        SoapActionBuilder builder = new SoapActionBuilder()
                    .withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public CamelRouteActionBuilder camel(BuilderSupport<CamelRouteActionBuilder> configurer) {
        CamelRouteActionBuilder builder = new CamelRouteActionBuilder()
                    .withReferenceResolver(context.getReferenceResolver());
        configurer.configure(builder);
        return run(builder);
    }

    @Override
    public ZooExecuteAction.Builder zookeeper(BuilderSupport<ZooExecuteAction.Builder> configurer) {
        ZooExecuteAction.Builder builder = new ZooExecuteAction.Builder()
                .withReferenceResolver(context.getReferenceResolver());
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
        testCase.setTestRunner(true);
        return testCase;
    }

}
