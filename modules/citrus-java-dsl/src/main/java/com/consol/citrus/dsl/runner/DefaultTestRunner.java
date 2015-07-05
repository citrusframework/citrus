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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.TestActions;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.report.TestActionListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.jms.ConnectionFactory;

/**
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class DefaultTestRunner implements TestRunner {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultTestRunner.class);

    /** This builders test case */
    private final TestCase testCase = new TestCase();

    /** This runners test context */
    private TestContext context;

    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /** Default constructor */
    public DefaultTestRunner() {
        name(this.getClass().getSimpleName());
        packageName(this.getClass().getPackage().getName());
    }

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     */
    public DefaultTestRunner(ApplicationContext applicationContext) {
        this();

        this.applicationContext = applicationContext;

        try {
            createTestContext();
            initialize();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to setup test runner", e);
        }
    }

    protected void initialize() {
        testCase.setTestActionListeners(applicationContext.getBean(TestActionListeners.class));

        if (!applicationContext.getBeansOfType(SequenceBeforeTest.class).isEmpty()) {
            testCase.setBeforeTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceBeforeTest.class).values().toArray()));
        }

        if (!applicationContext.getBeansOfType(SequenceAfterTest.class).isEmpty()) {
            testCase.setAfterTest(CollectionUtils.arrayToList(applicationContext.getBeansOfType(SequenceAfterTest.class).values().toArray()));
        }
    }

    @Override
    public void name(String name) {
        testCase.setBeanName(name);
        testCase.setName(name);
    }

    @Override
    public void packageName(String packageName) {
        testCase.setPackageName(packageName);
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
            String resolved = context.resolveDynamicValue((String) value);
            context.setVariable(name, resolved);
            return (T) resolved;
        } else {
            context.setVariable(name, value);
            return value;
        }
    }

    @Override
    public void parameter(String[] parameterNames, Object[] parameterValues) {
        testCase.setParameters(parameterNames, parameterValues);

        for (int i = 0; i < parameterNames.length; i++) {
            log.info(String.format("Initializing test parameter '%s' as variable", parameterNames[i]));
            context.setVariable(parameterNames[i], parameterValues[i]);
        }
    }

    @Override
    public void run(TestAction testAction) {
        testCase.addTestAction(testAction);
        testCase.executeAction(testAction, context);
    }

    @Override
    public void applyBehavior(TestBehavior behavior) {
        behavior.setApplicationContext(applicationContext);
        behavior.apply(this);
    }

    @Override
    public void antrun(TestActionConfigurer<AntRunActionDefinition> configurer) {
        AntRunActionDefinition definition = new AntRunActionDefinition(new AntRunAction());
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void echo(String message) {
        run(TestActions.echo(message));
    }

    @Override
    public void plsql(TestActionConfigurer<ExecutePLSQLActionDefinition> configurer) {
        ExecutePLSQLActionDefinition definition = new ExecutePLSQLActionDefinition(new ExecutePLSQLAction());
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void sql(TestActionConfigurer<ExecuteSQLActionDefinition> configurer) {
        ExecuteSQLActionDefinition definition = new ExecuteSQLActionDefinition(new ExecuteSQLAction());
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void query(TestActionConfigurer<ExecuteSQLQueryActionDefinition> configurer) {
        ExecuteSQLQueryActionDefinition definition = new ExecuteSQLQueryActionDefinition(new ExecuteSQLQueryAction());
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void fail(String message) {
        run(TestActions.fail(message));
    }

    @Override
    public void receiveTimeout(TestActionConfigurer<ReceiveTimeoutActionDefinition> configurer) {
        ReceiveTimeoutActionDefinition definition = new ReceiveTimeoutActionDefinition(new ReceiveTimeoutAction());
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void load(String filePath) {
        run(TestActions.load(filePath));
    }

    @Override
    public void purgeQueues(TestActionConfigurer<PurgeJMSQueuesActionDefinition> configurer) {
        PurgeJMSQueuesActionDefinition definition = new PurgeJMSQueuesActionDefinition(new PurgeJmsQueuesAction());
        configurer.configure(definition);

        if (!definition.hasConnectionFactory()) {
            definition.connectionFactory(applicationContext.getBean("connectionFactory", ConnectionFactory.class));
        }
        run(definition.getAction());
    }

    @Override
    public void purgeChannels(TestActionConfigurer<PurgeMessageChannelActionDefinition> configurer) {
        PurgeMessageChannelActionDefinition definition = new PurgeMessageChannelActionDefinition(new PurgeMessageChannelAction());
        definition.channelResolver(applicationContext);
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void receive(TestActionConfigurer<ReceiveMessageActionDefinition> configurer) {
        ReceiveMessageActionDefinition definition = new ReceiveMessageActionDefinition(new ReceiveMessageAction());
        definition.withApplicationContext(applicationContext);
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void send(TestActionConfigurer<SendMessageActionDefinition> configurer) {
        SendMessageActionDefinition definition = new SendMessageActionDefinition(new SendMessageAction());
        definition.withApplicationContext(applicationContext);
        configurer.configure(definition);
        run(definition.getAction());
    }

    @Override
    public void sleep() {
        run(TestActions.sleep());
    }

    @Override
    public void sleep(long milliseconds) {
        run(TestActions.sleep(milliseconds));
    }

    /**
     * Creates new test context from Spring bean application context.
     * @return
     */
    protected TestContext createTestContext() {
        if (context == null) {
            context = applicationContext.getBean(TestContext.class);
            context.setApplicationContext(applicationContext);
        }

        return context;
    }

    /**
     * Sets the application context either from ApplicationContextAware injection or from outside.
     * @param applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
