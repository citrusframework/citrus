/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.report.GraciousProcessingReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for the validation of json messages against json schemas / json schema repositories.
 */
public class JsonSchemaValidation {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /** Object Mapper to convert the message for validation*/
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Validates the given message against the schema repository
     * @param message The message to be validated
     * @param jsonSchemaRepository  The schema repositories to validate against
     */
    public GraciousProcessingReport validate(Message message, JsonSchemaRepository jsonSchemaRepository) {
        return validate(message, jsonSchemaRepository.getSchemas());
    }

    /**
     * Validates a message against all schemas contained in the given json schema repository
     * @param message The message to be validated
     * @param jsonSchemas The list of json schemas to iterate over
     */
    private GraciousProcessingReport validate(Message message, List<SimpleJsonSchema> jsonSchemas) {
        if(jsonSchemas.isEmpty()){
            return new GraciousProcessingReport(true);
        }
        else{
            List<ProcessingReport> processingReports = new LinkedList<>();
            for (SimpleJsonSchema simpleJsonSchema : jsonSchemas){
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
            return simpleJsonSchema.getSchema().validate(receivedJson);
        } catch (IOException | ProcessingException e) {
            throw new ValidationException("Failed to process message: " + message, e);
        }
    }

    /**
     * TODO:Add documentation
     * @param schemaRepositories
     * @param message
     * @param validationContext
     * @return
     */
    public ProcessingReport validate(List<JsonSchemaRepository> schemaRepositories, Message message, JsonMessageValidationContext validationContext) {
        List<SimpleJsonSchema> schemaList = schemaRepositories.stream()
                .map(JsonSchemaRepository::getSchemas)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return validate(message, schemaList);
    }
}
