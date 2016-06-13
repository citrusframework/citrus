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
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.design.*;
import com.consol.citrus.dsl.simulation.TestSimulator;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.junit.CitrusJUnit4Runner;
import com.consol.citrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 * JUnit Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JUnit4CitrusTestDesigner extends JUnit4CitrusTest implements TestDesigner, TestSimulator {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Test builder delegate */
    private TestDesigner testDesigner;

    @Override
    public void simulate(Method method, TestContext context, ApplicationContext applicationContext) {
        setApplicationContext(applicationContext);
        testDesigner = new TestDesignerSimulation(createTestDesigner(new CitrusJUnit4Runner.CitrusFrameworkMethod(method, method.getName(), method.getDeclaringClass().getPackage().getName()), context).getTestCase(), applicationContext, context);
    }

    @Override
    protected TestDesigner createTestDesigner(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestContext context) {
        testDesigner = super.createTestDesigner(frameworkMethod, context);
        return testDesigner;
    }

    @Override
    protected void invokeTestMethod(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod, TestCase testCase, TestContext context) {
        if (isConfigure(frameworkMethod.getMethod())) {
            configure();
            citrus.run(testCase, context);
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

    @Override
    protected void executeTest() {
        run(new CitrusJUnit4Runner.CitrusFrameworkMethod(ReflectionUtils.findMethod(this.getClass(), "configure"),
                this.getClass().getSimpleName(), this.getClass().getPackage().getName()));
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
    public void variable(String name, Object value) {
        testDesigner.variable(name, value);
    }

    @Override
    public CreateVariablesAction createVariable(String variableName, String value) {
        return testDesigner.createVariable(variableName, value);
    }

    @Override
    public void action(TestAction testAction) {
        testDesigner.action(testAction);
    }

    @Override
    public ApplyTestBehaviorAction applyBehavior(TestBehavior behavior) {
        return testDesigner.applyBehavior(behavior);
    }

    @Override
    public <T extends AbstractActionContainer> AbstractTestContainerBuilder<T> container(T container) {
        return testDesigner.container(container);
    }

    @Override
    public AntRunBuilder antrun(String buildFilePath) {
        return testDesigner.antrun(buildFilePath);
    }

    @Override
    public EchoAction echo(String message) {
        return testDesigner.echo(message);
    }

    @Override
    public ExecutePLSQLBuilder plsql(DataSource dataSource) {
        return testDesigner.plsql(dataSource);
    }

    @Override
    public ExecuteSQLBuilder sql(DataSource dataSource) {
        return testDesigner.sql(dataSource);
    }

    @Override
    public ExecuteSQLQueryBuilder query(DataSource dataSource) {
        return testDesigner.query(dataSource);
    }

    @Override
    public ReceiveTimeoutBuilder receiveTimeout(Endpoint messageEndpoint) {
        return testDesigner.receiveTimeout(messageEndpoint);
    }

    @Override
    public ReceiveTimeoutBuilder receiveTimeout(String messageEndpointName) {
        return testDesigner.receiveTimeout(messageEndpointName);
    }

    @Override
    public FailAction fail(String message) {
        return testDesigner.fail(message);
    }

    @Override
    public InputActionBuilder input() {
        return testDesigner.input();
    }

    @Override
    public JavaActionBuilder java(String className) {
        return testDesigner.java(className);
    }

    @Override
    public JavaActionBuilder java(Class<?> clazz) {
        return testDesigner.java(clazz);
    }

    @Override
    public JavaActionBuilder java(Object instance) {
        return testDesigner.java(instance);
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        return testDesigner.load(filePath);
    }

    @Override
    public PurgeJmsQueuesBuilder purgeQueues() {
        return testDesigner.purgeQueues();
    }

    @Override
    public PurgeChannelsBuilder purgeChannels() {
        return testDesigner.purgeChannels();
    }

    @Override
    public PurgeEndpointsBuilder purgeEndpoints() {
        return testDesigner.purgeEndpoints();
    }

    @Override
    public ReceiveMessageBuilder receive(Endpoint messageEndpoint) {
        return testDesigner.receive(messageEndpoint);
    }

    @Override
    public ReceiveMessageBuilder receive(String messageEndpointName) {
        return testDesigner.receive(messageEndpointName);
    }

    @Override
    public SendMessageBuilder send(Endpoint messageEndpoint) {
        return testDesigner.send(messageEndpoint);
    }

    @Override
    public SendMessageBuilder send(String messageEndpointName) {
        return testDesigner.send(messageEndpointName);
    }

    @Override
    @Deprecated
    public SendSoapFaultBuilder sendSoapFault(String messageEndpointName) {
        return testDesigner.sendSoapFault(messageEndpointName);
    }

    @Override
    @Deprecated
    public SendSoapFaultBuilder sendSoapFault(Endpoint messageEndpoint) {
        return testDesigner.sendSoapFault(messageEndpoint);
    }

    @Override
    public SleepAction sleep() {
        return testDesigner.sleep();
    }

    @Override
    public SleepAction sleep(long milliseconds) {
        return testDesigner.sleep(milliseconds);
    }

    @Override
    public SleepAction sleep(double seconds) {
        return testDesigner.sleep(seconds);
    }

    @Override
    public WaitActionBuilder waitFor() {
        return testDesigner.waitFor();
    }

    @Override
    public StartServerAction start(Server... servers) {
        return testDesigner.start(servers);
    }

    @Override
    public StartServerAction start(Server server) {
        return testDesigner.start(server);
    }

    @Override
    public StopServerAction stop(Server... servers) {
        return testDesigner.stop(servers);
    }

    @Override
    public StopServerAction stop(Server server) {
        return testDesigner.stop(server);
    }

    @Override
    public StopTimeAction stopTime() {
        return testDesigner.stopTime();
    }

    @Override
    public StopTimeAction stopTime(String id) {
        return testDesigner.stopTime(id);
    }

    @Override
    public TraceVariablesAction traceVariables() {
        return testDesigner.traceVariables();
    }

    @Override
    public TraceVariablesAction traceVariables(String... variables) {
        return testDesigner.traceVariables(variables);
    }

    @Override
    public GroovyActionBuilder groovy(String script) {
        return testDesigner.groovy(script);
    }

    @Override
    public GroovyActionBuilder groovy(Resource scriptResource) {
        return testDesigner.groovy(scriptResource);
    }

    @Override
    public TransformActionBuilder transform() {
        return testDesigner.transform();
    }

    @Override
    @Deprecated
    public AssertExceptionBuilder assertException(TestAction testAction) {
        return testDesigner.assertException(testAction);
    }

    @Override
    public AssertExceptionBuilder assertException() {
        return testDesigner.assertException();
    }

    @Override
    @Deprecated
    public CatchExceptionBuilder catchException(TestAction ... actions) {
        return testDesigner.catchException(actions);
    }

    @Override
    public CatchExceptionBuilder catchException() {
        return testDesigner.catchException();
    }

    @Override
    @Deprecated
    public AssertSoapFaultBuilder assertSoapFault(TestAction testAction) {
        return testDesigner.assertSoapFault(testAction);
    }

    @Override
    public AssertSoapFaultBuilder assertSoapFault() {
        return testDesigner.assertSoapFault();
    }

    @Override
    @Deprecated
    public ConditionalBuilder conditional(TestAction ... actions) {
        return testDesigner.conditional(actions);
    }

    @Override
    public ConditionalBuilder conditional() {
        return testDesigner.conditional();
    }

    @Override
    @Deprecated
    public IterateBuilder iterate(TestAction ... actions) {
        return testDesigner.iterate(actions);
    }

    @Override
    public IterateBuilder iterate() {
        return testDesigner.iterate();
    }

    @Override
    @Deprecated
    public ParallelBuilder parallel(TestAction ... actions) {
        return testDesigner.parallel(actions);
    }

    @Override
    public ParallelBuilder parallel() {
        return testDesigner.parallel();
    }

    @Override
    @Deprecated
    public RepeatOnErrorBuilder repeatOnError(TestAction ... actions) {
        return testDesigner.repeatOnError(actions);
    }

    @Override
    public RepeatOnErrorBuilder repeatOnError() {
        return testDesigner.repeatOnError();
    }

    @Override
    @Deprecated
    public RepeatBuilder repeat(TestAction ... actions) {
        return testDesigner.repeat(actions);
    }

    @Override
    public RepeatBuilder repeat() {
        return testDesigner.repeat();
    }

    @Override
    @Deprecated
    public SequenceBuilder sequential(TestAction ... actions) {
        return testDesigner.sequential(actions);
    }

    @Override
    public SequenceBuilder sequential() {
        return testDesigner.sequential();
    }

    @Override
    @Deprecated
    public TimerBuilder timer(TestAction ... actions) {
        return testDesigner.timer(actions);
    }

    @Override
    public StopTimerAction stopTimer(String timerId) {
        return testDesigner.stopTimer(timerId);
    }

    @Override
    public StopTimerAction stopTimers() {
        return testDesigner.stopTimers();
    }

    @Override
    public TimerBuilder timer() {
        return testDesigner.timer();
    }

    @Override
    public DockerActionBuilder docker() {
        return testDesigner.docker();
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
    public ZooActionBuilder zookeeper() {
        return testDesigner.zookeeper();
    }

    @Override
    public TemplateBuilder applyTemplate(String name) {
        return testDesigner.applyTemplate(name);
    }

    @Override
    @Deprecated
    public FinallySequenceBuilder doFinally(TestAction ... actions) {
        return testDesigner.doFinally(actions);
    }

    @Override
    public FinallySequenceBuilder doFinally() {
        return testDesigner.doFinally();
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
