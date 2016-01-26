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

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.endpoint.Executable;
import org.springframework.context.ApplicationContext;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecutableTestDesigner extends DefaultTestDesigner implements Executable {

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     */
    public ExecutableTestDesigner(ApplicationContext applicationContext) {
        super(applicationContext, Citrus.newInstance(applicationContext).createTestContext());
    }

    @Override
    public void execute() {
        execute(getTestContext());
    }

    /**
     * Builds and executes test case with given test context.
     * @param context
     */
    public void execute(TestContext context) {
        getTestCase().execute(context);
    }

    /**
     * Creates new test context from Spring bean application context.
     * If no Spring bean application context is set an exception is raised. Users may want to create proper test context
     * instance themselves in case Spring application context is not present.
     * @return
     */
    @Override
    public TestContext getTestContext() {
        if (super.getTestContext() == null) {
            TestContext context = getApplicationContext().getBean(TestContext.class);
            context.setApplicationContext(getApplicationContext());

            return context;
        } else {
            return super.getTestContext();
        }
    }
}
