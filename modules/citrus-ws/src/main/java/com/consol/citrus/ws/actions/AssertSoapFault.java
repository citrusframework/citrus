/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.actions;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.xml.namespace.QNameUtils;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

/**
 * Asserting SOAP fault exception to occur in embedded test actions.
 * 
 * In case SOAP fault is caught, tester can validate SOAP fault code and fault string to
 * match expected behavior.
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2009
 */
public class AssertSoapFault extends AbstractTestAction {
    /** TestAction to be executed */
    private TestAction action;

    /** Localized fault string */
    private String faultString = null;
    
    /** OName representing fault code */
    private String faultCode = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AssertSoapFault.class);

    /**
     * @see com.consol.citrus.TestAction#execute()
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.info("Asserting SOAP fault ...");

        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        } catch (Exception e) {
            log.info("Validating SOAP fault ...");
            if (SoapFaultClientException.class.isAssignableFrom(e.getClass())) {
                SoapFaultClientException soapFaultException = (SoapFaultClientException)e;
                
                try {
					if(faultString != null && context.replaceDynamicContentInString(faultString).equals(soapFaultException.getFaultStringOrReason()) == false) {
					    throw new ValidationException("SOAP fault validation failed! Fault string does not match - expected: '" + faultString + "' but was: '" + soapFaultException.getFaultStringOrReason() + "'");
					}
					
					if(StringUtils.hasText(faultCode)) {
						Assert.isTrue(QNameUtils.parseQNameString(context.replaceDynamicContentInString(faultCode)).equals(soapFaultException.getFaultCode()), 
								"SOAP fault validation failed! Fault code does not match - expected: '" +
								faultCode + "' but was: '" + soapFaultException.getFaultCode() + "'");
					}
				} catch (ParseException ex) {
					throw new CitrusRuntimeException("Error validating SOAP fault" + ex);
				}
                
                log.info("SOAP fault as expected: " + soapFaultException.getFaultCode() + ": " + soapFaultException.getFaultStringOrReason());
                log.info("SOAP fault validation successful");
                return;
            } else {
                throw new ValidationException("SOAP fault validation failed! Caught exception type does not fit - expected: '" + SoapFaultClientException.class + "' but was: '" + e.getClass().getName() + "'", e);
            }
        }

        throw new ValidationException("SOAP fault validation failed! Missing asserted SOAP fault exception");
    }

    /**
     * @param action the action to set
     */
    public void setAction(TestAction action) {
        this.action = action;
    }

	/**
	 * @param faultCode the faultCode to set
	 */
	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}

	/**
	 * @param faultString the faultString to set
	 */
	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}
}
