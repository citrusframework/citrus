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

package com.consol.citrus.dsl.junit;

import java.lang.reflect.Method;
import java.util.Date;

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
import com.consol.citrus.container.Template;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.container.Timer;
import com.consol.citrus.container.Wait;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.runner.ApplyTestBehaviorAction;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.junit.CitrusJUnit4Runner;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.selenium.actions.SeleniumActionBuilder;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.actions.SoapActionBuilder;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JUnit4CitrusTestRunner extends JUnit4CitrusTest implements TestRunner {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Test builder delegate */
    private TestRunner testRunner;

    @Override
    protected TestRunner createTestRunner(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestContext context) {
        testRunner = super.createTestRunner(frameworkMethod, context);
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
    public <T extends TestActionBuilder<?>> T run(T testAction) {
        return testRunner.run(testAction);
    }

    @Override
    public ApplyTestBehaviorAction.Builder applyBehavior(com.consol.citrus.dsl.runner.TestBehavior behavior) {
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
    public PurgeJmsQueuesAction.Builder purgeQueues(BuilderSupport<PurgeJmsQueuesAction.Builder> configurer) {
        return testRunner.purgeQueues(configurer);
    }

    @Override
    public PurgeMessageChannelAction.Builder purgeChannels(BuilderSupport<PurgeMessageChannelAction.Builder> configurer) {
        return testRunner.purgeChannels(configurer);
    }

    @Override
    public PurgeEndpointAction.Builder purgeEndpoints(BuilderSupport<PurgeEndpointAction.Builder> configurer) {
        return testRunner.purgeEndpoints(configurer);
    }

    @Override
    public ReceiveMessageAction.Builder receive(BuilderSupport<ReceiveMessageAction.Builder> configurer) {
        return testRunner.receive(configurer);
    }

    @Override
    public SendMessageAction.Builder send(BuilderSupport<SendMessageAction.Builder> configurer) {
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
    public AssertSoapFault.Builder assertSoapFault() {
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
    public DockerExecuteAction.Builder docker(BuilderSupport<DockerExecuteAction.Builder> configurer) {
        return testRunner.docker(configurer);
    }

    @Override
    public KubernetesExecuteAction.Builder kubernetes(BuilderSupport<KubernetesExecuteAction.Builder> configurer) {
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
    public ZooExecuteAction.Builder zookeeper(BuilderSupport<ZooExecuteAction.Builder> configurer) {
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
