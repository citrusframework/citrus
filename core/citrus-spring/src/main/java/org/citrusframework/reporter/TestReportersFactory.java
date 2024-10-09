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

package org.citrusframework.reporter;

import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestReporters;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean automatically adds all test reporters that live in the Spring bean application context.
 * The default test reporters get also added via Spring bean reference. This is why this registry explicitly does not use default reporter
 * in order to not duplicate those.
 *
 */
public class TestReportersFactory implements FactoryBean<TestReporters>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final TestReporters reporters;

    /**
     * Default constructor.
     */
    public TestReportersFactory() {
        this(new TestReporters());
    }

    /**
     * Constructor initializes with given registry.
     * @param registry
     */
    public TestReportersFactory(TestReporters registry) {
        this.reporters = registry;
    }

    @Override
    public TestReporters getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(TestReporter.class)
                    .forEach((key, value) -> reporters.addTestReporter(value));
        }

        return reporters;
    }

    @Override
    public Class<?> getObjectType() {
        return TestReporters.class;
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
