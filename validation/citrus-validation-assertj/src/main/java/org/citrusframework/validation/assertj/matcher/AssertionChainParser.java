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

package org.citrusframework.validation.assertj.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.citrusframework.exceptions.ValidationException;

/**
 * Parses an {@code @assertj(...)@} expression into a chain of assertion calls.
 * <pre>
 * chain   := call ( "." call )*
 * call    := name "(" [ literal ( "," literal )* ] ")"
 * literal := 'quoted string' | integer | decimal | true | false | null
 * </pre>
 * Quoted strings may contain {@code \'} and {@code \\} escapes. Test variables are resolved before parsing.
 */
final class AssertionChainParser {

    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern DECIMAL = Pattern.compile("-?\\d+\\.\\d+");

    private final String expression;
    private int position;

    private AssertionChainParser(String expression) {
        this.expression = expression;
    }

    static List<AssertionCall> parse(String expression) {
        return new AssertionChainParser(expression).chain();
    }

    private List<AssertionCall> chain() {
        List<AssertionCall> calls = new ArrayList<>();
        calls.add(call());

        skipWhitespace();
        while (position < expression.length()) {
            expect('.', "expected '.'");
            calls.add(call());
            skipWhitespace();
        }

        return calls;
    }

    private AssertionCall call() {
        skipWhitespace();
        String name = name();
        skipWhitespace();
        expect('(', "expected '('");

        List<Object> arguments = new ArrayList<>();
        skipWhitespace();
        if (peek() == ')') {
            position++;
            return new AssertionCall(name, arguments);
        }

        while (true) {
            if (position >= expression.length()) {
                throw error("expected argument or ')'");
            }

            arguments.add(literal());
            skipWhitespace();

            char next = peek();
            position++;
            if (next == ')') {
                return new AssertionCall(name, arguments);
            } else if (next != ',') {
                position--;
                throw error("expected ',' or ')'");
            }
            skipWhitespace();
        }
    }

    private String name() {
        int start = position;
        if (position < expression.length() && Character.isJavaIdentifierStart(expression.charAt(position))) {
            position++;
            while (position < expression.length() && Character.isJavaIdentifierPart(expression.charAt(position))) {
                position++;
            }
        }

        if (start == position) {
            throw error("expected assertion method name");
        }

        return expression.substring(start, position);
    }

    private Object literal() {
        if (peek() == '\'') {
            return quoted();
        }

        int start = position;
        while (position < expression.length() && ",) \t\r\n".indexOf(expression.charAt(position)) < 0) {
            position++;
        }

        String token = expression.substring(start, position);
        if (token.equals("true") || token.equals("false")) {
            return Boolean.valueOf(token);
        } else if (token.equals("null")) {
            return null;
        } else if (DECIMAL.matcher(token).matches()) {
            return Double.valueOf(token);
        } else if (INTEGER.matcher(token).matches()) {
            return integer(token, start);
        }

        position = start;
        throw error(token.isEmpty() ? "expected argument or ')'" : "unquoted argument '" + token + "' - quote text arguments with '");
    }

    private Object integer(String token, int start) {
        try {
            long value = Long.parseLong(token);
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }
            return value;
        } catch (NumberFormatException e) {
            position = start;
            throw error("number out of range '" + token + "'");
        }
    }

    private String quoted() {
        int start = position;
        position++;

        StringBuilder value = new StringBuilder();
        while (position < expression.length()) {
            char current = expression.charAt(position++);
            if (current == '\\' && position < expression.length()
                    && (expression.charAt(position) == '\'' || expression.charAt(position) == '\\')) {
                value.append(expression.charAt(position++));
            } else if (current == '\'') {
                return value.toString();
            } else {
                value.append(current);
            }
        }

        position = start;
        throw error("unterminated quoted argument");
    }

    private void expect(char expected, String reason) {
        if (peek() != expected) {
            throw error(reason);
        }
        position++;
    }

    private char peek() {
        return position < expression.length() ? expression.charAt(position) : 0;
    }

    private void skipWhitespace() {
        while (position < expression.length() && Character.isWhitespace(expression.charAt(position))) {
            position++;
        }
    }

    private ValidationException error(String reason) {
        return new ValidationException(String.format("Invalid @assertj()@ expression '%s': %s at position %d", expression, reason, position));
    }
}
