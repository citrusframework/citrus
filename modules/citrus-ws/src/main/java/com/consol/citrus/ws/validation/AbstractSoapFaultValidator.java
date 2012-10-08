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

package com.consol.citrus.ws.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;

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
    
    /**
     * @see com.consol.citrus.ws.validation.SoapFaultValidator#validateSoapFault(org.springframework.ws.soap.SoapFault, org.springframework.ws.soap.SoapFault, com.consol.citrus.context.TestContext)
     */
    public void validateSoapFault(SoapFault receivedFault, SoapFault controlFault, TestContext context)
            throws ValidationException {
        //fault string validation
        if (controlFault.getFaultStringOrReason() != null && 
                !controlFault.getFaultStringOrReason().equals(receivedFault.getFaultStringOrReason())) {
            if (controlFault.getFaultStringOrReason().equals(CitrusConstants.IGNORE_PLACEHOLDER)) {
                log.debug("SOAP fault-string is ignored by placeholder - skipped fault-string validation");
            } else if (controlFault.getFaultStringOrReason().startsWith(CitrusConstants.VALIDATION_MATCHER_PREFIX) &&
                    controlFault.getFaultStringOrReason().endsWith(CitrusConstants.VALIDATION_MATCHER_SUFFIX)) {
                ValidationMatcherUtils.resolveValidationMatcher("SOAP fault string", receivedFault.getFaultStringOrReason(), controlFault.getFaultStringOrReason(), context);
            } else {
                throw new ValidationException("SOAP fault validation failed! Fault string does not match - expected: '" + 
                        controlFault.getFaultStringOrReason() + "' but was: '" + receivedFault.getFaultStringOrReason() + "'");
            }
        }
        
        //fault code validation
        if (StringUtils.hasText(controlFault.getFaultCode().getLocalPart())) {
            Assert.isTrue(controlFault.getFaultCode().equals(receivedFault.getFaultCode()), 
                    "SOAP fault validation failed! Fault code does not match - expected: '" +
                    controlFault.getFaultCode() + "' but was: '" + receivedFault.getFaultCode() + "'");
        }
        
        //fault actor validation
        if (StringUtils.hasText(controlFault.getFaultActorOrRole())) {
            if (controlFault.getFaultActorOrRole().startsWith(CitrusConstants.VALIDATION_MATCHER_PREFIX) &&
                    controlFault.getFaultActorOrRole().endsWith(CitrusConstants.VALIDATION_MATCHER_SUFFIX)) {
                ValidationMatcherUtils.resolveValidationMatcher("SOAP fault actor", receivedFault.getFaultActorOrRole(), controlFault.getFaultActorOrRole(), context);
            } else {
                Assert.isTrue(controlFault.getFaultActorOrRole().equals(receivedFault.getFaultActorOrRole()), 
                        "SOAP fault validation failed! Fault actor does not match - expected: '" +
                        controlFault.getFaultActorOrRole() + "' but was: '" + receivedFault.getFaultActorOrRole() + "'");
            }
        }
        
        if (controlFault.getFaultDetail() != null) {
            SoapFaultDetail detail = receivedFault.getFaultDetail();
            
            if (detail == null) {
                throw new ValidationException("SOAP fault validation failed! Missing fault detail in received message.");
            }
            
            validateFaultDetail(detail, controlFault.getFaultDetail(), context);
        }
    }

    /**
     * Abstract method for soap fault detail validation.
     * 
     * @param receivedDetail
     * @param controlDetail
     * @param context
     */
    protected abstract void validateFaultDetail(SoapFaultDetail receivedDetail, SoapFaultDetail controlDetail, TestContext context);
}
