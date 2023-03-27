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

package org.citrusframework.citrus.ws.validation;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.ws.message.SoapFault;

/**
 * Interface for soap fault validation.
 *
 * @author Christoph Deppisch
 */
public interface SoapFaultValidator {

    /**
     * Validate soap fault with given control fault.
     *
     * @param receivedFault the received fault instance.
     * @param controlFault the control fault with expected fault information.
     * @param context
     * @param validationContext
     */
    void validateSoapFault(SoapFault receivedFault,
                           SoapFault controlFault,
                           TestContext context,
                           SoapFaultValidationContext validationContext) throws ValidationException;
}
