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


import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.ValidationException;

/**
 * Simple soap fault validator implementation just performing String equals on soap fault detail
 * as validation algorithm.
 * 
 * @author Christoph Deppisch
 */
public class SimpleSoapFaultValidator extends AbstractFaultDetailStringValidator {

    /**
     * @see com.consol.citrus.ws.validation.AbstractFaultDetailStringValidator#validateFaultDetailString(java.lang.String, java.lang.String)
     */
    @Override
    protected void validateFaultDetailString(String receivedDetailString, String controlDetailString) 
        throws ValidationException {
        if(!StringUtils.trimAllWhitespace(receivedDetailString).equals( 
                StringUtils.trimAllWhitespace(controlDetailString))) {
            throw new ValidationException("SOAP fault validation failed! Fault detail does not match: expected \n'" +
                    StringUtils.trimAllWhitespace(controlDetailString) + "' \n received \n'" + StringUtils.trimAllWhitespace(receivedDetailString) + "'");
        }
    }
}
