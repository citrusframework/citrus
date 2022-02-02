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

package com.consol.citrus.validation.json.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.IsJsonPredicate;
import com.consol.citrus.validation.SchemaValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.report.GraciousProcessingReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.DevNullProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for the validation of json messages against json schemas / json schema repositories.
 * @since 2.7.3
 */
public class JsonSchemaValidation implements SchemaValidator<JsonMessageValidationContext> {

    /** The logger */
    private Logger log = LoggerFactory.getLogger(JsonSchemaValidation.class);

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
     * @param jsonSchemaFilter
     */
    public JsonSchemaValidation(JsonSchemaFilter jsonSchemaFilter) {
        this.jsonSchemaFilter = jsonSchemaFilter;
    }

    @Override
    public void validate(Message message, TestContext context, JsonMessageValidationContext validationContext) {
        log.debug("Starting Json schema validation ...");

        ProcessingReport report = validate(message,
                findSchemaRepositories(context),
                validationContext,
                context.getReferenceResolver());
        if (!report.isSuccess()) {
            log.error("Failed to validate Json schema for message:\n" + message.getPayload(String.class));

            throw new ValidationException(constructErrorMessage(report));
        }

        log.info("Json schema validation successful: All values OK");
    }

    /**
     * Constructs the error message of a failed validation based on the processing report passed from
     * com.github.fge.jsonschema.core.report
     * @param report The report containing the error message
     * @return A string representation of all messages contained in the report
     */
    private String constructErrorMessage(ProcessingReport report) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Json validation failed: ");
        report.forEach(processingMessage -> stringBuilder.append(processingMessage.getMessage()));
        return stringBuilder.toString();
    }

    /**
     * Find json schema repositories in test context.
     * @param context
     * @return
     */
    private List<JsonSchemaRepository> findSchemaRepositories(TestContext context) {
        return new ArrayList<>(context.getReferenceResolver().resolveAll(JsonSchemaRepository.class).values());
    }

    /**
     * Validates the given message against a list of JsonSchemaRepositories under consideration of the actual context
     * @param message The message to be validated
     * @param schemaRepositories The schema repositories to be used for validation
     * @param validationContext The context of the validation to be used for the validation
     * @param referenceResolver holding bean references for lookup.
     * @return A report holding the results of the validation
     */
    public ProcessingReport validate(Message message,
                                     List<JsonSchemaRepository> schemaRepositories,
                                     JsonMessageValidationContext validationContext,
                                     ReferenceResolver referenceResolver) {
        return validate(message, jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolver));
    }

    /**
     * Validates a message against all schemas contained in the given json schema repository
     * @param message The message to be validated
     * @param jsonSchemas The list of json schemas to iterate over
     */
    private GraciousProcessingReport validate(Message message, List<SimpleJsonSchema> jsonSchemas) {
        if (jsonSchemas.isEmpty()) {
            return new GraciousProcessingReport(true);
        } else {
            List<ProcessingReport> processingReports = new LinkedList<>();
            for (SimpleJsonSchema simpleJsonSchema : jsonSchemas) {
                processingReports.add(validate(message, simpleJsonSchema));
            }
            return new GraciousProcessingReport(processingReports);
        }
    }

    /**
     * Validates a given message against a given json schema
     * @param message The message to be validated
     * @param simpleJsonSchema The json schema to validate against
     * @return returns the report holding the result of the validation
     */
    private ProcessingReport validate(Message message, SimpleJsonSchema simpleJsonSchema) {
        try {
            JsonNode receivedJson = objectMapper.readTree(message.getPayload(String.class));
            if (receivedJson.isEmpty()) {
                return new DevNullProcessingReport();
            } else {
                return simpleJsonSchema.getSchema().validate(receivedJson);
            }
        } catch (IOException | ProcessingException e) {
            throw new CitrusRuntimeException("Failed to validate Json schema", e);
        }
    }


    /**
     *
     * @param messageType
     * @param message
     * @return true if the message or message type is supported by this validator
     */
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return "JSON".equals(messageType) || (message != null && IsJsonPredicate.getInstance().test(message.getPayload(String.class)));
    }
}
