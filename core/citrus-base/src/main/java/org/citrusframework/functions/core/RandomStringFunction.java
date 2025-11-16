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

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;

/**
 * Function generating a random string containing alphabetic characters. Arguments specify
 * upper and lower case mode.
 */
public class RandomStringFunction implements ParameterizedFunction<RandomStringFunction.Parameters> {

    private static final Random random = new Random(currentTimeMillis());

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

    @Override
    public String execute(Parameters params, TestContext context) {
        return getRandomString(params);
    }

    public static String getRandomString(Parameters params) {
        if (params.getNotationMethod().equals(NotationMethod.UPPERCASE)) {
            return getRandomString(params, ALPHABET_UPPER);
        } else if (params.getNotationMethod().equals(NotationMethod.LOWERCASE)) {
            return getRandomString(params, ALPHABET_LOWER);
        } else {
            return getRandomString(params, ALPHABET_MIXED);
        }
    }

    public static String getRandomString(Parameters params, char[] alphabet) {
        return getRandomString(params.getLength(), alphabet, params.isIncludeNumbers(), params.getMinNumberOfLetters());
    }

    /**
     * Static random number generator aware string generating method.
     */
    public static String getRandomString(int numberOfLetters, char[] alphabet, boolean includeNumbers, int minNumberOfLetters) {
        StringBuilder builder = new StringBuilder();

        int upperRange = alphabet.length - 1;

        // make sure first character is not a number
        builder.append(alphabet[random.nextInt(upperRange)]);

        if (includeNumbers) {
            upperRange += NUMBERS.length;
        }

        if (minNumberOfLetters > -1) {
            numberOfLetters = ThreadLocalRandom.current()
                .nextInt(minNumberOfLetters, numberOfLetters + 1);
        }

        for (int i = 1; i < numberOfLetters; i++) {
            int letterIndex = random.nextInt(upperRange);

            if (letterIndex > alphabet.length - 1) {
                builder.append(NUMBERS[letterIndex - alphabet.length]);
            } else {
                builder.append(alphabet[letterIndex]);
            }
        }

        return builder.toString();
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public enum NotationMethod {
        MIXED, UPPERCASE, LOWERCASE
    }

    public static class Parameters implements FunctionParameters {

        private int length;
        private NotationMethod notationMethod = NotationMethod.MIXED;
        private boolean includeNumbers = false;
        private int minNumberOfLetters = -1;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            if (parameterList.size() > 4) {
                throw new InvalidFunctionUsageException("Too many parameters for function");
            }

            setLength(parseInt(parameterList.get(0)));
            if (getLength() < 0) {
                throw new InvalidFunctionUsageException("Invalid parameter definition. Number of letters must not be positive non-zero integer value");
            }

            if (parameterList.size() > 1 && Arrays.stream(NotationMethod.values())
                                                .map(NotationMethod::name)
                                                .anyMatch(parameterList.get(1)::equals)) {
                setNotationMethod(NotationMethod.valueOf(parameterList.get(1)));
            }

            if (parameterList.size() > 2) {
                setIncludeNumbers(parseBoolean(parameterList.get(2)));
            }

            if (parameterList.size() > 3) {
                setMinNumberOfLetters(parseInt(parameterList.get(3)));
            }
        }

        public int getLength() {
            return length;
        }

        @SchemaProperty(required = true, description = "Defines the length of the generated string.")
        public void setLength(int length) {
            this.length = length;
        }

        public boolean isIncludeNumbers() {
            return includeNumbers;
        }

        public void setIncludeNumbers(boolean includeNumbers) {
            this.includeNumbers = includeNumbers;
        }

        public int getMinNumberOfLetters() {
            return minNumberOfLetters;
        }

        @SchemaProperty(description = "Number of letters that must be part of the generated string.")
        public void setMinNumberOfLetters(int minNumberOfLetters) {
            this.minNumberOfLetters = minNumberOfLetters;
        }

        public NotationMethod getNotationMethod() {
            return notationMethod;
        }

        @SchemaProperty(description = "The notation method to use.")
        public void setNotationMethod(NotationMethod notationMethod) {
            this.notationMethod = notationMethod;
        }
    }
}
