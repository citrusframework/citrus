/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.TestCase;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.BeanNameMappingStrategy;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.server.AbstractServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.util.FileUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Special request dispatching endpoint adapter invokes XML test case for each incoming message. Incoming message is
 * passed to test case via normal in memory message queue connection as usual.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class XmlTestExecutingEndpointAdapter extends RequestDispatchingEndpointAdapter implements InitializingBean, BeanNameAware, ApplicationContextAware {
    /** Executor start action sequence logic in separate thread task */
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

    /** This adapter name - used for message queue generation */
    private String name = EndpointAdapter.class.getSimpleName();

    /** Spring bean application context holding all available test builders and basic Citrus config */
    private ApplicationContext applicationContext;

    /** First request message is handled by this endpoint adapter */
    private EndpointAdapter endpointAdapterDelegate;

    /** Default package to search for Xml test case files */
    private String packageName = "org.citrusframework.tests";

    @Override
    public Message dispatchMessage(final Message request, String mappingName) {
        final TestCase test;
        final TestContext testContext;

        try {
            testContext = getTestContext();
            test = getTestCase(testContext, mappingName);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find test builder with name '" +
                    mappingName + "' in Spring bean context", e);
        }

        taskExecutor.execute(new Runnable() {
            public void run() {
                prepareExecution(request, test);
                test.execute(testContext);
            }
        });

        return endpointAdapterDelegate.handleMessage(request);
    }

    /**
     * Gets the test case from application context.
     * @param context
     * @param testName
     * @return the new test case.
     */
    protected TestCase getTestCase(TestContext context, String testName) {
        ClassPathXmlApplicationContext ctx = createApplicationContext(context, packageName, testName);

        try {
            context.setReferenceResolver(new SpringBeanReferenceResolver(ctx));
            TestCase testCase = ctx.getBean(testName, TestCase.class);
            testCase.setName(testName);
            testCase.setPackageName(packageName);
            return testCase;
        } catch (NoSuchBeanDefinitionException e) {
            throw context.handleError(testName, packageName, "Could not find test with name '" + testName + "'", e);
        }
    }

    /**
     * Creates the Spring application context.
     * @return
     */
    protected ClassPathXmlApplicationContext createApplicationContext(TestContext context, String packageName, String testName) {
        try {
            return new ClassPathXmlApplicationContext(
                    new String[] {
                            packageName.replace('.', '/') + "/" + testName + FileUtils.FILE_EXTENSION_XML,
                            "org/citrusframework/spring/annotation-config-ctx.xml"},
                    true, applicationContext);
        } catch (Exception e) {
            throw context.handleError(getClass().getSimpleName(), getClass().getPackage().getName(), "Failed to load test case", e);
        }
    }

    /**
     * Prepares the test builder instance before execution. Subclasses may add custom properties to teest builder
     * here.
     * @param request the triggering request message.
     * @param testCase the found test builder.
     */
    protected void prepareExecution(Message request, TestCase testCase) {
    }

    /**
     * Creates Citrus Spring bean application context with basic beans and settings for Citrus.
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (endpointAdapterDelegate == null) {
            DirectSyncEndpointConfiguration endpointConfiguration = new DirectSyncEndpointConfiguration();
            endpointConfiguration.setQueueName(name + AbstractServer.DEFAULT_CHANNEL_ID_SUFFIX);

            DirectEndpointAdapter simpleEndpointAdapter = new DirectEndpointAdapter(endpointConfiguration);
            simpleEndpointAdapter.setTestContextFactory(getTestContextFactory());
            endpointAdapterDelegate = simpleEndpointAdapter;
        }

        if (getMappingStrategy() == null) {
            BeanNameMappingStrategy mappingStrategy = new BeanNameMappingStrategy(new SpringBeanReferenceResolver(applicationContext));
            setMappingStrategy(mappingStrategy);
        }

        SpringBeanReferenceResolver referenceResolver = new SpringBeanReferenceResolver(applicationContext);
        ReferenceResolver fallback = new SimpleReferenceResolver();
        fallback.bind("testContextFactory", TestContextFactoryBean.newInstance(applicationContext));
        referenceResolver.setFallback(fallback);
        setTestContextFactory(referenceResolver.resolve(TestContextFactory.class));
    }

    /**
     * Injects this adapters bean name.
     * @param name
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Gets default test case package.
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets default test case package.
     * @param packageName
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Gets the task executor.
     * @return
     */
    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * Sets the task executor. Usually some async task executor for test execution in
     * separate thread instance.
     *
     * @param taskExecutor
     */
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Gets the response generating endpoint adapter delegate.
     * @return
     */
    public EndpointAdapter getResponseEndpointAdapter() {
        return endpointAdapterDelegate;
    }

    /**
     * Sets the response generating endpoint adapter delegate.
     * @param endpointAdapterDelegate
     */
    public void setResponseEndpointAdapter(EndpointAdapter endpointAdapterDelegate) {
        this.endpointAdapterDelegate = endpointAdapterDelegate;
    }

    /**
     * Gets the application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Injects Spring bean application context this adapter is managed by.
     * @param applicationContext
     * @throws org.springframework.beans.BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
