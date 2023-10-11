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
 * Function generating a random string containing alphabetic characters. Arguments specify
 * upper and lower case mode.
 *
 * @author Christoph Deppisch
 */
public class RandomStringFunction implements Function {
    private static Random generator = new Random(System.currentTimeMillis());

    private static final char[] ALPHABET_UPPER = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z' };

    private static final char[] ALPHABET_LOWER = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z' };

    private static final char[] ALPHABET_MIXED = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z' };

    private static final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', };

    /** Mode upper case */
    public static final String UPPERCASE = "UPPERCASE";

    /** Mode lower case */
    public static final String LOWERCASE = "LOWERCASE";

    /** Mode mixed (upper and lower case characters) */
    public static final String MIXED = "MIXED";

    /**
     * @see org.citrusframework.functions.Function#execute(java.util.List, org.citrusframework.context.TestContext)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList, TestContext context) {
        int numberOfLetters;
        String notationMethod = MIXED;
        boolean includeNumbers = false;

        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        if (parameterList.size() > 3) {
            throw new InvalidFunctionUsageException("Too many parameters for function");
        }

        numberOfLetters = Integer.valueOf(parameterList.get(0));
        if (numberOfLetters < 0) {
            throw new InvalidFunctionUsageException("Invalid parameter definition. Number of letters must not be positive non-zero integer value");
        }

        if (parameterList.size() > 1) {
            notationMethod = parameterList.get(1);
        }

        if (parameterList.size() > 2) {
            includeNumbers = Boolean.valueOf(parameterList.get(2));
        }

        if (notationMethod.equals(UPPERCASE)) {
            return getRandomString(numberOfLetters, ALPHABET_UPPER, includeNumbers);
        } else if (notationMethod.equals(LOWERCASE)) {
            return getRandomString(numberOfLetters, ALPHABET_LOWER, includeNumbers);
        } else {
            return getRandomString(numberOfLetters, ALPHABET_MIXED, includeNumbers);
        }
    }

    /**
     * Static random number generator aware string generating method.
     * @param numberOfLetters
     * @param alphabet
     * @param includeNumbers
     * @return
     */
    public static String getRandomString(int numberOfLetters, char[] alphabet, boolean includeNumbers) {
        StringBuilder builder = new StringBuilder();

        int upperRange = alphabet.length - 1;

        // make sure first character is not a number
        builder.append(alphabet[generator.nextInt(upperRange)]);

        if (includeNumbers) {
            upperRange += NUMBERS.length;
        }

        for (int i = 1; i < numberOfLetters; i++) {
            int letterIndex = generator.nextInt(upperRange);

            if (letterIndex > alphabet.length - 1) {
                builder.append(NUMBERS[letterIndex - alphabet.length]);
            } else {
                builder.append(alphabet[letterIndex]);
            }
        }

        return builder.toString();
    }
}
