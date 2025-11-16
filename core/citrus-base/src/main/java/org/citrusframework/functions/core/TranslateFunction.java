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

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

/**
 * Function searches for occurrences of a given character sequence and replaces all
 * findings with given replacement string.
 */
public class TranslateFunction implements ParameterizedFunction<TranslateFunction.Parameters> {

    @Override
    public String execute(Parameters params, TestContext context) {
        return params.getValue().replaceAll(params.getRegex(), params.getReplacement());
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {
        private String value;
        private String regex;
        private String replacement;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.size() < 3) {
                throw new InvalidFunctionUsageException("Function parameters not set correctly");
            }

            value = parameterList.get(0);
            regex = parameterList.get(1);
            replacement = parameterList.get(2);
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The value to evaluate.")
        public void setValue(String value) {
            this.value = value;
        }

        public String getRegex() {
            return regex;
        }

        @SchemaProperty(required = true, description = "The regular expression to evaluate.")
        public void setRegex(String regex) {
            this.regex = regex;
        }

        public String getReplacement() {
            return replacement;
        }

        @SchemaProperty(required = true, description = "The transform replacement.")
        public void setReplacement(String replacement) {
            this.replacement = replacement;
        }
    }
}
