/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import org.springframework.core.io.Resource;
import org.springframework.ws.soap.SoapMessageFactory;

import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultValidator;

/**
 * @author Christoph Deppisch
 * since 1.3
 */
public class AssertSoapFaultDefinition extends AbstractActionDefinition<AssertSoapFault> {

	public AssertSoapFaultDefinition(AssertSoapFault action) {
	    super(action);
    }
	
	/**
	 * Expect fault code in SOAP fault message.
	 * @param code
	 * @return
	 */
	public AssertSoapFaultDefinition faultCode(String code) {
	    action.setFaultCode(code);
	    return this;
	}
	
	/**
     * Expect fault string in SOAP fault message.
     * @param faultString
     * @return
     */
    public AssertSoapFaultDefinition faultString(String faultString) {
        action.setFaultString(faultString);
        return this;
    }
    
    /**
     * Expect fault detail in SOAP fault message.
     * @param faultDetail
     * @return
     */
    public AssertSoapFaultDefinition faultDetail(String faultDetail) {
        action.setFaultDetail(faultDetail);
        return this;
    }
    
    /**
     * Expect fault detail from file resource.
     * @param resource
     * @return
     */
    public AssertSoapFaultDefinition faultDetailResource(Resource resource) {
        action.setFaultDetailResource(resource);
        return this;
    }
    
    /**
     * Set explicit SOAP fault validator implementation.
     * @param validator
     * @return
     */
    public AssertSoapFaultDefinition validator(SoapFaultValidator validator) {
        action.setValidator(validator);
        return this;
    }
    
    /**
     * Set explicit SOAP message factory implementation.
     * @param messageFactory
     * @return
     */
    public AssertSoapFaultDefinition messageFactory(SoapMessageFactory messageFactory) {
        action.setMessageFactory(messageFactory);
        return this;
    }
}
