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
import java.util.Random;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

/**
 * Function returning a random numeric value. Argument specifies the number of digits and
 * padding boolean flag.
 */
public class RandomNumberFunction implements ParameterizedFunction<RandomNumberFunction.Parameters> {

    /** Basic seed generating random number */
    private static final Random generator = new Random(System.currentTimeMillis());

    @Override
    public String execute(Parameters params, TestContext context) {
        if (params.getLength() < 1) {
            throw new InvalidFunctionUsageException("Invalid parameter definition. " +
                    "Number length must be a positive non-zero integer value");
        }

        return getRandomNumber(params);
    }

    public static String getRandomNumber(Parameters params) {
        return getRandomNumber(params.getLength(), params.isPaddingOn());
    }

    /**
     * Static number generator method.
     */
    public static String getRandomNumber(int numberLength, boolean paddingOn) {
        if (numberLength < 1) {
            throw new InvalidFunctionUsageException("Number length must be a positive non-zero integer value - supplied " + numberLength);
        }

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < numberLength; i++) {
            buffer.append(generator.nextInt(10));
        }

        return checkLeadingZeros(buffer.toString(), paddingOn);
    }

    /**
     * Remove leading Zero numbers.
     */
    public static String checkLeadingZeros(String generated, boolean paddingOn) {
        if (paddingOn) {
            return replaceLeadingZero(generated);
        } else {
            return removeLeadingZeros(generated);
        }

    }

    /**
     * Removes leading zero numbers if present.
     */
    private static String removeLeadingZeros(String generated) {
        StringBuilder builder = new StringBuilder();
        boolean leading = true;
        for (int i = 0; i < generated.length(); i++) {
            if (generated.charAt(i) == '0' && leading) {
                continue;
            } else {
                leading = false;
                builder.append(generated.charAt(i));
            }
        }

        if (builder.isEmpty()) {
            // very unlikely to happen, ensures that empty string is not returned
            builder.append('0');
        }

        return builder.toString();
    }

    /**
     * Replaces first leading zero number if present.
     */
    private static String replaceLeadingZero(String generated) {
        if (generated.charAt(0) == '0') {
            // find number > 0 as replacement to avoid leading zero numbers
            int replacement = 0;
            while (replacement == 0) {
                replacement = generator.nextInt(10);
            }

            return replacement + generated.substring(1);
        } else {
            return generated;
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {

        private int length;
        private boolean paddingOn = true;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            if (parameterList.size() > 2) {
                throw new InvalidFunctionUsageException("Too many parameters for function");
            }

            setLength(parseInt(parameterList.get(0)));

            if (parameterList.size() > 1) {
                setPaddingOn(parseBoolean(parameterList.get(1)));
            }
        }

        public int getLength() {
            return length;
        }

        @SchemaProperty(required = true, description = "Defines the length of the generated number.")
        public void setLength(int length) {
            this.length = length;
        }

        public boolean isPaddingOn() {
            return paddingOn;
        }

        @SchemaProperty(description = "When enabled the generated number is filled with zero numbers to always get the given length.")
        public void setPaddingOn(boolean paddingOn) {
            this.paddingOn = paddingOn;
        }
    }
}
