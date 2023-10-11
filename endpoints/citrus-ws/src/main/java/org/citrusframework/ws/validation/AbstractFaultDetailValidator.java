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

package org.citrusframework.ws.validation;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.ws.message.SoapFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link SoapFaultValidator} converting soap fault detail objects to simple String content for
 * further validation.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractFaultDetailValidator extends AbstractSoapFaultValidator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractFaultDetailValidator.class);

    @Override
    protected void validateFaultDetail(SoapFault receivedDetail, SoapFault controlDetail,
            TestContext context, final SoapFaultValidationContext validationContext) {
        if (controlDetail == null) { return; }

        if (logger.isDebugEnabled()) {
            logger.debug("Validating SOAP fault detail content ...");
        }

        if (receivedDetail == null) {
            throw new ValidationException("Missing SOAP fault detail in received message");
        }

        List<String> receivedDetailElements = receivedDetail.getFaultDetails();
        List<String> controlDetailElements = controlDetail.getFaultDetails();

        if (controlDetailElements.size() > receivedDetailElements.size()) {
            throw new ValidationException("Missing SOAP fault detail entry in received message");
        }

        for (int i = 0; i < controlDetailElements.size(); i++) {
            String receivedDetailString = receivedDetailElements.get(i);
            String controlDetailString = controlDetailElements.get(i);

            SoapFaultDetailValidationContext detailValidationContext;
            if (validationContext.getValidationContexts() == null || validationContext.getValidationContexts().isEmpty()) {
                detailValidationContext = new SoapFaultDetailValidationContext.Builder().build();
            } else {
                detailValidationContext = validationContext.getValidationContexts().get(i++);
            }

            validateFaultDetailString(XMLUtils.omitXmlDeclaration(receivedDetailString),
                                    XMLUtils.omitXmlDeclaration(controlDetailString),
                                    context, detailValidationContext);
        }
    }

    /**
     * Actual validation logic in this method.
     *
     * @param receivedDetail received soap fault representation as string.
     * @param controlDetail control soap fault representation as string.
     * @param context
     * @param validationContext
     */
    protected abstract void validateFaultDetailString(String receivedDetail, String controlDetail,
            TestContext context, SoapFaultDetailValidationContext validationContext) throws ValidationException;
}
