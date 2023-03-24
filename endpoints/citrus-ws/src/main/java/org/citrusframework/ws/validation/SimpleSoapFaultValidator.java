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
import org.springframework.util.StringUtils;

/**
 * Simple soap fault validator implementation just performing String equals on soap fault detail
 * as validation algorithm.
 *
 * @author Christoph Deppisch
 */
public class SimpleSoapFaultValidator extends AbstractFaultDetailValidator {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SimpleSoapFaultValidator.class);

    @Override
    protected void validateFaultDetailString(String received, String control,
            TestContext context, SoapFaultDetailValidationContext validationContext) throws ValidationException {

        log.debug("Validating SOAP fault detail ...");

        String receivedDetail = StringUtils.trimAllWhitespace(received);
        String controlDetail = StringUtils.trimAllWhitespace(control);

        if (log.isDebugEnabled()) {
            log.debug("Received fault detail:\n" + StringUtils.trimWhitespace(received));
            log.debug("Control fault detail:\n" + StringUtils.trimWhitespace(control));
        }

        if (!receivedDetail.equals(controlDetail)) {
            throw new ValidationException("SOAP fault validation failed! Fault detail does not match: expected \n'" +
                    controlDetail + "' \n received \n'" + receivedDetail + "'");
        }

        log.info("SOAP fault detail validation successful: All values OK");
    }
}
