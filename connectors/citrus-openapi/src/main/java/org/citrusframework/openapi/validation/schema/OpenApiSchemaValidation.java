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

package org.citrusframework.openapi.validation.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.atlassian.oai.validator.report.ValidationReport;
import jakarta.annotation.Nullable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiMessageHeaders;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder;
import org.citrusframework.openapi.validation.OpenApiRequestValidator;
import org.citrusframework.openapi.validation.OpenApiResponseValidator;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.validation.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static org.citrusframework.openapi.OpenApiMessageHeaders.OAS_SPECIFICATION_ID;
import static org.citrusframework.openapi.OpenApiMessageHeaders.OAS_UNIQUE_OPERATION_ID;
import static org.citrusframework.util.StringUtils.isNotEmpty;

public class OpenApiSchemaValidation implements SchemaValidator<OpenApiMessageValidationContext> {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiSchemaValidation.class);

    @Override
    public void validate(Message message, TestContext context,
        OpenApiMessageValidationContext validationContext) {
        logger.debug("Starting OpenApi schema validation ...");

        // In case we have a redirect header, we cannot  validate the message, as we have to expect, that the message has no valid response.
        if (!(message instanceof HttpMessage httpMessage) || httpMessage.getHeader("Location") != null) {
            return;
        }

        ValidationReportData validationReportData = validate(context,
            httpMessage,
            findSchemaRepositories(context),
            validationContext);

        if (validationReportData != null
            && validationReportData.report != null
            && validationReportData.report.hasErrors()) {
            if (logger.isErrorEnabled()) {
                logger.error(
                    "Failed to validate Json schema for message:\n{}\nand origin path:\n{}",
                    httpMessage.getPayload(String.class), httpMessage.getPath());
            }
            throw new ValidationException(constructErrorMessage(validationReportData));
        }

        logger.debug("Json schema validation successful: All values OK");
    }

    /**
     * When the correct {@link OpenApiRepository} is installed, schema validation can be performed
     * on any JSON message where a response can be derived based on the request method, URL, and
     * OpenAPI operation. Validation is also possible if the operationId is explicitly specified, as
     * set by an OpenApiClient.
     *
     * @param messageType The type of message to be validated.
     * @param message     The message content for validation.
     * @return Validation result or status.
     */
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return "JSON".equals(messageType)
            || (
            message != null && (IsJsonPredicate.getInstance().test(message.getPayload(String.class))
                || message.getHeader(OAS_UNIQUE_OPERATION_ID) != null));
    }

    private String constructErrorMessage(ValidationReportData validationReportData) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("OpenApi ");
        stringBuilder.append(validationReportData.type);
        stringBuilder.append(" validation failed for operation: ");
        stringBuilder.append(validationReportData.operationPathAdapter);
        validationReportData.report.getMessages()
            .forEach(message -> stringBuilder.append("\n\t").append(message));
        return stringBuilder.toString();
    }

    /**
     * Find json schema repositories in test context.
     */
    private List<OpenApiRepository> findSchemaRepositories(TestContext context) {
        return new ArrayList<>(
            context.getReferenceResolver().resolveAll(OpenApiRepository.class).values());
    }

    @Nullable
    private ValidationReportData validate(TestContext context,
        HttpMessage message,
        List<OpenApiRepository> schemaRepositories,
        OpenApiMessageValidationContext validationContext) {

        schemaRepositories = schemaRepositories != null ? schemaRepositories : emptyList();

        if (!validationContext.isSchemaValidationEnabled()) {
            return null;
        } else {
            String operationKey = validationContext.getSchema() != null ? validationContext.getSchema()
                : (String) message.getHeader(OAS_UNIQUE_OPERATION_ID);
            String specificationId = validationContext.getSchemaRepository() != null
                ? validationContext.getSchemaRepository()
                : (String) message.getHeader(OAS_SPECIFICATION_ID);

            if (isNotEmpty(specificationId) && isNotEmpty(operationKey)) {
                return validateOpenApiOperation(context, message, schemaRepositories,
                    specificationId, operationKey);
            }

            return null;

        }
    }

    private ValidationReportData validateOpenApiOperation(TestContext context, HttpMessage message,
        List<OpenApiRepository> schemaRepositories, String specificationId, String operationKey) {
        OpenApiSpecification openApiSpecification = schemaRepositories
            .stream()
            .map(repository -> repository.openApi(specificationId))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse((OpenApiSpecification) context.getVariables().get(specificationId));

        if (openApiSpecification == null) {
            throw new CitrusRuntimeException("""
            Unable to derive OpenAPI spec for operation '%s' for validation of message from available "
            schema repositories. Known repository aliases are: %s""".formatted(
                operationKey,
                OpenApiUtils.getKnownOpenApiAliases(context.getReferenceResolver())));
        }

        OperationPathAdapter operationPathAdapter = openApiSpecification.getOperation(
                operationKey, context)
            .orElseThrow(() -> new CitrusRuntimeException(
                "Unexpectedly could not resolve operation path adapter for operationKey: "
                    + operationKey));

        ValidationReportData validationReportData = null;
        if (isRequestMessage(message)) {
            ValidationReport validationReport = new OpenApiRequestValidator(
                openApiSpecification)
                .validateRequestToReport(operationPathAdapter, message);
            validationReportData = new ValidationReportData(operationPathAdapter, "request",
                validationReport);
        } else if (isResponseMessage(message)) {
            ValidationReport validationReport = new OpenApiResponseValidator(
                openApiSpecification)
                .validateResponseToReport(operationPathAdapter, message);
            validationReportData = new ValidationReportData(operationPathAdapter, "response",
                validationReport);
        }
        return validationReportData;
    }

    private boolean isResponseMessage(HttpMessage message) {
        return OpenApiMessageHeaders.RESPONSE_TYPE.equals(
            message.getHeader(OpenApiMessageHeaders.OAS_MESSAGE_TYPE)) || message.getHeader(
            HttpMessageHeaders.HTTP_STATUS_CODE) != null;
    }

    private boolean isRequestMessage(HttpMessage message) {
        return OpenApiMessageHeaders.REQUEST_TYPE.equals(
            message.getHeader(OpenApiMessageHeaders.OAS_MESSAGE_TYPE)) || message.getHeader(
            HttpMessageHeaders.HTTP_STATUS_CODE) == null;
    }

    @Override
    public boolean canValidate(Message message, boolean schemaValidationEnabled) {
        return schemaValidationEnabled
            && message instanceof HttpMessage httpMessage
            && (isRequestMessage(httpMessage) || isResponseMessage(httpMessage));
    }

    @Override
    public void validate(Message message, TestContext context, String schemaRepository,
        String schema) {
        if (!(message instanceof HttpMessage)) {
            return;
        }

        validate(message, context,
            new Builder().schemaValidation(true).schema(schema).schemaRepository(schemaRepository)
                .build());

    }

    private record ValidationReportData(OperationPathAdapter operationPathAdapter, String type,
                                        ValidationReport report) {

    }
}
