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


import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple soap fault validator implementation just performing String equals on soap fault detail
 * as validation algorithm.
 *
 * @author Christoph Deppisch
 */
public class SimpleSoapFaultValidator extends AbstractFaultDetailValidator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SimpleSoapFaultValidator.class);

    @Override
    protected void validateFaultDetailString(String received, String control,
            TestContext context, SoapFaultDetailValidationContext validationContext) throws ValidationException {

        logger.debug("Validating SOAP fault detail ...");

        String receivedDetail = received.replaceAll("\\s", "");
        String controlDetail = control.replaceAll("\\s", "");

        if (logger.isDebugEnabled()) {
            logger.debug("Received fault detail:\n" + received.strip());
            logger.debug("Control fault detail:\n" + control.strip());
        }

        if (!receivedDetail.equals(controlDetail)) {
            throw new ValidationException("SOAP fault validation failed! Fault detail does not match: expected \n'" +
                    controlDetail + "' \n received \n'" + receivedDetail + "'");
        }

        logger.info("SOAP fault detail validation successful: All values OK");
    }
}
