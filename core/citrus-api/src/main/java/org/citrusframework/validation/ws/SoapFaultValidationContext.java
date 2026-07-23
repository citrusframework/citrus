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

package org.citrusframework.validation.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.validation.context.DefaultValidationContext;

/**
 * Special validation context holds 1-n {@link SoapFaultDetailValidationContext} instances for
 * 1-n SOAP fault detail elements.
 */
public class SoapFaultValidationContext extends DefaultValidationContext {

    /** List of validation contexts to use for SOAP fault detail validation */
    private final List<SoapFaultDetailValidationContext.Builder> validationContexts;

    /**
     * Default constructor.
     */
    public SoapFaultValidationContext() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     */
    public SoapFaultValidationContext(Builder builder) {
         this.validationContexts = builder.validationContexts;
    }

    /**
     * Fluent builder.
     */
    public static class Builder implements
            SoapFaultValidationContextBuilder<SoapFaultValidationContext, Builder, SoapFaultDetailValidationContext.Builder> {

        private final List<SoapFaultDetailValidationContext.Builder> validationContexts = new ArrayList<>();

        @Override
        public SoapFaultDetailValidationContext.Builder detail() {
            SoapFaultDetailValidationContext.Builder detailValidationContext = new SoapFaultDetailValidationContext.Builder();
            validationContexts.add(detailValidationContext);
            return detailValidationContext;
        }

        @Override
        public Builder detail(SoapFaultDetailValidationContext.Builder detailValidationContext) {
            this.validationContexts.add(detailValidationContext);
            return this;
        }

        @Override
        public Builder details(List<SoapFaultDetailValidationContext.Builder> detailValidationContexts) {
            this.validationContexts.addAll(detailValidationContexts);
            return this;
        }

        @Override
        public Builder details(SoapFaultDetailValidationContext.Builder... detailValidationContexts) {
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
    public List<SoapFaultDetailValidationContext.Builder> getValidationContexts() {
        return validationContexts;
    }

    /**
     * Adds new validation context to the list of contexts.
     */
    public SoapFaultValidationContext addValidationContext(SoapFaultDetailValidationContext context) {
        this.validationContexts.add(new SoapFaultDetailValidationContext.Builder() {
            @Override
            public SoapFaultDetailValidationContext build() {
                return context;
            }
        });
        return this;
    }
}
