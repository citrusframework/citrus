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

package com.consol.citrus.dsl.runner;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class TestRunnerComponent extends DefaultTestRunner implements ApplicationContextAware, InitializingBean {

    /**
     * Main entrance method for subclasses to call Java DSL builder methods in order to
     * add test actions and basic test case properties to this builder instance.
     */
    public void execute() {
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        initialize();
    }
}
