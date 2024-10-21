package org.citrusframework.openapi.validation;

import com.atlassian.oai.validator.report.ValidationReport;
import jakarta.annotation.Nullable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiMessageHeaders;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpenApiSchemaValidation extends AbstractMessageValidator<OpenApiMessageValidationContext> implements SchemaValidator<OpenApiMessageValidationContext> {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiSchemaValidation.class);

    private static final Set<String> FILTERED_ERROR_MESSAGE_KEYS = Set.of(
            // Filtered because in general OpenAPI is not required to specify all response codes.
            // So this should not be considered as validation error.
            "validation.response.status.unknown"
    );

    /**
     * Filter specific messages from the report which are considered irrelevant.
     * See {@link OpenApiSchemaValidation#FILTERED_ERROR_MESSAGE_KEYS} for more details.
     */
    private static ValidationReport filterIrrelevantMessages(ValidationReportData validationReportData) {
        return ValidationReport.from(
                validationReportData.report.getMessages().stream()
                        .filter(msg -> !FILTERED_ERROR_MESSAGE_KEYS.contains(msg.getKey())).toList());
    }

    @Override
    protected Class<OpenApiMessageValidationContext> getRequiredValidationContextType() {
        return OpenApiMessageValidationContext.class;
    }

    @Override
    public void validateMessage(Message receivedMessage,
                                Message controlMessage,
                                TestContext context,
                                OpenApiMessageValidationContext validationContext) {
        // No control message validation, only schema validation
        validate(receivedMessage, context, validationContext);
    }

    @Override
    public void validate(Message message, TestContext context, OpenApiMessageValidationContext validationContext) {
        logger.debug("Starting OpenApi schema validation ...");

        if (!(message instanceof HttpMessage httpMessage)) {
            return;
        }

        ValidationReportData validationReportData = validate(context,
                httpMessage,
                findSchemaRepositories(context),
                validationContext);

        if (validationReportData != null && validationReportData.report != null) {
            ValidationReport filteredReport = filterIrrelevantMessages(validationReportData);
            if (filteredReport.hasErrors()) {
                logger.error("Failed to validate Json schema for message:\n{}", message.getPayload(String.class));
                throw new ValidationException(constructErrorMessage(validationReportData));
            }
        }

        logger.debug("Json schema validation successful: All values OK");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return message.getHeader(OpenApiMessageHeaders.OAS_UNIQUE_OPERATION_ID) != null;
    }

    private String constructErrorMessage(ValidationReportData validationReportData) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("OpenApi ");
        stringBuilder.append(validationReportData.type);
        stringBuilder.append(" validation failed for operation: ");
        stringBuilder.append(validationReportData.operationPathAdapter);
        validationReportData.report.getMessages().forEach(message -> stringBuilder.append("\n\t").append(message));
        return stringBuilder.toString();
    }

    /**
     * Find json schema repositories in test context.
     */
    private List<OpenApiRepository> findSchemaRepositories(TestContext context) {
        return new ArrayList<>(context.getReferenceResolver().resolveAll(OpenApiRepository.class).values());
    }

    @Nullable
    private ValidationReportData validate(TestContext context,
                                          HttpMessage message,
                                          List<OpenApiRepository> schemaRepositories,
                                          OpenApiMessageValidationContext validationContext) {

        if (!validationContext.isSchemaValidationEnabled()) {
            return null;
        } else if (schemaRepositories.isEmpty()) {
            return null;
        } else {
            // Is it request or response?
            String uniqueOperationId = (String) message.getHeader(OpenApiMessageHeaders.OAS_UNIQUE_OPERATION_ID);

            OpenApiSpecification openApiSpecification = schemaRepositories
                    .stream()
                    .flatMap(repository -> repository.getOpenApiSpecifications().stream())
                    .filter(spec -> spec.getOperation(uniqueOperationId, context).isPresent())
                    .findFirst()
                    .orElse(null);

            if (openApiSpecification == null) {
                throw new CitrusRuntimeException("""
                        Unable to derive OpenAPI spec for operation '%s' for validation of message from available "
                        schema repositories. Known repository aliases are: %s""".formatted(
                        uniqueOperationId, OpenApiUtils.getKnownOpenApiAliases(context.getReferenceResolver())));
            }

            OperationPathAdapter operationPathAdapter = openApiSpecification.getOperation(uniqueOperationId, context)
                    .orElseThrow(() -> new CitrusRuntimeException(
                    "Unexpectedly could not resolve operation path adapter for operationId: "
                            + uniqueOperationId));
            ValidationReportData validationReportData = null;
            if (isRequestMessage(message)) {
                ValidationReport validationReport = new OpenApiRequestValidator(openApiSpecification)
                        .validateRequestToReport(operationPathAdapter, message);
                validationReportData = new ValidationReportData(operationPathAdapter, "request", validationReport);
            } else if (isResponseMessage(message)) {
                ValidationReport validationReport = new OpenApiResponseValidator(openApiSpecification)
                        .validateResponseToReport(operationPathAdapter, message);
                validationReportData = new ValidationReportData(operationPathAdapter, "response", validationReport);
            }
            return validationReportData;
        }
    }

    private boolean isResponseMessage(HttpMessage message) {
        return OpenApiMessageHeaders.RESPONSE_TYPE.equals(
                message.getHeader(OpenApiMessageHeaders.OAS_MESSAGE_TYPE));
    }

    private boolean isRequestMessage(HttpMessage message) {
        return OpenApiMessageHeaders.REQUEST_TYPE.equals(
                message.getHeader(OpenApiMessageHeaders.OAS_MESSAGE_TYPE));
    }

    @Override
    public boolean canValidate(Message message, boolean schemaValidationEnabled) {
        return schemaValidationEnabled
                && message instanceof HttpMessage httpMessage
                && (isRequestMessage(httpMessage) || isResponseMessage(httpMessage));
    }

    @Override
    public void validate(Message message, TestContext context, String schemaRepository, String schema) {
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
