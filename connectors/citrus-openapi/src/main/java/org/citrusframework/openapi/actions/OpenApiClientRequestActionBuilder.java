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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.OpenApiTestDataGenerator;
import org.citrusframework.openapi.model.OasModelHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * @author Christoph Deppisch
 * @since 4.1
 */
public class OpenApiClientRequestActionBuilder extends HttpClientRequestActionBuilder {

    /**
     * Default constructor initializes http request message builder.
     */
    public OpenApiClientRequestActionBuilder(OpenApiSpecification openApiSpec, String operationId) {
        this(new HttpMessage(), openApiSpec, operationId);
    }

    public OpenApiClientRequestActionBuilder(HttpMessage httpMessage, OpenApiSpecification openApiSpec,
                                             String operationId) {
        super(new OpenApiClientRequestMessageBuilder(httpMessage, openApiSpec, operationId), httpMessage);
    }

    private static class OpenApiClientRequestMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecification openApiSpec;
        private final String operationId;

        private final HttpMessage httpMessage;

        public OpenApiClientRequestMessageBuilder(HttpMessage httpMessage, OpenApiSpecification openApiSpec,
                                                  String operationId) {
            super(httpMessage);
            this.openApiSpec = openApiSpec;
            this.operationId = operationId;
            this.httpMessage = httpMessage;
        }

        @Override
        public Message build(TestContext context, String messageType) {
            OasDocument oasDocument = openApiSpec.getOpenApiDoc(context);
            OasOperation operation = null;
            OasPathItem pathItem = null;
            HttpMethod method = null;

            for (OasPathItem path : OasModelHelper.getPathItems(oasDocument.paths)) {
                Optional<Map.Entry<String, OasOperation>> operationEntry = OasModelHelper.getOperationMap(path).entrySet().stream()
                        .filter(op -> operationId.equals(op.getValue().operationId))
                        .findFirst();

                if (operationEntry.isPresent()) {
                    method = HttpMethod.valueOf(operationEntry.get().getKey().toUpperCase(Locale.US));
                    operation = operationEntry.get().getValue();
                    pathItem = path;
                    break;
                }
            }

            if (operation == null) {
                throw new CitrusRuntimeException("Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(operationId, openApiSpec.getSpecUrl()));
            }

            if (operation.parameters != null) {
                operation.parameters.stream()
                        .filter(param -> "header".equals(param.in))
                        .filter(param -> (param.required != null && param.required) || context.getVariables().containsKey(param.getName()))
                        .forEach(param -> httpMessage.setHeader(param.getName(),
                                OpenApiTestDataGenerator.createRandomValueExpression(param.getName(), (OasSchema) param.schema,
                                        OasModelHelper.getSchemaDefinitions(oasDocument), false, openApiSpec, context)));

                operation.parameters.stream()
                        .filter(param -> "query".equals(param.in))
                        .filter(param -> (param.required != null && param.required) || context.getVariables().containsKey(param.getName()))
                        .forEach(param -> httpMessage.queryParam(param.getName(),
                                OpenApiTestDataGenerator.createRandomValueExpression(param.getName(), (OasSchema) param.schema, context)));
            }

            Optional<OasSchema> body = OasModelHelper.getRequestBodySchema(oasDocument, operation);
            body.ifPresent(oasSchema -> httpMessage.setPayload(OpenApiTestDataGenerator.createOutboundPayload(oasSchema,
                    OasModelHelper.getSchemaDefinitions(oasDocument), openApiSpec)));

            String randomizedPath = pathItem.getPath();
            if (operation.parameters != null) {
                List<OasParameter> pathParams = operation.parameters.stream()
                        .filter(p -> "path".equals(p.in)).toList();

                for (OasParameter parameter : pathParams) {
                    String parameterValue;
                    if (context.getVariables().containsKey(parameter.getName())) {
                        parameterValue = "\\" + CitrusSettings.VARIABLE_PREFIX + parameter.getName() + CitrusSettings.VARIABLE_SUFFIX;
                    } else {
                        parameterValue = OpenApiTestDataGenerator.createRandomValueExpression((OasSchema) parameter.schema);
                    }
                    randomizedPath = Pattern.compile("\\{" + parameter.getName() + "}")
                            .matcher(randomizedPath)
                            .replaceAll(parameterValue);
                }
            }

            OasModelHelper.getRequestContentType(operation)
                    .ifPresent(contentType -> httpMessage.setHeader(HttpHeaders.CONTENT_TYPE, contentType));

            httpMessage.path(randomizedPath);
            httpMessage.method(method);

            return super.build(context, messageType);
        }
    }
}
