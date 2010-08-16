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

import javax.xml.transform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                throw new ValidationException("Missing SOAP fault detail in received message");
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
