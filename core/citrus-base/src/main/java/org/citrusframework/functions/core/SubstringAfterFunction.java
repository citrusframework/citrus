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
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * Function implements substring after functionality.
 */
public class SubstringAfterFunction implements ParameterizedFunction<SubstringAfterFunction.Parameters> {

    @Override
    public String execute(Parameters param, TestContext context) {
        String resultString = param.getValue();

        if (StringUtils.hasText(param.getSearchString())) {
            String searchString = param.getSearchString();
            resultString = resultString.substring(resultString.indexOf(searchString) + searchString.length());
        }

        return resultString;
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {

        private String value;
        private String searchString;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.size() < 2) {
                throw new InvalidFunctionUsageException("Function parameters not set correctly");
            }

            setValue(parameterList.get(0));

            if (parameterList.size() > 1) {
                setSearchString(parameterList.get(1));
            }
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The value to perform substring.")
        public void setValue(String value) {
            this.value = value;
        }

        public String getSearchString() {
            return searchString;
        }

        @SchemaProperty(required = true, description = "Search string used to substring after the occurrence.")
        public void setSearchString(String searchString) {
            this.searchString = searchString;
        }
    }
}
