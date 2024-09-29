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
import static org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder.openApi;
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
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiMessageType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.OpenApiTestValidationDataGenerator;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.validation.OpenApiMessageProcessor;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.openapi.validation.OpenApiValidationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * @since 4.1
 */
public class OpenApiServerRequestActionBuilder extends HttpServerRequestActionBuilder {

    private OpenApiMessageProcessor openApiMessageProcessor;

    private final OpenApiSpecificationSource openApiSpecificationSource;

    private final String operationId;

    /**
     * Schema validation is enabled by default.
     */
    private boolean schemaValidation = true;

    /**
     * Default constructor initializes http request message builder.
     */
    public OpenApiServerRequestActionBuilder(OpenApiSpecificationSource openApiSpecificationSource,
        String operationId) {
        this(new HttpMessage(), openApiSpecificationSource, operationId);
    }

    public OpenApiServerRequestActionBuilder(HttpMessage httpMessage,
        OpenApiSpecificationSource openApiSpecificationSource,
        String operationId) {
        super(new OpenApiServerRequestMessageBuilder(httpMessage, openApiSpecificationSource,
                operationId),
            httpMessage);
        this.openApiSpecificationSource = openApiSpecificationSource;
        this.operationId = operationId;
    }

    @Override
    public ReceiveMessageAction doBuild() {

        OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(
            referenceResolver);

        // Honor default enablement of schema validation
        OpenApiValidationContext openApiValidationContext = openApiSpecification.getOpenApiValidationContext();
        if (openApiValidationContext != null && schemaValidation) {
            schemaValidation = openApiValidationContext.isRequestValidationEnabled();
        }

        if (schemaValidation && !messageProcessors.contains(openApiMessageProcessor)) {
            openApiMessageProcessor = new OpenApiMessageProcessor(
                openApiSpecification, operationId, OpenApiMessageType.REQUEST);
            process(openApiMessageProcessor);
        }

        if (schemaValidation && getValidationContexts().stream()
            .noneMatch(OpenApiMessageValidationContext.class::isInstance)) {
            validate(openApi(openApiSpecification)
                .schemaValidation(schemaValidation)
                .build());
        }

        return super.doBuild();
    }

    public OpenApiServerRequestActionBuilder schemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
        return this;
    }

    private static class OpenApiServerRequestMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecificationSource openApiSpecificationSource;
        private final String operationId;

        private final HttpMessage httpMessage;

        public OpenApiServerRequestMessageBuilder(HttpMessage httpMessage,
            OpenApiSpecificationSource openApiSpec,
            String operationId) {
            super(httpMessage);
            this.openApiSpecificationSource = openApiSpec;
            this.operationId = operationId;
            this.httpMessage = httpMessage;
        }

        @Override
        public Message build(TestContext context, String messageType) {

            OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(
                context.getReferenceResolver());

            openApiSpecification.getOperation(operationId, context)
                .ifPresentOrElse(operationPathAdapter ->
                        buildMessageFromOperation(openApiSpecification, operationPathAdapter, context),
                    () -> {
                        throw new CitrusRuntimeException(
                            "Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(
                                operationId, openApiSpecification.getSpecUrl()));
                    });

            return super.build(context, messageType);
        }

        private void buildMessageFromOperation(OpenApiSpecification openApiSpecification,
            OperationPathAdapter operationPathAdapter, TestContext context) {

            setSpecifiedMessageType(operationPathAdapter);
            setSpecifiedHeaders(context, openApiSpecification, operationPathAdapter);
            setSpecifiedQueryParameters(context, openApiSpecification, operationPathAdapter);
            setSpecifiedPath(context, openApiSpecification, operationPathAdapter);
            setSpecifiedBody(context, openApiSpecification, operationPathAdapter);
            setSpecifiedRequestContentType(operationPathAdapter);
            setSpecifiedMethod(operationPathAdapter);

        }

        private void setSpecifiedRequestContentType(OperationPathAdapter operationPathAdapter) {
            OasModelHelper.getRequestContentType(operationPathAdapter.operation())
                .ifPresent(contentType -> httpMessage.setHeader(HttpHeaders.CONTENT_TYPE,
                    String.format("@startsWith(%s)@", contentType)));
        }

        private void setSpecifiedPath(TestContext context,
            OpenApiSpecification openApiSpecification, OperationPathAdapter operationPathAdapter) {
            String randomizedPath =
                OasModelHelper.getBasePath(openApiSpecification.getOpenApiDoc(context))
                    + operationPathAdapter.apiPath();
            randomizedPath = randomizedPath.replace("//", "/");

            randomizedPath = appendSegmentToUrlPath(openApiSpecification.getRootContextPath(),
                randomizedPath);

            if (operationPathAdapter.operation().parameters != null) {
                randomizedPath = determinePath(context, operationPathAdapter.operation(),
                    randomizedPath);
            }

            httpMessage.path(randomizedPath);
        }

        private void setSpecifiedBody(TestContext context,
            OpenApiSpecification openApiSpecification, OperationPathAdapter operationPathAdapter) {
            Optional<OasSchema> body = OasModelHelper.getRequestBodySchema(
                openApiSpecification.getOpenApiDoc(context), operationPathAdapter.operation());
            body.ifPresent(oasSchema -> httpMessage.setPayload(
                OpenApiTestValidationDataGenerator.createInboundPayload(oasSchema,
                    OasModelHelper.getSchemaDefinitions(
                        openApiSpecification.getOpenApiDoc(context)),
                    openApiSpecification)));
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
                    parameterValue = OpenApiTestValidationDataGenerator.createValidationRegex(
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
            OpenApiSpecification openApiSpecification,
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
                    OpenApiTestValidationDataGenerator.createValidationExpression(param.getName(),
                        OasModelHelper.getParameterSchema(param).orElse(null),
                        OasModelHelper.getSchemaDefinitions(
                            openApiSpecification.getOpenApiDoc(context)), false,
                        openApiSpecification,
                        context)));

        }

        private void setSpecifiedHeaders(TestContext context,
            OpenApiSpecification openApiSpecification,
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
                    OpenApiTestValidationDataGenerator.createValidationExpression(param.getName(),
                        OasModelHelper.getParameterSchema(param).orElse(null),
                        OasModelHelper.getSchemaDefinitions(
                            openApiSpecification.getOpenApiDoc(context)), false,
                        openApiSpecification,
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
            httpMessage.method(
                HttpMethod.valueOf(operationPathAdapter.operation().getMethod().toUpperCase()));
        }
    }
}
