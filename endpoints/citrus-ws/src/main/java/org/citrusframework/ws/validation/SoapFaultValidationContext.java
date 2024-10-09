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

package org.citrusframework.ws.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Special validation context holds 1-n {@link SoapFaultDetailValidationContext} instances for
 * 1-n SOAP fault detail elements.
 *
 */
public class SoapFaultValidationContext extends DefaultValidationContext {

    /** List of validation contexts to use for SOAP fault detail validation */
    private final List<SoapFaultDetailValidationContext> validationContexts;

    /**
     * Default constructor.
     */
    public SoapFaultValidationContext() {
        this(Builder.fault());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public SoapFaultValidationContext(Builder builder) {
         this.validationContexts = builder.validationContexts;
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements ValidationContext.Builder<SoapFaultValidationContext, Builder> {

        private final List<SoapFaultDetailValidationContext> validationContexts = new ArrayList<>();

        /**
         * Static entry method for fluent builder API.
         * @return
         */
        public static Builder fault() {
            return new Builder();
        }

        /**
         * Add fault detail validation context.
         * @param detailValidationContext
         * @return
         */
        public Builder detail(SoapFaultDetailValidationContext detailValidationContext) {
            this.validationContexts.add(detailValidationContext);
            return this;
        }

        /**
         * Add fault detail validation context.
         * @param detailValidationContexts
         * @return
         */
        public Builder details(List<SoapFaultDetailValidationContext> detailValidationContexts) {
            this.validationContexts.addAll(detailValidationContexts);
            return this;
        }

        /**
         * Add fault detail validation context.
         * @param detailValidationContexts
         * @return
         */
        public Builder details(SoapFaultDetailValidationContext ... detailValidationContexts) {
            return details(Arrays.asList(detailValidationContexts));
        }

        @Override
        public SoapFaultValidationContext build() {
            return new SoapFaultValidationContext(this);
        }
    }

    /**
     * Gets the validationContexts.
     * @return the validationContexts.
     */
    public List<SoapFaultDetailValidationContext> getValidationContexts() {
        return validationContexts;
    }

    /**
     * Adds new validation context to the list of contexts.
     * @param context
     */
    public SoapFaultValidationContext addValidationContext(SoapFaultDetailValidationContext context) {
        this.validationContexts.add(context);
        return this;
    }
}
