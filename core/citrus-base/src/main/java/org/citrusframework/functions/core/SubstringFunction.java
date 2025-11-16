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

import static java.lang.Integer.parseInt;

/**
 * Function implements simple substring functionality.
 * <p>
 * Function requires at least a target string and a beginIndex as function parameters. An
 * optional endIndex may be given as function parameter, too. The parameter usage looks
 * like this: substring(targetString, beginIndex, [endIndex]).
 *
 */
public class SubstringFunction implements ParameterizedFunction<SubstringFunction.Parameters> {

    @Override
    public String execute(Parameters params, TestContext context) {
        String targetString = params.getValue();

        if (params.getEndIndex() > 0) {
            targetString = targetString.substring(params.getBeginIndex(), params.getEndIndex());
        } else {
            targetString = targetString.substring(params.getBeginIndex());
        }

        return targetString;
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {

        private String value;
        private int beginIndex;
        private int endIndex;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.size() < 2) {
                throw new InvalidFunctionUsageException("Insufficient function parameters - parameter usage: (targetString, beginIndex, [endIndex])");
            }

            setValue(parameterList.get(0));
            setBeginIndex(parseInt(parameterList.get(1)));

            if (parameterList.size() > 2) {
                setEndIndex(parseInt(parameterList.get(2)));
            }
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The value to perform substring.")
        public void setValue(String value) {
            this.value = value;
        }

        public int getBeginIndex() {
            return beginIndex;
        }

        @SchemaProperty(required = true, description = "The substring begin index.")
        public void setBeginIndex(int beginIndex) {
            this.beginIndex = beginIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        @SchemaProperty(description = "Optional substring end index.")
        public void setEndIndex(int endIndex) {
            this.endIndex = endIndex;
        }
    }
}
