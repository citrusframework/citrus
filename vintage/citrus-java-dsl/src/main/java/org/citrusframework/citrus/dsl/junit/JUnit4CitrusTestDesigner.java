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

package org.citrusframework.citrus.dsl.junit;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import javax.sql.DataSource;

import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.TestActionContainerBuilder;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.TestCaseMetaInfo;
import org.citrusframework.citrus.TestResult;
import org.citrusframework.citrus.actions.*;
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
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.builder.AssertSoapFaultBuilder;
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
import org.citrusframework.citrus.dsl.design.ApplyTestBehaviorAction;
import org.citrusframework.citrus.dsl.design.DefaultTestDesigner;
import org.citrusframework.citrus.dsl.design.TestBehavior;
import org.citrusframework.citrus.dsl.design.TestDesigner;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.exceptions.TestCaseFailedException;
import org.citrusframework.citrus.junit.CitrusFrameworkMethod;
import org.citrusframework.citrus.script.GroovyAction;
import org.citrusframework.citrus.server.Server;
import org.springframework.core.io.Resource;

/**
 * JUnit Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JUnit4CitrusTestDesigner extends JUnit4CitrusTest implements TestDesigner {

    /** Test builder delegate */
    private TestDesigner testDesigner;

    @Override
    protected TestDesigner createTestDesigner(CitrusFrameworkMethod frameworkMethod, TestContext context) {
        testDesigner = super.createTestDesigner(frameworkMethod, context);
        return testDesigner;
    }

    @Override
    protected void invokeTestMethod(CitrusFrameworkMethod frameworkMethod, TestCase testCase, TestContext context) {
        if (isConfigure(frameworkMethod.getMethod())) {
            try {
                configure();
                citrus.run(testCase, context);
            } catch (TestCaseFailedException e) {
                throw e;
            } catch (Exception | AssertionError e) {
                testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
                throw new TestCaseFailedException(e);
            } finally {
                testCase.finish(context);
            }
        } else {
            super.invokeTestMethod(frameworkMethod, testCase, context);
        }
    }

    @Override
    protected final boolean isDesignerMethod(Method method) {
        return true;
    }

    @Override
    protected final boolean isRunnerMethod(Method method) {
        return false;
    }

    /**
     * Main entrance method for builder pattern usage. Subclasses may override
     * this method and call Java DSL builder methods for adding test actions and
     * basic test case properties.
     */
    protected void configure() {
    }

    /**
     * Checks if the given method is this designer's configure method.
     * @param method
     * @return
     */
    private boolean isConfigure(Method method) {
        return method.getDeclaringClass().equals(this.getClass()) && method.getName().equals("configure");
    }

    @Override
    public TestCase getTestCase() {
        return testDesigner.getTestCase();
    }

    @Override
    public void testClass(Class<?> type) {
        testDesigner.testClass(type);
    }

    @Override
    public void name(String name) {
        testDesigner.name(name);
    }

    @Override
    public void description(String description) {
        testDesigner.description(description);
    }

    @Override
    public void author(String author) {
        testDesigner.author(author);
    }

    @Override
    public void packageName(String packageName) {
        testDesigner.packageName(packageName);
    }

    @Override
    public void status(TestCaseMetaInfo.Status status) {
        testDesigner.status(status);
    }

    @Override
    public void creationDate(Date date) {
        testDesigner.creationDate(date);
    }

    @Override
    public void groups(String[] groups) {
        testDesigner.groups(groups);
    }

    @Override
    public <T> T variable(String name, T value) {
        return testDesigner.variable(name, value);
    }

    @Override
    public CreateVariablesAction.Builder createVariable(String variableName, String value) {
        return testDesigner.createVariable(variableName, value);
    }

    @Override
    public void action(TestAction testAction) {
        testDesigner.action(testAction);
    }

    @Override
    public void action(TestActionBuilder<?> builder) {
        testDesigner.action(builder);
    }

    @Override
    public ApplyTestBehaviorAction.Builder applyBehavior(TestBehavior behavior) {
        return testDesigner.applyBehavior(behavior);
    }

    @Override
    public <T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container) {
        return testDesigner.container(container);
    }

    @Override
    public <T extends TestActionContainerBuilder<? extends TestActionContainer, ?>> T container(T container) {
        return testDesigner.container(container);
    }

    @Override
    public AntRunAction.Builder antrun(String buildFilePath) {
        return testDesigner.antrun(buildFilePath);
    }

    @Override
    public EchoAction.Builder echo(String message) {
        return testDesigner.echo(message);
    }

    @Override
    public ExecutePLSQLAction.Builder plsql(DataSource dataSource) {
        return testDesigner.plsql(dataSource);
    }

    @Override
    public ExecuteSQLAction.Builder sql(DataSource dataSource) {
        return testDesigner.sql(dataSource);
    }

    @Override
    public ExecuteSQLQueryAction.Builder query(DataSource dataSource) {
        return testDesigner.query(dataSource);
    }

    @Override
    public ReceiveTimeoutAction.Builder receiveTimeout(Endpoint messageEndpoint) {
        return testDesigner.receiveTimeout(messageEndpoint);
    }

    @Override
    public ReceiveTimeoutAction.Builder receiveTimeout(String messageEndpointName) {
        return testDesigner.receiveTimeout(messageEndpointName);
    }

    @Override
    public FailAction.Builder fail(String message) {
        return testDesigner.fail(message);
    }

    @Override
    public InputAction.Builder input() {
        return testDesigner.input();
    }

    @Override
    public JavaAction.Builder java(String className) {
        return testDesigner.java(className);
    }

    @Override
    public JavaAction.Builder java(Class<?> clazz) {
        return testDesigner.java(clazz);
    }

    @Override
    public JavaAction.Builder java(Object instance) {
        return testDesigner.java(instance);
    }

    @Override
    public LoadPropertiesAction.Builder load(String filePath) {
        return testDesigner.load(filePath);
    }

    @Override
    public PurgeJmsQueuesActionBuilder purgeQueues() {
        return testDesigner.purgeQueues();
    }

    @Override
    public PurgeMessageChannelActionBuilder purgeChannels() {
        return testDesigner.purgeChannels();
    }

    @Override
    public PurgeEndpointAction.Builder purgeEndpoints() {
        return testDesigner.purgeEndpoints();
    }

    @Override
    public ReceiveMessageActionBuilder<?> receive(Endpoint messageEndpoint) {
        return testDesigner.receive(messageEndpoint);
    }

    @Override
    public ReceiveMessageActionBuilder<?> receive(String messageEndpointName) {
        return testDesigner.receive(messageEndpointName);
    }

    @Override
    public SendMessageActionBuilder<?> send(Endpoint messageEndpoint) {
        return testDesigner.send(messageEndpoint);
    }

    @Override
    public SendMessageActionBuilder<?> send(String messageEndpointName) {
        return testDesigner.send(messageEndpointName);
    }

    @Override
    public SleepAction.Builder sleep() {
        return testDesigner.sleep();
    }

    @Override
    public SleepAction.Builder sleep(long milliseconds) {
        return testDesigner.sleep(milliseconds);
    }

    @Override
    public SleepAction.Builder sleep(double seconds) {
        return testDesigner.sleep(seconds);
    }

    @Override
    public Wait.Builder waitFor() {
        return testDesigner.waitFor();
    }

    @Override
    public StartServerAction.Builder start(Server... servers) {
        return testDesigner.start(servers);
    }

    @Override
    public StartServerAction.Builder start(Server server) {
        return testDesigner.start(server);
    }

    @Override
    public StopServerAction.Builder stop(Server... servers) {
        return testDesigner.stop(servers);
    }

    @Override
    public StopServerAction.Builder stop(Server server) {
        return testDesigner.stop(server);
    }

    @Override
    public StopTimeAction.Builder stopTime() {
        return testDesigner.stopTime();
    }

    @Override
    public StopTimeAction.Builder stopTime(String id) {
        return testDesigner.stopTime(id);
    }

    @Override
    public StopTimeAction.Builder stopTime(String id, String suffix) {
        return testDesigner.stopTime(id, suffix);
    }

    @Override
    public TraceVariablesAction.Builder traceVariables() {
        return testDesigner.traceVariables();
    }

    @Override
    public TraceVariablesAction.Builder traceVariables(String... variables) {
        return testDesigner.traceVariables(variables);
    }

    @Override
    public GroovyAction.Builder groovy(String script) {
        return testDesigner.groovy(script);
    }

    @Override
    public GroovyAction.Builder groovy(Resource scriptResource) {
        return testDesigner.groovy(scriptResource);
    }

    @Override
    public TransformAction.Builder transform() {
        return testDesigner.transform();
    }

    @Override
    public Assert.Builder assertException() {
        return testDesigner.assertException();
    }

    @Override
    public Catch.Builder catchException() {
        return testDesigner.catchException();
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault() {
        return testDesigner.assertSoapFault();
    }

    @Override
    public Conditional.Builder conditional() {
        return testDesigner.conditional();
    }

    @Override
    public Iterate.Builder iterate() {
        return testDesigner.iterate();
    }

    @Override
    public Parallel.Builder parallel() {
        return testDesigner.parallel();
    }

    @Override
    public RepeatOnErrorUntilTrue.Builder repeatOnError() {
        return testDesigner.repeatOnError();
    }

    @Override
    public RepeatUntilTrue.Builder repeat() {
        return testDesigner.repeat();
    }

    @Override
    public Sequence.Builder sequential() {
        return testDesigner.sequential();
    }

    @Override
    public Async.Builder async() {
        return testDesigner.async();
    }

    @Override
    public StopTimerAction.Builder stopTimer(String timerId) {
        return testDesigner.stopTimer(timerId);
    }

    @Override
    public StopTimerAction.Builder stopTimers() {
        return testDesigner.stopTimers();
    }

    @Override
    public Timer.Builder timer() {
        return testDesigner.timer();
    }

    @Override
    public DockerExecuteActionBuilder docker() {
        return testDesigner.docker();
    }

    @Override
    public KubernetesExecuteActionBuilder kubernetes() {
        return testDesigner.kubernetes();
    }

    @Override
    public SeleniumActionBuilder selenium() {
        return testDesigner.selenium();
    }

    @Override
    public HttpActionBuilder http() {
        return testDesigner.http();
    }

    @Override
    public SoapActionBuilder soap() {
        return testDesigner.soap();
    }

    @Override
    public CamelRouteActionBuilder camel() {
        return testDesigner.camel();
    }

    @Override
    public ZooExecuteActionBuilder zookeeper() {
        return testDesigner.zookeeper();
    }

    @Override
    public Template.Builder applyTemplate(String name) {
        return testDesigner.applyTemplate(name);
    }

    @Override
    public FinallySequence.Builder doFinally() {
        return testDesigner.doFinally();
    }

    @Override
    public void setTestContext(TestContext context) {
        testDesigner.setTestContext(context);
    }

    /**
     * Get the test variables.
     * @return
     */
    protected Map<String, Object> getVariables() {
        if (testDesigner instanceof DefaultTestDesigner) {
            return ((DefaultTestDesigner) testDesigner).getVariables();
        } else {
            return testDesigner.getTestCase().getVariableDefinitions();
        }
    }

}
