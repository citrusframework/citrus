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

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.citrusframework.CitrusSettings;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
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
public class OpenApiClientRequestActionBuilder extends HttpClientRequestActionBuilder {

    private final OpenApiSpecification openApiSpec;

    private final String operationId;

    private boolean oasValidationEnabled = true;

    private OpenApiRequestValidationProcessor openApiRequestValidationProcessor;

    /**
     * Default constructor initializes http request message builder.
     */
    public OpenApiClientRequestActionBuilder(OpenApiSpecification openApiSpec, String operationId) {
        this(new HttpMessage(), openApiSpec, operationId);
    }

    public OpenApiClientRequestActionBuilder(HttpMessage httpMessage, OpenApiSpecification openApiSpec,
                                             String operationId) {
        super(new OpenApiClientRequestMessageBuilder(httpMessage, openApiSpec, operationId), httpMessage);

        this.openApiSpec = openApiSpec;
        this.operationId = operationId;
   }

    @Override
    public SendMessageAction doBuild() {

        if (oasValidationEnabled && !messageProcessors.contains(openApiRequestValidationProcessor)) {
            openApiRequestValidationProcessor = new OpenApiRequestValidationProcessor(openApiSpec, operationId);
            process(openApiRequestValidationProcessor);
        }

        return super.doBuild();
    }

    public OpenApiClientRequestActionBuilder disableOasValidation(boolean disabled) {
        oasValidationEnabled = !disabled;
        return this;
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
            openApiSpec.getOperation(operationId, context).ifPresentOrElse(operationPathAdapter ->
                buildMessageFromOperation(operationPathAdapter, context), () -> {
                    throw new CitrusRuntimeException("Unable to locate operation with id '%s' in OpenAPI specification %s".formatted(operationId, openApiSpec.getSpecUrl()));
                });

            return super.build(context, messageType);
        }

        private void buildMessageFromOperation(OperationPathAdapter operationPathAdapter, TestContext context) {
                OasOperation operation = operationPathAdapter.operation();
                String path = operationPathAdapter.apiPath();
                HttpMethod method = HttpMethod.valueOf(operationPathAdapter.operation().getMethod().toUpperCase(Locale.US));

            if (operation.parameters != null) {
                setSpecifiedHeaders(context, operation);
                setSpecifiedQueryParameters(context, operation);
            }

            if(httpMessage.getPayload() == null || (httpMessage.getPayload() instanceof String p && p.isEmpty())) {
                setSpecifiedBody(context, operation);
            }

            String randomizedPath = path;
            if (operation.parameters != null) {
                List<OasParameter> pathParams = operation.parameters.stream()
                    .filter(p -> "path".equals(p.in)).toList();

                for (OasParameter parameter : pathParams) {
                    String parameterValue;
                    if (context.getVariables().containsKey(parameter.getName())) {
                        parameterValue = "\\" + CitrusSettings.VARIABLE_PREFIX + parameter.getName() + CitrusSettings.VARIABLE_SUFFIX;
                    } else {
                        parameterValue = OpenApiTestDataGenerator.createRandomValueExpression((OasSchema) parameter.schema, false);
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

        }

        private void setSpecifiedBody(TestContext context, OasOperation operation) {
            Optional<OasSchema> body = OasModelHelper.getRequestBodySchema(
                openApiSpec.getOpenApiDoc(context), operation);
            body.ifPresent(oasSchema -> httpMessage.setPayload(OpenApiTestDataGenerator.createOutboundPayload(oasSchema,
                OasModelHelper.getSchemaDefinitions(openApiSpec.getOpenApiDoc(context)), openApiSpec)));
        }

        private void setSpecifiedQueryParameters(TestContext context, OasOperation operation) {
            operation.parameters.stream()
                .filter(param -> "query".equals(param.in))
                .filter(param -> (param.required != null && param.required) || context.getVariables().containsKey(param.getName()))
                .forEach(param -> {
                    if(!httpMessage.getQueryParams().containsKey(param.getName())) {
                        httpMessage.queryParam(param.getName(),
                            OpenApiTestDataGenerator.createRandomValueExpression(param.getName(), (OasSchema) param.schema,
                                context));
                    }
                });
        }

        private void setSpecifiedHeaders(TestContext context, OasOperation operation) {
            List<String> configuredHeaders = getHeaderBuilders()
                .stream()
                .flatMap(b -> b.builderHeaders(context).keySet().stream())
                .toList();
            operation.parameters.stream()
                .filter(param -> "header".equals(param.in))
                .filter(param -> (param.required != null && param.required) || context.getVariables().containsKey(param.getName()))
                .forEach(param -> {
                    if(httpMessage.getHeader(param.getName()) == null && !configuredHeaders.contains(param.getName())) {
                        httpMessage.setHeader(param.getName(),
                            OpenApiTestDataGenerator.createRandomValueExpression(param.getName(), (OasSchema) param.schema,
                                OasModelHelper.getSchemaDefinitions(openApiSpec.getOpenApiDoc(
                                    context)), false, openApiSpec, context));
                    }
                });
        }
    }

}
