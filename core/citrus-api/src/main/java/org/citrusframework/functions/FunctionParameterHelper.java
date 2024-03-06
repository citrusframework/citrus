/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.functions;

import java.util.List;
import java.util.Stack;

/**
 * Helper class parsing a parameter string and converting the tokens to a parameter list.
 * 
 * @author Christoph Deppisch
 */
public final class FunctionParameterHelper {
    
    /**
     * Prevent class instantiation.
     */
    private FunctionParameterHelper() {}
    
    /**
     * Convert a parameter string to a list of parameters.
     *
     * @param parameterString comma separated parameter string.
     * @return list of parameters.
     */
    public static List<String> getParameterList(String parameterString) {
        return new ParameterParser(parameterString).parse();
    }

    public static class ParameterParser {

        private final String parameterString;
        private final Stack<String> parameterList = new Stack<>();
        private String currentParameter = "";
        private int lastQuoteIndex = -1;
        private boolean isBetweenParams = false;

        public ParameterParser(String parameterString) {
            this.parameterString = parameterString;
        }

        public List<String> parse() {
            parameterList.clear();
            for (int i = 0; i < parameterString.length(); i++) {
                parseCharacterAt(i);
            }
            return parameterList.stream().toList();
        }

        private void parseCharacterAt(int i) {
            char c = parameterString.charAt(i);
            if (isParameterSeparatingComma(c)) {
                isBetweenParams = true;
                addCurrentParamIfNotEmpty();
            } else if (isNestedSingleQuote(c)) {
                lastQuoteIndex = i;
                appendCurrentValueToLastParameter();
            } else if (isStartingSingleQuote(c)) {
                isBetweenParams = false;
                lastQuoteIndex = i;
            } else if (isSingleQuote(c)) { // closing quote
                addCurrentParamIfNotEmpty();
            } else {
                if (isBetweenParams && !String.valueOf(c).matches("\\s")) isBetweenParams = false;
                if (!isBetweenParams) currentParameter += c;
            }
            if (isLastChar(i)) { //  TestFramework!
                addCurrentParamIfNotEmpty();
            }
        }

        private void appendCurrentValueToLastParameter() {
            currentParameter = "%s'%s'".formatted(parameterList.pop(), currentParameter);
        }

        private boolean isLastChar(int i) {
            return i == parameterString.length() - 1;
        }

        private boolean isNestedSingleQuote(char c) {
            return isSingleQuote(c) && isNotWithinSingleQuotes() && !currentParameter.trim().isEmpty();
        }

        private boolean isStartingSingleQuote(char c) {
            return isSingleQuote(c) && isNotWithinSingleQuotes();
        }

        private boolean isParameterSeparatingComma(char c) {
            return isComma(c) && isNotWithinSingleQuotes();
        }

        private boolean isComma(char c) {
            return c == ',';
        }

        private boolean isNotWithinSingleQuotes() {
            return lastQuoteIndex < 0;
        }

        private static boolean isSingleQuote(char c) {
            return c == '\'';
        }

        private void addCurrentParamIfNotEmpty() {
            if (!currentParameter.replaceAll("^'|'$", "").isEmpty()) {
                parameterList.add(currentParameter);
            }
            lastQuoteIndex = -1;
            currentParameter = "";
        }
    }
}
