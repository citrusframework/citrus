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

package com.consol.citrus.util;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Parses boolean expression strings and evaluates to boolean result.
 *
 * @author Christoph Deppisch
 */
@SuppressWarnings("unchecked")
public final class BooleanExpressionParser {

    /**
     * List of known operators
     */
    private static final List<String> OPERATORS = new ArrayList<String>(
            CollectionUtils.arrayToList(new String[]{"=", "and", "or", "lt", "lt=", "gt", "gt="}));

    /**
     * List of known boolean values
     */
    private static final List<String> BOOLEAN_VALUES = new ArrayList<String>(
            CollectionUtils.arrayToList(new String[]{"true", "false"}));

    /**
     * SeparatorToken is an explicit type to identify different kinds of separators.
     */
    private enum SeparatorToken {
        SPACE(' '),
        OPEN_PARENTHESIS('('),
        CLOSE_PARENTHESIS(')');

        private final Character value;

        SeparatorToken(final Character value) {
            this.value = value;
        }
    }

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(BooleanExpressionParser.class);

    /**
     * Prevent instantiation.
     */
    private BooleanExpressionParser() {
    }

    /**
     * Perform evaluation of boolean expression string.
     *
     * @param expression The expression to evaluate
     * @return boolean result
     * @throws CitrusRuntimeException
     */
    public static boolean evaluate(final String expression) {
        final Stack<String> operators = new Stack<>();
        final Stack<String> values = new Stack<>();
        boolean result;

        char currentCharacter;

        try {
            for (int currentCharacterIndex = 0; currentCharacterIndex < expression.length(); currentCharacterIndex++) {
                currentCharacter = expression.charAt(currentCharacterIndex);

                if (SeparatorToken.OPEN_PARENTHESIS.value == currentCharacter) {
                    operators.push(SeparatorToken.OPEN_PARENTHESIS.value.toString());
                } else if (SeparatorToken.SPACE.value == currentCharacter) {
                    continue; //ignore
                } else if (SeparatorToken.CLOSE_PARENTHESIS.value == currentCharacter) {
                    String operator = operators.pop();
                    while (!(operator).equals(SeparatorToken.OPEN_PARENTHESIS.value.toString())) {
                        values.push(getBooleanResultAsString(operator, values.pop(), values.pop()));
                        operator = operators.pop();
                    }
                } else if (!Character.isDigit(currentCharacter)) {
                    final StringBuffer operatorBuffer = new StringBuffer();

                    int subExpressionIndex = currentCharacterIndex;
                    do {
                        operatorBuffer.append(currentCharacter);
                        subExpressionIndex++;

                        if (subExpressionIndex < expression.length()) {
                            currentCharacter = expression.charAt(subExpressionIndex);
                        }
                    } while (subExpressionIndex < expression.length() && !Character.isDigit(currentCharacter) && !isSeparatorToken(currentCharacter));

                    currentCharacterIndex = subExpressionIndex - 1;

                    if (BOOLEAN_VALUES.contains(operatorBuffer.toString())) {
                        values.push(Boolean.valueOf(operatorBuffer.toString()) ? "1" : "0");
                    } else {
                        operators.push(validateOperator(operatorBuffer.toString()));
                    }
                } else if (Character.isDigit(currentCharacter)) {
                    final StringBuffer digitBuffer = new StringBuffer();

                    int subExpressionIndex = currentCharacterIndex;
                    do {
                        digitBuffer.append(currentCharacter);
                        subExpressionIndex++;

                        if (subExpressionIndex < expression.length()) {
                            currentCharacter = expression.charAt(subExpressionIndex);
                        }
                    } while (subExpressionIndex < expression.length() && Character.isDigit(currentCharacter));

                    currentCharacterIndex = subExpressionIndex - 1;

                    values.push(digitBuffer.toString());
                }
            }

            while (!operators.isEmpty()) {
                values.push(getBooleanResultAsString(operators.pop(), values.pop(), values.pop()));
            }

            String value = values.pop();

            if (value.equals("0")) {
                value = "false";
            } else if (value.equals("1")) {
                value = "true";
            }

            result = Boolean.valueOf(value);

            if (log.isDebugEnabled()) {
                log.debug("Boolean expression " + expression + " evaluates to " + result);
            }
        } catch (final EmptyStackException e) {
            throw new CitrusRuntimeException("Unable to parse boolean expression '" + expression + "'. Maybe expression is incomplete!", e);
        }

        return result;
    }

    /**
     * This method reads digit characters from a given string, starting at a given index.
     * It will read till the end of the string or up until it encounters a non-digit character
     *
     * @param expression The string to parse
     * @param startIndex The start index from where to parse
     * @return The parsed substring
     */
    private static String parseDigits(final String expression, final int startIndex) {
        final StringBuffer digitBuffer = new StringBuffer();

        char currentCharacter = expression.charAt(startIndex);
        int subExpressionIndex = startIndex;

        do {
            digitBuffer.append(currentCharacter);
            ++subExpressionIndex;

            if (subExpressionIndex < expression.length()) {
                currentCharacter = expression.charAt(subExpressionIndex);
            }
        } while (subExpressionIndex < expression.length() && Character.isDigit(currentCharacter));

        return digitBuffer.toString();
    }

    /**
     * This method reads non-digit characters from a given string, starting at a given index.
     * It will read till the end of the string or up until it encounters
     *
     * - a digit
     * - a separator token
     *
     * @param expression The string to parse
     * @param startIndex The start index from where to parse
     * @return The parsed substring
     */
    private static String parseNonDigits(final String expression, final int startIndex) {
        final StringBuffer operatorBuffer = new StringBuffer();

        char currentCharacter = expression.charAt(startIndex);
        int subExpressionIndex = startIndex;
        do {
            operatorBuffer.append(currentCharacter);
            subExpressionIndex++;

            if (subExpressionIndex < expression.length()) {
                currentCharacter = expression.charAt(subExpressionIndex);
            }
        } while (subExpressionIndex < expression.length() && !Character.isDigit(currentCharacter) && !isSeparatorToken(currentCharacter));

        return operatorBuffer.toString();
    }

    /**
     * Checks whether a string can be interpreted as a boolean value.
     * @param possibleBoolean The possible boolean value as string
     * @return Either true or false
     */
    private static Boolean isBoolean(final String possibleBoolean) {
        return BOOLEAN_VALUES.contains(possibleBoolean);
    }

    /**
     * Checks whether a String is a Boolean value and replaces it with its Integer representation
     * "true" -> "1"
     * "false" -> "0"
     *
     * @param possibleBooleanString "true" or "false"
     * @return "1" or "0"
     */
    private static String replaceBooleanStringByIntegerRepresentation(final String possibleBooleanString) {
        if (possibleBooleanString.equals("true")) {
            return "1";
        } else if (possibleBooleanString.equals("false")) {
            return "0";
        }
        return possibleBooleanString;
    }

    /**
     * Counterpart of {@link #replaceBooleanStringByIntegerRepresentation}
     * Checks whether a String is the Integer representation of a Boolean value and replaces it with its Boolean representation
     * "1" -> "true"
     * "0" -> "false"
     * otherwise -> value
     *
     * @param value "1", "0" or other string
     * @return "true", "false" or the input value
     */
    private static String replaceIntegerStringByBooleanRepresentation(final String value) {
        if (value.equals("0")) {
            return "false";
        } else if (value.equals("1")) {
            return "true";
        }
        return value;
    }

    /**
     * Checks whether a given character is a known separator token or no
     *
     * @param possibleSeparatorChar The character to check
     * @return True in case its a separator, false otherwise
     */
    private static boolean isSeparatorToken(final char possibleSeparatorChar) {
        for (final SeparatorToken token : SeparatorToken.values()) {
            if (token.value == possibleSeparatorChar) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if operator is known to this class.
     *
     * @param operator to validate
     * @return the operator itself.
     * @throws CitrusRuntimeException
     */
    private static String validateOperator(final String operator) {
        if (!OPERATORS.contains(operator)) {
            throw new CitrusRuntimeException("Unknown operator '" + operator + "'");
        }
        return operator;
    }

    /**
     * Evaluates a boolean expression to a String representation (true/false).
     *
     * @param operator     The operator to apply on operands
     * @param rightOperand The right hand side of the expression
     * @param leftOperand  The left hand side of the expression
     * @return true/false as String
     */
    private static String getBooleanResultAsString(final String operator, final String rightOperand, final String leftOperand) {
        switch (operator) {
            case "lt":
                return Boolean.valueOf(Integer.valueOf(leftOperand) < Integer.valueOf(rightOperand)).toString();
            case "lt=":
                return Boolean.valueOf(Integer.valueOf(leftOperand) <= Integer.valueOf(rightOperand)).toString();
            case "gt":
                return Boolean.valueOf(Integer.valueOf(leftOperand) > Integer.valueOf(rightOperand)).toString();
            case "gt=":
                return Boolean.valueOf(Integer.valueOf(leftOperand) >= Integer.valueOf(rightOperand)).toString();
            case "=":
                return Boolean.valueOf(Integer.valueOf(leftOperand).intValue() == Integer.valueOf(rightOperand).intValue()).toString();
            case "and":
                return Boolean.valueOf(Boolean.valueOf(leftOperand) && Boolean.valueOf(rightOperand)).toString();
            case "or":
                return Boolean.valueOf(Boolean.valueOf(leftOperand) || Boolean.valueOf(rightOperand)).toString();
            default:
                throw new CitrusRuntimeException("Unknown operator '" + operator + "'");
        }
    }
}
