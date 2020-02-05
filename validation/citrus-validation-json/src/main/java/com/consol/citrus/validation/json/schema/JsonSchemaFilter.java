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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is responsible for filtering JsonSchemas based on a
 * JsonMessageValidationContext and the application context
 * @since 2.7.3
 */
public class JsonSchemaFilter {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Filters the all schema repositories based on the configuration in the jsonMessageValidationContext
     * and returns a list of relevant schemas for the validation
     * @param schemaRepositories The repositories to be filtered
     * @param jsonMessageValidationContext The context for the json message validation
     * @param applicationContext The application context to extract beans from
     * @return A list of json schemas relevant for the validation based on the configuration
     */
    public List<SimpleJsonSchema> filter(List<JsonSchemaRepository> schemaRepositories,
                                         JsonMessageValidationContext jsonMessageValidationContext,
                                         ApplicationContext applicationContext) {
        if (isSchemaRepositorySpecified(jsonMessageValidationContext)) {
            return filterByRepositoryName(schemaRepositories, jsonMessageValidationContext);
        } else if (isSchemaSpecified(jsonMessageValidationContext)) {
            return getSchemaFromContext(jsonMessageValidationContext, applicationContext);
        } else {
            return mergeRepositories(schemaRepositories);
        }
    }

    /**
     * Extracts the the schema specified in the jsonMessageValidationContext from the application context
     * @param jsonMessageValidationContext The message validation context containing the name of the schema to extract
     * @param applicationContext The application context to extract the schema from
     * @return A list containing the relevant schema
     * @throws CitrusRuntimeException If no matching schema was found
     */
    private List<SimpleJsonSchema> getSchemaFromContext(JsonMessageValidationContext jsonMessageValidationContext,
                                                        ApplicationContext applicationContext) {
        try {
            SimpleJsonSchema simpleJsonSchema =
                    applicationContext.getBean(jsonMessageValidationContext.getSchema(), SimpleJsonSchema.class);

            if (log.isDebugEnabled()) {
                log.debug("Found specified schema: \"" + jsonMessageValidationContext.getSchema() + "\".");
            }

            return Collections.singletonList(simpleJsonSchema);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException(
                    "Could not find the specified schema: \"" + jsonMessageValidationContext.getSchema() + "\".",
                    e);
        }
    }

    /**
     * Filters the schema repositories by the name configured in the jsonMessageValidationContext
     * @param schemaRepositories The List of schema repositories to filter
     * @param jsonMessageValidationContext The validation context of the json message containing the repository name
     * @return The list of json schemas found in the matching repository
     * @throws CitrusRuntimeException If no matching repository was found
     */
    private List<SimpleJsonSchema> filterByRepositoryName(List<JsonSchemaRepository> schemaRepositories,
                                                          JsonMessageValidationContext jsonMessageValidationContext) {
        for (JsonSchemaRepository jsonSchemaRepository : schemaRepositories) {
            if (Objects.equals(jsonSchemaRepository.getName(), jsonMessageValidationContext.getSchemaRepository())) {
                if (log.isDebugEnabled()) {
                    log.debug("Found specified schema-repository: \"" +
                            jsonMessageValidationContext.getSchemaRepository() + "\".");
                }
                return jsonSchemaRepository.getSchemas();
            }
        }

        throw new CitrusRuntimeException("Could not find the specified schema repository: " +
                "\"" + jsonMessageValidationContext.getSchemaRepository() + "\".");
    }

    /**
     * Merges the list of given schema repositories to one unified list of json schemas
     * @param schemaRepositories The list of json schemas to merge
     * @return A list of all json schemas contained in the repositories
     */
    private List<SimpleJsonSchema> mergeRepositories(List<JsonSchemaRepository> schemaRepositories) {
        return schemaRepositories.stream()
                .map(JsonSchemaRepository::getSchemas)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private boolean isSchemaSpecified(JsonMessageValidationContext context) {
        return StringUtils.hasText(context.getSchema());
    }

    private boolean isSchemaRepositorySpecified(JsonMessageValidationContext context) {
        return StringUtils.hasText(context.getSchemaRepository());
    }
}
