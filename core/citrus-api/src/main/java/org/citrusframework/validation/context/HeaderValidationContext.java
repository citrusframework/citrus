/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation.context;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.validation.HeaderValidator;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class HeaderValidationContext implements ValidationContext {

    /** List of special header validators */
    private List<HeaderValidator> validators = new ArrayList<>();

    /** List of special header validator references */
    private List<String> validatorNames = new ArrayList<>();

    /** Should header name validation ignore case sensitivity */
    private boolean headerNameIgnoreCase = false;

    /**
     * Gets the headerNameIgnoreCase.
     *
     * @return
     */
    public boolean isHeaderNameIgnoreCase() {
        return headerNameIgnoreCase;
    }

    /**
     * Sets the headerNameIgnoreCase.
     *
     * @param headerNameIgnoreCase
     */
    public void setHeaderNameIgnoreCase(boolean headerNameIgnoreCase) {
        this.headerNameIgnoreCase = headerNameIgnoreCase;
    }

    /**
     * Adds header validator.
     * @param validator
     */
    public void addHeaderValidator(HeaderValidator validator) {
        this.validators.add(validator);
    }

    /**
     * Adds header validator reference.
     * @param validatorName
     */
    public void addHeaderValidator(String validatorName) {
        this.validatorNames.add(validatorName);
    }

    /**
     * Gets the validators.
     *
     * @return
     */
    public List<HeaderValidator> getValidators() {
        return validators;
    }

    /**
     * Sets the validators.
     *
     * @param validators
     */
    public void setValidators(List<HeaderValidator> validators) {
        this.validators = validators;
    }

    /**
     * Gets the validatorNames.
     *
     * @return
     */
    public List<String> getValidatorNames() {
        return validatorNames;
    }

    /**
     * Sets the validatorNames.
     *
     * @param validatorNames
     */
    public void setValidatorNames(List<String> validatorNames) {
        this.validatorNames = validatorNames;
    }
}
