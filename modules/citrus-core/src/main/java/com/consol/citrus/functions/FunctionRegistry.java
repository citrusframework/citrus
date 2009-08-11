package com.consol.citrus.functions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.exceptions.NoSuchFunctionLibraryException;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class FunctionRegistry {
    /** list of libraries providing custom functions */
    @Autowired
    private List<FunctionLibrary> functionLibraries;
    
    /**
     * Check if variable expression is a custom function.
     * Expression has to start with one of the registered function library prefix.
     * @param variableExpression to be checked
     * @return flag (true/false)
     */
    public boolean isFunction(final String variableExpression) {
        if(variableExpression == null || variableExpression.length() == 0) {
            return false;
        }
        
        for (int i = 0; i < functionLibraries.size(); i++) {
            FunctionLibrary lib = (FunctionLibrary)functionLibraries.get(i);
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
        for (int i = 0; i < functionLibraries.size(); i++) {
            if (((FunctionLibrary)functionLibraries.get(i)).getPrefix().equals(functionPrefix)) {
                return (FunctionLibrary)functionLibraries.get(i);
            }
        }

        throw new NoSuchFunctionLibraryException("Can not find function library for prefix " + functionPrefix);
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
