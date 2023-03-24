/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.functions;

import java.util.Arrays;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.core.JsonPathFunction;

/**
 * @author Christoph Deppisch
 */
public final class JsonFunctions {

    /**
     * Prevent instantiation.
     */
    private JsonFunctions() {
    }

    /**
     * Runs Json path function with arguments.
     * @return
     */
    public static String jsonPath(String content, String expression, TestContext context) {
        return new JsonPathFunction().execute(Arrays.asList(content, expression), context);
    }
}
