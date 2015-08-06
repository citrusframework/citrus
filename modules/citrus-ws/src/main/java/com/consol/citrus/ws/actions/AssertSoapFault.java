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

package com.consol.citrus.ws.actions;

import com.consol.citrus.TestAction;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.ws.message.SoapFault;
import com.consol.citrus.ws.validation.SimpleSoapFaultValidator;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.client.SoapFaultClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Asserting SOAP fault exception in embedded test action.
 * 
 * Class constructs a control soap fault detail with given expeceted information (faultCode, faultString and faultDetail)
 * and delegates validation to {@link SoapFaultValidator} instance.
 * 
 * @author Christoph Deppisch 
 * @since 2009
 */
public class AssertSoapFault extends AbstractActionContainer {
    /** TestAction to be executed */
    private TestAction action;

    /** Localized fault string */
    private String faultString = null;
    
    /** OName representing fault code */
    private String faultCode = null;
    
    /** Fault actor */
    private String faultActor = null;
    
    /** List of fault details, either inline data or file resource path */
    private List<String> faultDetails = new ArrayList<String>();

    /** List of fault detail resource paths */
    private List<String> faultDetailResourcePaths = new ArrayList<String>();
    
    /** Soap fault validator implementation */
    private SoapFaultValidator validator = new SimpleSoapFaultValidator();
    
    /** Validation context */
    private ValidationContext validationContext;
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AssertSoapFault.class);

    /**
     * Default constructor.
     */
    public AssertSoapFault() {
        setName("soap-fault");
    }

    @Override
    public void doExecute(TestContext context) {
        log.info("Asserting SOAP fault ...");

        try {
            action.execute(context);
        } catch (SoapFaultClientException soapFaultException) {
            log.info("Validating SOAP fault ...");
            
            SoapFault controlFault = constructControlFault(context);
            
            validator.validateSoapFault(SoapFault.from(soapFaultException.getSoapFault()), controlFault, context, validationContext);
            
            log.info("SOAP fault as expected: " + soapFaultException.getFaultCode() + ": " + soapFaultException.getFaultStringOrReason());
            log.info("SOAP fault validation successful");
            
            return;
        } catch (RuntimeException e) {
            throw new ValidationException("SOAP fault validation failed for asserted exception type - expected: '" + 
                    SoapFaultClientException.class + "' but was: '" + e.getClass().getName() + "'", e);
        } catch (Exception e) {
            throw new ValidationException("SOAP fault validation failed for asserted exception type - expected: '" + 
                    SoapFaultClientException.class + "' but was: '" + e.getClass().getName() + "'", e);
        }
        
        throw new ValidationException("SOAP fault validation failed! Missing asserted SOAP fault exception");
    }

    /**
     * Constructs the control soap fault holding all expected fault information
     * like faultCode, faultString and faultDetail.
     * 
     * @return the constructed SoapFault instance.
     */
    private SoapFault constructControlFault(TestContext context) {
        SoapFault controlFault= new SoapFault();

        if (StringUtils.hasText(faultActor)) {
            controlFault.faultActor(context.replaceDynamicContentInString(faultActor));
        }

        controlFault.faultCode(context.replaceDynamicContentInString(faultCode));
        controlFault.faultString(context.replaceDynamicContentInString(faultString));

        for (String faultDetail : faultDetails) {
            controlFault.addFaultDetail(context.replaceDynamicContentInString(faultDetail));
        }

        try {
            for (String faultDetailPath : faultDetailResourcePaths) {
                String resourcePath = context.replaceDynamicContentInString(faultDetailPath);
                controlFault.addFaultDetail(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(resourcePath, context))));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create SOAP fault detail from file resource", e);
        }

        return controlFault;
    }

    /**
     * Set the nested test action.
     * @param action the action to set
     */
    public AssertSoapFault setAction(TestAction action) {
        addTestAction(action);
        return this;
    }

	/**
	 * Set the fault code.
	 * @param faultCode the faultCode to set
	 */
	public AssertSoapFault setFaultCode(String faultCode) {
		this.faultCode = faultCode;
        return this;
	}

	/**
	 * Set the fault string.
	 * @param faultString the faultString to set
	 */
	public AssertSoapFault setFaultString(String faultString) {
		this.faultString = faultString;
        return this;
	}

    /**
     * @param validator the validator to set
     */
    public AssertSoapFault setValidator(SoapFaultValidator validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public AssertSoapFault addTestAction(TestAction action) {
        this.action = action;
        super.addTestAction(action);
        return this;
    }

    @Override
    public TestAction getTestAction(int index) {
        if (index == 0) {
            return action;
        } else {
            throw new IndexOutOfBoundsException("Illegal index in action list:" + index);
        }
    }

    @Override
    public AssertSoapFault setActions(List<TestAction> actions) {
        if (actions.size() > 1) {
            throw new CitrusRuntimeException("Invalid number of nested test actions - only one single nested action is allowed");
        }

        action = actions.get(0);
        super.setActions(actions);
        return this;
    }

    /**
     * Gets the action.
     * @return the action
     */
    public TestAction getAction() {
        return action;
    }

    /**
     * Gets the faultString.
     * @return the faultString
     */
    public String getFaultString() {
        return faultString;
    }

    /**
     * Gets the faultCode.
     * @return the faultCode
     */
    public String getFaultCode() {
        return faultCode;
    }

    /**
     * Gets the list of fault details.
     * @return the faultDetails
     */
    public List<String> getFaultDetails() {
        return faultDetails;
    }
    
    /**
     * Sets the faultDetails.
     * @param faultDetails the faultDetails to set
     */
    public AssertSoapFault setFaultDetails(List<String> faultDetails) {
        this.faultDetails = faultDetails;
        return this;
    }

    /**
     * Gets the fault detail resource paths.
     * @return
     */
    public List<String> getFaultDetailResourcePaths() {
        return faultDetailResourcePaths;
    }

    /**
     * Sets the fault detail resource paths.
     * @param faultDetailResourcePaths
     */
    public AssertSoapFault setFaultDetailResourcePaths(List<String> faultDetailResourcePaths) {
        this.faultDetailResourcePaths = faultDetailResourcePaths;
        return this;
    }

    /**
     * Gets the validator.
     * @return the validator
     */
    public SoapFaultValidator getValidator() {
        return validator;
    }

    /**
     * Gets the faultActor.
     * @return the faultActor the faultActor to get.
     */
    public String getFaultActor() {
        return faultActor;
    }

    /**
     * Sets the faultActor.
     * @param faultActor the faultActor to set
     */
    public AssertSoapFault setFaultActor(String faultActor) {
        this.faultActor = faultActor;
        return this;
    }

    /**
     * Gets the validationContext.
     * @return the validationContext the validationContext to get.
     */
    public ValidationContext getValidationContext() {
        return validationContext;
    }

    /**
     * Sets the validationContext.
     * @param validationContext the validationContext to set
     */
    public AssertSoapFault setValidationContext(ValidationContext validationContext) {
        this.validationContext = validationContext;
        return this;
    }
    
}
