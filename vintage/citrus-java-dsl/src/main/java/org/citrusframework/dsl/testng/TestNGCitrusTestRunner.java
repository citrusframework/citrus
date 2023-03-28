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

package org.citrusframework.dsl.testng;

import java.lang.reflect.Method;
import java.util.Date;

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
import org.citrusframework.actions.LoadPropertiesAction;
import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.actions.StartServerAction;
import org.citrusframework.actions.StopServerAction;
import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.actions.StopTimerAction;
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.actions.TransformAction;
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
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.builder.AssertSoapFaultBuilder;
import org.citrusframework.dsl.builder.BuilderSupport;
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
import org.citrusframework.dsl.runner.ApplyTestBehaviorAction;
import org.citrusframework.dsl.runner.TestBehavior;
import org.citrusframework.dsl.runner.TestRunner;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.server.Server;

/**
 * TestNG Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class TestNGCitrusTestRunner extends TestNGCitrusTest implements TestRunner {

    /** Test builder delegate */
    private TestRunner testRunner;

    @Override
    protected TestRunner createTestRunner(Method method, TestContext context) {
        testRunner = super.createTestRunner(method, context);
        return testRunner;
    }

    @Override
    protected final boolean isDesignerMethod(Method method) {
        return false;
    }

    @Override
    protected final boolean isRunnerMethod(Method method) {
        return true;
    }

    @Override
    public TestCase getTestCase() {
        return testRunner.getTestCase();
    }

    @Override
    public void testClass(Class<?> type) {
        testRunner.testClass(type);
    }

    @Override
    public void name(String name) {
        testRunner.name(name);
    }

    @Override
    public void description(String description) {
        testRunner.description(description);
    }

    @Override
    public void author(String author) {
        testRunner.author(author);
    }

    @Override
    public void packageName(String packageName) {
        testRunner.packageName(packageName);
    }

    @Override
    public void status(TestCaseMetaInfo.Status status) {
        testRunner.status(status);
    }

    @Override
    public void creationDate(Date date) {
        testRunner.creationDate(date);
    }

    @Override
    public void groups(String[] groups) {
        testRunner.groups(groups);
    }

    @Override
    public void start() {
        testRunner.start();
    }

    @Override
    public void stop() {
        testRunner.stop();
    }

    @Override
    public <T> T variable(String name, T value) {
        return testRunner.variable(name, value);
    }

    @Override
    public <A extends TestAction> TestActionBuilder<A> run(A testAction) {
        return testRunner.run(testAction);
    }

    @Override
    public <T extends TestActionBuilder<?>> T run(T testAction) {
        return testRunner.run(testAction);
    }

    @Override
    public ApplyTestBehaviorAction.Builder applyBehavior(TestBehavior behavior) {
        return testRunner.applyBehavior(behavior);
    }

    @Override
    public <T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container) {
        return testRunner.container(container);
    }

    @Override
    public <T extends TestActionContainerBuilder<? extends TestActionContainer, ?>> T container(T container) {
        return testRunner.container(container);
    }

    @Override
    public CreateVariablesAction.Builder createVariable(String variableName, String value) {
        return testRunner.createVariable(variableName, value);
    }

    @Override
    public AntRunAction.Builder antrun(BuilderSupport<AntRunAction.Builder> configurer) {
        return testRunner.antrun(configurer);
    }

    @Override
    public EchoAction.Builder echo(String message) {
        return testRunner.echo(message);
    }

    @Override
    public ExecutePLSQLAction.Builder plsql(BuilderSupport<ExecutePLSQLAction.Builder> configurer) {
        return testRunner.plsql(configurer);
    }

    @Override
    public ExecuteSQLAction.Builder sql(BuilderSupport<ExecuteSQLAction.Builder> configurer) {
        return testRunner.sql(configurer);
    }

    @Override
    public ExecuteSQLQueryAction.Builder query(BuilderSupport<ExecuteSQLQueryAction.Builder> configurer) {
        return testRunner.query(configurer);
    }

    @Override
    public ReceiveTimeoutAction.Builder receiveTimeout(BuilderSupport<ReceiveTimeoutAction.Builder> configurer) {
        return testRunner.receiveTimeout(configurer);
    }

    @Override
    public FailAction.Builder fail(String message) {
        return testRunner.fail(message);
    }

    @Override
    public InputAction.Builder input(BuilderSupport<InputAction.Builder> configurer) {
        return testRunner.input(configurer);
    }

    @Override
    public LoadPropertiesAction.Builder load(String filePath) {
        return testRunner.load(filePath);
    }

    @Override
    public PurgeJmsQueuesActionBuilder purgeQueues(BuilderSupport<PurgeJmsQueuesActionBuilder> configurer) {
        return testRunner.purgeQueues(configurer);
    }

    @Override
    public PurgeMessageChannelActionBuilder purgeChannels(BuilderSupport<PurgeMessageChannelActionBuilder> configurer) {
        return testRunner.purgeChannels(configurer);
    }

    @Override
    public PurgeEndpointAction.Builder purgeEndpoints(BuilderSupport<PurgeEndpointAction.Builder> configurer) {
        return testRunner.purgeEndpoints(configurer);
    }

    @Override
    public ReceiveMessageActionBuilder<?> receive(BuilderSupport<ReceiveMessageActionBuilder<?>> configurer) {
        return testRunner.receive(configurer);
    }

    @Override
    public SendMessageActionBuilder<?> send(BuilderSupport<SendMessageActionBuilder<?>> configurer) {
        return testRunner.send(configurer);
    }

    @Override
    public SleepAction.Builder sleep() {
        return testRunner.sleep();
    }

    @Override
    public SleepAction.Builder sleep(long milliseconds) {
        return testRunner.sleep(milliseconds);
    }

    @Override
    public Wait.Builder waitFor() {
        return testRunner.waitFor();
    }

    @Override
    public StartServerAction.Builder start(Server... servers) {
        return testRunner.start(servers);
    }

    @Override
    public StartServerAction.Builder start(Server server) {
        return testRunner.start(server);
    }

    @Override
    public StopServerAction.Builder stop(Server... servers) {
        return testRunner.stop(servers);
    }

    @Override
    public StopServerAction.Builder stop(Server server) {
        return testRunner.stop(server);
    }

    @Override
    public StopTimeAction.Builder stopTime() {
        return testRunner.stopTime();
    }

    @Override
    public StopTimeAction.Builder stopTime(String id) {
        return testRunner.stopTime(id);
    }

    @Override
    public StopTimeAction.Builder stopTime(String id, String suffix) {
        return testRunner.stopTime(id, suffix);
    }

    @Override
    public TraceVariablesAction.Builder traceVariables() {
        return testRunner.traceVariables();
    }

    @Override
    public TraceVariablesAction.Builder traceVariables(String... variables) {
        return testRunner.traceVariables(variables);
    }

    @Override
    public GroovyAction.Builder groovy(BuilderSupport<GroovyAction.Builder> configurer) {
        return testRunner.groovy(configurer);
    }

    @Override
    public TransformAction.Builder transform(BuilderSupport<TransformAction.Builder> configurer) {
        return testRunner.transform(configurer);
    }

    @Override
    public Assert.Builder assertException() {
        return testRunner.assertException();
    }

    @Override
    public Catch.Builder catchException() {
        return testRunner.catchException();
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault() {
        return testRunner.assertSoapFault();
    }

    @Override
    public Conditional.Builder conditional() {
        return testRunner.conditional();
    }

    @Override
    public Iterate.Builder iterate() {
        return testRunner.iterate();
    }

    @Override
    public Parallel.Builder parallel() {
        return testRunner.parallel();
    }

    @Override
    public RepeatOnErrorUntilTrue.Builder repeatOnError() {
        return testRunner.repeatOnError();
    }

    @Override
    public RepeatUntilTrue.Builder repeat() {
        return testRunner.repeat();
    }

    @Override
    public Sequence.Builder sequential() {
        return testRunner.sequential();
    }

    @Override
    public Async.Builder async() {
        return testRunner.async();
    }

    @Override
    public Timer.Builder timer() {
        return testRunner.timer();
    }

    @Override
    public StopTimerAction.Builder stopTimer(String timerId) {
        return testRunner.stopTimer(timerId);
    }

    @Override
    public StopTimerAction.Builder stopTimers() {
        return testRunner.stopTimers();
    }

    @Override
    public DockerExecuteActionBuilder docker(BuilderSupport<DockerExecuteActionBuilder> configurer) {
        return testRunner.docker(configurer);
    }

    @Override
    public KubernetesExecuteActionBuilder kubernetes(BuilderSupport<KubernetesExecuteActionBuilder> configurer) {
        return testRunner.kubernetes(configurer);
    }

    @Override
    public SeleniumActionBuilder selenium(BuilderSupport<SeleniumActionBuilder> configurer) {
        return testRunner.selenium(configurer);
    }

    @Override
    public HttpActionBuilder http(BuilderSupport<HttpActionBuilder> configurer) {
        return testRunner.http(configurer);
    }

    @Override
    public SoapActionBuilder soap(BuilderSupport<SoapActionBuilder> configurer) {
        return testRunner.soap(configurer);
    }

    @Override
    public CamelRouteActionBuilder camel(BuilderSupport<CamelRouteActionBuilder> configurer) {
        return testRunner.camel(configurer);
    }

    @Override
    public ZooExecuteActionBuilder zookeeper(BuilderSupport<ZooExecuteActionBuilder> configurer) {
        return testRunner.zookeeper(configurer);
    }

    @Override
    public Template.Builder applyTemplate(BuilderSupport<Template.Builder> configurer) {
        return testRunner.applyTemplate(configurer);
    }

    @Override
    public FinallySequence.Builder doFinally() {
        return testRunner.doFinally();
    }

    @Override
    public void setTestContext(TestContext context) {
        testRunner.setTestContext(context);
    }
}
