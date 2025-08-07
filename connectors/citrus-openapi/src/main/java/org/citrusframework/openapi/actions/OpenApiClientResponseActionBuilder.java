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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasResponse;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.OpenApiSupport;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.openapi.validation.OpenApiOperationToMessageHeadersProcessor;
import org.citrusframework.openapi.validation.OpenApiValidationContext;

import static org.citrusframework.openapi.OpenApiMessageType.RESPONSE;
import static org.citrusframework.openapi.util.OpenApiUtils.fillMessageTypeFromResponse;
import static org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder.openApi;

/**
 * @since 4.1
 */
public class OpenApiClientResponseActionBuilder extends HttpClientResponseActionBuilder
        implements OpenApiSpecificationSourceAwareBuilder<ReceiveMessageAction>, org.citrusframework.actions.openapi.OpenApiClientResponseActionBuilder<ReceiveMessageAction, HttpClientResponseActionBuilder.HttpMessageBuilderSupport, HttpClientResponseActionBuilder> {

    private final OpenApiSpecificationSource openApiSpecificationSource;
    private final String operationKey;
    private OpenApiOperationToMessageHeadersProcessor openApiOperationToMessageHeadersProcessor;
    private boolean schemaValidation = true;

    /**
     * Default constructor initializes http response message builder.
     */
    public OpenApiClientResponseActionBuilder(OpenApiSpecificationSource openApiSpec, String operationKey, String statusCode) {
        this(new HttpMessage(), openApiSpec, operationKey, statusCode);
    }

    public OpenApiClientResponseActionBuilder(HttpMessage httpMessage,
                                              OpenApiSpecificationSource openApiSpecificationSource,
                                              String operationKey,
                                              String statusCode) {
        this(openApiSpecificationSource, new OpenApiClientResponseMessageBuilder(httpMessage, openApiSpecificationSource,
            operationKey, statusCode), httpMessage, operationKey);
    }

    public OpenApiClientResponseActionBuilder(OpenApiSpecificationSource openApiSpec,
                                              OpenApiClientResponseMessageBuilder messageBuilder,
                                              HttpMessage message,
                                              String operationKey) {
        super(messageBuilder, message);
        this.openApiSpecificationSource = openApiSpec;
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
        if (getValidationContexts().stream().noneMatch(OpenApiMessageValidationContext.class::isInstance)) {
            validate(openApi(openApiSpecification)
                .schemaValidation(schemaValidation)
                .build());
        }
    }

    @Override
    public ReceiveMessageAction doBuild() {
        OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(referenceResolver);

        // Honor default enablement of schema validation
        OpenApiValidationContext openApiValidationContext = openApiSpecification.getOpenApiValidationContext();
        if (openApiValidationContext != null && schemaValidation) {
            schemaValidation = openApiValidationContext.isResponseValidationEnabled();
        }

        if (!messageProcessors.contains(openApiOperationToMessageHeadersProcessor)) {
            openApiOperationToMessageHeadersProcessor = new OpenApiOperationToMessageHeadersProcessor(openApiSpecification,
                operationKey, RESPONSE);
            process(openApiOperationToMessageHeadersProcessor);
        }

        return super.doBuild();
    }

    @Override
    public OpenApiClientResponseActionBuilder schemaValidation(boolean enabled) {
        schemaValidation = enabled;
        return this;
    }

    public static class OpenApiClientResponseMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecificationSource openApiSpecificationSource;
        private final String operationKey;
        private final HttpMessage httpMessage;
        private String statusCode;

        public OpenApiClientResponseMessageBuilder(HttpMessage httpMessage,
                                                   OpenApiSpecificationSource openApiSpecificationSource,
                                                   String operationKey,
                                                   String statusCode) {
            super(httpMessage);
            this.openApiSpecificationSource = openApiSpecificationSource;
            this.operationKey = operationKey;
            this.statusCode = statusCode;
            this.httpMessage = httpMessage;
        }

        public OpenApiClientResponseMessageBuilder statusCode(String statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @Override
        public Message build(TestContext context, String messageType) {
            OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(context.getReferenceResolver());

            openApiSpecification.getOperation(operationKey, context)
                    .ifPresentOrElse(operationPathAdapter ->
                            buildMessageFromOperation(openApiSpecification, operationPathAdapter, context), () -> {
                        throw new CitrusRuntimeException("Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(operationKey, openApiSpecification.getSpecUrl()));
                    });

            return super.build(context, messageType);
        }

        private void buildMessageFromOperation(OpenApiSpecification openApiSpecification, OperationPathAdapter operationPathAdapter, TestContext context) {
            OasOperation operation = operationPathAdapter.operation();

            // Headers already present in httpMessage should not be overwritten by open api.
            // E.g. if a reasonPhrase was explicitly set in the builder, it must not be overwritten.
            Map<String, Object> currentHeaders = new HashMap<>(httpMessage.getHeaders());

            if (operation.responses != null) {
                Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
                        openApiSpecification.getOpenApiDoc(context), operation,
                        String.valueOf(OpenApiSupport.getStatusCode(statusCode, context).value()), null);

                responseForRandomGeneration.ifPresent(
                        oasResponse -> fillMessageTypeFromResponse(openApiSpecification, httpMessage, operation, oasResponse));
            }

            httpMessage.status(OpenApiSupport.getStatusCode(statusCode, context));

            httpMessage.getHeaders().putAll(currentHeaders);
        }
    }
}
