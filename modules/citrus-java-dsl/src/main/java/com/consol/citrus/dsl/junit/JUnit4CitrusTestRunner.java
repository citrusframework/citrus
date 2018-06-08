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

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.runner.*;
import com.consol.citrus.dsl.simulation.TestSimulator;
import com.consol.citrus.junit.CitrusJUnit4Runner;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * JUnit Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JUnit4CitrusTestRunner extends JUnit4CitrusTest implements TestRunner, TestSimulator {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Test builder delegate */
    private TestRunner testRunner;

    @Override
    public void simulate(Method method, TestContext context, ApplicationContext applicationContext) {
        setApplicationContext(applicationContext);
        testRunner = new TestRunnerSimulation(createTestRunner(new CitrusJUnit4Runner.CitrusFrameworkMethod(method, method.getName(), method.getDeclaringClass().getPackage().getName()), context).getTestCase(), applicationContext, context);
    }

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
    public <T extends TestAction> T run(T testAction) {
        return testRunner.run(testAction);
    }

    @Override
    public ApplyTestBehaviorAction applyBehavior(com.consol.citrus.dsl.runner.TestBehavior behavior) {
        return testRunner.applyBehavior(behavior);
    }

    @Override
    public <T extends AbstractActionContainer> AbstractTestContainerBuilder<T> container(T container) {
        return testRunner.container(container);
    }

    @Override
    public CreateVariablesAction createVariable(String variableName, String value) {
        return testRunner.createVariable(variableName, value);
    }

    @Override
    public AntRunAction antrun(BuilderSupport<AntRunBuilder> configurer) {
        return testRunner.antrun(configurer);
    }

    @Override
    public EchoAction echo(String message) {
        return testRunner.echo(message);
    }

    @Override
    public ExecutePLSQLAction plsql(BuilderSupport<ExecutePLSQLBuilder> configurer) {
        return testRunner.plsql(configurer);
    }

    @Override
    public ExecuteSQLAction sql(BuilderSupport<ExecuteSQLBuilder> configurer) {
        return testRunner.sql(configurer);
    }

    @Override
    public ExecuteSQLQueryAction query(BuilderSupport<ExecuteSQLQueryBuilder> configurer) {
        return testRunner.query(configurer);
    }

    @Override
    public ReceiveTimeoutAction receiveTimeout(BuilderSupport<ReceiveTimeoutBuilder> configurer) {
        return testRunner.receiveTimeout(configurer);
    }

    @Override
    public FailAction fail(String message) {
        return testRunner.fail(message);
    }

    @Override
    public InputAction input(BuilderSupport<InputActionBuilder> configurer) {
        return testRunner.input(configurer);
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        return testRunner.load(filePath);
    }

    @Override
    public TestAction purgeQueues(BuilderSupport<PurgeJmsQueuesBuilder> configurer) {
        return testRunner.purgeQueues(configurer);
    }

    @Override
    public PurgeMessageChannelAction purgeChannels(BuilderSupport<PurgeChannelsBuilder> configurer) {
        return testRunner.purgeChannels(configurer);
    }

    @Override
    public PurgeEndpointAction purgeEndpoints(BuilderSupport<PurgeEndpointsBuilder> configurer) {
        return testRunner.purgeEndpoints(configurer);
    }

    @Override
    public ReceiveMessageAction receive(BuilderSupport<ReceiveMessageBuilder> configurer) {
        return testRunner.receive(configurer);
    }

    @Override
    public SendMessageAction send(BuilderSupport<SendMessageBuilder> configurer) {
        return testRunner.send(configurer);
    }

    @Override
    public SleepAction sleep() {
        return testRunner.sleep();
    }

    @Override
    public SleepAction sleep(long milliseconds) {
        return testRunner.sleep(milliseconds);
    }

    @Override
    @Deprecated
    public Wait waitFor(BuilderSupport<WaitBuilder> configurer) {
        return testRunner.waitFor(configurer);
    }

    @Override
    public WaitBuilder waitFor() {
        return testRunner.waitFor();
    }

    @Override
    public StartServerAction start(Server... servers) {
        return testRunner.start(servers);
    }

    @Override
    public StartServerAction start(Server server) {
        return testRunner.start(server);
    }

    @Override
    public StopServerAction stop(Server... servers) {
        return testRunner.stop(servers);
    }

    @Override
    public StopServerAction stop(Server server) {
        return testRunner.stop(server);
    }

    @Override
    public StopTimeAction stopTime() {
        return testRunner.stopTime();
    }

    @Override
    public StopTimeAction stopTime(String id) {
        return testRunner.stopTime(id);
    }

    @Override
    public StopTimeAction stopTime(String id, String suffix) {
        return testRunner.stopTime(id, suffix);
    }

    @Override
    public TraceVariablesAction traceVariables() {
        return testRunner.traceVariables();
    }

    @Override
    public TraceVariablesAction traceVariables(String... variables) {
        return testRunner.traceVariables(variables);
    }

    @Override
    public GroovyAction groovy(BuilderSupport<GroovyActionBuilder> configurer) {
        return testRunner.groovy(configurer);
    }

    @Override
    public TransformAction transform(BuilderSupport<TransformActionBuilder> configurer) {
        return testRunner.transform(configurer);
    }

    @Override
    public AssertExceptionBuilder assertException() {
        return testRunner.assertException();
    }

    @Override
    public CatchExceptionBuilder catchException() {
        return testRunner.catchException();
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault() {
        return testRunner.assertSoapFault();
    }

    @Override
    public ConditionalBuilder conditional() {
        return testRunner.conditional();
    }

    @Override
    public IterateBuilder iterate() {
        return testRunner.iterate();
    }

    @Override
    public ParallelBuilder parallel() {
        return testRunner.parallel();
    }

    @Override
    public RepeatOnErrorBuilder repeatOnError() {
        return testRunner.repeatOnError();
    }

    @Override
    public RepeatBuilder repeat() {
        return testRunner.repeat();
    }

    @Override
    public SequenceBuilder sequential() {
        return testRunner.sequential();
    }

    @Override
    public AsyncBuilder async() {
        return testRunner.async();
    }

    @Override
    public TimerBuilder timer() {
        return testRunner.timer();
    }

    @Override
    public StopTimerAction stopTimer(String timerId) {
        return testRunner.stopTimer(timerId);
    }

    @Override
    public StopTimerAction stopTimers() {
        return testRunner.stopTimers();
    }

    @Override
    public TestAction docker(BuilderSupport<DockerActionBuilder> configurer) {
        return testRunner.docker(configurer);
    }

    @Override
    public TestAction kubernetes(BuilderSupport<KubernetesActionBuilder> configurer) {
        return testRunner.kubernetes(configurer);
    }

    @Override
    public TestAction selenium(BuilderSupport<SeleniumActionBuilder> configurer) {
        return testRunner.selenium(configurer);
    }

    @Override
    public TestAction http(BuilderSupport<HttpActionBuilder> configurer) {
        return testRunner.http(configurer);
    }

    @Override
    public TestAction soap(BuilderSupport<SoapActionBuilder> configurer) {
        return testRunner.soap(configurer);
    }

    @Override
    public TestAction camel(BuilderSupport<CamelRouteActionBuilder> configurer) {
        return testRunner.camel(configurer);
    }

    @Override
    public TestAction zookeeper(BuilderSupport<ZooActionBuilder> configurer) {
        return testRunner.zookeeper(configurer);
    }

    @Override
    public Template applyTemplate(BuilderSupport<TemplateBuilder> configurer) {
        return testRunner.applyTemplate(configurer);
    }

    @Override
    public FinallySequenceBuilder doFinally() {
        return testRunner.doFinally();
    }
}
