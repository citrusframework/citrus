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
import static org.citrusframework.util.StringUtils.appendSegmentToUrlPath;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.List;
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
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.validation.OpenApiRequestValidationProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * @since 4.1
 */
public class OpenApiServerRequestActionBuilder extends HttpServerRequestActionBuilder {

    private final OpenApiRequestValidationProcessor openApiRequestValidationProcessor;

    /**
     * Default constructor initializes http request message builder.
     */
    public OpenApiServerRequestActionBuilder(OpenApiSpecification openApiSpec, String operationId) {
        this(new HttpMessage(), openApiSpec, operationId);
    }

    public OpenApiServerRequestActionBuilder(HttpMessage httpMessage,
        OpenApiSpecification openApiSpec,
        String operationId) {
        super(new OpenApiServerRequestMessageBuilder(httpMessage, openApiSpec, operationId),
            httpMessage);

        openApiRequestValidationProcessor = new OpenApiRequestValidationProcessor(openApiSpec, operationId);
        validate(openApiRequestValidationProcessor);
    }

    public OpenApiServerRequestActionBuilder disableOasValidation(boolean b) {
        if (openApiRequestValidationProcessor != null) {
            openApiRequestValidationProcessor.setEnabled(!b);
        }
        return this;
    }

    private static class OpenApiServerRequestMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecification openApiSpec;
        private final String operationId;

        private final HttpMessage httpMessage;

        public OpenApiServerRequestMessageBuilder(HttpMessage httpMessage,
            OpenApiSpecification openApiSpec,
            String operationId) {
            super(httpMessage);
            this.openApiSpec = openApiSpec;
            this.operationId = operationId;
            this.httpMessage = httpMessage;
        }

        @Override
        public Message build(TestContext context, String messageType) {

            openApiSpec.getOperation(operationId, context).ifPresentOrElse(operationPathAdapter ->
                buildMessageFromOperation(operationPathAdapter, context), () -> {
                throw new CitrusRuntimeException("Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(operationId, openApiSpec.getSpecUrl()));
            });

            return super.build(context, messageType);
        }

        private void buildMessageFromOperation(OperationPathAdapter operationPathAdapter, TestContext context) {

            setSpecifiedMessageType(operationPathAdapter);
            setSpecifiedHeaders(context, operationPathAdapter);
            setSpecifiedQueryParameters(context, operationPathAdapter);
            setSpecifiedPath(context, operationPathAdapter);
            setSpecifiedBody(context, operationPathAdapter);
            setSpecifiedRequestContentType(operationPathAdapter);
            setSpecifiedMethod(operationPathAdapter);

        }

        private void setSpecifiedRequestContentType(OperationPathAdapter operationPathAdapter) {
            OasModelHelper.getRequestContentType(operationPathAdapter.operation())
                .ifPresent(contentType -> httpMessage.setHeader(HttpHeaders.CONTENT_TYPE,
                    String.format("@startsWith(%s)@", contentType)));
        }

        private void setSpecifiedPath(TestContext context, OperationPathAdapter operationPathAdapter) {
            String randomizedPath = OasModelHelper.getBasePath(openApiSpec.getOpenApiDoc(context))
                + operationPathAdapter.apiPath();
            randomizedPath = randomizedPath.replace("//", "/");

            randomizedPath = appendSegmentToUrlPath(openApiSpec.getRootContextPath(), randomizedPath);

            if (operationPathAdapter.operation().parameters != null) {
                randomizedPath = determinePath(context, operationPathAdapter.operation(),
                    randomizedPath);
            }

            httpMessage.path(randomizedPath);
        }

        private void setSpecifiedBody(TestContext context, OperationPathAdapter operationPathAdapter) {
            Optional<OasSchema> body = OasModelHelper.getRequestBodySchema(
                openApiSpec.getOpenApiDoc(context), operationPathAdapter.operation());
            body.ifPresent(oasSchema -> httpMessage.setPayload(
                OpenApiTestDataGenerator.createInboundPayload(oasSchema,
                    OasModelHelper.getSchemaDefinitions(
                        openApiSpec.getOpenApiDoc(context)), openApiSpec)));
        }

        private String determinePath(TestContext context, OasOperation operation,
            String randomizedPath) {
            List<OasParameter> pathParams = operation.parameters.stream()
                .filter(p -> "path".equals(p.in)).toList();

            for (OasParameter parameter : pathParams) {
                String parameterValue;
                if (context.getVariables().containsKey(parameter.getName())) {
                    parameterValue = "\\" + CitrusSettings.VARIABLE_PREFIX + parameter.getName()
                        + CitrusSettings.VARIABLE_SUFFIX;
                    randomizedPath = Pattern.compile("\\{" + parameter.getName() + "}")
                        .matcher(randomizedPath)
                        .replaceAll(parameterValue);
                } else {
                    parameterValue = OpenApiTestDataGenerator.createValidationRegex(
                        parameter.getName(),
                        OasModelHelper.getParameterSchema(parameter).orElse(null));

                    randomizedPath = Pattern.compile("\\{" + parameter.getName() + "}")
                        .matcher(randomizedPath)
                        .replaceAll(parameterValue);

                    randomizedPath = format("@matches('%s')@", randomizedPath);
                }
            }
            return randomizedPath;
        }

        private void setSpecifiedQueryParameters(TestContext context,
            OperationPathAdapter operationPathAdapter) {

            if (operationPathAdapter.operation().parameters == null) {
                return;
            }

            operationPathAdapter.operation().parameters.stream()
                .filter(param -> "query".equals(param.in))
                .filter(
                    param -> (param.required != null && param.required) || context.getVariables()
                        .containsKey(param.getName()))
                .forEach(param -> httpMessage.queryParam(param.getName(),
                    OpenApiTestDataGenerator.createValidationExpression(param.getName(),
                        OasModelHelper.getParameterSchema(param).orElse(null),
                        OasModelHelper.getSchemaDefinitions(openApiSpec.getOpenApiDoc(context)), false,
                        openApiSpec,
                        context)));

        }

        private void setSpecifiedHeaders(TestContext context,
            OperationPathAdapter operationPathAdapter) {

            if (operationPathAdapter.operation().parameters == null) {
                return;
            }

            operationPathAdapter.operation().parameters.stream()
                .filter(param -> "header".equals(param.in))
                .filter(
                    param -> (param.required != null && param.required) || context.getVariables()
                        .containsKey(param.getName()))
                .forEach(param -> httpMessage.setHeader(param.getName(),
                    OpenApiTestDataGenerator.createValidationExpression(param.getName(),
                        OasModelHelper.getParameterSchema(param).orElse(null),
                        OasModelHelper.getSchemaDefinitions(openApiSpec.getOpenApiDoc(context)), false,
                        openApiSpec,
                        context)));
        }

        private void setSpecifiedMessageType(OperationPathAdapter operationPathAdapter) {
            Optional<String> requestContentType = getRequestContentType(
                operationPathAdapter.operation());
            if (requestContentType.isPresent() && APPLICATION_JSON_VALUE.equals(
                requestContentType.get())) {
                httpMessage.setType(JSON);
            } else if (requestContentType.isPresent() && APPLICATION_XML_VALUE.equals(
                requestContentType.get())) {
                httpMessage.setType(XML);
            } else {
                httpMessage.setType(PLAINTEXT);
            }
        }

        private void setSpecifiedMethod(OperationPathAdapter operationPathAdapter) {
            httpMessage.method(HttpMethod.valueOf(operationPathAdapter.operation().getMethod().toUpperCase()));
        }

    }


}
