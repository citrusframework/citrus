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

import org.citrusframework.citrus.CitrusSettings;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.validation.matcher.ValidationMatcherUtils;
import org.citrusframework.citrus.ws.message.SoapFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract soap fault validation implementation offering basic faultCode and faultString validation.
 * Subclasses may add fault detail validation in addition to that.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractSoapFaultValidator implements SoapFaultValidator {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AbstractSoapFaultValidator.class);

    @Override
    public void validateSoapFault(SoapFault receivedFault, SoapFault controlFault,
            TestContext context, SoapFaultValidationContext validationContext) throws ValidationException {
        //fault string validation
        if (controlFault.getFaultString() != null &&
                !controlFault.getFaultString().equals(receivedFault.getFaultString())) {
            if (controlFault.getFaultString().equals(CitrusSettings.IGNORE_PLACEHOLDER)) {
                log.debug("SOAP fault-string is ignored by placeholder - skipped fault-string validation");
            } else if (ValidationMatcherUtils.isValidationMatcherExpression(controlFault.getFaultString())) {
                ValidationMatcherUtils.resolveValidationMatcher("SOAP fault string", receivedFault.getFaultString(), controlFault.getFaultString(), context);
            } else {
                throw new ValidationException("SOAP fault validation failed! Fault string does not match - expected: '" +
                        controlFault.getFaultString() + "' but was: '" + receivedFault.getFaultString() + "'");
            }
        }

        //fault code validation
        if (StringUtils.hasText(controlFault.getFaultCodeQName().getLocalPart())) {
            Assert.isTrue(controlFault.getFaultCodeQName().equals(receivedFault.getFaultCodeQName()),
                    "SOAP fault validation failed! Fault code does not match - expected: '" +
                    controlFault.getFaultCodeQName() + "' but was: '" + receivedFault.getFaultCodeQName() + "'");
        }

        //fault actor validation
        if (StringUtils.hasText(controlFault.getFaultActor())) {
            if (controlFault.getFaultActor().startsWith(CitrusSettings.VALIDATION_MATCHER_PREFIX) &&
                    controlFault.getFaultActor().endsWith(CitrusSettings.VALIDATION_MATCHER_SUFFIX)) {
                ValidationMatcherUtils.resolveValidationMatcher("SOAP fault actor", receivedFault.getFaultActor(), controlFault.getFaultActor(), context);
            } else {
                Assert.isTrue(controlFault.getFaultActor().equals(receivedFault.getFaultActor()),
                        "SOAP fault validation failed! Fault actor does not match - expected: '" +
                        controlFault.getFaultActor() + "' but was: '" + receivedFault.getFaultActor() + "'");
            }
        }

        if (!CollectionUtils.isEmpty(controlFault.getFaultDetails())) {
            validateFaultDetail(receivedFault, controlFault, context, validationContext);
        }
    }

    /**
     * Abstract method for soap fault detail validation.
     *
     * @param receivedDetail
     * @param controlDetail
     * @param context
     * @param validationContext
     */
    protected abstract void validateFaultDetail(SoapFault receivedDetail, SoapFault controlDetail,
            TestContext context, SoapFaultValidationContext validationContext);
}
