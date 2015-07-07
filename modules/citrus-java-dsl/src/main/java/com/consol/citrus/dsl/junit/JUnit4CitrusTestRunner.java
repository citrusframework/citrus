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

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.*;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.dsl.runner.*;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.junit.AbstractJUnit4CitrusTest;
import com.consol.citrus.junit.CitrusJUnit4Runner;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import org.springframework.util.ReflectionUtils;

/**
 * JUnit Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class JUnit4CitrusTestRunner extends AbstractJUnit4CitrusTest implements TestRunner {

    /** Test builder delegate */
    private DefaultTestRunner testRunner;

    /**
     * Initialize test case and variables. Must be done with each test run.
     */
    public void init() {
        testRunner = new DefaultTestRunner(applicationContext);
        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    @Override
    protected void run(CitrusJUnit4Runner.CitrusFrameworkMethod frameworkMethod) {
        if (frameworkMethod.getMethod().getAnnotation(CitrusTest.class) != null) {
            init();
            name(frameworkMethod.getTestName());

            testRunner.start();
            ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this);
            testRunner.stop();
        } else {
            super.run(frameworkMethod);
        }
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
    public void name(String name) {
        testRunner.name(name);
    }

    @Override
    public void packageName(String packageName) {
        testRunner.packageName(packageName);
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
    public void applyBehavior(com.consol.citrus.dsl.runner.TestBehavior behavior) {
        testRunner.applyBehavior(behavior);
    }

    @Override
    public void parameter(String[] parameterNames, Object[] parameterValues) {
        testRunner.parameter(parameterNames, parameterValues);
    }

    @Override
    public AntRunAction antrun(TestActionConfigurer<AntRunActionDefinition> configurer) {
        return testRunner.antrun(configurer);
    }

    @Override
    public EchoAction echo(String message) {
        return testRunner.echo(message);
    }

    @Override
    public ExecutePLSQLAction plsql(TestActionConfigurer<ExecutePLSQLActionDefinition> configurer) {
        return testRunner.plsql(configurer);
    }

    @Override
    public ExecuteSQLAction sql(TestActionConfigurer<ExecuteSQLActionDefinition> configurer) {
        return testRunner.sql(configurer);
    }

    @Override
    public ExecuteSQLQueryAction query(TestActionConfigurer<ExecuteSQLQueryActionDefinition> configurer) {
        return testRunner.query(configurer);
    }

    @Override
    public ReceiveTimeoutAction receiveTimeout(TestActionConfigurer<ReceiveTimeoutActionDefinition> configurer) {
        return testRunner.receiveTimeout(configurer);
    }

    @Override
    public FailAction fail(String message) {
        return testRunner.fail(message);
    }

    @Override
    public LoadPropertiesAction load(String filePath) {
        return testRunner.load(filePath);
    }

    @Override
    public PurgeJmsQueuesAction purgeQueues(TestActionConfigurer<PurgeJmsQueueActionDefinition> configurer) {
        return testRunner.purgeQueues(configurer);
    }

    @Override
    public PurgeMessageChannelAction purgeChannels(TestActionConfigurer<PurgeMessageChannelActionDefinition> configurer) {
        return testRunner.purgeChannels(configurer);
    }

    @Override
    public ReceiveMessageAction receive(TestActionConfigurer<ReceiveMessageActionDefinition> configurer) {
        return testRunner.receive(configurer);
    }

    @Override
    public SendMessageAction send(TestActionConfigurer<SendMessageActionDefinition> configurer) {
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
    public TraceVariablesAction traceVariables() {
        return testRunner.traceVariables();
    }

    @Override
    public TraceVariablesAction traceVariables(String... variables) {
        return testRunner.traceVariables(variables);
    }

    @Override
    public GroovyAction groovy(TestActionConfigurer<GroovyActionDefinition> configurer) {
        return testRunner.groovy(configurer);
    }

    @Override
    public TransformAction transform(TestActionConfigurer<TransformActionDefinition> configurer) {
        return testRunner.transform(configurer);
    }

    @Override
    public TestRunner assertException(TestActionConfigurer<AssertDefinition> configurer) {
        return testRunner.assertException(configurer);
    }

    @Override
    public TestAction when(TestAction... predicate) {
        return testRunner.when(predicate);
    }
}
