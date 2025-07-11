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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasSchema;
import org.citrusframework.CitrusSettings;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.AutoFillType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.validation.OpenApiOperationToMessageHeadersProcessor;
import org.citrusframework.openapi.validation.OpenApiValidationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static org.citrusframework.openapi.OpenApiMessageType.REQUEST;
import static org.citrusframework.openapi.OpenApiTestDataGenerator.createOutboundPayload;
import static org.citrusframework.openapi.OpenApiTestDataGenerator.createRandomValueExpression;
import static org.citrusframework.openapi.model.OasModelHelper.getRequestBodySchema;
import static org.citrusframework.openapi.model.OasModelHelper.isOperationRequestBodyRequired;
import static org.citrusframework.util.StringUtils.isNotEmpty;

/**
 * @since 4.1
 */
public class OpenApiClientRequestActionBuilder extends HttpClientRequestActionBuilder implements OpenApiSpecificationSourceAwareBuilder<SendMessageAction> {

    private final OpenApiSpecificationSource openApiSpecificationSource;
    private final String operationKey;
    private OpenApiOperationToMessageHeadersProcessor openApiOperationToMessageHeadersProcessor;
    private boolean schemaValidation = true;


    /**
     * Default constructor initializes http request message builder.
     */
    public OpenApiClientRequestActionBuilder(OpenApiSpecificationSource openApiSpec,
        String operationKey) {
        this(new HttpMessage(), openApiSpec, operationKey);
    }

    public OpenApiClientRequestActionBuilder(HttpMessage httpMessage,
        OpenApiSpecificationSource openApiSpec,
        String operationKey) {
        this(openApiSpec,
            new OpenApiClientRequestMessageBuilder(httpMessage, openApiSpec, operationKey),
            httpMessage, operationKey);
    }

    public OpenApiClientRequestActionBuilder(OpenApiSpecificationSource openApiSpec,
        OpenApiClientRequestMessageBuilder messageBuilder,
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

    public OpenApiClientRequestActionBuilder autoFill(AutoFillType autoFill) {
        ((OpenApiClientRequestMessageBuilder) this.messageBuilderSupport.getMessageBuilder()).autoFill(
            autoFill);
        return this;
    }

    @Override
    public SendMessageAction doBuild() {
        OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(
            referenceResolver);

        // Honor default enablement of schema validation
        OpenApiValidationContext openApiValidationContext = openApiSpecification.getOpenApiValidationContext();
        if (openApiValidationContext != null && schemaValidation) {
            schemaValidation = openApiValidationContext.isRequestValidationEnabled();
        }

        if (schemaValidation && !messageProcessors.contains(
            openApiOperationToMessageHeadersProcessor)) {
            openApiOperationToMessageHeadersProcessor = new OpenApiOperationToMessageHeadersProcessor(
                openApiSpecification, operationKey, REQUEST);
            process(openApiOperationToMessageHeadersProcessor);
        }

        return super.doBuild();
    }

    /**
     * By default, enable schema validation as the OpenAPI is always available.
     */
    @Override
    protected HttpMessageBuilderSupport createHttpMessageBuilderSupport() {
        HttpMessageBuilderSupport httpMessageBuilderSupport = super.createHttpMessageBuilderSupport();
        httpMessageBuilderSupport.schemaValidation(true);
        return httpMessageBuilderSupport;
    }

    public OpenApiClientRequestActionBuilder schemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
        return this;
    }

    public static class OpenApiClientRequestMessageBuilder extends HttpMessageBuilder {

        private final OpenApiSpecificationSource openApiSpecificationSource;

        private final String operationKey;

        private AutoFillType autoFill ;

        public OpenApiClientRequestMessageBuilder(HttpMessage httpMessage,
            OpenApiSpecificationSource openApiSpec,
            String operationKey) {
            super(httpMessage);
            this.openApiSpecificationSource = openApiSpec;
            this.operationKey = operationKey;
        }

        public OpenApiClientRequestMessageBuilder autoFill(AutoFillType autoFill) {
            this.autoFill = autoFill;
            return this;
        }

        @Override
        public Message build(TestContext context, String messageType) {
            OpenApiSpecification openApiSpecification = openApiSpecificationSource.resolve(
                context.getReferenceResolver());

            if (autoFill == null) {
                autoFill = openApiSpecification.getRequestAutoFill();
            }

            openApiSpecification.initOpenApiDoc(context);
            openApiSpecification.getOperation(operationKey, context)
                .ifPresentOrElse(operationPathAdapter ->
                        buildMessageFromOperation(openApiSpecification, operationPathAdapter,
                            context),
                    () -> {
                        throw new CitrusRuntimeException(
                            "Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(
                                operationKey, openApiSpecification.getSpecUrl()));
                    });
            context.setVariable(openApiSpecification.getUid(), openApiSpecification);

            return super.build(context, messageType);
        }

        @Override
        public Object buildMessagePayload(TestContext context, String messageType) {
            if (getPayloadBuilder() == null) {
                this.setPayloadBuilder(new OpenApiPayloadBuilder(getMessage().getPayload()));
            }
            return super.buildMessagePayload(context, messageType);
        }

        private void buildMessageFromOperation(OpenApiSpecification openApiSpecification,
            OperationPathAdapter operationPathAdapter,
            TestContext context) {
            OasOperation operation = operationPathAdapter.operation();
            String path = operationPathAdapter.fullPath();
            HttpMethod method = HttpMethod.valueOf(
                operationPathAdapter.operation().getMethod().toUpperCase(Locale.US));

            if (operation.parameters != null) {
                setMissingHeadersToRandomValues(openApiSpecification, context, operation);
                setMissingQueryParametersToRandomValues(openApiSpecification, context, operation);
            }

            setMissingBodyToRandomValue(openApiSpecification, context, operation);
            String randomizedPath = getMessage().getPath() != null ? getMessage().getPath() : path;
            if (operation.parameters != null) {
                List<OasParameter> pathParams = operation.parameters.stream()
                    .filter(p -> "path".equals(p.in)).toList();

                for (OasParameter parameter : pathParams) {
                    String parameterValue;
                    String pathParameterValue = getDefinedPathParameter(context,
                        parameter.getName());
                    if (isNotEmpty(pathParameterValue)) {
                        parameterValue = "\\" + pathParameterValue;
                    } else {
                        parameterValue = createRandomValueExpression(
                            (OasSchema) parameter.schema);
                    }

                    randomizedPath = randomizedPath.replaceAll("\\{" + parameter.getName() + "}",
                        parameterValue);
                }
            }

            OasModelHelper.getRequestContentType(operation)
                .ifPresent(
                    contentType -> getMessage().setHeader(HttpHeaders.CONTENT_TYPE, contentType));

            getMessage().path(randomizedPath);
            getMessage().method(method);
        }

        protected String getDefinedPathParameter(TestContext context, String name) {
            if (context.getVariables().containsKey(name)) {
                return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
            }
            return null;
        }

        private void setMissingBodyToRandomValue(OpenApiSpecification openApiSpecification,
            TestContext context, OasOperation operation) {
            if (getMessage().getPayload() == null || (
                getMessage().getPayload() instanceof String payloadString
                    && payloadString.isEmpty())) {
                Optional<OasSchema> body = getRequestBodySchema(
                    openApiSpecification.getOpenApiDoc(context), operation);


                body.ifPresent(oasSchema -> {

                    if (autoFill.shouldFill(isOperationRequestBodyRequired(openApiSpecification.getOpenApiDoc(context), operation))) {
                        getMessage().setPayload(
                            createOutboundPayload(oasSchema, openApiSpecification));
                    }
                });
            }
        }

        /**
         * Creates all required query parameters, if they have not already been specified.
         */
        private void setMissingQueryParametersToRandomValues(
            OpenApiSpecification openApiSpecification, TestContext context,
            OasOperation operation) {
            operation.parameters.stream()
                .filter(param -> "query".equals(param.in))
                // Not configured manually
                .filter(param -> !getMessage().getQueryParams().containsKey(param.getName()))
                // Only targeted parameters
                .filter(param -> autoFill.shouldFill(param.required))
                .forEach(param -> {
                    Object queryParameterValue = context.getVariables()
                        .get(param.getName());
                    if (queryParameterValue == null) {
                        queryParameterValue = createRandomValueExpression(param.getName(),
                            (OasSchema) param.schema, openApiSpecification,
                            context);
                    }
                    try {
                        getMessage().queryParam(param.getName(), queryParameterValue.toString());
                    } catch (Exception e) {
                        // Note that exploded object query parameter representation for example, cannot properly
                        // be randomized.
                        logger.warn(
                            "Unable to set missing required query parameter to random value: {}",
                            param);
                    }
                });
        }

        /**
         * Creates all required headers, if they have not already been specified.
         */
        private void setMissingHeadersToRandomValues(OpenApiSpecification openApiSpecification,
            TestContext context, OasOperation operation) {
            List<String> configuredHeaders = getHeaderBuilders()
                .stream()
                .flatMap(b -> b.builderHeaders(context).keySet().stream())
                .toList();
            operation.parameters.stream()
                .filter(param -> "header".equals(param.in))
                // Not configured manually
                .filter(param -> getMessage().getHeader(param.getName()) == null
                    && !configuredHeaders.contains(param.getName()))
                // Only targeted parameters
                .filter(param -> autoFill.shouldFill(param.required))
                .forEach(param -> {
                    Object headerValue = context.getVariables()
                        .get(param.getName());
                    if (headerValue == null) {
                        headerValue = createRandomValueExpression(param.getName(),
                            (OasSchema) param.schema,
                            openApiSpecification, context);
                    }
                    getMessage().setHeader(param.getName(), headerValue);
                });
        }
    }
}
