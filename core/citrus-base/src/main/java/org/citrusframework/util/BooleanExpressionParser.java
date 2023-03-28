/*
 * Copyright 2006-2019 the original author or authors.
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

package org.citrusframework.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Parses boolean expression strings and evaluates to boolean result.
 */
public final class BooleanExpressionParser {

    /**
     * List of known non-boolean operators
     */
    private static final List<String> OPERATORS = new ArrayList<>(Arrays.asList("lt", "lt=", "gt", "gt=", "<", "<=", ">", ">="));

    /**
     * List of known boolean operators
     */
    private static final List<String> BOOLEAN_OPERATORS = new ArrayList<>(Arrays.asList("=", "and", "or"));

    /**
     * List of known boolean values
     */
    private static final List<String> BOOLEAN_VALUES = new ArrayList<>(
            Arrays.asList(TRUE.toString(), FALSE.toString()));

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

        @Override
        public String toString(){
            return value.toString();
        }
    }

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(BooleanExpressionParser.class);

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
     * @throws CitrusRuntimeException When unable to parse expression
     */
    public static boolean evaluate(final String expression) {
        final Deque<String> operators = new ArrayDeque<>();
        final Deque<String> values = new ArrayDeque<>();
        final boolean result;

        char currentCharacter;
        int currentCharacterIndex = 0;

        try {
            while (currentCharacterIndex < expression.length()) {
                currentCharacter = expression.charAt(currentCharacterIndex);

                if (SeparatorToken.OPEN_PARENTHESIS.value == currentCharacter) {
                    operators.push(SeparatorToken.OPEN_PARENTHESIS.toString());
                    currentCharacterIndex += moveCursor(SeparatorToken.OPEN_PARENTHESIS.toString());
                } else if (SeparatorToken.SPACE.value == currentCharacter) {
                    currentCharacterIndex += moveCursor(SeparatorToken.SPACE.toString());
                } else if (SeparatorToken.CLOSE_PARENTHESIS.value == currentCharacter) {
                    evaluateSubexpression(operators, values);
                    currentCharacterIndex += moveCursor(SeparatorToken.CLOSE_PARENTHESIS.toString());
                } else if (!Character.isDigit(currentCharacter)) {
                    final String parsedNonDigit = parseNonDigits(expression, currentCharacterIndex);
                    if (isBoolean(parsedNonDigit)) {
                        values.push(replaceBooleanStringByIntegerRepresentation(parsedNonDigit));
                    } else {
                        operators.push(validateOperator(parsedNonDigit));
                    }
                    currentCharacterIndex += moveCursor(parsedNonDigit);
                } else if (Character.isDigit(currentCharacter)) {
                    final String parsedDigits = parseDigits(expression, currentCharacterIndex);
                    values.push(parsedDigits);
                    currentCharacterIndex += moveCursor(parsedDigits);
                }
            }

            result = Boolean.valueOf(evaluateExpressionStack(operators, values));

            if (log.isDebugEnabled()) {
                log.debug("Boolean expression {} evaluates to {}", expression, result);
            }
        } catch (final NoSuchElementException e) {
            throw new CitrusRuntimeException("Unable to parse boolean expression '" + expression + "'. Maybe expression is incomplete!", e);
        }

        return result;
    }

    /**
     * This method takes stacks of operators and values and evaluates possible expressions
     * This is done by popping one operator and two values, applying the operator to the values and pushing the result back onto the value stack
     *
     * @param operators Operators to apply
     * @param values    Values
     * @return The final result popped of the values stack
     */
    private static String evaluateExpressionStack(final Deque<String> operators, final Deque<String> values) {
        while (!operators.isEmpty()) {
            values.push(getBooleanResultAsString(operators.pop(), values.pop(), values.pop()));
        }
        return replaceIntegerStringByBooleanRepresentation(values.pop());
    }

    /**
     * Evaluates a sub expression within a pair of parentheses and pushes its result onto the stack of values
     *
     * @param operators Stack of operators
     * @param values    Stack of values
     */
    private static void evaluateSubexpression(final Deque<String> operators, final Deque<String> values) {
        String operator = operators.pop();
        while (!(operator).equals(SeparatorToken.OPEN_PARENTHESIS.toString())) {
            values.push(getBooleanResultAsString(operator,
                    values.pop(),
                    values.pop()));
            operator = operators.pop();
        }
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
        final StringBuilder digitBuffer = new StringBuilder();

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
     * <p>
     * - a digit
     * - a separator token
     *
     * @param expression The string to parse
     * @param startIndex The start index from where to parse
     * @return The parsed substring
     */
    private static String parseNonDigits(final String expression, final int startIndex) {
        final StringBuilder operatorBuffer = new StringBuilder();

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
     *
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
        if (possibleBooleanString.equals(TRUE.toString())) {
            return "1";
        } else if (possibleBooleanString.equals(FALSE.toString())) {
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
            return FALSE.toString();
        } else if (value.equals("1")) {
            return TRUE.toString();
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
     * @throws CitrusRuntimeException When encountering an unknown operator
     */
    private static String validateOperator(final String operator) {
        if (!OPERATORS.contains(operator) && !BOOLEAN_OPERATORS.contains(operator)) {
            throw new CitrusRuntimeException("Unknown operator '" + operator + "'");
        }
        return operator;
    }

    /**
     * Returns the amount of characters to move the cursor after parsing a token
     *
     * @param lastToken Last parsed token
     * @return Amount of characters to move forward
     */
    private static int moveCursor(final String lastToken) {
        return lastToken.length();
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
            case "<":
                return Boolean.toString(Integer.valueOf(leftOperand) < Integer.valueOf(rightOperand));
            case "lt=":
            case "<=":
                return Boolean.toString(Integer.valueOf(leftOperand) <= Integer.valueOf(rightOperand));
            case "gt":
            case ">":
                return Boolean.toString(Integer.valueOf(leftOperand) > Integer.valueOf(rightOperand));
            case "gt=":
            case ">=":
                return Boolean.toString(Integer.valueOf(leftOperand) >= Integer.valueOf(rightOperand));
            case "=":
                return Boolean.toString(Integer.parseInt(leftOperand) == Integer.parseInt(rightOperand));
            case "and":
                return Boolean.toString(Boolean.valueOf(leftOperand) && Boolean.valueOf(rightOperand));
            case "or":
                return Boolean.toString(Boolean.valueOf(leftOperand) || Boolean.valueOf(rightOperand));
            default:
                throw new CitrusRuntimeException("Unknown operator '" + operator + "'");
        }
    }
}
