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
import org.citrusframework.citrus.dsl.endpoint.Executable;

/**
 * Test builder component usually used in Spring bean application context as bean definition or as
 * {@link org.springframework.stereotype.Component} annotated bean loaded with Spring's annotation scan support. Adds execution
 * methods to the builder so component is executable as a test case.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecutableTestDesignerComponent extends TestDesignerComponent implements Executable {
    @Override
    public void execute() {
        execute(getTestContext());
    }

    /**
     * Builds and executes test case with given test context.
     * @param context
     */
    public void execute(TestContext context) {
        configure();
        getTestCase().execute(context);
    }
}
