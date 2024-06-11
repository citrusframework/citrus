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

import static java.lang.String.format;
import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.message.MessageType.PLAINTEXT;
import static org.citrusframework.message.MessageType.XML;
import static org.citrusframework.openapi.model.OasModelHelper.getRequestContentType;
import static org.citrusframework.util.StringUtils.appendSegmentToPath;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.OpenApiTestDataGenerator;
import org.citrusframework.openapi.model.OasModelHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * @since 4.1
 */
public class OpenApiServerRequestActionBuilder extends HttpServerRequestActionBuilder {

    /**
     * Default constructor initializes http request message builder.
     */
    public OpenApiServerRequestActionBuilder(OpenApiSpecification openApiSpec, String operationId) {
        this(new HttpMessage(), openApiSpec, operationId);
    }

    public OpenApiServerRequestActionBuilder(HttpMessage httpMessage, OpenApiSpecification openApiSpec,
                                             String operationId) {
        super(new OpenApiServerRequestMessageBuilder(httpMessage, openApiSpec, operationId), httpMessage);
    }

    private static class OpenApiServerRequestMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecification openApiSpec;
        private final String operationId;

        private final HttpMessage httpMessage;

        public OpenApiServerRequestMessageBuilder(HttpMessage httpMessage, OpenApiSpecification openApiSpec,
                                                  String operationId) {
            super(httpMessage);
            this.openApiSpec = openApiSpec;
            this.operationId = operationId;
            this.httpMessage = httpMessage;
        }

        @Override
        public Message build(TestContext context, String messageType) {
            OasOperationParams oasOperationParams = getResult(context);

            if (oasOperationParams.operation() == null) {
                throw new CitrusRuntimeException("Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(operationId, openApiSpec.getSpecUrl()));
            }

            setSpecifiedMessageType(oasOperationParams);
            setSpecifiedHeaders(context, oasOperationParams);
            setSpecifiedQueryParameters(context, oasOperationParams);
            setSpecifiedPath(context, oasOperationParams);
            setSpecifiedBody(oasOperationParams);
            setSpecifiedRequestContentType(oasOperationParams);
            setSpecifiedMethod(oasOperationParams);

            return super.build(context, messageType);
        }

        private OasOperationParams getResult(TestContext context) {
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
            return new OasOperationParams(oasDocument, operation, pathItem, method);
        }

        private void setSpecifiedRequestContentType(OasOperationParams oasOperationParams) {
            OasModelHelper.getRequestContentType(oasOperationParams.operation)
                    .ifPresent(contentType -> httpMessage.setHeader(HttpHeaders.CONTENT_TYPE, String.format("@startsWith(%s)@", contentType)));
        }

        private void setSpecifiedPath(TestContext context, OasOperationParams oasOperationParams) {
            String randomizedPath = OasModelHelper.getBasePath(oasOperationParams.oasDocument) + oasOperationParams.pathItem.getPath();
            randomizedPath = randomizedPath.replace("//", "/");

            randomizedPath = appendSegmentToPath(openApiSpec.getRootContextPath(), randomizedPath);

            if (oasOperationParams.operation.parameters != null) {
                randomizedPath = determinePath(context, oasOperationParams.operation, randomizedPath);
            }

            httpMessage.path(randomizedPath);
        }

        private void setSpecifiedBody(OasOperationParams oasOperationParams) {
            Optional<OasSchema> body = OasModelHelper.getRequestBodySchema(oasOperationParams.oasDocument, oasOperationParams.operation);
            body.ifPresent(oasSchema -> httpMessage.setPayload(OpenApiTestDataGenerator.createInboundPayload(oasSchema, OasModelHelper.getSchemaDefinitions(
                oasOperationParams.oasDocument), openApiSpec)));
        }

        private String determinePath(TestContext context, OasOperation operation,
            String randomizedPath) {
            List<OasParameter> pathParams = operation.parameters.stream()
                    .filter(p -> "path".equals(p.in)).toList();

            for (OasParameter parameter : pathParams) {
                String parameterValue;
                if (context.getVariables().containsKey(parameter.getName())) {
                    parameterValue = "\\" + CitrusSettings.VARIABLE_PREFIX + parameter.getName() + CitrusSettings.VARIABLE_SUFFIX;
                    randomizedPath = Pattern.compile("\\{" + parameter.getName() + "}")
                        .matcher(randomizedPath)
                        .replaceAll(parameterValue);
                } else {
                    parameterValue = OpenApiTestDataGenerator.createValidationRegex(parameter.getName(), OasModelHelper.getParameterSchema(parameter).orElse(null));

                    randomizedPath = Pattern.compile("\\{" + parameter.getName() + "}")
                        .matcher(randomizedPath)
                        .replaceAll(parameterValue);

                    randomizedPath =  format("@matches('%s')@", randomizedPath);
                }
            }
            return randomizedPath;
        }

        private void setSpecifiedQueryParameters(TestContext context, OasOperationParams oasOperationParams) {

            if (oasOperationParams.operation.parameters == null) {
                return;
            }

            oasOperationParams.operation.parameters.stream()
                    .filter(param -> "query".equals(param.in))
                    .filter(param -> (param.required != null && param.required) || context.getVariables().containsKey(param.getName()))
                    .forEach(param -> httpMessage.queryParam(param.getName(),
                            OpenApiTestDataGenerator.createValidationExpression(param.getName(), OasModelHelper.getParameterSchema(param).orElse(null),
                                    OasModelHelper.getSchemaDefinitions(oasOperationParams.oasDocument), false, openApiSpec,
                                context)));

        }

        private void setSpecifiedHeaders(TestContext context, OasOperationParams oasOperationParams) {

            if (oasOperationParams.operation.parameters == null) {
                return;
            }

            oasOperationParams.operation.parameters.stream()
                .filter(param -> "header".equals(param.in))
                .filter(
                    param -> (param.required != null && param.required) || context.getVariables()
                        .containsKey(param.getName()))
                .forEach(param -> httpMessage.setHeader(param.getName(),
                    OpenApiTestDataGenerator.createValidationExpression(param.getName(),
                        OasModelHelper.getParameterSchema(param).orElse(null),
                        OasModelHelper.getSchemaDefinitions(oasOperationParams.oasDocument), false, openApiSpec,
                        context)));
        }

        private void setSpecifiedMessageType(OasOperationParams oasOperationParams) {
            Optional<String> requestContentType = getRequestContentType(
                oasOperationParams.operation);
            if (requestContentType.isPresent() && APPLICATION_JSON_VALUE.equals(requestContentType.get())) {
                httpMessage.setType(JSON);
            } else if (requestContentType.isPresent() && APPLICATION_XML_VALUE.equals(requestContentType.get())) {
                httpMessage.setType(XML);
            } else {
                httpMessage.setType(PLAINTEXT);
            }
        }

        private void setSpecifiedMethod(OasOperationParams oasOperationParams) {
            httpMessage.method(oasOperationParams.method);
        }

    }

    private record OasOperationParams(OasDocument oasDocument, OasOperation operation, OasPathItem pathItem, HttpMethod method) {
    }
}
