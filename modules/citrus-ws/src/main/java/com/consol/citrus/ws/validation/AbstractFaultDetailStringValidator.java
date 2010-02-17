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

import javax.xml.transform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.xml.transform.StringResult;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

/**
 * Abstract implementation of {@link SoapFaultValidator} converting soap fault detail objects to simple String content for
 * further validation.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractFaultDetailStringValidator extends AbstractSoapFaultValidator {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractFaultDetailStringValidator.class);
    
    /**
     * @see com.consol.citrus.ws.validation.AbstractSoapFaultValidator#validateFaultDetail(org.springframework.ws.soap.SoapFaultDetail, org.springframework.ws.soap.SoapFaultDetail, com.consol.citrus.context.TestContext)
     */
    @Override
    protected void validateFaultDetail(SoapFaultDetail receivedDetail, SoapFaultDetail controlDetail) {
        if(controlDetail == null) { return; }
        
        if(log.isDebugEnabled()) {
            log.debug("Validating SOAP fault detail content ...");
        }
        
        try {
            if(receivedDetail != null) {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                
                StringResult receivedDetailResult = new StringResult();
                StringResult controlDetailResult = new StringResult();
                
                transformer.transform(receivedDetail.getSource(), receivedDetailResult);
                transformer.transform(controlDetail.getSource(), controlDetailResult);
                
                String receivedDetailString = receivedDetailResult.toString();
                String controlDetailString = controlDetailResult.toString();
                if(log.isDebugEnabled()) {
                    log.debug("Received fault detail: " + StringUtils.trimWhitespace(receivedDetailString));
                    log.debug("Control fault detail: " + StringUtils.trimWhitespace(controlDetailString));
                }
                
                validateFaultDetailString(receivedDetailString, controlDetailString);
            } else {
                Assert.isTrue(controlDetail == null, 
                        "Values not equal for SOAP fault detail content, " +
                        "expected detail content but was '" + null + "'");
            }
        } catch (TransformerException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Actual validation logic in this method.
     * 
     * @param receivedDetailString received soap fault representation as String.
     * @param controlDetailString control soap fault representation as String.
     */
    protected abstract void validateFaultDetailString(String receivedDetailString, String controlDetailString) 
        throws ValidationException;
}
