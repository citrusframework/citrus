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
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.*;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.container.Template;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.runner.*;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.junit.AbstractJUnit4CitrusTest;
import com.consol.citrus.junit.CitrusJUnit4Runner;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import org.springframework.util.ReflectionUtils;

import java.util.Date;

/**
 * JUnit Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.3
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
            packageName(frameworkMethod.getPackageName());

            try {
                start();
                ReflectionUtils.invokeMethod(frameworkMethod.getMethod(), this);
            } finally {
                stop();
            }
        } else {
            super.run(frameworkMethod);
        }
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
    public void applyBehavior(com.consol.citrus.dsl.runner.TestBehavior behavior) {
        testRunner.applyBehavior(behavior);
    }

    @Override
    public void parameter(String[] parameterNames, Object[] parameterValues) {
        testRunner.parameter(parameterNames, parameterValues);
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
    public PurgeJmsQueuesAction purgeQueues(BuilderSupport<PurgeJmsQueuesBuilder> configurer) {
        return testRunner.purgeQueues(configurer);
    }

    @Override
    public PurgeMessageChannelAction purgeChannels(BuilderSupport<PurgeChannelsBuilder> configurer) {
        return testRunner.purgeChannels(configurer);
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
    public SendSoapFaultAction sendSoapFault(BuilderSupport<SendSoapFaultBuilder> configurer) {
        return testRunner.sendSoapFault(configurer);
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
    public Template applyTemplate(BuilderSupport<TemplateBuilder> configurer) {
        return testRunner.applyTemplate(configurer);
    }

    @Override
    public FinallySequenceBuilder doFinally() {
        return testRunner.doFinally();
    }
}
