/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.testng;

import java.lang.annotation.*;

/**
 * Parameter annotation provides parameter names that are passed to the test case test context
 * as variables. Variable values are provided by TestNG data provider.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CitrusParameters {
    /**
     * The list of parameter names corresponding with TestNG data provider parameter values.
     * Each parameter name and value is injected to the test case as test variable before execution.
     */
    String[] value();
}
