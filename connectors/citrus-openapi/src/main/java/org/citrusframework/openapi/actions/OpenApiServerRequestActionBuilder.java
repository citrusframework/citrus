/*
 * Copyright the original author or authors.
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

package org.citrusframework.openapi.actions;

import org.citrusframework.CitrusSettings;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.openapi.validation.OpenApiOperationToMessageHeadersProcessor;
import org.citrusframework.openapi.validation.OpenApiValidationContext;

import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.openapi.OpenApiMessageType.REQUEST;
import static org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder.openApi;

/**
 * @since 4.1
 */
public class OpenApiServerRequestActionBuilder extends HttpServerRequestActionBuilder implements OpenApiSpecificationSourceAwareBuilder<ReceiveMessageAction> {

    private final OpenApiSpecificationSource openApiSpecificationSource;
    private final String operationKey;
    private OpenApiOperationToMessageHeadersProcessor openApiOperationToMessageHeadersProcessor;

    /**
     * Schema validation is enabled by default.
     */
    private boolean schemaValidation = true;

    /**
     * Default constructor initializes http request message builder.
     */
    public OpenApiServerRequestActionBuilder(OpenApiSpecificationSource openApiSpecificationSource, String operationKey) {
        this(new HttpMessage(), openApiSpecificationSource, operationKey);
    }

    public OpenApiServerRequestActionBuilder(HttpMessage httpMessage,
                                             OpenApiSpecificationSource openApiSpecificationSource,
                                             String operationKey) {
        super(new OpenApiServerRequestMessageBuilder(httpMessage, openApiSpecificationSource), httpMessage);
        this.openApiSpecificationSource = openApiSpecificationSource;
        this.operationKey = operationKey;
    }

    @Override
    public OpenApiSpecificationSource getOpenApiSpecificationSource() {
        return openApiSpecificationSource;
    }


    @Override
    protected void reconcileValidationContexts() {
        super.reconcileValidationContexts();
        OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(referenceResolver);
        if (getValidationContexts().stream()
            .noneMatch(OpenApiMessageValidationContext.class::isInstance)) {
            validate(openApi(openApiSpecification)
                .schemaValidation(schemaValidation)
                .build());
        }
    }

    /**
     * Overridden to change the default message type to JSON, as Json is more common in OpenAPI context.
     */
    @Override
    protected HttpMessageBuilderSupport createMessageBuilderSupport() {
        HttpMessageBuilderSupport support = super.createMessageBuilderSupport();
        support.type(CitrusSettings.getPropertyEnvOrDefault(
            CitrusSettings.DEFAULT_MESSAGE_TYPE_PROPERTY,
            CitrusSettings.DEFAULT_MESSAGE_TYPE_ENV,
            JSON.toString()));
        return support;
    }

    @Override
    public ReceiveMessageAction doBuild() {
        OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(referenceResolver);

        // Honor default enablement of schema validation
        OpenApiValidationContext openApiValidationContext = openApiSpecification.getOpenApiValidationContext();
        if (openApiValidationContext != null && schemaValidation) {
            schemaValidation = openApiValidationContext.isRequestValidationEnabled();
        }

        if (!messageProcessors.contains(openApiOperationToMessageHeadersProcessor)) {
            openApiOperationToMessageHeadersProcessor = new OpenApiOperationToMessageHeadersProcessor(openApiSpecification, operationKey, REQUEST);
            process(openApiOperationToMessageHeadersProcessor);
        }

        return super.doBuild();
    }

    public OpenApiServerRequestActionBuilder schemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
        return this;
    }

    private static class OpenApiServerRequestMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecificationSource openApiSpecificationSource;

        public OpenApiServerRequestMessageBuilder(HttpMessage httpMessage,
            OpenApiSpecificationSource openApiSpecificationSource) {
            super(httpMessage);

            this.openApiSpecificationSource = openApiSpecificationSource;
        }

        @Override
        public Message build(TestContext context, String messageType) {
            OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(context.getReferenceResolver());

            context.setVariable(openApiSpecification.getUid(), openApiSpecification);
            return super.build(context, messageType);
        }
    }
}
