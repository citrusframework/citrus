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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.NoSuchFunctionLibraryException;

/**
 * Function registry holding all available function libraries.
 *
 * @author Christoph Deppisch
 */
public class FunctionRegistry {

    /** list of libraries providing custom functions */
    private List<FunctionLibrary> functionLibraries = new ArrayList<>();

    /**
     * Check if variable expression is a custom function.
     * Expression has to start with one of the registered function library prefix.
     * @param variableExpression to be checked
     * @return flag (true/false)
     */
    public boolean isFunction(final String variableExpression) {
        if (variableExpression == null || variableExpression.length() == 0) {
            return false;
        }

        for (FunctionLibrary lib : functionLibraries) {
            if (variableExpression.startsWith(lib.getPrefix())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get library for function prefix.
     * @param functionPrefix to be searched for
     * @return FunctionLibrary instance
     */
    public FunctionLibrary getLibraryForPrefix(String functionPrefix) {
        for (FunctionLibrary functionLibrary : functionLibraries) {
            if (functionLibrary.getPrefix().equals(functionPrefix)) {
                return functionLibrary;
            }
        }

        throw new NoSuchFunctionLibraryException("Can not find function library for prefix " + functionPrefix);
    }

    /**
     * Adds given function library to this registry.
     */
    public void addFunctionLibrary(FunctionLibrary functionLibrary) {
        boolean prefixAlreadyUsed = this.functionLibraries.stream()
                .anyMatch(lib -> lib.getPrefix().equals(functionLibrary.getPrefix()));

        if (prefixAlreadyUsed) {
            throw new CitrusRuntimeException(String.format("Function library prefix '%s' is already bound to another instance. " +
                    "Please choose another prefix.", functionLibrary.getPrefix()));
        }

        this.functionLibraries.add(functionLibrary);
    }

    /**
     * @param functionLibraries
     */
    public void setFunctionLibraries(List<FunctionLibrary> functionLibraries) {
        this.functionLibraries = functionLibraries;
    }

    /**
     * @return the functionLibraries
     */
    public List<FunctionLibrary> getFunctionLibraries() {
        return functionLibraries;
    }
}
