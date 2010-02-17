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
