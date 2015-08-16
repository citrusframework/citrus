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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultDetailValidationContext;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 * since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.AssertSoapFaultBuilder}
 */
public class AssertSoapFaultDefinition extends AbstractActionContainerDefinition<AssertSoapFault> {

    private XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

    /**
     * Constructor using action field.
     * @param action
     */
	public AssertSoapFaultDefinition(AssertSoapFault action) {
	    super(action);

	    // for now support one single soap fault detail
	    SoapFaultDetailValidationContext soapFaultDetailValidationContext = new SoapFaultDetailValidationContext();
	    soapFaultDetailValidationContext.addValidationContext(validationContext);
	    action.setValidationContext(soapFaultDetailValidationContext);
    }

    /**
     * Default constructor
     */
    public AssertSoapFaultDefinition() {
        this(new AssertSoapFault());
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
     * Expect fault actor in SOAP fault message.
     * @param faultActor
     * @return
     */
    public AssertSoapFaultDefinition faultActor(String faultActor) {
        action.setFaultActor(faultActor);
        return this;
    }

    /**
     * Expect fault detail in SOAP fault message.
     * @param faultDetail
     * @return
     */
    public AssertSoapFaultDefinition faultDetail(String faultDetail) {
        action.getFaultDetails().add(faultDetail);
        return this;
    }

    /**
     * Expect fault detail from file resource.
     * @param resource
     * @return
     */
    public AssertSoapFaultDefinition faultDetailResource(Resource resource) {
        try {
            action.getFaultDetails().add(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read fault detail resource", e);
        }
        return this;
    }

    /**
     * Expect fault detail from file resource.
     * @param filePath
     * @return
     */
    public AssertSoapFaultDefinition faultDetailResource(String filePath) {
        action.getFaultDetailResourcePaths().add(filePath);
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
     * Set explicit SOAP fault validator implementation by bean name.
     * @param validatorName
     * @param applicationContext
     * @return
     */
    public AssertSoapFaultDefinition validator(String validatorName, ApplicationContext applicationContext) {
        action.setValidator(applicationContext.getBean(validatorName, SoapFaultValidator.class));
        return this;
    }

    /**
     * Sets schema validation enabled/disabled for this SOAP fault assertion.
     * @param enabled
     * @return
     */
    public AssertSoapFaultDefinition schemaValidation(boolean enabled) {
        validationContext.setSchemaValidation(enabled);
        return this;
    }

    /**
     * Sets explicit schema instance name to use for schema validation.
     * @param schemaName
     * @return
     */
    public AssertSoapFaultDefinition xsd(String schemaName) {
        validationContext.setSchema(schemaName);
        return this;
    }

    /**
     * Sets explicit xsd schema repository instance to use for validation.
     * @param schemaRepository
     * @return
     */
    public AssertSoapFaultDefinition xsdSchemaRepository(String schemaRepository) {
        validationContext.setSchemaRepository(schemaRepository);
        return this;
    }
}
