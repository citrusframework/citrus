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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.consol.citrus.AbstractTestContainerBuilder;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.message.SoapFault;
import com.consol.citrus.ws.validation.SimpleSoapFaultValidator;
import com.consol.citrus.ws.validation.SoapFaultDetailValidationContext;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.client.SoapFaultClientException;

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
    private final TestAction action;

    /** Localized fault string */
    private final String faultString;

    /** OName representing fault code */
    private final String faultCode;

    /** Fault actor */
    private final String faultActor;

    /** List of fault details, either inline data or file resource path */
    private final List<String> faultDetails;

    /** List of fault detail resource paths */
    private final List<String> faultDetailResourcePaths;

    /** Soap fault validator implementation */
    private final SoapFaultValidator validator;

    /** Validation context */
    private final ValidationContext validationContext;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AssertSoapFault.class);

    /**
     * Default constructor.
     */
    public AssertSoapFault(Builder builder) {
        super("soap-fault", builder);

        this.action = builder.action.build();
        this.faultString = builder.faultString;
        this.faultCode = builder.faultCode;
        this.faultActor = builder.faultActor;
        this.faultDetails = builder.faultDetails;
        this.faultDetailResourcePaths = builder.faultDetailResourcePaths;
        this.validator = builder.validator;

        this.validationContext = builder.validationContext;
    }

    @Override
    public void doExecute(TestContext context) {
        log.debug("Asserting SOAP fault ...");

        try {
            action.execute(context);
        } catch (SoapFaultClientException soapFaultException) {
            log.debug("Validating SOAP fault ...");

            SoapFault controlFault = constructControlFault(context);

            validator.validateSoapFault(SoapFault.from(soapFaultException.getSoapFault()), controlFault, context, validationContext);

            log.debug("Asserted SOAP fault as expected: " + soapFaultException.getFaultCode() + ": " + soapFaultException.getFaultStringOrReason());
            log.info("Assert SOAP fault validation successful");

            return;
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
                controlFault.addFaultDetail(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(resourcePath, context), FileUtils.getCharset(resourcePath))));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create SOAP fault detail from file resource", e);
        }

        return controlFault;
    }

    /**
     * Gets the action.
     * @return the action
     */
    public TestAction getAction() {
        return action;
    }

    @Override
    public TestAction getTestAction(int index) {
        return action;
    }

    @Override
    public List<TestAction> getActions() {
        return Collections.singletonList(action);
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
     * Gets the fault detail resource paths.
     * @return
     */
    public List<String> getFaultDetailResourcePaths() {
        return faultDetailResourcePaths;
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
     * Gets the validationContext.
     * @return the validationContext the validationContext to get.
     */
    public ValidationContext getValidationContext() {
        return validationContext;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<AssertSoapFault, Builder> {

        private TestActionBuilder<?> action;
        private String faultString;
        private String faultCode;
        private String faultActor;
        private List<String> faultDetails = new ArrayList<>();
        private List<String> faultDetailResourcePaths = new ArrayList<>();
        private SoapFaultValidator validator = new SimpleSoapFaultValidator();
        private XmlMessageValidationContext xmlValidationContext = new XmlMessageValidationContext();
        private SoapFaultDetailValidationContext validationContext = new SoapFaultDetailValidationContext()
                .addValidationContext(xmlValidationContext);

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder assertSoapFault() {
            return new Builder();
        }

        /**
         * Action producing the SOAP fault.
         * @param action
         * @return
         */
        public Builder when(TestAction action) {
            return when(() -> action);
        }

        /**
         * Action producing the SOAP fault.
         * @param action
         * @return
         */
        public Builder when(TestActionBuilder<?> action) {
            return actions(action);
        }

        @Override
        public Builder actions(TestActionBuilder<?>... actions) {
            this.action = actions[0];
            return super.actions(actions[0]);
        }
        /**
         * Expect fault code in SOAP fault message.
         * @param code
         * @return
         */
        public Builder faultCode(String code) {
            this.faultCode = code;
            return this;
        }

        /**
         * Expect fault string in SOAP fault message.
         * @param faultString
         * @return
         */
        public Builder faultString(String faultString) {
            this.faultString = faultString;
            return this;
        }

        /**
         * Expect fault actor in SOAP fault message.
         * @param faultActor
         * @return
         */
        public Builder faultActor(String faultActor) {
            this.faultActor = faultActor;
            return this;
        }

        /**
         * Expect fault detail in SOAP fault message.
         * @param faultDetail
         * @return
         */
        public Builder faultDetail(String faultDetail) {
            this.faultDetails.add(faultDetail);
            return this;
        }

        /**
         * Expect fault detail from file resource.
         * @param resource
         * @return
         */
        public Builder faultDetailResource(Resource resource) {
            return faultDetailResource(resource, FileUtils.getDefaultCharset());
        }

        /**
         * Expect fault detail from file resource.
         * @param resource
         * @param charset
         * @return
         */
        public Builder faultDetailResource(Resource resource, Charset charset) {
            try {
                this.faultDetails.add(FileUtils.readToString(resource, charset));
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
        public Builder faultDetailResource(String filePath) {
            this.faultDetailResourcePaths.add(filePath);
            return this;
        }

        /**
         * Set explicit SOAP fault validator implementation.
         * @param validator
         * @return
         */
        public Builder validator(SoapFaultValidator validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Set explicit SOAP fault validator implementation by bean name.
         * @param validatorName
         * @param applicationContext
         * @return
         */
        public Builder validator(String validatorName, ApplicationContext applicationContext) {
            this.validator = applicationContext.getBean(validatorName, SoapFaultValidator.class);
            return this;
        }

        /**
         * Sets schema validation enabled/disabled for this SOAP fault assertion.
         * @param enabled
         * @return
         */
        public Builder schemaValidation(boolean enabled) {
            xmlValidationContext.setSchemaValidation(enabled);
            return this;
        }

        /**
         * Sets explicit schema instance name to use for schema validation.
         * @param schemaName
         * @return
         */
        public Builder xsd(String schemaName) {
            xmlValidationContext.setSchema(schemaName);
            return this;
        }

        /**
         * Sets explicit xsd schema repository instance to use for validation.
         * @param schemaRepository
         * @return
         */
        public Builder xsdSchemaRepository(String schemaRepository) {
            xmlValidationContext.setSchemaRepository(schemaRepository);
            return this;
        }

        /**
         * Specifies the validationContext.
         * @param validationContext
         */
        public Builder validationContext(SoapFaultDetailValidationContext validationContext) {
            this.validationContext = validationContext;
            return this;
        }

        /**
         * Sets the Spring bean application context.
         * @param referenceResolver
         */
        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            if (referenceResolver.isResolvable("soapFaultValidator")) {
                validator(referenceResolver.resolve("soapFaultValidator", SoapFaultValidator.class));
            }

            return this;
        }

        @Override
        public AssertSoapFault build() {
            return new AssertSoapFault(this);
        }
    }

}
