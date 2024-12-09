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

import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.openapi.OpenApiMessageType.RESPONSE;
import static org.citrusframework.openapi.model.OasModelHelper.resolveSchema;
import static org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder.openApi;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.citrusframework.CitrusSettings;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.openapi.validation.OpenApiOperationToMessageHeadersProcessor;
import org.citrusframework.openapi.validation.OpenApiValidationContext;
import org.springframework.http.HttpStatus;

/**
 * @since 4.1
 */
public class OpenApiClientResponseActionBuilder extends HttpClientResponseActionBuilder {

    private final OpenApiSpecificationSource openApiSpecificationSource;
    private final String operationId;
    private OpenApiOperationToMessageHeadersProcessor openApiOperationToMessageHeadersProcessor;
    private boolean schemaValidation = true;

    /**
     * Default constructor initializes http response message builder.
     */
    public OpenApiClientResponseActionBuilder(OpenApiSpecificationSource openApiSpec, String operationId, String statusCode) {
        this(new HttpMessage(), openApiSpec, operationId, statusCode);
    }

    public OpenApiClientResponseActionBuilder(HttpMessage httpMessage,
                                              OpenApiSpecificationSource openApiSpecificationSource,
                                              String operationId,
                                              String statusCode) {
        this(openApiSpecificationSource, new OpenApiClientResponseMessageBuilder(httpMessage, openApiSpecificationSource, operationId, statusCode), httpMessage, operationId);
    }

    public OpenApiClientResponseActionBuilder(OpenApiSpecificationSource openApiSpec,
                                              OpenApiClientResponseMessageBuilder messageBuilder,
                                              HttpMessage message,
                                              String operationId) {
        super(messageBuilder, message);
        this.openApiSpecificationSource = openApiSpec;
        this.operationId = operationId;

        // Set json as default instead of xml. This is most common for rest.
        // TODO: we need to specify the type on message builder support level. So actually we need to
        // 1. determine the response from operationId and statusCode
        // 2. If status code is missing, take the most probable as response as determined by OasModelHelper.getResponseForRandomGeneration
        // 3. Determine message type from response and set it on builder support
        // If we do not set a proper type here, validations may not even be executed. E.g. simple json message validation.
        this.getMessageBuilderSupport().type(JSON);
    }

    public static void fillMessageTypeFromResponse(OpenApiSpecification openApiSpecification,
                                                   HttpMessage httpMessage,
                                                   @Nullable OasOperation operation,
                                                   @Nullable OasResponse response) {
        if (operation == null || response == null) {
            return;
        }

        Optional<OasSchema> responseSchema = OasModelHelper.getSchema(response);
        responseSchema.ifPresent(oasSchema -> {
                    OasSchema resolvedSchema = resolveSchema(openApiSpecification.getOpenApiDoc(null), oasSchema);
                    if (OasModelHelper.isObjectType(resolvedSchema) || OasModelHelper.isObjectArrayType(resolvedSchema)) {
                        Collection<String> responseTypes = OasModelHelper.getResponseTypes(operation,response);
                        if (responseTypes.contains(APPLICATION_JSON_VALUE)) {
                            httpMessage.setType(JSON);
                        }
                    }
                }
        );
    }

    /**
     * Overridden to change the default message type to JSON, as Json is more common in OpenAPI context.
     */
    @Override
    protected HttpMessageBuilderSupport createHttpMessageBuilderSupport() {
        HttpMessageBuilderSupport support = super.createHttpMessageBuilderSupport();
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
            schemaValidation = openApiValidationContext.isResponseValidationEnabled();
        }

        if (schemaValidation && !messageProcessors.contains(openApiOperationToMessageHeadersProcessor)) {
            openApiOperationToMessageHeadersProcessor = new OpenApiOperationToMessageHeadersProcessor(openApiSpecification, operationId, RESPONSE);
            process(openApiOperationToMessageHeadersProcessor);
        }

        if (schemaValidation && getValidationContexts().stream().noneMatch(OpenApiMessageValidationContext.class::isInstance)) {
            validate(openApi(openApiSpecification)
                    .schemaValidation(schemaValidation)
                    .build());
        }

        return super.doBuild();
    }

    public OpenApiClientResponseActionBuilder schemaValidation(boolean enabled) {
        schemaValidation = enabled;
        return this;
    }

    public static class OpenApiClientResponseMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecificationSource openApiSpecificationSource;
        private final String operationId;
        private final HttpMessage httpMessage;
        private String statusCode;

        public OpenApiClientResponseMessageBuilder(HttpMessage httpMessage,
                                                   OpenApiSpecificationSource openApiSpecificationSource,
                                                   String operationId,
                                                   String statusCode) {
            super(httpMessage);
            this.openApiSpecificationSource = openApiSpecificationSource;
            this.operationId = operationId;
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

            openApiSpecification.getOperation(operationId, context)
                    .ifPresentOrElse(operationPathAdapter ->
                            buildMessageFromOperation(openApiSpecification, operationPathAdapter, context), () -> {
                        throw new CitrusRuntimeException("Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(operationId, openApiSpecification.getSpecUrl()));
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
                        openApiSpecification.getOpenApiDoc(context), operation, statusCode, null);

                responseForRandomGeneration.ifPresent(
                        oasResponse -> fillMessageTypeFromResponse(openApiSpecification, httpMessage, operation, oasResponse));
            }

            if (Pattern.compile("\\d+").matcher(statusCode).matches()) {
                httpMessage.status(HttpStatus.valueOf(Integer.parseInt(statusCode)));
            } else {
                httpMessage.status(HttpStatus.OK);
            }

            httpMessage.getHeaders().putAll(currentHeaders);
        }
    }
}
