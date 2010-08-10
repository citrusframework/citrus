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

package com.consol.citrus.functions;

import java.text.ParseException;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.NoSuchFunctionException;
import com.consol.citrus.variable.VariableUtils;

/**
 * Utility class for functions.
 * 
 * @author Christoph Deppisch
 */
public class FunctionUtils {

    /**
     * Prevent class instantiation.
     */
    private FunctionUtils() {}
    
    /**
     * Search for functions in string and replace with respective function result. 
     * @param string to parse
     * @return parsed string result
     */
    public static String replaceFunctionsInString(String str, TestContext context) {
        return replaceFunctionsInString(str, context, false);
    }
   
    /**
     * Search for functions in string and replace with respective function result.
     * @param string to parse.
     * @param enableQuoting enables quoting of function results.
     * @return parsed string result.
     */
    public static String replaceFunctionsInString(final String stringValue, TestContext context, boolean enableQuoting) {
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

                    if ((!Character.isJavaIdentifierPart(newString.charAt(curIndex)) && (newString.charAt(curIndex) == ')')) || (curIndex+1 == newString.length())) {
                        if (control == 0) {
                            isVarComplete = true;
                        } else {
                            control--;
                        }
                    }

                    variableNameBuf.append(newString.charAt(curIndex));
                    ++curIndex;
                }

                final String value = resolveFunction(variableNameBuf.toString(), context);
                if (value == null) {
                    throw new NoSuchFunctionException("Function: " + variableNameBuf.toString() + " could not be found");
                }

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
     * @throws CitrusRuntimeException
     * @return evaluated result
     */
    public static String resolveFunction(String functionString, TestContext context) {
        functionString = VariableUtils.cutOffVariablesPrefix(functionString);

        String functionPrefix = functionString.substring(0, functionString.indexOf(':')+1);
        String parameterString = functionString.substring(functionString.indexOf('(')+1, functionString.length()-1);
        String function = functionString.substring(functionString.indexOf(':')+1, functionString.indexOf('('));

        FunctionLibrary library = context.getFunctionRegistry().getLibraryForPrefix(functionPrefix);

        try {
            parameterString = VariableUtils.replaceVariablesInString(parameterString, context);
            parameterString = replaceFunctionsInString(parameterString, context);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }

        return library.getFunction(function).execute(FunctionParameterHelper.getParameterList(parameterString));
    }
}
