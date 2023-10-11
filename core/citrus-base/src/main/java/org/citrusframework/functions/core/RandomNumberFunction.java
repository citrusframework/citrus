/*
 * Copyright 2006-2010 the original author or authors.
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
import org.citrusframework.functions.Function;

/**
 * Function returning a random numeric value. Argument specifies the number of digits and
 * padding boolean flag.
 *
 * @author Christoph Deppisch
 */
public class RandomNumberFunction implements Function {
    /** Basic seed generating random number */
    private static Random generator = new Random(System.currentTimeMillis());

    /**
     * @see org.citrusframework.functions.Function#execute(java.util.List, org.citrusframework.context.TestContext)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList, TestContext context) {
        int numberLength;
        boolean paddingOn = true;

        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        if (parameterList.size() > 2) {
            throw new InvalidFunctionUsageException("Too many parameters for function");
        }

        numberLength = Integer.valueOf(parameterList.get(0));
        if (numberLength < 0) {
            throw new InvalidFunctionUsageException("Invalid parameter definition. Number of letters must not be positive non-zero integer value");
        }

        if (parameterList.size() > 1) {
            paddingOn = Boolean.valueOf(parameterList.get(1));
        }

        return getRandomNumber(numberLength, paddingOn);
    }

    /**
     * Static number generator method.
     * @param numberLength
     * @param paddingOn
     * @return
     */
    public static String getRandomNumber(int numberLength, boolean paddingOn) {
        if (numberLength < 1) {
            throw new InvalidFunctionUsageException("numberLength must be greater than 0 - supplied " + numberLength);
        }

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < numberLength; i++) {
            buffer.append(generator.nextInt(10));
        }

        return checkLeadingZeros(buffer.toString(), paddingOn);
    }

    /**
     * Remove leading Zero numbers.
     * @param generated
     * @param paddingOn
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
     * @param generated
     * @return
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

        if (builder.length() == 0) {
            // very unlikely to happen, ensures that empty string is not returned
            builder.append('0');
        }

        return builder.toString();
    }

    /**
     * Replaces first leading zero number if present.
     * @param generated
     * @return
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
}
