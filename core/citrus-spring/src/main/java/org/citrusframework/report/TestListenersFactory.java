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
 * Factory bean automatically adds all test listeners that live in the Spring bean application context.
 *
 */
public class TestListenersFactory implements FactoryBean<TestListeners>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final TestListeners listeners;

    /**
     * Default constructor.
     */
    public TestListenersFactory() {
        this(new TestListeners());
    }

    /**
     * Constructor initializes with given listeners.
     * @param listeners
     */
    public TestListenersFactory(TestListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    public TestListeners getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(TestListener.class)
                    .forEach((key, value) -> listeners.addTestListener(value));
        }

        return listeners;
    }

    @Override
    public Class<?> getObjectType() {
        return TestListeners.class;
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
