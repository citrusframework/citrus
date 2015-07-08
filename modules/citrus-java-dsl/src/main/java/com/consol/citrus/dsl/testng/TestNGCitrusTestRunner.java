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

package com.consol.citrus.dsl.testng;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.*;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.container.Template;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.dsl.runner.*;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.testng.*;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * TestNG Citrus test provides Java DSL access to builder pattern methods in
 * CitrusTestDesigner by simple method delegation.
 *
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class TestNGCitrusTestRunner extends AbstractTestNGCitrusTest implements TestRunner {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

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
    public void run(final IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method != null && method.getAnnotation(CitrusTest.class) != null) {
            CitrusTest citrusTestAnnotation = method.getAnnotation(CitrusTest.class);
            init();

            if (StringUtils.hasText(citrusTestAnnotation.name())) {
                name(citrusTestAnnotation.name());
            } else {
                name(method.getName());
            }

            Object[][] parameters = null;
            if (method.getAnnotation(Test.class) != null &&
                    StringUtils.hasText(method.getAnnotation(Test.class).dataProvider())) {
                parameters = (Object[][]) ReflectionUtils.invokeMethod(
                        ReflectionUtils.findMethod(method.getDeclaringClass(), method.getAnnotation(Test.class).dataProvider()), this);
            }

            try {
                testRunner.start();

                if (parameters != null) {
                    ReflectionUtils.invokeMethod(method, this,
                            parameters[testResult.getMethod().getCurrentInvocationCount() % parameters.length]);

                    handleTestParameters(testResult.getMethod(),
                            parameters[testResult.getMethod().getCurrentInvocationCount() % parameters.length]);
                } else {
                    ReflectionUtils.invokeMethod(method, this);
                }

                testRunner.stop();
            } catch (RuntimeException e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            } catch (Exception e) {
                testResult.setThrowable(e);
                testResult.setStatus(ITestResult.FAILURE);
            }

            super.run(new FakeExecutionCallBack(callBack.getParameters()), testResult);
        } else {
            super.run(callBack, testResult);
        }
    }

    /**
     * Methods adds optional TestNG parameters as variables to the test case.
     *
     * @param method the testng method currently executed
     * @param parameterValues
     */
    protected void handleTestParameters(ITestNGMethod method, Object[] parameterValues) {
        String[] parameterNames = getParameterNames(method);

        if (parameterValues.length != parameterNames.length) {
            throw new CitrusRuntimeException("Parameter mismatch: " + parameterNames.length +
                    " parameter names defined with " + parameterValues.length + " parameter values available");
        }

        testRunner.parameter(parameterNames, parameterValues);
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
    public CreateVariablesAction createVariable(String variableName, String value) {
        return testRunner.createVariable(variableName, value);
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
    public InputAction input(TestActionConfigurer<InputActionDefinition> configurer) {
        return testRunner.input(configurer);
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
    public ExceptionContainerRunner assertException(TestActionConfigurer<AssertDefinition> configurer) {
        return testRunner.assertException(configurer);
    }

    @Override
    public ExceptionContainerRunner catchException(TestActionConfigurer<CatchDefinition> configurer) {
        return testRunner.catchException(configurer);
    }

    @Override
    public ExceptionContainerRunner assertSoapFault(TestActionConfigurer<AssertSoapFaultDefinition> configurer) {
        return testRunner.assertSoapFault(configurer);
    }

    @Override
    public ContainerRunner conditional(TestActionConfigurer<ConditionalDefinition> configurer) {
        return testRunner.conditional(configurer);
    }

    @Override
    public ContainerRunner iterate(TestActionConfigurer<IterateDefinition> configurer) {
        return testRunner.iterate(configurer);
    }

    @Override
    public ContainerRunner parallel() {
        return testRunner.parallel();
    }

    @Override
    public ContainerRunner repeatOnError(TestActionConfigurer<RepeatOnErrorUntilTrueDefinition> configurer) {
        return testRunner.repeatOnError(configurer);
    }

    @Override
    public ContainerRunner repeat(TestActionConfigurer<RepeatUntilTrueDefinition> configurer) {
        return testRunner.repeat(configurer);
    }

    @Override
    public ContainerRunner sequential() {
        return testRunner.sequential();
    }

    @Override
    public Template applyTemplate(TestActionConfigurer<TemplateDefinition> configurer) {
        return testRunner.applyTemplate(configurer);
    }

    @Override
    public ContainerRunner doFinally() {
        return testRunner.doFinally();
    }
}
