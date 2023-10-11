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

package org.citrusframework.ws.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.builder.MessageBuilderSupport;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.message.SoapFault;
import org.citrusframework.ws.validation.SimpleSoapFaultValidator;
import org.citrusframework.ws.validation.SoapFaultDetailValidationContext;
import org.citrusframework.ws.validation.SoapFaultValidationContext;
import org.citrusframework.ws.validation.SoapFaultValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * Asserting SOAP fault exception in embedded test action.
 *
 * Class constructs a control soap fault detail with given expected information (faultCode, faultString and faultDetail)
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
    private final SoapFaultValidationContext validationContext;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AssertSoapFault.class);

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

        this.validationContext = builder.validationContext.build();
    }

    @Override
    public void doExecute(TestContext context) {
        logger.debug("Asserting SOAP fault ...");

        try {
            executeAction(action, context);
        } catch (SoapFaultClientException soapFaultException) {
            logger.debug("Validating SOAP fault ...");

            SoapFault controlFault = constructControlFault(context);

            validator.validateSoapFault(SoapFault.from(soapFaultException.getSoapFault()), controlFault, context, validationContext);

            logger.debug("Asserted SOAP fault as expected: " + soapFaultException.getFaultCode() + ": " + soapFaultException.getFaultStringOrReason());
            logger.info("Assert SOAP fault validation successful");

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
     * @return the faultActor.
     */
    public String getFaultActor() {
        return faultActor;
    }

    /**
     * Gets the validationContext.
     * @return the validationContext.
     */
    public SoapFaultValidationContext getValidationContext() {
        return validationContext;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<AssertSoapFault, Builder> implements ReferenceResolverAware {

        private TestActionBuilder<?> action;

        protected Endpoint endpoint;
        protected String endpointUri;

        private String faultString;
        private String faultCode;
        private String faultActor;
        private final List<String> faultDetails = new ArrayList<>();
        private final List<String> faultDetailResourcePaths = new ArrayList<>();
        private String validatorName;
        private SoapFaultValidator validator;
        private SoapFaultValidationContext.Builder validationContext;

        private ReferenceResolver referenceResolver;

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

            if (this.action instanceof MessageBuilderSupport.MessageActionBuilder<?,?,?>) {
                if (this.endpointUri != null) {
                    ((MessageBuilderSupport.MessageActionBuilder<?, ?, ?>) this.action).endpoint(endpointUri);
                }

                if (this.endpoint != null) {
                    ((MessageBuilderSupport.MessageActionBuilder<?, ?, ?>) this.action).endpoint(endpoint);
                }
            }
            return super.actions(actions[0]);
        }

        /**
         * Sets the message endpoint to send messages to.
         * @param messageEndpoint
         * @return
         */
        public Builder endpoint(Endpoint messageEndpoint) {
            this.endpoint = messageEndpoint;
            return this;
        }

        /**
         * Sets the message endpoint uri to send messages to.
         * @param messageEndpointUri
         * @return
         */
        public Builder endpoint(String messageEndpointUri) {
            this.endpointUri = messageEndpointUri;
            return this;
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
         * @return
         */
        public Builder validator(String validatorName) {
            this.validatorName = validatorName;
            return this;
        }

        /**
         * Specifies the validationContext.
         * @param validationContext
         */
        public Builder validate(SoapFaultValidationContext.Builder validationContext) {
            this.validationContext = validationContext;
            return this;
        }

        /**
         * Specifies the validationContext.
         * @param validationContext
         */
        public Builder validateDetail(SoapFaultDetailValidationContext.Builder validationContext) {
            if (this.validationContext == null) {
                this.validationContext = new SoapFaultValidationContext.Builder();
            }

            this.validationContext.detail(validationContext.build());
            return this;
        }

        /**
         * Sets the Spring bean application context.
         * @param referenceResolver
         */
        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        @Override
        public AssertSoapFault build() {
            return doBuild();
        }

        @Override
        public AssertSoapFault doBuild() {
            if (validationContext == null) {
                this.validationContext = new SoapFaultValidationContext.Builder();

                if (!faultDetailResourcePaths.isEmpty() || !faultDetails.isEmpty()) {
                    this.validationContext.detail(new SoapFaultDetailValidationContext.Builder().build());
                }
            }

            if (referenceResolver != null) {
                if (StringUtils.hasText(validatorName)) {
                    validator(referenceResolver.resolve(validatorName, SoapFaultValidator.class));
                }

                if (validator == null && referenceResolver.isResolvable("soapFaultValidator")) {
                    validator(referenceResolver.resolve("soapFaultValidator", SoapFaultValidator.class));
                }
            }

            if (validator == null) {
                validator = new SimpleSoapFaultValidator();
            }
            return new AssertSoapFault(this);
        }
    }

}
