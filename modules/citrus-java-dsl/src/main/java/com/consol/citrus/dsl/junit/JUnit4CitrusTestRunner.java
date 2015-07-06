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
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.dsl.runner.*;
import com.consol.citrus.junit.AbstractJUnit4CitrusTest;
import com.consol.citrus.junit.CitrusJUnit4Runner;
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
    public void run(TestAction testAction) {
        testRunner.run(testAction);
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
    public void antrun(TestActionConfigurer<AntRunActionDefinition> configurer) {
        testRunner.antrun(configurer);
    }

    @Override
    public void echo(String message) {
        testRunner.echo(message);
    }

    @Override
    public void plsql(TestActionConfigurer<ExecutePLSQLActionDefinition> configurer) {
        testRunner.plsql(configurer);
    }

    @Override
    public void sql(TestActionConfigurer<ExecuteSQLActionDefinition> configurer) {
        testRunner.sql(configurer);
    }

    @Override
    public void query(TestActionConfigurer<ExecuteSQLQueryActionDefinition> configurer) {
        testRunner.query(configurer);
    }

    @Override
    public void receiveTimeout(TestActionConfigurer<ReceiveTimeoutActionDefinition> configurer) {
        testRunner.receiveTimeout(configurer);
    }

    @Override
    public void fail(String message) {
        testRunner.fail(message);
    }

    @Override
    public void load(String filePath) {
        testRunner.load(filePath);
    }

    @Override
    public void purgeQueues(TestActionConfigurer<PurgeJmsQueueActionDefinition> configurer) {
        testRunner.purgeQueues(configurer);
    }

    @Override
    public void purgeChannels(TestActionConfigurer<PurgeMessageChannelActionDefinition> configurer) {
        testRunner.purgeChannels(configurer);
    }

    @Override
    public void receive(TestActionConfigurer<ReceiveMessageActionDefinition> configurer) {
        testRunner.receive(configurer);
    }

    @Override
    public void send(TestActionConfigurer<SendMessageActionDefinition> configurer) {
        testRunner.send(configurer);
    }

    @Override
    public void sleep() {
        testRunner.sleep();
    }

    @Override
    public void sleep(long milliseconds) {
        testRunner.sleep(milliseconds);
    }

}
