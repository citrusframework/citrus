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

import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonMap;
import static org.citrusframework.openapi.OpenApiTestDataGenerator.createOutboundPayload;
import static org.citrusframework.openapi.OpenApiTestDataGenerator.createRandomValueExpression;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.citrusframework.CitrusSettings;
import org.citrusframework.openapi.OpenApiMessageType;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpServerResponseActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaderBuilder;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OasAdapter;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.validation.OpenApiMessageProcessor;
import org.citrusframework.openapi.validation.OpenApiValidationContext;
import org.springframework.http.HttpStatus;

/**
 * @since 4.1
 */
public class OpenApiServerResponseActionBuilder extends HttpServerResponseActionBuilder {

    private OpenApiMessageProcessor openApiMessageProcessor;

    private final OpenApiSpecificationSource openApiSpecificationSource;

    private final String operationId;

    private boolean schemaValidation = true;

    /**
     * Default constructor initializes http response message builder.
     */
    public OpenApiServerResponseActionBuilder(OpenApiSpecificationSource openApiSpecificationSource, String operationId,
        String statusCode, String accept) {
        this(new HttpMessage(), openApiSpecificationSource, operationId, statusCode, accept);
    }

    public OpenApiServerResponseActionBuilder(HttpMessage httpMessage,
        OpenApiSpecificationSource openApiSpecificationSource,
        String operationId, String statusCode, String accept) {
        super(new OpenApiServerResponseMessageBuilder(httpMessage, openApiSpecificationSource, operationId,
            statusCode, accept), httpMessage);
        this.openApiSpecificationSource = openApiSpecificationSource;
        this.operationId = operationId;
    }

    @Override
    public SendMessageAction doBuild() {
        OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(referenceResolver);

        // Honor default enablement of schema validation
        OpenApiValidationContext openApiValidationContext = openApiSpecification.getOpenApiValidationContext();
        if (openApiValidationContext != null && schemaValidation) {
            schemaValidation = openApiValidationContext.isResponseValidationEnabled();
        }

        if (schemaValidation && !messageProcessors.contains(openApiMessageProcessor)) {
            openApiMessageProcessor = new OpenApiMessageProcessor(openApiSpecification, operationId,
                OpenApiMessageType.RESPONSE);
            process(openApiMessageProcessor);
        }

        return super.doBuild();
    }

    public OpenApiServerResponseActionBuilder schemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
        return this;
    }

    /**
     * By default, enable schema validation as the OpenAPI is always available.
     */
    @Override
    protected HttpMessageBuilderSupport createMessageBuilderSupport() {
        HttpMessageBuilderSupport messageBuilderSupport = super.createMessageBuilderSupport();
        messageBuilderSupport.schemaValidation(true);
        return messageBuilderSupport;
    }

    public OpenApiServerResponseActionBuilder enableRandomGeneration(boolean enable) {
        ((OpenApiServerResponseMessageBuilder)getMessageBuilderSupport().getMessageBuilder()).enableRandomGeneration(enable);
        return this;
    }

    private static class OpenApiServerResponseMessageBuilder extends HttpMessageBuilder {

        private static final Pattern STATUS_CODE_PATTERN = Pattern.compile("\\d+");

        private final OpenApiSpecificationSource openApiSpecificationSource;
        private final String operationId;
        private final String statusCode;
        private final String accept;
        private boolean randomGenerationEnabled = true;

        public OpenApiServerResponseMessageBuilder(HttpMessage httpMessage,
            OpenApiSpecificationSource openApiSpecificationSource,
            String operationId, String statusCode, String accept) {
            super(httpMessage);
            this.openApiSpecificationSource = openApiSpecificationSource;
            this.operationId = operationId;
            this.statusCode = statusCode;
            this.accept = accept;
        }

        public OpenApiServerResponseMessageBuilder enableRandomGeneration(boolean enable) {
            this.randomGenerationEnabled = enable;
            return this;
        }

        @Override
        public Message build(TestContext context, String messageType) {

            OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(context.getReferenceResolver());
            if (STATUS_CODE_PATTERN.matcher(statusCode).matches()) {
                getMessage().status(HttpStatus.valueOf(parseInt(statusCode)));
            } else {
                getMessage().status(OK);
            }

            List<MessageHeaderBuilder> initialHeaderBuilders = new ArrayList<>(getHeaderBuilders());
            getHeaderBuilders().clear();

            if (randomGenerationEnabled) {
                openApiSpecification.getOperation(operationId, context)
                    .ifPresentOrElse(operationPathAdapter ->
                        fillRandomData(openApiSpecification, operationPathAdapter, context), () -> {
                        throw new CitrusRuntimeException(
                            "Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(
                                operationId, openApiSpecification.getSpecUrl()));
                    });
            }

            // Initial header builder need to be prepended, so that they can overwrite randomly generated headers.
            getHeaderBuilders().addAll(initialHeaderBuilders);

            return super.build(context, messageType);
        }

        private void fillRandomData(OpenApiSpecification openApiSpecification, OperationPathAdapter operationPathAdapter, TestContext context) {

            if (operationPathAdapter.operation().responses != null) {
                buildResponse(context, openApiSpecification, operationPathAdapter.operation());
            }
        }

        private void buildResponse(TestContext context, OpenApiSpecification openApiSpecification, OasOperation operation) {

            Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
                openApiSpecification.getOpenApiDoc(context), operation, statusCode, null);

            if (responseForRandomGeneration.isPresent()) {
                buildRandomHeaders(context, openApiSpecification, responseForRandomGeneration.get());
                buildRandomPayload(openApiSpecification, operation, responseForRandomGeneration.get());
            }
        }

        private void buildRandomHeaders(TestContext context, OpenApiSpecification openApiSpecification, OasResponse response) {
            Set<String> filteredHeaders = new HashSet<>(getMessage().getHeaders().keySet());
            Predicate<Entry<String, OasSchema>> filteredHeadersPredicate = entry -> !filteredHeaders.contains(
                entry.getKey());

            Map<String, OasSchema> requiredHeaders = OasModelHelper.getRequiredHeaders(
                response);
            requiredHeaders.entrySet().stream()
                .filter(filteredHeadersPredicate)
                .forEach(entry -> addHeaderBuilder(new DefaultHeaderBuilder(
                    singletonMap(entry.getKey(), createRandomValueExpression(entry.getKey(),
                        entry.getValue(),
                        openApiSpecification,
                        context))))
                );

            // Also filter the required headers, as they have already been processed
            filteredHeaders.addAll(requiredHeaders.keySet());

            Map<String, OasSchema> headers = OasModelHelper.getHeaders(response);
            headers.entrySet().stream()
                .filter(filteredHeadersPredicate)
                .filter(entry -> context.getVariables().containsKey(entry.getKey()))
                .forEach((entry -> addHeaderBuilder(
                    new DefaultHeaderBuilder(singletonMap(entry.getKey(),
                        CitrusSettings.VARIABLE_PREFIX + entry.getKey()
                            + CitrusSettings.VARIABLE_SUFFIX)))));
        }

        private void buildRandomPayload(OpenApiSpecification openApiSpecification, OasOperation operation, OasResponse response) {

            Optional<OasAdapter<OasSchema, String>> schemaForMediaTypeOptional;
            if (statusCode.startsWith("2")) {
                // if status code is good, and we have an accept, try to get the media type. Note that only json and plain text can be generated randomly.
                schemaForMediaTypeOptional = OasModelHelper.getSchema(operation,
                    response, accept != null ? List.of(accept) : null);
            }  else {
                // In the bad case, we cannot expect, that the accept type is the type which we must generate.
                // We request the type supported by the response and the random generator (json and plain text).
                schemaForMediaTypeOptional = OasModelHelper.getSchema(operation, response, null);
            }

            if (schemaForMediaTypeOptional.isPresent()) {
                OasAdapter<OasSchema, String> schemaForMediaType = schemaForMediaTypeOptional.get();
                if (getMessage().getPayload() == null || (
                    getMessage().getPayload() instanceof String string && string.isEmpty())) {
                    createRandomPayload(getMessage(), openApiSpecification, schemaForMediaType);
                }

                // If we have a schema and a media type and the content type has not yet been set, do it.
                // If schema is null, we do not set the content type, as there is no content.
                if (!getMessage().getHeaders().containsKey(HttpMessageHeaders.HTTP_CONTENT_TYPE) && schemaForMediaType.adapted() != null && schemaForMediaType.node() != null) {
                    addHeaderBuilder(new DefaultHeaderBuilder(singletonMap(HttpMessageHeaders.HTTP_CONTENT_TYPE, schemaForMediaType.adapted())));
                }
            }
        }

        private void createRandomPayload(HttpMessage message, OpenApiSpecification openApiSpecification, OasAdapter<OasSchema, String> schemaForMediaType) {

            if (schemaForMediaType.node() == null) {
                // No schema means no payload, no type
                message.setPayload(null);
            } else {
                if (TEXT_PLAIN_VALUE.equals(schemaForMediaType.adapted())) {
                    // Schema but plain text
                    message.setPayload(createOutboundPayload(schemaForMediaType.node(),
                        openApiSpecification));
                    message.setHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE, TEXT_PLAIN_VALUE);
                } else if (APPLICATION_JSON_VALUE.equals(schemaForMediaType.adapted())) {
                    // Json Schema
                    message.setPayload(createOutboundPayload(schemaForMediaType.node(),
                        openApiSpecification));
                    message.setHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE, APPLICATION_JSON_VALUE);
                }
            }
        }
    }

}
