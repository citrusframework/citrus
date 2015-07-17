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

package com.consol.citrus.dsl.design;

import com.consol.citrus.context.TestContext;
import org.springframework.context.ApplicationContext;

/**
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class DefaultExecutableTestDesigner extends DefaultTestDesigner implements ExecutableTestDesigner {

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     */
    public DefaultExecutableTestDesigner(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void execute() {
        execute(createTestContext());
    }

    /**
     * Builds and executes test case with given test context.
     * @param context
     */
    public void execute(TestContext context) {
        build().execute(context);
    }

    /**
     * Creates new test context from Spring bean application context.
     * If no Spring bean application context is set an exception is raised. Users may want to create proper test context
     * instance themselves in case Spring application context is not present.
     * @return
     */
    protected TestContext createTestContext() {
        TestContext context = getApplicationContext().getBean(TestContext.class);
        context.setApplicationContext(getApplicationContext());

        return context;
    }
}
