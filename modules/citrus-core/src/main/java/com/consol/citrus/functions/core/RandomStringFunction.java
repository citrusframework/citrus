package com.consol.citrus.functions.core;

import java.util.List;
import java.util.Random;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.Function;

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

    private static final String UPPERCASE = "UPPERCASE";

    private static final String LOWERCASE = "LOWERCASE";

    private static final String MIXED = "MIXED";

    public String execute(List<String> parameterList) throws TestSuiteException {
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
