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

package com.consol.citrus.validation.matcher;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.exceptions.NoSuchFunctionLibraryException;

/**
 * ValidationMatcher registry holding all available validation matcher libraries.
 * 
 * @author Christian Wied
 */
public class ValidationMatcherRegistry {
    /** list of libraries providing custom validation matchers */
    @Autowired
    private List<ValidationMatcherLibrary> validationMatcherLibraries = new ArrayList<ValidationMatcherLibrary>();
    
    /**
     * Check if variable expression is a custom validationMatcher.
     * Expression has to start with one of the registered validationMatcher library prefix.
     * @param variableExpression to be checked
     * @return flag (true/false)
     */
    public boolean isValidationMatcher(final String variableExpression) {
        if (variableExpression == null || variableExpression.length() == 0) {
            return false;
        }
        
        for (int i = 0; i < validationMatcherLibraries.size(); i++) {
            ValidationMatcherLibrary lib = (ValidationMatcherLibrary)validationMatcherLibraries.get(i);
            if (variableExpression.startsWith(lib.getPrefix())) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Get library for validationMatcher prefix.
     * @param validationMatcherPrefix to be searched for
     * @return ValidationMatcherLibrary instance
     */
    public ValidationMatcherLibrary getLibraryForPrefix(String validationMatcherPrefix) {
        for (int i = 0; i < validationMatcherLibraries.size(); i++) {
            if (((ValidationMatcherLibrary)validationMatcherLibraries.get(i)).getPrefix().equals(validationMatcherPrefix)) {
                return (ValidationMatcherLibrary)validationMatcherLibraries.get(i);
            }
        }

        throw new NoSuchFunctionLibraryException("Can not find validationMatcher library for prefix " + validationMatcherPrefix);
    }
    
    /**
     * @param validationMatcherLibraries
     */
    public void setValidationMatcherLibraries(List<ValidationMatcherLibrary> validationMatcherLibraries) {
        this.validationMatcherLibraries = validationMatcherLibraries;
    }

    /**
     * @return the validationMatcherLibraries
     */
    public List<ValidationMatcherLibrary> getValidationMatcherLibraries() {
        return validationMatcherLibraries;
    }
}
