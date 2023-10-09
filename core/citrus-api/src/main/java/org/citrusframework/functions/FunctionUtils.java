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

package org.citrusframework.functions;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.variable.VariableUtils;

/**
 * Utility class for functions.
 *
 * @author Christoph Deppisch
 */
public final class FunctionUtils {

    /**
     * Prevent class instantiation.
     */
    private FunctionUtils() {}

    /**
     * Search for functions in string and replace with respective function result.
     * @param str to parse
     * @return parsed string result
     */
    public static String replaceFunctionsInString(String str, TestContext context) {
        return replaceFunctionsInString(str, context, false);
    }

    /**
     * Search for functions in string and replace with respective function result.
     * @param stringValue to parse.
     * @param enableQuoting enables quoting of function results.
     * @return parsed string result.
     */
    public static String replaceFunctionsInString(final String stringValue, TestContext context, boolean enableQuoting) {
        // make sure given string expression meets requirements for having a function
        if (stringValue == null || stringValue.isBlank() ||
                (stringValue.indexOf(':') < 0) || (stringValue.indexOf('(') < 0) || (stringValue.indexOf(')') < 0) ) {

            // it is not a function, as it is defined as 'prefix:methodName(arguments)'
            return stringValue;
        }

        String newString = stringValue;
        StringBuffer strBuffer = new StringBuffer();

        boolean isVarComplete = false;
        StringBuffer variableNameBuf = new StringBuffer();

        int startIndex = 0;
        int curIndex;
        int searchIndex;

        for (FunctionLibrary library: context.getFunctionRegistry().getFunctionLibraries()) {
            startIndex = 0;

            while ((searchIndex = newString.indexOf(library.getPrefix(), startIndex)) != -1) {
                int control = -1;
                isVarComplete = false;

                curIndex = searchIndex;

                while (curIndex < newString.length() && !isVarComplete) {
                    if (newString.indexOf('(', curIndex) == curIndex) {
                        control++;
                    }

                    if (newString.charAt(curIndex) == ')' || curIndex == newString.length() - 1) {
                        if (control == 0) {
                            isVarComplete = true;
                        } else {
                            control--;
                        }
                    }

                    variableNameBuf.append(newString.charAt(curIndex));
                    curIndex++;
                }

                final String value = resolveFunction(variableNameBuf.toString(), context);

                strBuffer.append(newString.substring(startIndex, searchIndex));

                if (enableQuoting) {
                    strBuffer.append("'" + value + "'");
                } else {
                    strBuffer.append(value);
                }

                startIndex = curIndex;

                variableNameBuf = new StringBuffer();
                isVarComplete = false;
            }

            strBuffer.append(newString.substring(startIndex));
            newString = strBuffer.toString();

            strBuffer = new StringBuffer();
        }

        return newString;
    }

    /**
     * This method resolves a custom function to its respective result.
     * @param functionString to evaluate.
     * @throws org.citrusframework.exceptions.CitrusRuntimeException
     * @return evaluated result
     */
    public static String resolveFunction(String functionString, TestContext context) {
        String functionExpression = VariableUtils.cutOffVariablesPrefix(functionString);

        if (!functionExpression.contains("(") || !functionExpression.endsWith(")") || !functionExpression.contains(":")) {
            throw new InvalidFunctionUsageException("Unable to resolve function: " + functionExpression);
        }

        String functionPrefix = functionExpression.substring(0, functionExpression.indexOf(':') + 1);
        String parameterString = functionExpression.substring(functionExpression.indexOf('(') + 1, functionExpression.length() - 1);
        String function = functionExpression.substring(functionPrefix.length(), functionExpression.indexOf('('));

        FunctionLibrary library = context.getFunctionRegistry().getLibraryForPrefix(functionPrefix);

        parameterString = VariableUtils.replaceVariablesInString(parameterString, context, false);
        parameterString = replaceFunctionsInString(parameterString, context);

        String value = library.getFunction(function).execute(FunctionParameterHelper.getParameterList(parameterString), context);

        if (value == null) {
            return "";
        } else {
            return value;
        }
    }
}
