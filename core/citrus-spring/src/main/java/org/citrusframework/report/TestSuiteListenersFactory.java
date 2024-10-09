/*
 * Copyright the original author or authors.
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

package org.citrusframework.report;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean automatically adds all test suite listeners that live in the Spring bean application context.
 *
 */
public class TestSuiteListenersFactory implements FactoryBean<TestSuiteListeners>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final TestSuiteListeners listeners;

    /**
     * Default constructor.
     */
    public TestSuiteListenersFactory() {
        this(new TestSuiteListeners());
    }

    /**
     * Constructor initializes with given listeners.
     * @param listeners
     */
    public TestSuiteListenersFactory(TestSuiteListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    public TestSuiteListeners getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(TestSuiteListener.class)
                    .forEach((key, value) -> listeners.addTestSuiteListener(value));
        }

        return listeners;
    }

    @Override
    public Class<?> getObjectType() {
        return TestSuiteListeners.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
