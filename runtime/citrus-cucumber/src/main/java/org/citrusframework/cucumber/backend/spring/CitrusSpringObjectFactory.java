/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.cucumber.backend.spring;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusSpringContext;
import org.citrusframework.CitrusSpringContextProvider;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactoryBean;
import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.spring.SpringFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusSpringObjectFactory implements ObjectFactory {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusSpringObjectFactory.class);

    /** Test runner */
    private TestCaseRunner runner;

    /** Test context */
    private TestContext context;

    /** Static self reference */
    private static CitrusSpringObjectFactory selfReference;

    /** Delegate object factory */
    private final SpringFactory delegate = new SpringFactory();

    /**
     * Default constructor with static self reference initialization.
     */
    public CitrusSpringObjectFactory() {
        selfReference = this;
    }

    @Override
    public void start() {
        delegate.start();
        context = getInstance(TestContext.class);
        runner = new DefaultTestCaseRunner(context);
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (context == null) {
            try {
                TestContextFactoryBean contextFactoryBean = delegate.getInstance(TestContextFactoryBean.class);
                context = contextFactoryBean.getObject();
                initializeCitrus(context, contextFactoryBean.getApplicationContext());
            } catch (CucumberBackendException e) {
                log.warn("Failed to get proper TestContext from Cucumber Spring application context: " + e.getMessage());
                context = CitrusInstanceManager.getOrDefault().getCitrusContext().createTestContext();
            }
        }

        if (TestContext.class.isAssignableFrom(type)) {
            return (T) context;
        }

        if (CitrusSpringObjectFactory.class.isAssignableFrom(type)) {
            return (T) this;
        }

        T instance = delegate.getInstance(type);
        CitrusAnnotations.injectAll(instance, CitrusInstanceManager.getOrDefault(), context);
        CitrusAnnotations.injectTestRunner(instance, runner);

        return instance;
    }

    /**
     * Initialize new Citrus instance only if it has not been initialized before
     * or in case given application context is different to that one stored in the Citrus context.
     *
     * @param context
     * @param applicationContext
     */
    private void initializeCitrus(TestContext context, ApplicationContext applicationContext) {
        if (CitrusInstanceManager.hasInstance()) {
            CitrusContext citrusContext = CitrusInstanceManager.getOrDefault().getCitrusContext();

            if (citrusContext instanceof CitrusSpringContext
                    && !((CitrusSpringContext) citrusContext).getApplicationContext().equals(applicationContext)) {
                log.warn("Citrus instance has already been initialized - creating new instance and shutting down current instance");
                citrusContext.close();
            } else {
                return;
            }
        }

        Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        return delegate.addClass(glueClass);
    }

    /**
     * Static access to self reference.
     * @return
     */
    public static CitrusSpringObjectFactory instance() throws IllegalAccessException {
        if (selfReference == null) {
            throw new IllegalAccessException("Illegal access to self reference - not available yet");
        }

        return selfReference;
    }
}
