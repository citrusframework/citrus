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

package org.citrusframework.citrus.dsl.design;

import org.citrusframework.citrus.context.TestContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This test builder should be exclusively used as bean in a Spring application context. Either as bean definition in a XML configuration file or as
 * {@link org.springframework.stereotype.Component} annotated bean loaded with Spring's annotation scan support. The builder is aware of the application context and
 * the bean lifecycle automatically setting up application context and initializing tasks.
 *
 * Subclass may add custom logic in {@link TestDesignerComponent#configure()} method by calling builder methods.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class TestDesignerComponent extends DefaultTestDesigner implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * Main entrance method for subclasses to call Java DSL builder methods in order to
     * add test actions and basic test case properties to this builder instance.
     */
    protected void configure() {
    }

    /**
     * Obtains the applicationContext.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Specifies the applicationContext.
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        setTestContext(applicationContext.getBean(TestContext.class));
        this.applicationContext = applicationContext;
    }
}
