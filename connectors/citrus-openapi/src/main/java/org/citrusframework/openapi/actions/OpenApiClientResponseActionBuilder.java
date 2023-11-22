/*
 * Copyright 2006-2015 the original author or authors.
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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.OpenApiTestDataGenerator;
import org.citrusframework.openapi.model.OasModelHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 * @since 4.1
 */
public class OpenApiClientResponseActionBuilder extends HttpClientResponseActionBuilder {

    /**
     * Default constructor initializes http response message builder.
     */
    public OpenApiClientResponseActionBuilder(OpenApiSpecification openApiSpec, String operationId, String statusCode) {
        this(new HttpMessage(), openApiSpec, operationId, statusCode);
    }

    public OpenApiClientResponseActionBuilder(HttpMessage httpMessage, OpenApiSpecification openApiSpec,
                                              String operationId, String statusCode) {
        super(new OpenApiClientResponseMessageBuilder(httpMessage, openApiSpec, operationId, statusCode), httpMessage);
    }

    private static class OpenApiClientResponseMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecification openApiSpec;
        private final String operationId;
        private final String statusCode;

        private final HttpMessage httpMessage;

        public OpenApiClientResponseMessageBuilder(HttpMessage httpMessage, OpenApiSpecification openApiSpec,
                                                   String operationId, String statusCode) {
            super(httpMessage);
            this.openApiSpec = openApiSpec;
            this.operationId = operationId;
            this.statusCode = statusCode;
            this.httpMessage = httpMessage;
        }

        @Override
        public Message build(TestContext context, String messageType) {
            OasOperation operation = null;
            OasDocument oasDocument = openApiSpec.getOpenApiDoc(context);

            for (OasPathItem path : OasModelHelper.getPathItems(oasDocument.paths)) {
                Optional<Map.Entry<String, OasOperation>> operationEntry = OasModelHelper.getOperationMap(path).entrySet().stream()
                        .filter(op -> operationId.equals(op.getValue().operationId))
                        .findFirst();

                if (operationEntry.isPresent()) {
                    operation = operationEntry.get().getValue();
                    break;
                }
            }

            if (operation == null) {
                throw new CitrusRuntimeException("Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(operationId, openApiSpec.getSpecUrl()));
            }

            if (operation.responses != null) {
                OasResponse response = Optional.ofNullable(operation.responses.getItem(statusCode))
                        .orElse(operation.responses.default_);

                if (response != null) {
                    Map<String, OasSchema> requiredHeaders = OasModelHelper.getRequiredHeaders(response);
                    for (Map.Entry<String, OasSchema> header : requiredHeaders.entrySet()) {
                        httpMessage.setHeader(header.getKey(), OpenApiTestDataGenerator.createValidationExpression(header.getKey(), header.getValue(),
                                OasModelHelper.getSchemaDefinitions(oasDocument), false, openApiSpec, context));
                    }

                    Map<String, OasSchema> headers = OasModelHelper.getHeaders(response);
                    for (Map.Entry<String, OasSchema> header : headers.entrySet()) {
                        if (!requiredHeaders.containsKey(header.getKey()) && context.getVariables().containsKey(header.getKey())) {
                            httpMessage.setHeader(header.getKey(), CitrusSettings.VARIABLE_PREFIX + header.getKey() + CitrusSettings.VARIABLE_SUFFIX);
                        }
                    }

                    Optional<OasSchema> responseSchema = OasModelHelper.getSchema(response);
                    responseSchema.ifPresent(oasSchema -> httpMessage.setPayload(OpenApiTestDataGenerator.createInboundPayload(oasSchema, OasModelHelper.getSchemaDefinitions(oasDocument), openApiSpec)));
                }
            }

            OasModelHelper.getResponseContentType(oasDocument, operation)
                    .ifPresent(contentType -> httpMessage.setHeader(HttpHeaders.CONTENT_TYPE, contentType));

            if (Pattern.compile("[0-9]+").matcher(statusCode).matches()) {
                httpMessage.status(HttpStatus.valueOf(Integer.parseInt(statusCode)));
            } else {
                httpMessage.status(HttpStatus.OK);
            }

            return super.build(context, messageType);
        }
    }

}
