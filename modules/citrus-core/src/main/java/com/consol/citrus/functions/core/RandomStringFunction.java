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

package com.consol.citrus.functions.core;

import java.util.List;
import java.util.Random;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;

/**
 * Function generating a random string containing alphabetic characters. Arguments specify
 * upper and lower case mode.
 * 
 * @author Christoph Deppisch
 */
public class RandomStringFunction implements Function {
    private static Random generator = new Random(System.currentTimeMillis());

    private static char[] ALPHABET_UPPER = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z' };

    private static char[] ALPHABET_LOWER = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z' };

    private static char[] ALPHABET_MIXED = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z' };

    /** Mode upper case */
    private static final String UPPERCASE = "UPPERCASE";

    /** Mode lower case */
    private static final String LOWERCASE = "LOWERCASE";

    /** Mode mixed (upper and lower case characters) */
    private static final String MIXED = "MIXED";

    /**
     * @see com.consol.citrus.functions.Function#execute(java.util.List)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList) {
        int numberOfLetters;
        String notationMethod;

        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        if (parameterList.size() > 2) {
            throw new InvalidFunctionUsageException("Too many parameters for function");
        }

        numberOfLetters = new Integer(parameterList.get(0)).intValue();
        if (numberOfLetters < 0) {
            throw new InvalidFunctionUsageException("Invalid parameter definition. Number of letters must not be positive non-zero integer value");
        }

        if (parameterList.size() > 1) {
            notationMethod = parameterList.get(1);

            if (notationMethod.equals(UPPERCASE)) {
                return getRandomString(numberOfLetters, ALPHABET_UPPER);
            } else if (notationMethod.equals(LOWERCASE)) {
                return getRandomString(numberOfLetters, ALPHABET_LOWER);
            } else if (notationMethod.equals(MIXED)) {
                return getRandomString(numberOfLetters, ALPHABET_MIXED);
            }
        }

        return getRandomString(numberOfLetters, ALPHABET_MIXED);
    }

    public static String getRandomString(int numberOfLetters, char[] alphabet) {
        StringBuffer sBuf = new StringBuffer();
        int upperRange = alphabet.length - 1;
        for (int i = 0; i < numberOfLetters; i++) {
            int letterIndex = generator.nextInt(upperRange);
            sBuf.append(alphabet[letterIndex]);
        }
        return sBuf.toString();
    }
}
