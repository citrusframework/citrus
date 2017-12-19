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

package com.consol.citrus.validation.json;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.message.Message;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class is responsible for the validation of json messages against json schemas.
 */
public class JsonSchemaValidation {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Validates the given message against all provided json schema repositories
     * @param message The message to be validated
     * @param schemaRepositories  The schema repositories to validate against
     */
    public void validate(Message message, List<JsonSchemaRepository> schemaRepositories) {
        for (JsonSchemaRepository jsonSchemaRepository: schemaRepositories) {
            validate(message, jsonSchemaRepository);
        }
    }

    /**
     * Validates a message against all schemas contained in the given json schema repository
     * @param receivedMessage The message to be validated
     * @param jsonSchemaRepository The json schema repository to iterate through
     */
    void validate(Message receivedMessage, JsonSchemaRepository jsonSchemaRepository) {
        for (SimpleJsonSchema simpleJsonSchema : jsonSchemaRepository.getSchemas()){
            validate(receivedMessage, simpleJsonSchema);
        }
    }

    /**
     * Validates a given message against a given json schema
     * @param receivedMessage The message to be validated
     * @param simpleJsonSchema The json schema to validate against
     */
    private void validate(Message receivedMessage, SimpleJsonSchema simpleJsonSchema) {
        ProcessingReport report = simpleJsonSchema.validate(receivedMessage);
        if(report.isSuccess()){
            log.info("Json validation successful: All values OK");
        }else{
            String errorMessage = constructErrorMessage(report);
            log.error(errorMessage);

            throw new ValidationException(errorMessage);
        }
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
}
