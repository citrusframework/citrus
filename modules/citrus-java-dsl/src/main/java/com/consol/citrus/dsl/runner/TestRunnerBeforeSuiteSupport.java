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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.Citrus;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.context.TestContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Adds before suite actions using Java DSL runner methods. Instances of subclasses should be added as Spring beans to application context.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public abstract class TestRunnerBeforeSuiteSupport extends SequenceBeforeSuite implements ApplicationContextAware, InitializingBean {

    /** Runner instance to receive before suite actions */
    private TestRunner testRunner;

    /** Spring application context for test context initialization */
    private ApplicationContext applicationContext;

    /**
     * Subclasses implement this method to add before suite logic.
     * @param runner
     */
    public abstract void beforeSuite(TestRunner runner);

    @Override
    public void doExecute(TestContext context) {
        beforeSuite(testRunner);
    }

    /**
     * Sets the applicationContext property.
     *
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        testRunner = new DefaultTestRunner(applicationContext, Citrus.newInstance(applicationContext).createTestContext());
    }
}
