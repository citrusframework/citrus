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

package org.citrusframework.validation.context;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.validation.HeaderValidator;

/**
 * @since 2.7.6
 */
public class HeaderValidationContext implements ValidationContext {

    /** List of special header validators */
    private final List<HeaderValidator> validators;

    /** List of special header validator references */
    private final List<String> validatorNames;

    /** Should header name validation ignore case sensitivity */
    private final boolean headerNameIgnoreCase;

    /** The status of this context */
    private ValidationStatus status = ValidationStatus.UNKNOWN;

    public HeaderValidationContext() {
        this(new Builder());
    }

    public HeaderValidationContext(Builder builder) {
        this.validators = builder.validators;
        this.validatorNames = builder.validatorNames;
        this.headerNameIgnoreCase = builder.headerNameIgnoreCase;
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements ValidationContext.Builder<HeaderValidationContext, Builder> {

        /** List of special header validators */
        private List<HeaderValidator> validators = new ArrayList<>();

        /** List of special header validator references */
        private List<String> validatorNames = new ArrayList<>();

        /** Should header name validation ignore case sensitivity */
        private boolean headerNameIgnoreCase = false;

        /**
         * Sets the headerNameIgnoreCase.
         */
        public Builder ignoreCase(boolean headerNameIgnoreCase) {
            this.headerNameIgnoreCase = headerNameIgnoreCase;
            return this;
        }


        /**
         * Adds header validator.
         */
        public Builder validator(HeaderValidator validator) {
            this.validators.add(validator);
            return this;
        }

        /**
         * Adds header validator reference.
         */
        public Builder validator(String validatorName) {
            this.validatorNames.add(validatorName);
            return this;
        }

        /**
         * Sets the validators.
         */
        public Builder validators(List<HeaderValidator> validators) {
            this.validators.addAll(validators);
            return this;
        }

        /**
         * Sets the validatorNames.
         */
        public Builder validatorNames(List<String> validatorNames) {
            this.validatorNames.addAll(validatorNames);
            return this;
        }

        @Override
        public HeaderValidationContext build() {
            return new HeaderValidationContext(this);
        }
    }

    /**
     * Gets the headerNameIgnoreCase.
     *
     * @return
     */
    public boolean isHeaderNameIgnoreCase() {
        return headerNameIgnoreCase;
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
     * Gets the validatorNames.
     *
     * @return
     */
    public List<String> getValidatorNames() {
        return validatorNames;
    }

    @Override
    public void updateStatus(ValidationStatus status) {
        if (status != ValidationStatus.FAILED) {
            this.status = status;
        }
    }

    @Override
    public ValidationStatus getStatus() {
        return status;
    }
}
