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

package org.citrusframework.functions.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.functions.parameter.StringParameters;

/**
 * Function concatenating multiple tokens to a single string. Tokens can be either
 * static string values or dynamic variables or functions.
 */
public class ConcatFunction implements ParameterizedFunction<StringParameters> {

    @Override
    public String execute(StringParameters params, TestContext context) {
        StringBuilder resultString = new StringBuilder();

        for (var parameter : params.getValues()) {
            resultString.append(parameter);
        }

        return resultString.toString();
    }

    @Override
    public StringParameters getParameters() {
        return new StringParameters().withAllowEmpty(false);
    }
}
