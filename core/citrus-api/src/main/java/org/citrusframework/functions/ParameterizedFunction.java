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

package org.citrusframework.functions;

import java.util.List;

import javax.swing.*;

import org.citrusframework.context.TestContext;

public interface ParameterizedFunction<P extends ParameterizedFunction.FunctionParameters> extends Function {

    /**
     * Execute function with typed parameters.
     */
    String execute(P parameters, TestContext context);

    /**
     * Instantiate new function parameters.
     */
    P getParameters();

    default String execute(List<String> parameterList, TestContext context) {
        P params = getParameters();
        params.configure(parameterList, context);
        return execute(params, context);
    }

    @FunctionalInterface
    interface FunctionParameters {
        /**
         * Converts list of String parameters to typed parameters.
         */
        void configure(List<String> parameterList, TestContext context);
    }
}
