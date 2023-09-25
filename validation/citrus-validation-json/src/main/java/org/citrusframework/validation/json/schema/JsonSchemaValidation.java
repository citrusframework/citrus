/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.report.GraciousProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for the validation of json messages against json schemas / json schema repositories.
 * @since 2.7.3
 */
public class JsonSchemaValidation implements SchemaValidator<JsonMessageValidationContext> {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidation.class);

    private final JsonSchemaFilter jsonSchemaFilter;

    /** Object Mapper to convert the message for validation*/
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Default constructor using default filter.
     */
    public JsonSchemaValidation() {
        this(new JsonSchemaFilter());
    }

    /**
     * Constructor using filter implementation.
     */
    public JsonSchemaValidation(JsonSchemaFilter jsonSchemaFilter) {
        this.jsonSchemaFilter = jsonSchemaFilter;
    }

    @Override
    public void validate(Message message, TestContext context, JsonMessageValidationContext validationContext) {
        logger.debug("Starting Json schema validation ...");

        GraciousProcessingReport report = validate(message,
                findSchemaRepositories(context),
                validationContext,
                context.getReferenceResolver());

        if (!report.isSuccess()) {
            logger.error("Failed to validate Json schema for message:\n" + message.getPayload(String.class));
            throw new ValidationException(constructErrorMessage(report));
        }

        logger.info("Json schema validation successful: All values OK");
    }

    /**
     * Constructs the error message of a failed validation based on the processing report passed from
     * {@link ValidationMessage}.
     *
     * @param report The report containing the error message
     * @return A string representation of all messages contained in the report
     */
    private String constructErrorMessage(GraciousProcessingReport report) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Json validation failed: ");
        report.getValidationMessages().forEach(processingMessage -> stringBuilder.append("\n\t").append(processingMessage.getMessage()));
        return stringBuilder.toString();
    }

    /**
     * Find json schema repositories in test context.
     */
    private List<JsonSchemaRepository> findSchemaRepositories(TestContext context) {
        return new ArrayList<>(context.getReferenceResolver().resolveAll(JsonSchemaRepository.class).values());
    }

    /**
     * Validates the given message against a list of JsonSchemaRepositories under consideration of the actual context
     *
     * @param message            The message to be validated
     * @param schemaRepositories The schema repositories to be used for validation
     * @param validationContext  The context of the validation to be used for the validation
     * @param referenceResolver  holding bean references for lookup.
     * @return A report holding the results of the validation
     */
    public GraciousProcessingReport validate(Message message,
                                            List<JsonSchemaRepository> schemaRepositories,
                                            JsonMessageValidationContext validationContext,
                                            ReferenceResolver referenceResolver) {
        return validate(message, jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolver));
    }

    /**
     * Validates a message against all schemas contained in the given json schema repository
     *
     * @param message     The message to be validated
     * @param jsonSchemas The list of json schemas to iterate over
     */
    private GraciousProcessingReport validate(Message message, List<SimpleJsonSchema> jsonSchemas) {
        if (jsonSchemas.isEmpty()) {
            return new GraciousProcessingReport(true);
        } else {
            GraciousProcessingReport processingReport = new GraciousProcessingReport();
            for (SimpleJsonSchema simpleJsonSchema : jsonSchemas) {
                processingReport.mergeWith(validate(message, simpleJsonSchema));
            }
            return processingReport;
        }
    }

    /**
     * Validates a given message against a given json schema.
     *
     * @param message          The message to be validated
     * @param simpleJsonSchema The json schema to validate against
     * @return returns the report holding the result of the validation
     */
    private Set<ValidationMessage> validate(Message message, SimpleJsonSchema simpleJsonSchema) {
        try {
            JsonNode receivedJson = objectMapper.readTree(message.getPayload(String.class));
            if (receivedJson.isEmpty()) {
                return Collections.emptySet();
            } else {
                return simpleJsonSchema.getSchema().validate(
                        objectMapper.readTree(
                                message.getPayload(String.class)
                        )
                );
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to validate Json schema", e);
        }
    }


    /**
     * Checks whether the supplied message type is supported by the message.
     *
     * @param messageType the message type to check
     * @param message the message
     * @return true if the message or message type is supported by this validator
     */
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return "JSON".equals(messageType) || (message != null && IsJsonPredicate.getInstance().test(message.getPayload(String.class)));
    }
}
