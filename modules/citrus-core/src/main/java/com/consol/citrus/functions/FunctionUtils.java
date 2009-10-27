/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.functions;

import java.text.ParseException;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.NoSuchFunctionException;
import com.consol.citrus.variable.VariableUtils;

public class FunctionUtils {

    private FunctionUtils() {}
    
    /**
     *
     * @param str
     * @return
     */
    public static String replaceFunctionsInString(String str, TestContext context) {
        return replaceFunctionsInString(str, context, false);
    }
   
    /**
     * @param str
     * @param enableQuoting
     * @return
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
     * This method resolves a custom function
     * @param functionString
     * @throws CitrusRuntimeException
     * @return
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
