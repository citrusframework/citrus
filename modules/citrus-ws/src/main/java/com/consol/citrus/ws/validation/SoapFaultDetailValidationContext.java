/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.ws.validation;

import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Special validation context holds 1-n {@link XmlMessageValidationContext} instances for
 * 1-n SOAP fault detail elements.
 * 
 * @author Christoph Deppisch
 */
public class SoapFaultDetailValidationContext extends DefaultValidationContext {

    /** List of validation contexts to use for SOAP fault detail validation */
    private List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();

    /**
     * Gets the validationContexts.
     * @return the validationContexts the validationContexts to get.
     */
    public List<ValidationContext> getValidationContexts() {
        return validationContexts;
    }

    /**
     * Sets the validationContexts.
     * @param validationContexts the validationContexts to set
     */
    public void setValidationContexts(List<ValidationContext> validationContexts) {
        this.validationContexts = validationContexts;
    }

    /**
     * Adds new validation context to the list of contexts.
     * @param context
     */
    public void addValidationContext(XmlMessageValidationContext context) {
        this.validationContexts.add(context);
    }
}
