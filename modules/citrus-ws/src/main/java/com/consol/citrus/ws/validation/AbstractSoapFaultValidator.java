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
import com.consol.citrus.exceptions.ValidationException;

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
    private static final Logger log = LoggerFactory.getLogger(AbstractSoapFaultValidator.class);
    
    /**
     * @see com.consol.citrus.ws.validation.SoapFaultValidator#validateSoapFault(org.springframework.ws.soap.SoapFault, org.springframework.ws.soap.SoapFault, com.consol.citrus.context.TestContext)
     */
    public void validateSoapFault(SoapFault receivedFault, SoapFault controlFault)
            throws ValidationException {
        if(controlFault.getFaultStringOrReason() != null && 
                !controlFault.getFaultStringOrReason().equals(receivedFault.getFaultStringOrReason())) {
            if(controlFault.getFaultStringOrReason().equals(CitrusConstants.IGNORE_PLACEHOLDER)) {
                log.debug("SOAP fault-string is ignored by placeholder - skipped fault-string validation");
            } else {
                throw new ValidationException("SOAP fault validation failed! Fault string does not match - expected: '" + 
                        controlFault.getFaultStringOrReason() + "' but was: '" + receivedFault.getFaultStringOrReason() + "'");
            }
        }
        
        if(StringUtils.hasText(controlFault.getFaultCode().getLocalPart())) {
            Assert.isTrue(controlFault.getFaultCode().equals(receivedFault.getFaultCode()), 
                    "SOAP fault validation failed! Fault code does not match - expected: '" +
                    controlFault.getFaultCode() + "' but was: '" + receivedFault.getFaultCode() + "'");
        }
        
        if(controlFault.getFaultDetail() != null) {
            SoapFaultDetail detail = receivedFault.getFaultDetail();
            
            if(detail == null) {
                throw new ValidationException("SOAP fault validation failed! Missing fault detail in received message.");
            }
            
            validateFaultDetail(detail, controlFault.getFaultDetail());
        }
    }

    /**
     * Abstract method for soap fault detail validation.
     * 
     * @param receivedDetail
     * @param controlDetail
     * @param context
     */
    protected abstract void validateFaultDetail(SoapFaultDetail receivedDetail, SoapFaultDetail controlDetail);
}
