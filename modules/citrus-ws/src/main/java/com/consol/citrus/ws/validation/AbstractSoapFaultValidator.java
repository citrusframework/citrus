/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.validation;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

import com.consol.citrus.exceptions.ValidationException;

/**
 * Abstract soap fault validation implementation offering basic faultCode and faultString validation.
 * Subclasses may add fault detail validation in addition to that.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractSoapFaultValidator implements SoapFaultValidator {

    /**
     * @see com.consol.citrus.ws.validation.SoapFaultValidator#validateSoapFault(org.springframework.ws.soap.SoapFault, org.springframework.ws.soap.SoapFault, com.consol.citrus.context.TestContext)
     */
    public void validateSoapFault(SoapFault receivedFault, SoapFault controlFault)
            throws ValidationException {
        if(controlFault.getFaultStringOrReason() != null && 
                !controlFault.getFaultStringOrReason().equals(receivedFault.getFaultStringOrReason())) {
            throw new ValidationException("SOAP fault validation failed! Fault string does not match - expected: '" + 
                    controlFault.getFaultStringOrReason() + "' but was: '" + receivedFault.getFaultStringOrReason() + "'");
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
