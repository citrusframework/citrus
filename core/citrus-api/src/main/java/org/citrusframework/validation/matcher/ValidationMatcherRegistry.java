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

package org.citrusframework.validation.matcher;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.NoSuchValidationMatcherLibraryException;

/**
 * ValidationMatcher registry holding all available validation matcher libraries.
 *
 * @author Christian Wied
 */
public class ValidationMatcherRegistry {
    /** list of libraries providing custom validation matchers */
    private List<ValidationMatcherLibrary> validationMatcherLibraries = new ArrayList<>();

    /**
     * Get library for validationMatcher prefix.
     * @param validationMatcherPrefix to be searched for
     * @return ValidationMatcherLibrary instance
     */
    public ValidationMatcherLibrary getLibraryForPrefix(String validationMatcherPrefix) {
        if (validationMatcherLibraries != null) {
            for (ValidationMatcherLibrary validationMatcherLibrary : validationMatcherLibraries) {
                if (validationMatcherLibrary.getPrefix().equals(validationMatcherPrefix)) {
                    return validationMatcherLibrary;
                }
            }
        }

        throw new NoSuchValidationMatcherLibraryException(String.format("Can not find validationMatcher library for prefix '%s'", validationMatcherPrefix));
    }

    /**
     * Adds given validation matcher library to this registry.
     */
    public void addValidationMatcherLibrary(ValidationMatcherLibrary validationMatcherLibrary) {
        boolean prefixAlreadyUsed = this.validationMatcherLibraries.stream()
                .anyMatch(lib -> lib.getPrefix().equals(validationMatcherLibrary.getPrefix()));

        if (prefixAlreadyUsed) {
            throw new CitrusRuntimeException(String.format("Validation matcher library prefix '%s' is already bound to another instance. " +
                    "Please choose another prefix.", validationMatcherLibrary.getPrefix()));
        }

        this.validationMatcherLibraries.add(validationMatcherLibrary);
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
